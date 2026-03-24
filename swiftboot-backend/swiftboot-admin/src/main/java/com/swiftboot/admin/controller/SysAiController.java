package com.swiftboot.admin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.swiftboot.admin.domain.entity.SysAiSession;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.event.OperLogEvent;
import com.swiftboot.admin.service.SysAdminPreRuleConfigService;
import com.swiftboot.admin.service.SysAiSessionService;
import com.swiftboot.admin.service.SysOperLogService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import com.swiftboot.common.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * AI 智能助手控制器 - Agent -模式
 * 负责处理前端 AI 助手的对话请求，集成 Agent Function Calling 支持。
 * 
 * 【Agent 工具调用流程】：
 * 1. 用户提问 → LLM（携带 tools 定义）
 * 2. LLM 决定是否调用工具 → 如需调用，返回 tool_calls
 * 3. 执行工具（调用 RAG 检索引擎）→ 获取代码上下文
 * 4. 将工具结果发送给 LLM → LLM 生成最终回答
 * 5. 流式输出给用户
 */
@Tag(name = "智能会话")
@RestController
@RequestMapping("/system/ai")
@SuppressWarnings("unchecked")
public class SysAiController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysAiSessionService aiSessionService;
    
    @Resource
    private com.swiftboot.admin.service.SysAiTraceService aiTraceService;

    @Resource
    private SysOperLogService operLogService;

    @Resource
    private SysAdminPreRuleConfigService adminPreRuleConfigService;

    // Redis 历史记录 Key 前缀
    private static final String HISTORY_KEY_PREFIX = "ai:history:";
    
    // Redis AI 配置 Key
    private static final String AI_CONFIG_KEY = "ai:config";

    // DeepSeek 配置 (默认值)
    @Value("${ai.deepseek.api-url:}")
    private String defaultDeepseekApiUrl;

    @Value("${ai.deepseek.api-key:}")
    private String defaultDeepseekApiKey;

    @Value("${ai.deepseek.model:}")
    private String defaultDeepseekModel;

    // 运行时使用的配置 (优先从 Redis 读取)
    private String deepseekApiUrl;
    private String deepseekApiKey;
    private String deepseekModel;

    // 缓存项目内置的 Skills (技能库) 内容
    private String skillsContext = "";
    
    // 缓存 RAG 提示词规则（旧模式，保留作为降级方案）
    private String ragRuleContext = "";
    
    // 缓存 Agent 模式提示词规则
    private String agentRuleContext = "";
    
    // 缓存工具搜索规则
    private String toolSearchRuleContext = "";

    // 缓存简单常规问题快路径规则
    private JSONObject quickChatRulesConfig = null;
    
    // 实时日志流连接池
    private final List<SseEmitter> logEmitters = new CopyOnWriteArrayList<>();

    // Agent 工具定义 (Function Calling)
    // 关键在于 description 的引导性，让 LLM 主动调用工具
    private JSONArray agentTools;
    
    /**
     * 构建 Agent 工具定义
     * DeepSeek API 支持 OpenAI 兼容的 Function Calling 格式
     */
    private JSONArray buildAgentTools() {
        JSONArray tools = new JSONArray();
        
        // Tool 1: fetch_source_impl - 源码级代码检索
        JSONObject fetchSourceTool = new JSONObject();
        fetchSourceTool.set("type", "function");
        JSONObject f1 = new JSONObject();
        f1.set("name", "fetch_source_impl");
        f1.set("description", "【必须调用】当用户询问具体代码实现、报错排查、数据库表结构、接口定义或类方法时调用。专门搜索项目源码库。");
        JSONObject p1 = new JSONObject();
        p1.set("type", "object");
        JSONObject prop1 = new JSONObject();
        JSONObject q1 = new JSONObject();
        q1.set("type", "string");
        q1.set("description", "代码搜索关键词，建议包含具体的类名、方法名、表名或异常信息");
        prop1.set("query", q1);
        p1.set("properties", prop1);
        p1.set("required", new JSONArray().put("query"));
        f1.set("parameters", p1);
        fetchSourceTool.set("function", f1);
        tools.add(fetchSourceTool);
        
        // Tool 2: fetch_business_doc - 业务文档检索
        JSONObject fetchDocTool = new JSONObject();
        fetchDocTool.set("type", "function");
        JSONObject f2 = new JSONObject();
        f2.set("name", "fetch_business_doc");
        f2.set("description", "【必须调用】当用户询问系统整体架构、业务流程怎么走、如何使用某个功能、或者需要开发指南时调用。专门搜索项目文档库。");
        JSONObject p2 = new JSONObject();
        p2.set("type", "object");
        JSONObject prop2 = new JSONObject();
        JSONObject q2 = new JSONObject();
        q2.set("type", "string");
        q2.set("description", "文档搜索关键词，建议包含业务模块名称、架构概念或功能说明词");
        prop2.set("query", q2);
        p2.set("properties", prop2);
        p2.set("required", new JSONArray().put("query"));
        f2.set("parameters", p2);
        fetchDocTool.set("function", f2);
        tools.add(fetchDocTool);
        
        return tools;
    }
    
    // Python 检索引擎地址 (RAG 服务)
    private static final String RAG_API_URL = "http://localhost:8001/retrieve";
    private static final String INTENT_DETECT_URL = "http://localhost:8001/intent/detect";
    private static final String MEMORY_QUERY_URL = "http://localhost:8001/memory/query";
    private static final String MEMORY_ADD_URL = "http://localhost:8001/memory/add";
    private static final String MEMORY_DELETE_URL = "http://localhost:8001/memory/delete";
    private static final String MEMORY_UPDATE_CITATION_URL = "http://localhost:8001/memory/update_citation";
    private static final String NLP_TOPIC_URL = "http://localhost:8001/nlp/topic";
    private static final String NLP_SIMILARITY_URL = "http://localhost:8001/nlp/similarity";
    private static final String STATS_URL = "http://localhost:8001/stats";
    private static final String HEALTH_URL = "http://localhost:8001/health";
    private static final String KNOWLEDGE_STATS_URL = "http://localhost:8001/knowledge/stats";
    private static final String INDEX_REBUILD_URL = "http://localhost:8001/index/rebuild";
    private static final String INDEX_REBUILD_STATUS_URL = "http://localhost:8001/index/rebuild/status";
    private static final ExecutorService AI_AUX_EXECUTOR = Executors.newFixedThreadPool(4);
    private static final long AUXILIARY_BUDGET_MS = 800L;
    private static final long INTENT_HTTP_TIMEOUT_MS = 650L;
    private static final long SIMILARITY_HTTP_TIMEOUT_MS = 650L;
    private static final long MEMORY_HTTP_TIMEOUT_MS = 1200L;
    private static final int LLM_FIRST_CALL_MAX_ATTEMPTS = 2;
    private static final int QUICK_CHAT_LLM_TIMEOUT_MS = 15000;

    private static final class IntentDecision {
        private final String intent;
        private final double confidence;

        private IntentDecision(String intent, double confidence) {
            this.intent = intent;
            this.confidence = confidence;
        }
    }

    private static final class MemoryRecallDecision {
        private final String context;
        private final List<String> memoryIds;

        private MemoryRecallDecision(String context, List<String> memoryIds) {
            this.context = context;
            this.memoryIds = memoryIds;
        }

        private static MemoryRecallDecision empty() {
            return new MemoryRecallDecision(null, new ArrayList<>());
        }
    }

    private static final class AiRuntimeConfig {
        private final String apiUrl;
        private final String apiKey;
        private final String model;
        private final String provider;

        private AiRuntimeConfig(String apiUrl, String apiKey, String model, String provider) {
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.model = model;
            this.provider = provider;
        }

        private boolean isAnthropic() {
            return "anthropic".equalsIgnoreCase(provider) || apiUrl.contains("/anthropic/");
        }
    }

    private static final class QuickChatRuleMatch {
        private final String id;
        private final String instruction;
        private final String fallbackAnswer;

        private QuickChatRuleMatch(String id, String instruction, String fallbackAnswer) {
            this.id = id;
            this.instruction = instruction;
            this.fallbackAnswer = fallbackAnswer;
        }
    }

    /**
     * 实时索引日志流 (SSE)
     * 用于前端监控页面 (Neural Stream)
     */
    @GetMapping(value = "/index/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter indexStream(@RequestParam(defaultValue = "100") int limit) {
        SseEmitter emitter = new SseEmitter(0L); // 永不超时
        logEmitters.add(emitter);
        
        // 发送历史日志
        try {
            // 查询最新的 limit 条 AI 引擎日志
            Page<SysOperLog> page = new Page<>(1, limit);
            LambdaQueryWrapper<SysOperLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(w -> w.eq(SysOperLog::getOperName, "AI Engine")
                                .or()
                                .like(SysOperLog::getMethod, "VectorStore."))
                        .orderByDesc(SysOperLog::getOperTime);
            
            Page<SysOperLog> result = operLogService.page(page, queryWrapper);
            List<SysOperLog> logs = result.getRecords();
            
            // 反转顺序，按时间正序发送（旧 -> 新）
            Collections.reverse(logs);
            
            for (SysOperLog log : logs) {
                JSONObject msg = new JSONObject();
                if (log.getOperTime() != null) {
                    msg.set("time", cn.hutool.core.date.DateUtil.format(log.getOperTime(), "HH:mm:ss"));
                }
                msg.set("msg", log.getTitle());
                // 根据日志内容简单的着色逻辑
                if (log.getTitle() != null) {
                    if (log.getTitle().contains("检测到")) {
                        msg.set("color", "text-orange-400");
                    } else if (log.getTitle().contains("完成") || log.getTitle().contains("成功")) {
                        msg.set("color", "text-emerald-400");
                    } else if (log.getTitle().contains("失败") || log.getTitle().contains("错误")) {
                        msg.set("color", "text-red-400");
                    } else {
                        msg.set("color", "text-slate-400");
                    }
                } else {
                     msg.set("color", "text-slate-400");
                }
                
                emitter.send(SseEmitter.event().data(msg.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        emitter.onCompletion(() -> logEmitters.remove(emitter));
        emitter.onTimeout(() -> logEmitters.remove(emitter));
        emitter.onError((e) -> logEmitters.remove(emitter));
        
        return emitter;
    }

    /**
     * 监听操作日志事件，并推送到 SSE 流
     */
    @EventListener
    public void handleOperLogEvent(OperLogEvent event) {
        if (logEmitters.isEmpty()) {
            return;
        }
        
        SysOperLog log = event.getOperLog();
        // 仅推送 AI 引擎产生的日志
        if ("AI Engine".equals(log.getOperName()) || 
            (log.getTitle() != null && log.getTitle().contains("检测到"))) {
            
            JSONObject msg = new JSONObject();
            if (log.getOperTime() != null) {
                msg.set("time", cn.hutool.core.date.DateUtil.format(log.getOperTime(), "HH:mm:ss"));
            }
            msg.set("msg", log.getTitle());
            // 根据日志内容简单的着色逻辑
            if (log.getTitle().contains("检测到")) {
                msg.set("color", "text-orange-400");
            } else if (log.getTitle().contains("完成") || log.getTitle().contains("成功")) {
                msg.set("color", "text-emerald-400");
            } else if (log.getTitle().contains("失败") || log.getTitle().contains("错误")) {
                msg.set("color", "text-red-400");
            } else {
                msg.set("color", "text-slate-400");
            }
            
            String jsonStr = msg.toString();
            List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
            
            for (SseEmitter emitter : logEmitters) {
                try {
                    emitter.send(SseEmitter.event().data(jsonStr));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }
            
            logEmitters.removeAll(deadEmitters);
        }
    }

    /**
     * 获取 AI 引擎统计信息
     */
    @Operation(summary = "获取AI引擎统计信息")
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        boolean engineUp = false;
        try {
            String health = HttpRequest.get(HEALTH_URL).timeout(800).execute().body();
            engineUp = StrUtil.isNotEmpty(health);
        } catch (Exception ignored) {
        }

        if (!engineUp) {
            return R.ok(Map.of("engine_up", false, "knowledge_count", 0, "memory_count", 0, "total_chunks", 0));
        }

        try {
            String result = HttpRequest.get(KNOWLEDGE_STATS_URL).timeout(3000).execute().body();
            if (StrUtil.isNotEmpty(result)) {
                Map<String, Object> map = JSONUtil.toBean(result, Map.class);
                Object totalChunks = map.get("total_chunks");
                if (totalChunks != null) {
                    map.put("knowledge_count", totalChunks);
                }
                map.put("engine_up", true);
                return R.ok(map);
            }
        } catch (Exception ignored) {
        }

        try {
            String result = HttpRequest.get(STATS_URL).timeout(2000).execute().body();
            if (StrUtil.isEmpty(result)) {
                return R.ok(Map.of("engine_up", true, "knowledge_count", 0, "memory_count", 0, "total_chunks", 0));
            }
            Map<String, Object> map = JSONUtil.toBean(result, Map.class);
            map.put("engine_up", true);
            return R.ok(map);
        } catch (Exception e) {
            return R.ok(Map.of("engine_up", true, "knowledge_count", 0, "memory_count", 0, "total_chunks", 0));
        }
    }

    @Operation(summary = "触发AI索引重建")
    @PostMapping("/index/rebuild")
    public R<Map<String, Object>> rebuildIndex(@RequestBody(required = false) Map<String, Object> body) {
        boolean force = body != null && Boolean.TRUE.equals(body.get("force"));
        try {
            JSONObject req = new JSONObject();
            req.set("force", force);
            String resp = HttpRequest.post(INDEX_REBUILD_URL).timeout(2000).body(req.toString()).execute().body();
            if (StrUtil.isEmpty(resp)) {
                return R.ok(Map.of("status", "unknown"));
            }
            Map<String, Object> map = JSONUtil.toBean(resp, Map.class);
            return R.ok(map);
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "AI 引擎未启动或重建失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取AI索引重建状态")
    @GetMapping("/index/rebuild/status")
    public R<Map<String, Object>> rebuildIndexStatus() {
        try {
            String resp = HttpRequest.get(INDEX_REBUILD_STATUS_URL).timeout(1000).execute().body();
            if (StrUtil.isEmpty(resp)) {
                return R.ok(Map.of("running", false));
            }
            Map<String, Object> map = JSONUtil.toBean(resp, Map.class);
            return R.ok(map);
        } catch (Exception e) {
            return R.ok(Map.of("running", false));
        }
    }

    // 安全拦截规则配置对象
    private JSONObject securityRulesConfig = null;
    private String defaultSecurityInterceptionMessage = "⚠️ 检测到潜在的安全风险或违规指令，请求已被拦截。我是 SwiftBoot 智能助手，请询问与项目相关的问题。";

    /**
     * 初始化 Skills 技能库、Agent 规则和安全规则
     */
    @PostConstruct
    public void initSkills() {
        // 1. 加载 AI 配置 (优先从 Redis，无则使用默认值)
        loadAiConfig();
        
        try {
            // 定义外部规则目录 (项目根目录/ai_rule)
            String projectRoot = System.getProperty("user.dir");
            if (projectRoot.endsWith("swiftboot-admin")) {
                projectRoot = new File(projectRoot).getParentFile().getParent();
            } else if (projectRoot.endsWith("swiftboot-backend")) {
                projectRoot = new File(projectRoot).getParent();
            }
            File ruleDir = new File(projectRoot, "ai_rule");
            
            // 优先加载 system_identity.md
            File identityFile = new File(ruleDir, "system_identity.md");
            if (identityFile.exists()) {
                agentRuleContext = FileUtil.readString(identityFile, StandardCharsets.UTF_8);
                System.out.println("System Identity Rule loaded from external: " + identityFile.getAbsolutePath());
            } else {
                System.err.println("Warning: system_identity.md not found in " + ruleDir.getAbsolutePath());
            }
            
            // 加载工具搜索规则文件
            File searchRuleFile = new File(ruleDir, "tool_search_codebase_rule.md");
            if (searchRuleFile.exists()) {
                toolSearchRuleContext = FileUtil.readString(searchRuleFile, StandardCharsets.UTF_8);
                System.out.println("Tool Search Rule loaded from external: " + searchRuleFile.getAbsolutePath());
            }

            File quickChatRuleFile = new File(ruleDir, "quick_chat_rules.json");
            if (quickChatRuleFile.exists()) {
                try {
                    String quickChatJsonStr = FileUtil.readString(quickChatRuleFile, StandardCharsets.UTF_8);
                    quickChatRulesConfig = JSONUtil.parseObj(quickChatJsonStr);
                    System.out.println("Quick Chat Rules loaded from external: " + quickChatRuleFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to parse quick_chat_rules.json: " + e.getMessage());
                }
            }
             
            // 加载安全拦截规则 V2
            File securityRuleFile = new File(ruleDir, "security_rules.json");
            if (securityRuleFile.exists()) {
                try {
                    String securityJsonStr = FileUtil.readString(securityRuleFile, StandardCharsets.UTF_8);
                    securityRulesConfig = JSONUtil.parseObj(securityJsonStr);
                    System.out.println("Security Rules V2 loaded from external: " + securityRuleFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to parse security_rules.json: " + e.getMessage());
                }
            }
            
            // 初始化工具定义 (需在规则加载后执行)
            this.agentTools = buildAgentTools();

            // 扫描项目根目录下的 project-skills 目录
            File skillsDir = new File(projectRoot, "project-skills");
            if (skillsDir.exists() && skillsDir.isDirectory()) {
                StringBuilder sb = new StringBuilder();
                sb.append("以下是项目已有的 Skills 知识库，当用户问题涉及相关领域时，请优先参考这些内容：\n\n");
                
                File[] skillFolders = skillsDir.listFiles(File::isDirectory);
                if (skillFolders != null) {
                    for (File folder : skillFolders) {
                        File skillFile = new File(folder, "SKILL.md");
                        if (skillFile.exists()) {
                            String content = FileUtil.readString(skillFile, StandardCharsets.UTF_8);
                            String name = folder.getName();
                            if (content.startsWith("---")) {
                                int endIndex = content.indexOf("---", 3);
                                if (endIndex > 0) {
                                    content = content.substring(endIndex + 3).trim();
                                }
                            }
                            sb.append("=== Skill: ").append(name).append(" ===\n");
                            sb.append(content).append("\n\n");
                        }
                    }
                }
                skillsContext = sb.toString();
                System.out.println("Skills loaded successfully, length: " + skillsContext.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load skills: " + e.getMessage());
        }
    }

    /**
     * 加载 AI 配置
     * 优先从 Redis 读取，无则使用 application.yml 中的默认值
     */
    private void loadAiConfig() {
        // 先尝试从 Redis 获取
        String configJson = stringRedisTemplate.opsForValue().get(AI_CONFIG_KEY);
        
        if (StrUtil.isNotEmpty(configJson)) {
            try {
                JSONObject config = JSONUtil.parseObj(configJson);
                this.deepseekApiUrl = config.getStr("apiUrl");
                this.deepseekApiKey = config.getStr("apiKey");
                this.deepseekModel = config.getStr("model");
                System.out.println("AI Config loaded from Redis, provider: " + config.getStr("provider") + ", model: " + this.deepseekModel);
            } catch (Exception e) {
                System.err.println("Failed to load AI config from Redis: " + e.getMessage());
                // 回退到默认值
                useDefaultConfig();
            }
        } else {
            // 使用 application.yml 中的默认值
            useDefaultConfig();
        }
    }

    /**
     * 使用默认配置
     */
    private void useDefaultConfig() {
        this.deepseekApiUrl = defaultDeepseekApiUrl;
        this.deepseekApiKey = defaultDeepseekApiKey;
        this.deepseekModel = defaultDeepseekModel;
        System.out.println("Using default AI Config from application.yml, model: " + this.deepseekModel);
    }

    /**
     * 刷新 AI 配置
     * 用于配置更新后立即生效
     */
    public void refreshAiConfig() {
        loadAiConfig();
    }

    /**
     * 清除 AI 对话缓存 (Redis + 向量库)
     */
    @Operation(summary = "清除AI缓存")
    @Log(title = "智能会话", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> cleanHistory(@RequestParam(required = false) Long targetUserId) {
        Long currentUserId = SecurityUtils.getUserId();
        if (targetUserId != null && !currentUserId.equals(targetUserId)) {
            if (!SecurityUtils.isAdmin()) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "无权操作其他用户的缓存");
            }
        } else {
            targetUserId = currentUserId;
        }

        String redisKey = HISTORY_KEY_PREFIX + targetUserId;
        stringRedisTemplate.delete(redisKey);

        try {
            JSONObject body = new JSONObject();
            body.set("user_id", String.valueOf(targetUserId));
            HttpRequest.post(MEMORY_DELETE_URL).timeout(5000).body(body.toString()).execute();
        } catch (Exception e) {
            System.err.println("Vector DB delete failed: " + e.getMessage());
        }

        return R.ok();
    }

    /**
     * 获取历史记录
     */
    @Operation(summary = "获取历史记录")
    @GetMapping("/history")
    public R<List<JSONObject>> getHistory() {
        Long userId = SecurityUtils.getUserId();
        String key = HISTORY_KEY_PREFIX + userId;
        List<String> historyStr = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (historyStr == null) {
            return R.ok();
        }
        List<JSONObject> history = historyStr.stream()
                .map(JSONUtil::parseObj)
                .collect(Collectors.toList());
        return R.ok(history);
    }

    /**
     * 清空历史记录
     */
    @Operation(summary = "清空历史记录")
    @Log(title = "智能会话", businessType = BusinessType.CLEAN)
    @DeleteMapping("/history/clean")
    public R<Void> cleanHistory() {
        Long userId = SecurityUtils.getUserId();
        String key = HISTORY_KEY_PREFIX + userId;
        stringRedisTemplate.delete(key);
        return R.ok();
    }

    private boolean isQuestionHistoryRequest(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        String text = content.toLowerCase();
        boolean mentionsPast = StrUtil.containsAny(text, "之前", "历史", "以前", "问过", "提过");
        boolean asksQuestionList = StrUtil.containsAny(text, "问题", "提问");
        boolean asksList = StrUtil.containsAny(text, "列举", "列出", "所有", "全部", "汇总", "清单");
        return asksQuestionList && (mentionsPast || asksList);
    }

    private boolean isAllUsersQuestionHistoryRequest(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        String text = content.toLowerCase();
        return StrUtil.containsAny(text, "所有人", "所有用户", "全部用户", "大家问过", "所有人的问题");
    }

    private String buildQuestionHistoryAnswer(Long currentUserId, boolean allUsers) {
        boolean canViewAll = allUsers && SecurityUtils.isAdmin();
        if (allUsers && !canViewAll) {
            return "当前仅支持查看你自己的历史问题。只有管理员可以查看所有用户的提问记录。";
        }

        List<Map<String, Object>> rows = aiSessionService.getQuestionHistory(currentUserId, canViewAll);
        if (rows == null || rows.isEmpty()) {
            return canViewAll ? "当前系统里还没有可用的历史提问记录。" : "你当前还没有可用的历史提问记录。";
        }

        StringBuilder sb = new StringBuilder();
        if (canViewAll) {
            sb.append("已查询到系统中的历史提问记录，共 ").append(rows.size()).append(" 条：\n\n");
        } else {
            sb.append("已查询到你之前的历史提问，共 ").append(rows.size()).append(" 条：\n\n");
        }

        if (canViewAll) {
            Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                String username = row.get("username") == null ? "" : String.valueOf(row.get("username"));
                String nickname = row.get("nickname") == null ? "" : String.valueOf(row.get("nickname"));
                String displayName = StrUtil.isNotBlank(nickname) ? nickname : username;
                grouped.computeIfAbsent(StrUtil.isNotBlank(displayName) ? displayName : "未知用户", k -> new ArrayList<>()).add(row);
            }

            int globalIndex = 1;
            for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
                sb.append("### 用户：").append(entry.getKey()).append("\n");
                for (Map<String, Object> row : entry.getValue()) {
                    String question = row.get("question") == null ? "" : String.valueOf(row.get("question"));
                    String createTime = row.get("create_time") == null ? "未知时间" : String.valueOf(row.get("create_time"));
                    sb.append(globalIndex).append(". [").append(createTime).append("] ").append(question).append("\n");
                    globalIndex++;
                }
            }
        } else {
            int index = 1;
            for (Map<String, Object> row : rows) {
                String question = row.get("question") == null ? "" : String.valueOf(row.get("question"));
                String createTime = row.get("create_time") == null ? "未知时间" : String.valueOf(row.get("create_time"));
                sb.append(index).append(". [").append(createTime).append("] ").append(question).append("\n");
                index++;
            }
        }

        return sb.toString().trim();
    }

    private boolean shouldDisableToolsForChat(String detectedIntent, double intentConfidence) {
        return "CHAT".equalsIgnoreCase(detectedIntent) && intentConfidence >= 0.75;
    }

    private int updateMemoryCitations(Long userId, List<String> memoryHitIds) {
        if (memoryHitIds == null || memoryHitIds.isEmpty()) {
            return 0;
        }
        try {
            JSONObject req = new JSONObject();
            req.set("user_id", String.valueOf(userId));
            req.set("memory_ids", memoryHitIds);
            String resp = HttpRequest.post(MEMORY_UPDATE_CITATION_URL)
                    .timeout(3000)
                    .body(req.toString())
                    .execute()
                    .body();
            if (StrUtil.isNotEmpty(resp)) {
                return JSONUtil.parseObj(resp).getInt("updated_count", 0);
            }
        } catch (Exception e) {
            System.err.println("Update memory citation failed: " + e.getMessage());
        }
        return 0;
    }

    private String generateFollowUpSuggestions(boolean isAnthropic, String apiUrl, String apiKey, String model, String finalAnswer) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", model);
            requestBody.set("stream", false);

            String prompt = "你是一个智能助手。基于以下刚刚向用户提供的回答，请生成2个继续追问的建议问题，引导用户进一步探索。只返回问题文本，每行一个，不要序号标题。\n\n回答内容：\n" + finalAnswer;

            JSONArray messages = new JSONArray();
            if (isAnthropic) {
                requestBody.set("max_tokens", 300);
                requestBody.set("system", prompt);
                messages.add(new JSONObject().set("role", "user").set("content", "生成继续追问的问题"));
                requestBody.set("messages", messages);
            } else {
                messages.add(new JSONObject().set("role", "system").set("content", prompt));
                messages.add(new JSONObject().set("role", "user").set("content", "生成继续追问的问题"));
                requestBody.set("messages", messages);
            }

            HttpResponse response = HttpRequest.post(apiUrl)
                    .timeout(10000)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            if (!response.isOk()) {
                return null;
            }

            JSONObject json = JSONUtil.parseObj(response.body());
            if (isAnthropic) {
                JSONArray contentBlocks = json.getJSONArray("content");
                if (contentBlocks != null && !contentBlocks.isEmpty()) {
                    return contentBlocks.getJSONObject(0).getStr("text");
                }
            } else {
                JSONArray choices = json.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    return choices.getJSONObject(0).getJSONObject("message").getStr("content");
                }
            }
        } catch (Exception e) {
            System.err.println("Generate follow-up suggestions failed: " + e.getMessage());
        }
        return null;
    }

    private String formatFollowUpSuggestions(String suggestionsText) {
        if (StrUtil.isBlank(suggestionsText)) {
            return null;
        }
        List<String> lines = StrUtil.splitTrim(suggestionsText, '\n');
        List<String> normalized = new ArrayList<>();
        for (String line : lines) {
            String cleaned = line.replaceFirst("^[\\-\\d\\.、\\s]+", "").trim();
            if (StrUtil.isNotBlank(cleaned)) {
                normalized.add(cleaned);
            }
            if (normalized.size() >= 2) {
                break;
            }
        }
        if (normalized.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("### 你还可以继续问：\n");
        for (String line : normalized) {
            sb.append("- ").append(line).append("\n");
        }
        return sb.toString().trim();
    }

    private String resolveCurrentModelName() {
        String configJson = stringRedisTemplate.opsForValue().get(AI_CONFIG_KEY);
        if (StrUtil.isNotEmpty(configJson)) {
            JSONObject config = JSONUtil.parseObj(configJson);
            return config.getStr("model", defaultDeepseekModel);
        }
        return defaultDeepseekModel;
    }

    private AiRuntimeConfig resolveCurrentAiRuntimeConfig() {
        String configJson = stringRedisTemplate.opsForValue().get(AI_CONFIG_KEY);
        if (StrUtil.isNotEmpty(configJson)) {
            JSONObject config = JSONUtil.parseObj(configJson);
            return new AiRuntimeConfig(
                    config.getStr("apiUrl", defaultDeepseekApiUrl),
                    config.getStr("apiKey", defaultDeepseekApiKey),
                    config.getStr("model", defaultDeepseekModel),
                    config.getStr("provider", "")
            );
        }
        return new AiRuntimeConfig(defaultDeepseekApiUrl, defaultDeepseekApiKey, defaultDeepseekModel, "");
    }

    private String normalizeQuickChatText(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return content.toLowerCase()
                .replaceAll("[\\s，。！？!?,、~～：:；;“”\"'（）()【】\\[\\]<>《》]+", "");
    }

    private boolean containsProjectOrHistoryKeywords(String content) {
        String lower = content.toLowerCase();
        return StrUtil.containsAny(lower,
                "代码", "源码", "接口", "类", "方法", "sql", "数据库",
                "功能", "模块", "业务", "架构", "项目", "swiftboot", "报错", "异常", "bug",
                "之前", "上次", "历史", "记忆", "上下文", "继续", "刚才");
    }

    private QuickChatRuleMatch matchQuickChatRule(String content) {
        if (StrUtil.isBlank(content) || quickChatRulesConfig == null) {
            return null;
        }
        String normalized = normalizeQuickChatText(content);
        if (StrUtil.isBlank(normalized) || normalized.length() > 30 || containsProjectOrHistoryKeywords(content)) {
            return null;
        }
        JSONObject settings = quickChatRulesConfig.getJSONObject("settings");
        int maxLength = settings != null ? settings.getInt("max_length", 30) : 30;
        if (normalized.length() > maxLength) {
            return null;
        }
        JSONArray rules = quickChatRulesConfig.getJSONArray("rules");
        if (rules == null) {
            return null;
        }
        for (int i = 0; i < rules.size(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            JSONArray patterns = rule.getJSONArray("patterns");
            if (patterns == null) {
                continue;
            }
            for (int j = 0; j < patterns.size(); j++) {
                String pattern = patterns.getStr(j);
                try {
                    if (pattern != null && java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(content.trim()).find()) {
                        return new QuickChatRuleMatch(
                                rule.getStr("id", "QUICK_CHAT"),
                                rule.getStr("response_instruction", ""),
                                rule.getStr("fallback_answer", "你好，我是 SwiftBoot 智能助手，可以协助你处理当前项目相关问题。")
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Quick chat rule regex invalid: " + pattern + ", error: " + e.getMessage());
                }
            }
        }
        return null;
    }

    private String buildQuickChatSystemPrompt(QuickChatRuleMatch ruleMatch) {
        StringBuilder prompt = new StringBuilder();
        if (StrUtil.isNotBlank(agentRuleContext)) {
            prompt.append(agentRuleContext).append("\n\n");
        } else {
            prompt.append("你是 SwiftBoot 智能助手，请直接自然回答用户，不调用工具，不输出思考标签。\n\n");
        }
        prompt.append("【简单常规问题快路径】\n");
        prompt.append("当前问题属于简单常规问题，请直接自然回答。\n");
        prompt.append("- 禁止调用任何工具\n");
        prompt.append("- 禁止检索历史记忆\n");
        prompt.append("- 禁止输出 <thought>、function_calls、DSML 等标签\n");
        prompt.append("- 回答尽量短、自然、礼貌\n");
        if (ruleMatch != null && StrUtil.isNotBlank(ruleMatch.instruction)) {
            prompt.append("- 本次补充规则：").append(ruleMatch.instruction).append("\n");
        }
        return prompt.toString();
    }

    private String answerQuickChatFastPath(AiRuntimeConfig runtimeConfig, String content, QuickChatRuleMatch ruleMatch) {
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "user").set("content", content));
        String answer = completeNonStream(
                runtimeConfig.isAnthropic(),
                runtimeConfig.apiUrl,
                runtimeConfig.apiKey,
                runtimeConfig.model,
                messages,
                buildQuickChatSystemPrompt(ruleMatch)
        );
        if (StrUtil.isNotBlank(answer)) {
            return answer.trim();
        }
        return ruleMatch != null ? ruleMatch.fallbackAnswer : "你好，我是 SwiftBoot 智能助手，可以协助你处理当前项目相关问题。";
    }

    private String loadLastUserMessage(Long userId) {
        String redisKey = HISTORY_KEY_PREFIX + userId;
        Long historySize = stringRedisTemplate.opsForList().size(redisKey);
        if (historySize == null || historySize <= 0) {
            return null;
        }
        List<String> allHistory = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        if (allHistory == null) {
            return null;
        }
        for (int i = allHistory.size() - 1; i >= 0; i--) {
            JSONObject msg = JSONUtil.parseObj(allHistory.get(i));
            if ("user".equals(msg.getStr("role"))) {
                return msg.getStr("content");
            }
        }
        return null;
    }

    private IntentDecision detectIntentWithFallback(String content) {
        try {
            JSONObject intentReq = new JSONObject();
            intentReq.set("text", content);
            String intentRes = HttpRequest.post(INTENT_DETECT_URL)
                    .timeout((int) INTENT_HTTP_TIMEOUT_MS)
                    .body(intentReq.toString())
                    .execute()
                    .body();
            if (StrUtil.isNotEmpty(intentRes)) {
                JSONObject intentJson = JSONUtil.parseObj(intentRes);
                return new IntentDecision(
                        intentJson.getStr("intent", "CHAT"),
                        intentJson.getDouble("confidence", 0.5)
                );
            }
        } catch (Exception e) {
            System.err.println("Intent detection failed: " + e.getMessage());
        }
        return new IntentDecision("CHAT", 0.5);
    }

    private boolean checkHistorySimilarity(String currentContent, String lastUserMsgJson) {
        if (StrUtil.isBlank(lastUserMsgJson)) {
            return false;
        }
        try {
            JSONObject simRequest = new JSONObject();
            simRequest.set("text1", currentContent);
            simRequest.set("text2", lastUserMsgJson);
            String simResult = HttpRequest.post(NLP_SIMILARITY_URL)
                    .timeout((int) SIMILARITY_HTTP_TIMEOUT_MS)
                    .body(simRequest.toString())
                    .execute()
                    .body();
            if (StrUtil.isNotEmpty(simResult)) {
                double similarity = JSONUtil.parseObj(simResult).getDouble("similarity");
                System.out.println("Context Similarity: " + similarity + " (Current: " + currentContent + " vs Last: " + lastUserMsgJson + ")");
                return similarity >= 0.5;
            }
        } catch (Exception e) {
            System.err.println("Similarity check failed: " + e.getMessage());
        }
        return false;
    }

    private boolean shouldAttemptMemoryRecall(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        String lower = content.toLowerCase();
        boolean obviousChat = StrUtil.containsAny(lower, "你好", "hello", "hi", "谢谢", "感谢", "哈哈", "再见", "在吗");
        boolean projectRelated = StrUtil.containsAny(lower,
                "代码", "源码", "接口", "类", "方法", "sql", "数据库",
                "功能", "模块", "业务", "架构", "项目", "swiftboot", "报错", "异常", "bug",
                "之前", "上次", "历史", "记忆", "上下文");
        if (obviousChat && !projectRelated && content.length() <= 20) {
            return false;
        }
        return projectRelated || content.length() >= 12;
    }

    private MemoryRecallDecision recallMemoryContext(Long userId, String content) {
        if (!shouldAttemptMemoryRecall(content)) {
            return MemoryRecallDecision.empty();
        }
        try {
            JSONObject memReq = new JSONObject();
            memReq.set("user_id", String.valueOf(userId));
            memReq.set("question", content);
            memReq.set("n_results", 3);
            memReq.set("max_distance", 1.0);

            String memRes = HttpRequest.post(MEMORY_QUERY_URL)
                    .timeout((int) MEMORY_HTTP_TIMEOUT_MS)
                    .body(memReq.toString())
                    .execute()
                    .body();
            if (StrUtil.isEmpty(memRes)) {
                return MemoryRecallDecision.empty();
            }

            JSONObject memJson = JSONUtil.parseObj(memRes);
            JSONArray memResults = memJson.getJSONArray("results");
            if (memResults == null || memResults.isEmpty()) {
                return MemoryRecallDecision.empty();
            }

            StringBuilder memContext = new StringBuilder("【系统提示：以下是您过去的高频重要记忆，请参考】\n");
            List<String> hitMemoryIds = new ArrayList<>();
            for (int i = 0; i < memResults.size(); i++) {
                JSONObject item = memResults.getJSONObject(i);
                String text = item.getStr("content");
                JSONObject meta = item.getJSONObject("metadata");
                String role = meta != null ? meta.getStr("role", "unknown") : "unknown";
                memContext.append("[").append(role).append("]: ").append(text).append("\n");

                if (item.containsKey("id")) {
                    hitMemoryIds.add(item.getStr("id"));
                } else if (item.containsKey("metadata") && item.getJSONObject("metadata").containsKey("id")) {
                    hitMemoryIds.add(item.getJSONObject("metadata").getStr("id"));
                }
            }
            System.out.println("[Agent] 已准备长期记忆上下文，数量: " + memResults.size());
            return new MemoryRecallDecision(memContext.toString(), hitMemoryIds);
        } catch (Exception e) {
            System.err.println("Memory recall failed: " + e.getMessage());
            return MemoryRecallDecision.empty();
        }
    }

    private <T> T awaitWithinBudget(CompletableFuture<T> future, T fallback, long deadlineMillis, String stageName) {
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining <= 0) {
            future.cancel(true);
            System.out.println("[Agent Budget] " + stageName + " 超出总预算，直接降级");
            return fallback;
        }
        try {
            return future.get(remaining, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("[Agent Budget] " + stageName + " 预算耗尽，降级处理");
            return fallback;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fallback;
        } catch (ExecutionException e) {
            return fallback;
        }
    }

    private boolean isRetryableLlmException(Exception e) {
        if (e == null || StrUtil.isBlank(e.getMessage())) {
            return false;
        }
        String msg = e.getMessage().toLowerCase();
        return msg.contains("connection reset")
                || msg.contains("read timed out")
                || msg.contains("connect timed out")
                || msg.contains("broken pipe")
                || msg.contains("socketexception");
    }

    private HttpResponse executeFirstLlmCall(String apiUrl, String apiKey, JSONObject requestBody) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= LLM_FIRST_CALL_MAX_ATTEMPTS; attempt++) {
            try {
                return HttpRequest.post(apiUrl)
                        .timeout(120000)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(requestBody.toString())
                        .execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt >= LLM_FIRST_CALL_MAX_ATTEMPTS || !isRetryableLlmException(e)) {
                    throw e;
                }
                System.err.println("[Agent] 首轮 LLM 请求异常，准备重试(" + attempt + "/" + LLM_FIRST_CALL_MAX_ATTEMPTS + "): " + e.getMessage());
                try {
                    Thread.sleep(300L * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw lastException == null ? new IllegalStateException("LLM request failed") : lastException;
    }

    /**
     * 前置安全拦截 (轻量级 V2)
     * 支持多层防御、正则匹配与关键词匹配，返回拦截信息。
     * 若未被拦截，返回 null。
     */
    private String checkSecurityRisk(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }

        String adminInterceptionMessage = adminPreRuleConfigService.check(content);
        if (StrUtil.isNotBlank(adminInterceptionMessage)) {
            return adminInterceptionMessage;
        }

        if (securityRulesConfig == null) {
            // 降级：简单的默认拦截
            String lowerContent = content.toLowerCase();
            String[] defaultKeywords = {"忽略之前", "系统提示词", "jailbreak"};
            for (String kw : defaultKeywords) {
                if (lowerContent.contains(kw)) {
                    return defaultSecurityInterceptionMessage;
                }
            }
            return null;
        }

        String lowerContent = content.toLowerCase();
        JSONArray defenseLayers = securityRulesConfig.getJSONArray("defense_layers");
        if (defenseLayers != null) {
            for (int i = 0; i < defenseLayers.size(); i++) {
                JSONObject layer = defenseLayers.getJSONObject(i);
                String matchMode = layer.getStr("match_mode", "keyword");
                JSONArray rules = layer.getJSONArray("rules");
                
                if (rules != null) {
                    for (int j = 0; j < rules.size(); j++) {
                        String rule = rules.getStr(j);
                        if ("regex".equalsIgnoreCase(matchMode)) {
                            try {
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(rule, java.util.regex.Pattern.CASE_INSENSITIVE);
                                if (pattern.matcher(content).find()) {
                                    System.out.println("[Security] Blocked by regex layer: " + layer.getStr("name") + ", rule: " + rule);
                                    return layer.getStr("interception_message", securityRulesConfig.getStr("global_interception_message", defaultSecurityInterceptionMessage));
                                }
                            } catch (Exception e) {
                                // 正则编译失败忽略
                            }
                        } else {
                            if (lowerContent.contains(rule.toLowerCase())) {
                                System.out.println("[Security] Blocked by keyword layer: " + layer.getStr("name") + ", rule: " + rule);
                                return layer.getStr("interception_message", securityRulesConfig.getStr("global_interception_message", defaultSecurityInterceptionMessage));
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * 发送对话 (流式 Stream) - Agent 模式
     * 使用 SSE (Server-Sent Events) 技术，实现打字机效果。
     * 
     * 【升级版 Agent 工具调用流程】：
     * 1. 安全前置校验
     * 2. 上下文判断（相似度计算）：决定是否引入历史对话
     * 3. 意图预检 (调用 Python /intent/detect)
     * 4. 根据意图动态调整 Tool Choice
     * 5. 执行工具（调用 RAG 混合重排引擎）→ 获取代码/文档上下文
     * 6. 将工具结果发送给 LLM → LLM 生成最终回答
     * 7. 异步生成发散提问与记忆权重更新
     * 8. 流式输出给用户
     */
    @Operation(summary = "发送对话(流式)")
    @Log(title = "智能会话", businessType = BusinessType.OTHER)
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> params) {
        final SseEmitter emitter = new SseEmitter(0L);
        String content = (String) params.get("content");
        Long userId = SecurityUtils.getUserId();
        String userIp = SecurityUtils.getLoginUser().getLoginIp();
        long startTime = System.currentTimeMillis();
        
        if (content == null || content.trim().isEmpty()) {
            try {
                emitter.send(new JSONObject().set("content", "内容不能为空").toString());
            } catch (Exception ignored) {
            }
            emitter.complete();
            return emitter;
        }
        
        // 1. 安全前置校验 (防注入 V2)
        String interceptionMsg = checkSecurityRisk(content);
        if (interceptionMsg != null) {
            try {
                emitter.send(new JSONObject().set("content", interceptionMsg).toString());
            } catch (Exception ignored) {}
            emitter.complete();
            return emitter;
        }

        // 特殊优先级：历史问题查询走真实会话表，不交给大模型猜测
        if (isQuestionHistoryRequest(content)) {
            try {
                boolean queryAllUsers = isAllUsersQuestionHistoryRequest(content);
                String directAnswer = buildQuestionHistoryAnswer(userId, queryAllUsers);
                String currentModel = resolveCurrentModelName();
                saveConversationHistory(userId, content, directAnswer, startTime, userIp, currentModel);
                emitter.send(new JSONObject().set("content", directAnswer).toString());
            } catch (Exception e) {
                try {
                    emitter.send(new JSONObject().set("content", "历史问题查询失败：" + e.getMessage()).toString());
                } catch (Exception ignored) {}
            }
            emitter.complete();
            return emitter;
        }

        // 简单常规问题快路径：命中新规则后直接走轻量 LLM 回答，跳过并行预检与 RAG 主链路
        QuickChatRuleMatch quickChatRuleMatch = matchQuickChatRule(content);
        if (quickChatRuleMatch != null) {
            java.lang.Thread quickThread = new java.lang.Thread(() -> {
                try {
                    AiRuntimeConfig runtimeConfig = resolveCurrentAiRuntimeConfig();
                    String quickChatAnswer = answerQuickChatFastPath(runtimeConfig, content, quickChatRuleMatch);
                    saveConversationHistory(userId, content, quickChatAnswer, startTime, userIp, runtimeConfig.model);
                    emitter.send(new JSONObject().set("content", quickChatAnswer).toString());
                } catch (Exception e) {
                    try {
                        String fallbackAnswer = quickChatRuleMatch.fallbackAnswer;
                        saveConversationHistory(userId, content, fallbackAnswer, startTime, userIp, resolveCurrentModelName());
                        emitter.send(new JSONObject().set("content", fallbackAnswer).toString());
                    } catch (Exception ignored) {
                    }
                } finally {
                    emitter.complete();
                }
            });
            quickThread.start();
            return emitter;
        }

        java.lang.Thread thread = new java.lang.Thread(() -> {
            com.swiftboot.admin.context.AiTraceContext.init();
            StringBuilder fullReply = new StringBuilder();
            
            try {
                // 从 Redis 获取并加载当前 AI 配置 (确保线程内变量一致性)
                String configJson = stringRedisTemplate.opsForValue().get(AI_CONFIG_KEY);
                final String currentApiUrl;
                final String currentApiKey;
                final String currentModel;
                final String currentProvider;
                if (StrUtil.isNotEmpty(configJson)) {
                    JSONObject config = JSONUtil.parseObj(configJson);
                    currentApiUrl = config.getStr("apiUrl", defaultDeepseekApiUrl);
                    currentApiKey = config.getStr("apiKey", defaultDeepseekApiKey);
                    currentModel = config.getStr("model", defaultDeepseekModel);
                    currentProvider = config.getStr("provider", "");
                } else {
                    currentApiUrl = defaultDeepseekApiUrl;
                    currentApiKey = defaultDeepseekApiKey;
                    currentModel = defaultDeepseekModel;
                    currentProvider = "";
                }
                
                final boolean isAnthropic = "anthropic".equalsIgnoreCase(currentProvider) || currentApiUrl.contains("/anthropic/");
                
                // 1. 构建 Agent 模式的 System Prompt
                String systemPrompt = buildAgentSystemPrompt();
                
                // 2. 构建消息列表
                JSONArray messages = new JSONArray();
                messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
                
                // 3. 并行执行本地预检，并在总预算内按结果动态降级
                long auxStart = System.currentTimeMillis();
                long auxDeadline = auxStart + AUXILIARY_BUDGET_MS;
                String lastUserMsgJson = loadLastUserMessage(userId);

                CompletableFuture<IntentDecision> intentFuture = CompletableFuture.supplyAsync(
                        () -> detectIntentWithFallback(content), AI_AUX_EXECUTOR
                );
                CompletableFuture<Boolean> historyFuture = StrUtil.isNotBlank(lastUserMsgJson)
                        ? CompletableFuture.supplyAsync(() -> checkHistorySimilarity(content, lastUserMsgJson), AI_AUX_EXECUTOR)
                        : CompletableFuture.completedFuture(false);
                CompletableFuture<MemoryRecallDecision> memoryFuture = CompletableFuture.supplyAsync(
                        () -> recallMemoryContext(userId, content), AI_AUX_EXECUTOR
                );

                IntentDecision intentDecision = awaitWithinBudget(
                        intentFuture,
                        new IntentDecision("CHAT", 0.5),
                        auxDeadline,
                        "intent"
                );
                boolean shouldIncludeHistory = awaitWithinBudget(
                        historyFuture,
                        false,
                        auxDeadline,
                        "history_similarity"
                );
                MemoryRecallDecision memoryDecision = awaitWithinBudget(
                        memoryFuture,
                        MemoryRecallDecision.empty(),
                        auxDeadline,
                        "memory_recall"
                );
                long auxElapsed = System.currentTimeMillis() - auxStart;
                System.out.println("[Agent Budget] 本地预检完成，耗时: " + auxElapsed + "ms");

                String detectedIntent = intentDecision.intent;
                double intentConfidence = intentDecision.confidence;

                JSONObject dynamicToolChoice = new JSONObject();
                dynamicToolChoice.set("type", "auto");
                if (intentConfidence > 0.8) {
                    if ("CODE".equals(detectedIntent)) {
                        JSONObject f = new JSONObject();
                        f.set("name", "fetch_source_impl");
                        dynamicToolChoice.set("type", "function");
                        dynamicToolChoice.set("function", f);
                    } else if ("DOC".equals(detectedIntent)) {
                        JSONObject f = new JSONObject();
                        f.set("name", "fetch_business_doc");
                        dynamicToolChoice.set("type", "function");
                        dynamicToolChoice.set("function", f);
                    }
                }

                // 4. 注入近期对话历史（仅在预算内判定相关时）
                if (shouldIncludeHistory) {
                    addRecentHistory(messages, userId);
                }

                // 5. 注入长期记忆（预算内召回成功才使用）
                if (StrUtil.isNotBlank(memoryDecision.context)) {
                    JSONObject memMsg = new JSONObject();
                    memMsg.set("role", "user");
                    memMsg.set("content", memoryDecision.context);
                    messages.add(1, memMsg);

                    com.swiftboot.admin.context.AiTraceContext.AiTraceData traceData = com.swiftboot.admin.context.AiTraceContext.get();
                    if (traceData != null && memoryDecision.memoryIds != null && !memoryDecision.memoryIds.isEmpty()) {
                        com.swiftboot.admin.context.AiTraceContext.setMemoryHitIds(memoryDecision.memoryIds);
                    }
                }
                
                // 6. 添加用户消息
                messages.add(new JSONObject().set("role", "user").set("content", content));
                
                // 【修改】：优化流式输出的前置思考过程，如果是简单问题（不需要调用工具），这部分也会很快被实际回答覆盖
                try {
                    // 如果是大模型，先发送一个占位，让前端显示思考状态
                    // 但不要写死复杂的步骤，保持简洁
                    emitter.send(new JSONObject().set("content", 
                        "<thought>\n" +
                        "正在分析您的问题...\n").toString());
                } catch (Exception ignored) {}
                
                // 7. Agent 循环：允许 2 轮工具调用，以应对复杂问题
                boolean allowToolUsage = !shouldDisableToolsForChat(detectedIntent, intentConfidence);
                int maxToolCalls = allowToolUsage ? 2 : 1;
                int toolCallCount = 0;
                boolean toolCallLimitReached = false;
                boolean hasMemoryHit = false; // 记录本次对话是否命中了历史记忆
                
                while (toolCallCount < maxToolCalls) {
                    JSONObject requestBody = new JSONObject();
                    requestBody.set("model", currentModel);
                    requestBody.set("stream", false);
                    
                    // 仅在第一轮应用动态工具选择
                    if (allowToolUsage && toolCallCount == 0 && !"auto".equals(dynamicToolChoice.getStr("type"))) {
                         requestBody.set("tool_choice", dynamicToolChoice);
                    }
                    
                    if (isAnthropic) {
                        // Anthropic 格式转换
                        requestBody.set("max_tokens", 4096);
                        JSONArray anthropicMessages = new JSONArray();
                        String systemContent = "";
                        for (int i = 0; i < messages.size(); i++) {
                            JSONObject m = messages.getJSONObject(i);
                            String role = m.getStr("role");
                            
                            if ("system".equals(role)) {
                                systemContent = m.getStr("content");
                                continue;
                            }
                            
                            JSONObject am = new JSONObject();
                            am.set("role", role);
                            
                            if ("assistant".equals(role) && m.containsKey("tool_calls")) {
                                JSONArray contentArray = new JSONArray();
                                String textContent = m.getStr("content");
                                if (StrUtil.isNotEmpty(textContent)) {
                                    contentArray.add(new JSONObject().set("type", "text").set("text", textContent));
                                }
                                
                                JSONArray tcs = m.getJSONArray("tool_calls");
                                for (int j = 0; j < tcs.size(); j++) {
                                    JSONObject tc = tcs.getJSONObject(j);
                                    JSONObject f = tc.getJSONObject("function");
                                    JSONObject toolUse = new JSONObject();
                                    toolUse.set("type", "tool_use");
                                    toolUse.set("id", tc.getStr("id"));
                                    toolUse.set("name", f.getStr("name"));
                                    toolUse.set("input", JSONUtil.parseObj(f.getStr("arguments")));
                                    contentArray.add(toolUse);
                                }
                                am.set("content", contentArray);
                            } else if ("tool".equals(role)) {
                                // 理论上内部 messages 应该保持 OpenAI 格式，由这里统一转
                                am.set("role", "user");
                                JSONArray contentArray = new JSONArray();
                                JSONObject tr = new JSONObject();
                                tr.set("type", "tool_result");
                                tr.set("tool_use_id", m.getStr("tool_call_id"));
                                tr.set("content", m.getStr("content"));
                                contentArray.add(tr);
                                am.set("content", contentArray);
                            } else {
                                am.set("content", m.getStr("content"));
                            }
                            
                            // 检查连续角色并合并 (Anthropic 不允许连续同角色)
                            if (!anthropicMessages.isEmpty()) {
                                JSONObject lastMsg = anthropicMessages.getJSONObject(anthropicMessages.size() - 1);
                                if (lastMsg.getStr("role").equals(am.getStr("role"))) {
                                    // 合并 content
                                    Object lastContent = lastMsg.get("content");
                                    Object currentContent = am.get("content");
                                    
                                    JSONArray mergedContent = new JSONArray();
                                    if (lastContent instanceof JSONArray) {
                                        mergedContent.addAll((JSONArray) lastContent);
                                    } else {
                                        mergedContent.add(new JSONObject().set("type", "text").set("text", String.valueOf(lastContent)));
                                    }
                                    
                                    if (currentContent instanceof JSONArray) {
                                        mergedContent.addAll((JSONArray) currentContent);
                                    } else {
                                        mergedContent.add(new JSONObject().set("type", "text").set("text", String.valueOf(currentContent)));
                                    }
                                    lastMsg.set("content", mergedContent);
                                    continue;
                                }
                            }
                            
                            anthropicMessages.add(am);
                        }
                        if (StrUtil.isNotEmpty(systemContent)) {
                            requestBody.set("system", systemContent);
                        }
                        requestBody.set("messages", anthropicMessages);
                        
                        // Anthropic 工具格式 (M2.7 Native)
                        if (allowToolUsage && this.agentTools != null) {
                            JSONArray anthropicTools = new JSONArray();
                            for (int i = 0; i < this.agentTools.size(); i++) {
                                JSONObject t = this.agentTools.getJSONObject(i);
                                if ("function".equals(t.getStr("type"))) {
                                    JSONObject f = t.getJSONObject("function");
                                    JSONObject at = new JSONObject();
                                    at.set("name", f.getStr("name"));
                                    at.set("description", f.getStr("description"));
                                    at.set("input_schema", f.getJSONObject("parameters"));
                                    anthropicTools.add(at);
                                }
                            }
                            requestBody.set("tools", anthropicTools);
                        }
                    } else {
                        requestBody.set("messages", messages);
                        // 如果是大模型为 minimax 且不是 anthropic 模式，暂时不开启 tools
                        if (allowToolUsage && !"minimax".equalsIgnoreCase(currentModel) && !currentModel.contains("abab")) {
                            requestBody.set("tools", this.agentTools);
                            requestBody.set("tool_choice", "auto");
                        } else if (!allowToolUsage) {
                            requestBody.set("tool_choice", "none");
                        }
                    }
                    
                    System.out.println("[Agent] 发送请求到 LLM，当前工具调用轮次: " + (toolCallCount + 1) + " URL: " + currentApiUrl);
                    HttpResponse response = executeFirstLlmCall(currentApiUrl, currentApiKey, requestBody);
                    
                    if (!response.isOk()) {
                        emitter.send(new JSONObject().set("content", "AI 服务响应异常: " + response.getStatus()).toString());
                        emitter.complete();
                        return;
                    }
                    
                    JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                    if (jsonResponse.containsKey("error")) {
                        emitter.send(new JSONObject().set("content", "AI 服务错误: " + jsonResponse.getJSONObject("error").getStr("message")).toString());
                        emitter.complete();
                        return;
                    }
                    
                    JSONObject assistantMessage;
                    String finishReason;
                    
                    if (isAnthropic) {
                        // Anthropic 响应转换
                        if (jsonResponse.containsKey("stop_reason")) {
                            finishReason = jsonResponse.getStr("stop_reason");
                        } else {
                            finishReason = "stop";
                        }
                        
                        JSONArray contentBlocks = jsonResponse.getJSONArray("content");
                        assistantMessage = new JSONObject();
                        assistantMessage.set("role", "assistant");
                        
                        JSONArray toolCalls = new JSONArray();
                        if (contentBlocks != null) {
                            for (int i = 0; i < contentBlocks.size(); i++) {
                                JSONObject block = contentBlocks.getJSONObject(i);
                                if ("tool_use".equals(block.getStr("type"))) {
                                    JSONObject tc = new JSONObject();
                                    tc.set("id", block.getStr("id"));
                                    tc.set("type", "function");
                                    JSONObject fn = new JSONObject();
                                    fn.set("name", block.getStr("name"));
                                    fn.set("arguments", JSONUtil.toJsonStr(block.getJSONObject("input")));
                                    tc.set("function", fn);
                                    toolCalls.add(tc);
                                } else if ("text".equals(block.getStr("type"))) {
                                    assistantMessage.set("content", block.getStr("text"));
                                }
                            }
                        }
                        
                        if (!toolCalls.isEmpty()) {
                            assistantMessage.set("tool_calls", toolCalls);
                            finishReason = "tool_calls"; // 兼容后续逻辑
                        }
                    } else {
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject choice = choices.getJSONObject(0);
                            assistantMessage = choice.getJSONObject("message");
                            finishReason = choice.getStr("finish_reason");
                        } else {
                            assistantMessage = new JSONObject();
                            finishReason = "stop";
                        }
                    }
                    
                    // 检查是否需要调用工具
                    if ("tool_calls".equals(finishReason) || "tool_use".equals(finishReason)) {
                        JSONArray toolCalls = assistantMessage.getJSONArray("tool_calls");
                        System.out.println("[Agent] LLM 请求调用工具，数量: " + (toolCalls != null ? toolCalls.size() : 0));
                        
                        messages.add(assistantMessage);
                        
                        if (toolCalls != null && !toolCalls.isEmpty()) {
                            for (int i = 0; i < toolCalls.size(); i++) {
                                JSONObject toolCall = toolCalls.getJSONObject(i);
                                String toolCallId = toolCall.getStr("id");
                                JSONObject functionObj = toolCall.getJSONObject("function");
                                String functionName = functionObj.getStr("name");
                                String argumentsStr = functionObj.getStr("arguments");
                                
                                System.out.println("[Agent] 执行工具: " + functionName + ", 参数: " + argumentsStr);
                                
                                String toolResult = executeAgentTool(functionName, argumentsStr, detectedIntent, emitter);
                                
                                JSONObject toolMessage = new JSONObject();
                                toolMessage.set("role", "tool");
                                toolMessage.set("tool_call_id", toolCallId);
                                toolMessage.set("content", toolResult);
                                messages.add(toolMessage);
                                System.out.println("[Agent] 工具执行完成，结果长度: " + toolResult.length());
                            }
                        }
                        
                        toolCallCount++;
                        
                        // 如果达到最大轮次，强制结束循环，不再给模型尝试的机会
                        if (toolCallCount >= maxToolCalls) {
                            System.out.println("[Agent] 达到工具调用上限 (" + maxToolCalls + " 轮)，强制生成回答");
                            toolCallLimitReached = true;
                            break;
                        }
                        continue;
                    }
                    
                    System.out.println("[Agent] LLM 准备生成最终回答，切换到流式输出");
                    break;
                }
                
                // 如果没有调用任何工具，说明模型直接回答了，这时候需要先把占位的 thought 标签闭合掉
                if (toolCallCount == 0) {
                    try {
                        emitter.send(new JSONObject().set("content", "\n</thought>\n\n").toString());
                    } catch (Exception ignored) {}
                }
                
                // 6. 流式输出最终回答
                // 【重要修改】：对于 DeepSeek 模型，如果之前强制使用了 tools，流式请求中必须保持 messages 格式一致
                // 由于我们前面为了避免幻觉去掉了 tools，但部分模型（特别是 DeepSeek）会因为之前的 assistant 消息中带有 tool_calls 而强制要求当前请求也带 tools 或者要求更严格的上下文格式。
                // 解决策略：在最后一步生成最终回答时，重构 messages 数组，将 tool_calls 和 tool_result 转换为普通的文本上下文，彻底切断模型与 Function Calling 的关联。
                JSONObject streamRequest = new JSONObject();
                streamRequest.set("model", currentModel);
                streamRequest.set("stream", true);
                
                JSONArray finalMessages = new JSONArray();
                String systemContent = "";
                
                for (int i = 0; i < messages.size(); i++) {
                    JSONObject m = messages.getJSONObject(i);
                    String role = m.getStr("role");
                    
                    if ("system".equals(role)) {
                        systemContent += m.getStr("content") + "\n";
                        continue;
                    }
                    
                    if (isAnthropic) {
                        finalMessages.add(m); // Anthropic 已经在前面处理过合并了
                    } else {
                        // 针对 DeepSeek/OpenAI 格式：将 tool_calls 和 tool_result 压平为文本
                        if ("assistant".equals(role) && m.containsKey("tool_calls")) {
                            // 忽略 assistant 发出的 tool_calls 消息，避免触发严格的校验
                            continue;
                        } else if ("tool".equals(role)) {
                            // 将工具返回的结果转换为普通的 user 消息，作为补充上下文
                            JSONObject flatMsg = new JSONObject();
                            flatMsg.set("role", "user");
                            flatMsg.set("content", "【检索到的参考信息】\n" + m.getStr("content"));
                            finalMessages.add(flatMsg);
                        } else {
                            finalMessages.add(m);
                        }
                    }
                }
                
                // 追加最后一条强制约束
                systemContent += "\n【系统提示】请根据以上提供的参考信息，直接用自然语言回答用户的问题。严禁输出任何 XML 标签、function_calls 或 DSML 格式。";
                
                if (isAnthropic) {
                    streamRequest.set("max_tokens", 4096);
                    if (StrUtil.isNotEmpty(systemContent)) {
                        streamRequest.set("system", systemContent);
                    }
                    streamRequest.set("messages", finalMessages);
                } else {
                    // OpenAI 格式，把 system 放在第一条
                    JSONArray oaiMessages = new JSONArray();
                    if (StrUtil.isNotEmpty(systemContent)) {
                        oaiMessages.add(new JSONObject().set("role", "system").set("content", systemContent));
                    }
                    oaiMessages.addAll(finalMessages);
                    streamRequest.set("messages", oaiMessages);
                }
                
                System.out.println("[Agent] 开始生成最终回答 (Stream Mode), 已重构上下文切断幻觉源");
                
                try (HttpResponse streamResponse = HttpRequest.post(currentApiUrl)
                        .timeout(300000) // 延长超时时间到 5分钟，避免长文本生成中断
                        .header("Authorization", "Bearer " + currentApiKey)
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/event-stream")
                        .body(streamRequest.toString())
                        .execute(true)) {
                    
                    if (!streamResponse.isOk()) {
                        String fallbackText = completeNonStream(isAnthropic, currentApiUrl, currentApiKey, currentModel, finalMessages, systemContent);
                        if (StrUtil.isNotEmpty(fallbackText)) {
                            fullReply.append(fallbackText);
                            emitter.send(new JSONObject().set("content", fallbackText).toString());
                        } else {
                            System.err.println("[Agent] AI 服务响应异常: " + streamResponse.getStatus());
                            emitter.send(new JSONObject().set("content", "AI 服务响应异常: " + streamResponse.getStatus()).toString());
                            emitter.complete();
                            return;
                        }
                    } else {
                        try (InputStream inputStream = streamResponse.bodyStream();
                             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                            String line;
                            // 【缓冲区机制】：用于累积检测跨chunk的幻觉标签
                            StringBuilder pendingBuffer = new StringBuilder();
                            final int BUFFER_THRESHOLD = 100; // 缓冲区阈值，避免无限累积
                            boolean nativeThinkingStarted = false;
                            
                            while ((line = reader.readLine()) != null) {
                                if (line.isEmpty() || !line.startsWith("data:")) {
                                    continue;
                                }
                                String data = line.substring(5).trim();
                                if (data.isEmpty() || "[DONE]".equals(data)) {
                                    continue;
                                }
                                
                                JSONObject json = JSONUtil.parseObj(data);
                                if (json.containsKey("error")) {
                                    String errorMsg = json.getStr("error");
                                    System.err.println("[Agent] AI 服务返回错误: " + errorMsg);
                                    emitter.send(new JSONObject().set("content", errorMsg).toString());
                                    break;
                                }
                                
                                String chunk = null;
                                if (isAnthropic) {
                                    String type = json.getStr("type");
                                    if ("content_block_delta".equals(type)) {
                                        JSONObject delta = json.getJSONObject("delta");
                                        if (delta != null) {
                                            String deltaType = delta.getStr("type");
                                            if ("text_delta".equals(deltaType)) {
                                                String text = delta.getStr("text");
                                                if (nativeThinkingStarted) {
                                                    // 当模型从 thinking 切换到 text 时，自动闭合 thought 标签
                                                    chunk = "\n</thought>\n\n" + text;
                                                    nativeThinkingStarted = false;
                                                } else {
                                                    chunk = text;
                                                }
                                            } else if ("thinking_delta".equals(deltaType)) {
                                                String thinking = delta.getStr("thinking");
                                                if (StrUtil.isNotEmpty(thinking)) {
                                                    if (!nativeThinkingStarted) {
                                                        // 首次遇到 thinking，如果不发送前缀，直接累加
                                                        chunk = thinking;
                                                        nativeThinkingStarted = true;
                                                    } else {
                                                        chunk = thinking;
                                                    }
                                                }
                                            }
                                        }
                                    } else if ("message_delta".equals(type)) {
                                        // 处理 message_delta
                                    } else if ("message_stop".equals(type)) {
                                        // 确保结束时如果还有未闭合的 thought 标签则闭合
                                        if (nativeThinkingStarted) {
                                            chunk = "\n</thought>\n\n";
                                            nativeThinkingStarted = false;
                                        }
                                    }
                                } else {
                                    JSONArray choices = json.getJSONArray("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                        if (delta != null && delta.containsKey("content")) {
                                            chunk = delta.getStr("content");
                                        }
                                    }
                                }
                                
                                if (chunk != null) {
                                    // 将新chunk加入缓冲区
                                    pendingBuffer.append(chunk);
                                    String bufferContent = pendingBuffer.toString();
                                    
                                    // 【核心拦截逻辑】：检测缓冲区中的幻觉标签
                                    // 检测完整的幻觉标签模式（不区分大小写）
                                    String lowerBuffer = bufferContent.toLowerCase();
                                    boolean containsHallucination = 
                                        bufferContent.toUpperCase().contains("DSML") ||
                                        lowerBuffer.contains("<function_calls>") ||
                                        lowerBuffer.contains("</function_calls>") ||
                                        lowerBuffer.contains("<function_calls") ||
                                        lowerBuffer.contains("<invoke") ||
                                        lowerBuffer.contains("</invoke>") ||
                                        lowerBuffer.contains("<parameter") ||
                                        lowerBuffer.contains("</parameter>") ||
                                        lowerBuffer.contains("name=\"search_codebase\"") ||
                                        lowerBuffer.contains("name=\"query\"");
                                    
                                    // 检测可能正在形成的标签（部分匹配）
                                    boolean possibleHallucination = 
                                        lowerBuffer.contains("<function") ||
                                        lowerBuffer.contains("<invok") ||
                                        lowerBuffer.contains("<param") ||
                                        lowerBuffer.contains("name=\"") ||
                                        bufferContent.endsWith("<") ||
                                        bufferContent.endsWith("</") ||
                                        bufferContent.endsWith("<f") ||
                                        bufferContent.endsWith("<fu") ||
                                        bufferContent.endsWith("<fun");
                                    
                                    if (containsHallucination) {
                                        // 检测到完整的幻觉标签，清除整个缓冲区
                                        System.out.println("[Security] 拦截到幻觉标签，清除缓冲区: " + bufferContent.substring(0, Math.min(50, bufferContent.length())) + "...");
                                        pendingBuffer.setLength(0);
                                        continue;
                                    }
                                    
                                    if (possibleHallucination && pendingBuffer.length() < BUFFER_THRESHOLD) {
                                        // 可能正在形成幻觉标签，继续累积观察
                                        continue;
                                    }
                                    
                                    // 安全内容，发送给前端
                                    // 但只发送到最后一个安全断点（避免发送不完整的标签）
                                    String safeContent = bufferContent;
                                    int lastSafePoint = safeContent.length();
                                    
                                    // 查找最后一个 '<' 的位置，如果它后面没有 '>'，则不发送这部分
                                    int lastOpenBracket = safeContent.lastIndexOf('<');
                                    if (lastOpenBracket >= 0 && safeContent.indexOf('>', lastOpenBracket) < 0) {
                                        lastSafePoint = lastOpenBracket;
                                    }
                                    
                                    if (lastSafePoint > 0) {
                                        String toSend = safeContent.substring(0, lastSafePoint);
                                        
                                        // 【最终过滤】：发送前再次检查并清除任何残留的幻觉标签
                                        toSend = cleanHallucinationTags(toSend);
                                        
                                        if (toSend != null && !toSend.isEmpty()) {
                                            fullReply.append(toSend);
                                            emitter.send(new JSONObject().set("content", toSend).toString());
                                        }
                                        
                                        // 保留未发送的部分到缓冲区
                                        pendingBuffer.setLength(0);
                                        if (lastSafePoint < safeContent.length()) {
                                            pendingBuffer.append(safeContent.substring(lastSafePoint));
                                        }
                                    }
                                }
                            }
                            
                            // 处理缓冲区中剩余的内容
                            if (pendingBuffer.length() > 0) {
                                String remaining = pendingBuffer.toString();
                                if (nativeThinkingStarted) {
                                    remaining += "\n</thought>";
                                }
                                // 清洗残留内容
                                remaining = cleanHallucinationTags(remaining);
                                if (remaining != null && !remaining.isEmpty()) {
                                    fullReply.append(remaining);
                                    emitter.send(new JSONObject().set("content", remaining).toString());
                                }
                            }
                        }
                    }
                }
                
                    // 7. 存储对话历史
                    if (fullReply.length() > 0) {
                        String fullContent = fullReply.toString();
                        String thought = null;
                        String finalAnswer = fullContent;
                        
                        // 尝试提取 <thought> 标签内容
                        try {
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<thought>(.*?)</thought>", java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(fullContent);
                            if (matcher.find()) {
                                thought = matcher.group(1).trim();
                                // 记录到 TraceContext
                                com.swiftboot.admin.context.AiTraceContext.addThought(thought);
                                
                                // 移除 thought 标签及其内容，只保留最终回答存入会话记录
                                finalAnswer = matcher.replaceAll("").trim();
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to parse thought tag: " + e.getMessage());
                        }
                        
                        // 【二次清洗】：防止幻觉标签泄漏到数据库
                        finalAnswer = finalAnswer
                            .replaceAll("<[｜]DSML[｜][\\s\\S]*?([｜]/DSML[｜]>|</[｜]DSML[｜]function_calls>)", "")
                            .replaceAll("<[|]DSML[|][\\s\\S]*?([|]/DSML[|]>|</[|]DSML[|]function_calls>)", "")
                            .replaceAll("<function_calls>[\\s\\S]*?</function_calls>", "")
                            .replaceAll("<invoke[\\s\\S]*?</invoke>", "")
                            .replaceAll("<parameter[\\s\\S]*?</parameter>", "")
                            .replaceAll("<function_calls>.*$", "")
                            .replaceAll("<invoke.*$", "")
                            .trim();
                        
                        // 保存会话记录
                        SysAiSession savedSession = saveConversationHistory(userId, content, finalAnswer, startTime, userIp, currentModel);
                        System.out.println("[Agent] 流式响应正常结束，回复长度: " + fullReply.length());
                        
                        // 【异步机制】：处理记忆权重更新与发散提问
                        com.swiftboot.admin.context.AiTraceContext.AiTraceData traceData = com.swiftboot.admin.context.AiTraceContext.get();
                        if (traceData != null && traceData.getMemoryHitIds() != null && !traceData.getMemoryHitIds().isEmpty()) {
                            updateMemoryCitations(userId, traceData.getMemoryHitIds());
                            String suggestionBlock = formatFollowUpSuggestions(
                                    generateFollowUpSuggestions(isAnthropic, currentApiUrl, currentApiKey, currentModel, finalAnswer)
                            );
                            if (StrUtil.isNotBlank(suggestionBlock)) {
                                try {
                                    emitter.send(new JSONObject().set("content", "\n\n" + suggestionBlock).toString());
                                } catch (Exception ignored) {
                                }
                            }
                            traceData.setMemoryHitIds(new ArrayList<>());
                        }
                        if (traceData != null && traceData.getMemoryHitIds() != null && !traceData.getMemoryHitIds().isEmpty()) {
                            final List<String> hitMemoryIds = traceData.getMemoryHitIds();
                            final String asyncModel = currentModel;
                            final String asyncApiUrl = currentApiUrl;
                            final String asyncApiKey = currentApiKey;
                            final boolean asyncIsAnthropic = isAnthropic;
                            final String asyncFinalAnswer = finalAnswer;
                            
                            java.lang.Thread asyncThread = new java.lang.Thread(() -> {
                                try {
                                    if (!hitMemoryIds.isEmpty()) {
                                        int updatedCount = updateMemoryCitations(userId, hitMemoryIds);
                                        System.out.println("[Agent Async] Updated citation count: " + updatedCount);
                                        System.out.println("[Agent Async] 开始更新记忆权重，命中的记忆数: " + hitMemoryIds.size());
                                        // 1. 更新记忆权重 (调用 Python 引擎)
                                        // 这里为了简化，假设 Python 引擎提供了一个 /memory/update_citation 接口
                                        // 实际可以复用 add 或专门开一个更新权重的接口
                                        // TODO: Python 端需增加 update_citation 接口支持
                                        
                                        // 2. 生成发散提问
                                        System.out.println("[Agent Async] 开始生成发散引导提问...");
                                        JSONObject asyncReq = new JSONObject();
                                        asyncReq.set("model", asyncModel);
                                        asyncReq.set("stream", false);
                                        
                                        String prompt = "你是一个智能助手。基于以下刚刚向用户提供的回答，请生成2个发散性的探索问题，引导用户进一步提问。只返回问题文本，每行一个，不要任何序号或前缀。\n\n回答内容：\n" + asyncFinalAnswer;
                                        
                                        JSONArray asyncMessages = new JSONArray();
                                        if (asyncIsAnthropic) {
                                            asyncReq.set("max_tokens", 500);
                                            asyncReq.set("system", prompt);
                                            asyncMessages.add(new JSONObject().set("role", "user").set("content", "生成发散问题"));
                                            asyncReq.set("messages", asyncMessages);
                                        } else {
                                            asyncMessages.add(new JSONObject().set("role", "system").set("content", prompt));
                                            asyncMessages.add(new JSONObject().set("role", "user").set("content", "生成发散问题"));
                                            asyncReq.set("messages", asyncMessages);
                                        }
                                        
                                        HttpResponse asyncRes = HttpRequest.post(asyncApiUrl)
                                                .timeout(10000)
                                                .header("Authorization", "Bearer " + asyncApiKey)
                                                .header("Content-Type", "application/json")
                                                .body(asyncReq.toString())
                                                .execute();
                                                
                                        if (asyncRes.isOk()) {
                                            JSONObject asyncJson = JSONUtil.parseObj(asyncRes.body());
                                            String suggestions = "";
                                            if (asyncIsAnthropic) {
                                                JSONArray blocks = asyncJson.getJSONArray("content");
                                                if (blocks != null && !blocks.isEmpty()) {
                                                    suggestions = blocks.getJSONObject(0).getStr("text");
                                                }
                                            } else {
                                                JSONArray choices = asyncJson.getJSONArray("choices");
                                                if (choices != null && !choices.isEmpty()) {
                                                    suggestions = choices.getJSONObject(0).getJSONObject("message").getStr("content");
                                                }
                                            }
                                            
                                            if (StrUtil.isNotEmpty(suggestions)) {
                                                System.out.println("[Agent Async] 成功生成发散问题:\n" + suggestions);
                                                // 可以通过 SSE 或 WebSocket 推送给前端，或者存入数据库供下次拉取
                                                // 演示中直接打印日志
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println("[Agent Async] 异步任务执行失败: " + e.getMessage());
                                }
                            });
                            asyncThread.start();
                        }
                        
                        // 保存 Trace
                        try {
                            if (traceData != null) {
                                com.swiftboot.admin.domain.SysAiTrace trace = new com.swiftboot.admin.domain.SysAiTrace();
                                trace.setTraceId(traceData.getTraceId());
                                if (savedSession != null && savedSession.getId() != null) {
                                    trace.setSessionId(savedSession.getId());
                                }
                                
                                trace.setThoughtPath(JSONUtil.toJsonStr(traceData.getThoughtPath()));
                                trace.setToolCalls(JSONUtil.toJsonStr(traceData.getToolCalls()));
                                trace.setContextInfo(traceData.getContextInfo());
                                trace.setFinalAnswer(finalAnswer);
                                trace.setDuration(System.currentTimeMillis() - traceData.getStartTime());
                                trace.setCreateTime(java.time.LocalDateTime.now());
                                trace.setUpdateTime(java.time.LocalDateTime.now());
                                
                                aiTraceService.save(trace);
                                System.out.println("[Trace] Saved trace record: " + trace.getTraceId());
                            }
                        } catch (Exception e) {
                            System.err.println("[Trace] Failed to save trace: " + e.getMessage());
                        } finally {
                            // 清理上下文
                            com.swiftboot.admin.context.AiTraceContext.clear();
                        }
                        
                    } else {
                        System.out.println("[Agent] 流式响应结束，但回复为空");
                    }
                
                emitter.send("[DONE]");
                emitter.complete();
                
            } catch (Exception e) {
                System.err.println("[Agent] 流式响应异常: " + e.getMessage());
                e.printStackTrace();
                try {
                    // 发送错误详情，前端已做兼容，不会覆盖已有内容
                    emitter.send(new JSONObject().set("content", "AI 服务调用失败: " + e.getMessage()).toString());
                } catch (Exception ignored) {
                }
                emitter.complete();
            }
        });
        thread.start();
        
        return emitter;
    }
    
    private String completeNonStream(boolean isAnthropic, String apiUrl, String apiKey, String model, JSONArray messages, String systemPrompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", model);
            requestBody.set("stream", false);
            
            if (isAnthropic) {
                requestBody.set("max_tokens", 4096);
                if (StrUtil.isNotEmpty(systemPrompt)) {
                    requestBody.set("system", systemPrompt);
                }
                requestBody.set("messages", messages);
            } else {
                JSONArray oaiMessages = new JSONArray();
                if (StrUtil.isNotEmpty(systemPrompt)) {
                    oaiMessages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
                }
                oaiMessages.addAll(messages);
                requestBody.set("messages", oaiMessages);
            }
            
            HttpResponse response = HttpRequest.post(apiUrl)
                    .timeout(120000)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(requestBody.toString())
                    .execute();
                    
            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                if (isAnthropic) {
                    JSONArray contentBlocks = jsonResponse.getJSONArray("content");
                    if (contentBlocks != null && !contentBlocks.isEmpty()) {
                        for (int i = 0; i < contentBlocks.size(); i++) {
                            JSONObject block = contentBlocks.getJSONObject(i);
                            if ("text".equals(block.getStr("type"))) {
                                return block.getStr("text");
                            }
                        }
                    }
                } else {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject choice = choices.getJSONObject(0);
                        return choice.getJSONObject("message").getStr("content");
                    }
                }
            } else {
                System.err.println("[Agent] Non-stream fallback failed: " + response.getStatus() + " " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[Agent] Non-stream fallback exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * 构建 Agent 模式的 System Prompt
     */
    private String buildAgentSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        
        if (StrUtil.isNotEmpty(agentRuleContext)) {
            prompt.append(agentRuleContext).append("\n\n");
        } else {
            // Fallback规则（如果agent_rule.md加载失败）
            prompt.append("【身份设定】\n");
            prompt.append("你是一个名为“SwiftBoot 智能助手”的全栈开发专家。你的核心职责是帮助开发者理解、维护和扩展当前基于 Spring Boot 3 和 Vue 3 的 SwiftBoot 框架项目。\n");
            prompt.append("你需要时刻保持你的专家身份，语气专业、直接、乐于助人。当用户问你是谁时，请明确回答你是“SwiftBoot 智能助手”。\n\n");
            
            prompt.append("【工作流程与原则】\n");
            prompt.append("1. **直接回答优先**：如果用户的问题是通用技术问题、闲聊、或你不需要查阅代码就能准确回答的问题，请**直接回答**，不需要调用任何工具。\n");
            prompt.append("2. **代码检索**：只有当用户明确询问项目的具体实现、业务逻辑、接口定义或排查项目内的 Bug 时，才调用 `search_codebase` 工具查阅代码库。\n\n");
            
            prompt.append("## 核心约束\n");
            prompt.append("1. **深度思考**：在生成回答前，可以先梳理思路，但不要在最终输出中暴露繁琐的格式。\n");
            prompt.append("2. **严禁幻觉标签**：禁止输出 DSML、function_calls、invoke 等任何非标准Markdown的标签\n");
            prompt.append("3. **业务化表达**：将代码逻辑翻译为业务规则，避免大段直接粘贴代码\n");
            prompt.append("4. **直接回答**：不要说\"根据代码上下文\"等铺垫语\n");
        }
        
        if (StrUtil.isNotEmpty(skillsContext)) {
            prompt.append("【项目背景知识】\n");
            prompt.append(skillsContext).append("\n\n");
        }
        
        // 额外强化：防止DSML幻觉 (这部分非常核心，保留硬编码以防规则文件丢失导致灾难性输出)
        prompt.append("\n\n## 严禁输出的内容\n");
        prompt.append("- ❌ 任何包含 `DSML`、`｜DSML｜`、`<|DSML|` 的标签\n");
        prompt.append("- ❌ 任何 `<function_calls>`、`<invoke>`、`<parameter>` 等XML格式\n");
        prompt.append("- ❌ 任何尝试调用工具的特殊语法（如果需要查代码，请使用标准的 function call 机制，否则直接输出纯文本回答）\n");
        prompt.append("- ✅ 只输出标准的Markdown格式内容\n");
        
        return prompt.toString();
    }
    
    /**
     * 清洗幻觉标签
     * 用于在发送内容给前端之前，移除任何可能的幻觉标签
     */
    private String cleanHallucinationTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // 使用更精确的正则表达式清除各种幻觉标签
        String cleaned = content
            // 1. 清除 XML 风格的工具调用标签 (OpenAI 常见幻觉)
            .replaceAll("(?i)<function_calls>[\\s\\S]*?</function_calls>", "")
            .replaceAll("(?i)<invoke[^>]*>[\\s\\S]*?</invoke>", "")
            .replaceAll("(?i)<parameter[^>]*>[\\s\\S]*?</parameter>", "")
            
            // 2. 清除 DSML 风格标签 (DeepSeek/MiniMax 常见幻觉)
            .replaceAll("(?i)<[｜|]?DSML[｜|]?[\\s\\S]*?[｜|]?/?DSML[｜|]?>", "")
            
            // 3. 清除未闭合的标签开头 (流式输出中常见的残留)
            .replaceAll("(?i)<function_calls[\\s\\S]*$", "")
            .replaceAll("(?i)<invoke[\\s\\S]*$", "")
            .replaceAll("(?i)<parameter[\\s\\S]*$", "")
            .replaceAll("(?i)<[｜|]?DSML[\\s\\S]*$", "")
            
            // 4. 清除残留的属性文本 (针对标签尖括号被截断的情况)
            .replaceAll("(?i)invokename=\"[^\"]*\"", "")
            .replaceAll("(?i)parametername=\"[^\"]*\"", "")
            .replaceAll("(?i)string=\"(?:true|false)\"", "");
        
        // 如果清洗后内容只剩下空白，则返回空
        if (cleaned.trim().isEmpty() && !content.trim().isEmpty()) {
            return "";
        }
        
        return cleaned;
    }
    
    /**
     * 执行 Agent 工具
     */
    private String executeAgentTool(String functionName, String argumentsJson, String detectedIntent, SseEmitter emitter) {
        try {
            if ("fetch_source_impl".equals(functionName) || "fetch_business_doc".equals(functionName)) {
                JSONObject args = JSONUtil.parseObj(argumentsJson);
                String query = args.getStr("query");
                
                if (StrUtil.isBlank(query)) {
                    return "错误：搜索关键词不能为空";
                }
                
                JSONObject ragRequest = new JSONObject();
                ragRequest.set("question", query);
                ragRequest.set("n_results", 10);
                ragRequest.set("intent", detectedIntent);
                ragRequest.set("tool_name", functionName);
                
                long ragStartTime = System.currentTimeMillis();
                System.out.println("[Agent Tool] 调用 RAG 引擎: " + RAG_API_URL + ", tool=" + functionName + ", query=" + query);
                
                String ragResponse = HttpRequest.post(RAG_API_URL)
                        .timeout(60000) // 延长超时时间到 60秒，应对大量数据检索
                        .body(ragRequest.toString())
                        .execute()
                        .body();
                
                long ragDuration = System.currentTimeMillis() - ragStartTime;
                System.out.println("[Agent Tool] RAG 响应耗时: " + ragDuration + "ms");
                
                // 记录 Trace 工具调用
                com.swiftboot.admin.context.AiTraceContext.addToolCall(functionName, args, "RAG_SEARCH_RESULT", ragDuration);
                
                JSONObject ragJson = JSONUtil.parseObj(ragResponse);
                JSONArray results = ragJson.getJSONArray("results");
                
                if (results == null || results.isEmpty()) {
                    return "未找到与 \"" + query + "\" 相关的代码或文档。请尝试使用更具体的类名、方法名或功能描述。";
                }
                
                // 在后端进行二次过滤 (Intent Filtering)
                JSONArray filteredResults = new JSONArray();
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    JSONObject meta = item.getJSONObject("metadata");
                    if (meta != null) {
                        String type = meta.getStr("type", "");
                        if ("fetch_source_impl".equals(functionName)) {
                            // 排除 markdown 文档，只保留代码切片
                            if (!"markdown_section".equals(type)) {
                                filteredResults.add(item);
                            }
                        } else if ("fetch_business_doc".equals(functionName)) {
                            // 增加 markdown 的权重，或者直接只保留 markdown 和数据库 Schema
                            if ("markdown_section".equals(type) || "database_schema".equals(type)) {
                                filteredResults.add(item);
                            }
                        }
                    }
                }
                
                // 如果过滤后为空，回退使用原始结果
                if (filteredResults.isEmpty()) {
                    filteredResults = results;
                }
                
                // 记录 Trace 上下文
                com.swiftboot.admin.context.AiTraceContext.setContextInfo(filteredResults.toString());
                
                StringBuilder sb = new StringBuilder();
                sb.append("找到 ").append(filteredResults.size()).append(" 个相关参考片段：\n\n");
                
                // 提取引用的文件名
                List<String> fileNames = new ArrayList<>();
                for (int i = 0; i < filteredResults.size(); i++) {
                    JSONObject item = filteredResults.getJSONObject(i);
                    JSONObject meta = item.getJSONObject("metadata");
                    String codeContent = item.getStr("content");
                    String source = meta != null ? meta.getStr("source") : "unknown";
                    
                    // 仅提取文件名，不包含路径
                    String fileName = source;
                    if (source.contains("/") || source.contains("\\")) {
                        fileName = new File(source).getName();
                    }
                    
                    if (StrUtil.isNotBlank(fileName) && !fileNames.contains(fileName)) {
                        fileNames.add(fileName);
                    }
                    
                    sb.append("### 引用 ").append(i + 1).append(": ").append(fileName).append("\n");
                    sb.append("```java\n").append(codeContent).append("\n```\n\n");
                }
                
                // 向前端思考过程注入详细信息
                if (emitter != null) {
                    try {
                        String fileList = fileNames.stream().collect(Collectors.joining(", "));
                        String toolType = "fetch_source_impl".equals(functionName) ? "源码检索" : "文档检索";
                        emitter.send(new JSONObject().set("content", "\n**" + toolType + "**：已查阅相关文件：[" + fileList + "]\n").toString());
                    } catch (Exception ignored) {}
                }
                
                sb.append("\n【重要提示】检索已完成。以上代码或文档已包含回答问题所需的全部核心逻辑。请现在停止调用工具，直接利用这些参考片段生成最终回答。");
                
                return sb.toString();
            }
            
            return "未知工具: " + functionName;
            
        } catch (Exception e) {
            System.err.println("[Agent Tool] 工具执行失败: " + e.getMessage());
            return "工具执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 从问题中提取主题关键词
     * 策略升级：优先调用 Python 引擎的 NLP 接口进行智能提取，失败则回退到本地规则匹配
     */
    private String extractTopic(String question) {
        if (StrUtil.isBlank(question)) {
            return "未知";
        }
        
        // 1. 尝试调用 Python NLP 接口
        try {
            JSONObject body = new JSONObject();
            body.set("text", question);
            
            // 短超时，避免阻塞太久 (300ms)
            String result = HttpRequest.post(NLP_TOPIC_URL)
                    .timeout(300)
                    .body(body.toString())
                    .execute()
                    .body();
            
            if (StrUtil.isNotEmpty(result)) {
                JSONObject json = JSONUtil.parseObj(result);
                String topic = json.getStr("topic");
                if (StrUtil.isNotBlank(topic)) {
                    System.out.println("NLP Topic Extracted: " + topic + " (from: " + question + ")");
                    return topic;
                }
            }
        } catch (Exception e) {
            // NLP 服务未启动或超时，降级到本地规则
            // System.err.println("NLP Topic extraction failed, fallback to local rules.");
        }
        
        // 2. 降级策略：本地规则匹配 (保留原有逻辑作为兜底)
        String[] keywords = {
            // 核心技术栈
            "Spring Boot", "Spring Security", "MyBatis", "Redis", "Vue", "Element Plus", "TypeScript", "Vite", "Pinia", "Axios",
            "Python", "FastAPI", "Chroma", "Watchdog", "Java", "Maven", "Gradle", "MySQL", "Logback",
            // AI 相关
            "RAG", "向量", "DeepSeek", "Gemini", "LLM", "SSE", "流式", "Embedding", "Token", "Context", "Prompt", "Agent",
            // 系统模块
            "权限", "菜单", "角色", "用户", "部门", "日志", "字典", "代码生成", "定时任务", "系统监控", "服务监控",
            // 开发运维
            "启动", "部署", "配置", "环境", "调试", "报错", "异常", "Bug", "接口", "API"
        };
        
        String lowerQuestion = question.toLowerCase();
        for (String keyword : keywords) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        
        return null; 
    }

    /**
     * 保存对话历史（Redis + 向量库 + MySQL）
     * @return 保存的 SysAiSession 对象
     */
    private SysAiSession saveConversationHistory(Long userId, String question, String answer, long startTime, String userIp, String modelName) {
        long now = System.currentTimeMillis();
        
        // 1. 存入 Redis 短期历史
        JSONObject userRecord = new JSONObject().set("role", "user").set("content", question);
        JSONObject aiRecord = new JSONObject().set("role", "assistant").set("content", answer);
        String key = HISTORY_KEY_PREFIX + userId;
        try {
            stringRedisTemplate.opsForList().rightPush(key, userRecord.toString());
            stringRedisTemplate.opsForList().rightPush(key, aiRecord.toString());
            if (stringRedisTemplate.opsForList().size(key) > 20) {
                stringRedisTemplate.opsForList().trim(key, -20, -1);
            }
            stringRedisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println("Redis save failed: " + e.getMessage());
        }

        // 2. 存入 Vector DB 长期记忆
        boolean vectorSyncSuccess = false;
        try {
            JSONObject memoryAdd = new JSONObject();
            memoryAdd.set("user_id", String.valueOf(userId));
            JSONArray memoryMessages = new JSONArray();
            memoryMessages.add(new JSONObject().set("content", question).set("role", "user").set("timestamp", now).set("sequence", now));
            memoryMessages.add(new JSONObject().set("content", answer).set("role", "assistant").set("timestamp", now + 1).set("sequence", now + 1));
            memoryAdd.set("messages", memoryMessages);
            System.out.println("[AI History] Syncing to Vector DB: " + MEMORY_ADD_URL);
            HttpResponse response = HttpRequest.post(MEMORY_ADD_URL).timeout(30000).body(memoryAdd.toString()).execute();
            vectorSyncSuccess = response.isOk();
            System.out.println("[AI History] Vector DB Sync result: " + vectorSyncSuccess + ", status: " + response.getStatus());
        } catch (Exception e) {
            System.err.println("Vector DB save failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        SysAiSession aiSession = new SysAiSession();
        // 3. 持久化到 MySQL
        try {
            long duration = System.currentTimeMillis() - startTime;
            aiSession.setUserId(userId);
            aiSession.setQuestion(question);
            aiSession.setAnswer(answer);
            aiSession.setModel(modelName);
            int tokens = (question.length() + answer.length()) * 2;
            aiSession.setTokens(tokens);
            aiSession.setDuration((int) duration);
            
            // 提取并保存主题
            String topic = extractTopic(question);
            if (topic != null) {
                aiSession.setTopic(topic);
            }
            
            aiSessionService.save(aiSession);
        } catch (Exception e) {
            System.err.println("Failed to save ai session: " + e.getMessage());
        }

        // 4. 如果向量同步成功，记录操作日志并推送到 SSE
        if (vectorSyncSuccess) {
            try {
                String logTitle = "AI Engine 检测到问题记忆持久化【" + (question.length() > 20 ? question.substring(0, 20) + "..." : question) + "】";
                
                SysOperLog operLog = new SysOperLog();
                operLog.setTitle(logTitle);
                operLog.setBusinessType(0); // 其他
                operLog.setMethod("VectorStore.memory_sync");
                operLog.setRequestMethod("POST");
                operLog.setOperName(String.valueOf(userId)); // 存入用户ID，供前端/Service 转换
                operLog.setOperUrl("/system/ai/memory/add");
                operLog.setOperIp(userIp);
                operLog.setStatus(0);
                operLog.setOperTime(java.time.LocalDateTime.now());
                
                operLogService.save(operLog);
                
                // 立即推送到 SSE 流 (AI 看板索引构建流)
                JSONObject sseMsg = new JSONObject();
                sseMsg.set("time", cn.hutool.core.date.DateUtil.format(new java.util.Date(), "HH:mm:ss"));
                sseMsg.set("msg", logTitle);
                sseMsg.set("color", "text-orange-400"); // 橙色圆点
                
                String jsonStr = sseMsg.toString();
                List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
                for (SseEmitter emitter : logEmitters) {
                    try {
                        emitter.send(SseEmitter.event().data(jsonStr));
                    } catch (Exception e) {
                        deadEmitters.add(emitter);
                    }
                }
                logEmitters.removeAll(deadEmitters);
            } catch (Exception e) {
                System.err.println("Failed to save oper log or push SSE: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return aiSession;
    }

    /**
     * 判断是否需要检索向量库（保留作为降级方案）
     */
    private boolean shouldQueryVector(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        String text = content.toLowerCase();
        String[] keywords = new String[]{
                "接口", "api", "controller", "service", "mapper", "sql", "数据库", "表", "字段", "报错", "异常", "堆栈",
                "代码", "类", "方法", "函数", "bug", "日志", "运行", "编译", "构建", "依赖", "配置", "yml",
                "刚才", "上次", "之前", "继续", "前面", "我们聊过", "你说过", "历史", "上下文"
        };
        return StrUtil.containsAny(text, keywords);
    }

    /**
     * 将 Redis 中的近期对话历史添加到消息列表中
     */
    private void addRecentHistory(JSONArray messages, Long userId) {
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            List<String> historyStr = stringRedisTemplate.opsForList().range(key, -10, -1);
            if (historyStr != null) {
                for (String str : historyStr) {
                    JSONObject historyItem = JSONUtil.parseObj(str);
                    JSONObject msg = new JSONObject();
                    msg.set("role", historyItem.getStr("role"));
                    msg.set("content", historyItem.getStr("content"));
                    messages.add(msg);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load recent history: " + e.getMessage());
        }
    }
}
