package com.swiftboot.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.swiftboot.admin.domain.dto.SysAdminPreRuleConfigDTO;
import com.swiftboot.admin.domain.entity.SysAdminPreRuleConfig;
import com.swiftboot.admin.domain.vo.SysAdminPreRuleConfigVO;
import com.swiftboot.admin.mapper.SysAdminPreRuleConfigMapper;
import com.swiftboot.admin.service.SysAdminPreRuleConfigService;
import com.swiftboot.common.core.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * Administrator pre-check safety rule configuration service implementation.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SysAdminPreRuleConfigServiceImpl implements SysAdminPreRuleConfigService {

    private static final String ADMIN_PRE_RULE_KEY = "ai:admin:pre-rules";
    private static final String DEFAULT_CONFIG_KEY = "default";
    private static final int MAX_RULES = 10;
    private static final int MAX_RULE_LENGTH = 200;
    private static final String DEFAULT_INTERCEPTION_MESSAGE = "⚠️ 管理员安全前置规则已命中，当前问题不允许直接回答，请调整问题内容后重试。";
    private static final String REGEX_PREFIX = "regex:";
    private static final String REGEX_PREFIX_CN = "regex：";
    private static final String KEYWORD_PREFIX = "keyword:";
    private static final String KEYWORD_PREFIX_CN = "keyword：";
    private static final Pattern ANSWER_SUFFIX_PATTERN = Pattern.compile(
            "(?:每个|所有|每次)?(?:问题|回答|回复).{0,12}(?:结尾|最后|后面|末尾).{0,12}(?:添加|附上|追加|加上)\\s*[:：]?\\s*(.+)",
            Pattern.CASE_INSENSITIVE
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final SysAdminPreRuleConfigMapper adminPreRuleConfigMapper;

    private volatile SysAdminPreRuleConfigVO runtimeConfig;

    @PostConstruct
    public void init() {
        refreshRuntimeConfig();
    }

    @Override
    public SysAdminPreRuleConfigVO getConfig() {
        return copyConfig(runtimeConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(SysAdminPreRuleConfigDTO dto) {
        SysAdminPreRuleConfigVO normalized = normalize(copyConfig(dto, SysAdminPreRuleConfigVO.class));
        validateConfig(normalized);
        saveToDatabase(normalized);
        stringRedisTemplate.opsForValue().set(ADMIN_PRE_RULE_KEY, JSONUtil.toJsonStr(normalized));
        runtimeConfig = normalized;
    }

    @Override
    public String check(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        SysAdminPreRuleConfigVO config = runtimeConfig;
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || config.getRules() == null || config.getRules().isEmpty()) {
            return null;
        }
        String lowerContent = content.toLowerCase(Locale.ROOT);
        for (SysAdminPreRuleConfigDTO.RuleItem rule : config.getRules()) {
            if (!Boolean.TRUE.equals(rule.getEnabled())) {
                continue;
            }
            if (matchRuleOrInstruction(content, lowerContent, rule.getRuleContent())) {
                log.info("[AdminPreRule] blocked by rule={}, priority={}", rule.getRuleName(), rule.getPriority());
                return StrUtil.blankToDefault(config.getInterceptionMessage(), DEFAULT_INTERCEPTION_MESSAGE);
            }
        }
        return null;
    }

    @Override
    public String buildGovernancePrompt() {
        SysAdminPreRuleConfigVO config = runtimeConfig;
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || config.getRules() == null || config.getRules().isEmpty()) {
            return "";
        }
        List<String> instructions = new ArrayList<>();
        for (SysAdminPreRuleConfigDTO.RuleItem rule : config.getRules()) {
            if (!Boolean.TRUE.equals(rule.getEnabled())) {
                continue;
            }
            String instruction = normalizeRuleInstruction(rule.getRuleContent());
            if (StrUtil.isBlank(instruction) || isMatcherOnlyRule(rule.getRuleContent())) {
                continue;
            }
            instructions.add(rule.getRuleName() + "：" + instruction);
        }
        if (instructions.isEmpty()) {
            return "";
        }
        StringBuilder prompt = new StringBuilder("【管理员治理规则】\n");
        for (int i = 0; i < instructions.size(); i++) {
            prompt.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
        }
        prompt.append("以上规则为管理员运行时治理要求，回答时必须严格遵守。");
        return prompt.toString().trim();
    }

    @Override
    public String applyAnswerRules(String answer) {
        if (StrUtil.isBlank(answer)) {
            return answer;
        }
        SysAdminPreRuleConfigVO config = runtimeConfig;
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || config.getRules() == null || config.getRules().isEmpty()) {
            return answer;
        }
        String finalAnswer = answer.trim();
        for (SysAdminPreRuleConfigDTO.RuleItem rule : config.getRules()) {
            if (!Boolean.TRUE.equals(rule.getEnabled())) {
                continue;
            }
            String suffix = extractAnswerSuffix(normalizeRuleInstruction(rule.getRuleContent()));
            if (StrUtil.isBlank(suffix) || finalAnswer.contains(suffix)) {
                continue;
            }
            finalAnswer = finalAnswer + "\n\n" + suffix;
        }
        return finalAnswer;
    }

    private void refreshRuntimeConfig() {
        String configJson = stringRedisTemplate.opsForValue().get(ADMIN_PRE_RULE_KEY);
        if (StrUtil.isBlank(configJson)) {
            runtimeConfig = loadFromDatabase();
            return;
        }
        try {
            runtimeConfig = normalize(JSONUtil.toBean(configJson, SysAdminPreRuleConfigVO.class));
        } catch (Exception e) {
            log.warn("Failed to parse admin pre-rules config from Redis, fallback to empty config", e);
            runtimeConfig = loadFromDatabase();
        }
    }

    private SysAdminPreRuleConfigVO loadFromDatabase() {
        SysAdminPreRuleConfig entity = adminPreRuleConfigMapper.selectOne(new LambdaQueryWrapper<SysAdminPreRuleConfig>()
                .eq(SysAdminPreRuleConfig::getConfigKey, DEFAULT_CONFIG_KEY)
                .last("limit 1"));
        if (entity == null) {
            SysAdminPreRuleConfigVO emptyConfig = normalize(new SysAdminPreRuleConfigVO());
            stringRedisTemplate.delete(ADMIN_PRE_RULE_KEY);
            return emptyConfig;
        }
        SysAdminPreRuleConfigVO config = fromEntity(entity);
        stringRedisTemplate.opsForValue().set(ADMIN_PRE_RULE_KEY, JSONUtil.toJsonStr(config));
        return config;
    }

    private void saveToDatabase(SysAdminPreRuleConfigVO config) {
        SysAdminPreRuleConfig entity = adminPreRuleConfigMapper.selectOne(new LambdaQueryWrapper<SysAdminPreRuleConfig>()
                .eq(SysAdminPreRuleConfig::getConfigKey, DEFAULT_CONFIG_KEY)
                .last("limit 1"));
        SysAdminPreRuleConfig target = entity == null ? new SysAdminPreRuleConfig() : entity;
        target.setConfigKey(DEFAULT_CONFIG_KEY);
        target.setEnabled(config.getEnabled());
        target.setInterceptionMessage(config.getInterceptionMessage());
        target.setRulesJson(JSONUtil.toJsonStr(config.getRules()));
        target.setRemark("管理员安全前置规则");
        if (entity == null) {
            adminPreRuleConfigMapper.insert(target);
        } else {
            adminPreRuleConfigMapper.updateById(target);
        }
    }

    private SysAdminPreRuleConfigVO fromEntity(SysAdminPreRuleConfig entity) {
        SysAdminPreRuleConfigVO config = new SysAdminPreRuleConfigVO();
        config.setEnabled(entity.getEnabled());
        config.setInterceptionMessage(entity.getInterceptionMessage());
        if (StrUtil.isNotBlank(entity.getRulesJson())) {
            config.setRules(JSONUtil.toList(entity.getRulesJson(), SysAdminPreRuleConfigDTO.RuleItem.class));
        }
        return normalize(config);
    }

    private void validateConfig(SysAdminPreRuleConfigVO config) {
        List<SysAdminPreRuleConfigDTO.RuleItem> rules = config.getRules();
        if (rules.size() > MAX_RULES) {
            throw new BusinessException("At most " + MAX_RULES + " administrator safety rules are allowed");
        }
        for (SysAdminPreRuleConfigDTO.RuleItem rule : rules) {
            if (StrUtil.isBlank(rule.getRuleName())) {
                throw new BusinessException("Rule name cannot be empty");
            }
            if (StrUtil.isBlank(rule.getRuleContent())) {
                throw new BusinessException("Rule content cannot be empty");
            }
            if (rule.getRuleContent().length() > MAX_RULE_LENGTH) {
                throw new BusinessException("Rule content length cannot exceed " + MAX_RULE_LENGTH);
            }
            verifyRegexLines(rule.getRuleContent(), rule.getRuleName());
        }
    }

    private void verifyRegexLines(String content, String ruleName) {
        for (String rawLine : StrUtil.splitTrim(content, '\n')) {
            if (isRegexLine(rawLine)) {
                String regex = stripRegexPrefix(rawLine);
                if (StrUtil.isBlank(regex)) {
                    throw new BusinessException("Rule [" + ruleName + "] contains an empty regex expression");
                }
                try {
                    Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                } catch (PatternSyntaxException ex) {
                    throw new BusinessException("Rule [" + ruleName + "] regex is invalid: " + ex.getDescription());
                }
            }
        }
    }

    private boolean matchRule(String content, String lowerContent, String ruleContent) {
        for (String rawLine : StrUtil.splitTrim(ruleContent, '\n')) {
            if (StrUtil.isBlank(rawLine)) {
                continue;
            }
            if (isRegexLine(rawLine)) {
                String regex = stripRegexPrefix(rawLine);
                try {
                    if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(content).find()) {
                        return true;
                    }
                } catch (PatternSyntaxException ex) {
                    log.warn("Skip invalid regex rule line: {}", rawLine, ex);
                }
            } else {
                String keyword = stripKeywordPrefix(rawLine);
                if (StrUtil.isNotBlank(keyword) && lowerContent.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRegexLine(String line) {
        return StrUtil.startWithIgnoreCase(line, REGEX_PREFIX) || StrUtil.startWithIgnoreCase(line, REGEX_PREFIX_CN);
    }

    private String stripRegexPrefix(String line) {
        String normalized = StrUtil.trim(line);
        if (StrUtil.startWithIgnoreCase(normalized, REGEX_PREFIX_CN)) {
            return StrUtil.trim(StrUtil.removePrefixIgnoreCase(normalized, REGEX_PREFIX_CN));
        }
        return StrUtil.trim(StrUtil.removePrefixIgnoreCase(normalized, REGEX_PREFIX));
    }

    private String stripKeywordPrefix(String line) {
        String normalized = StrUtil.trim(line);
        if (StrUtil.startWithIgnoreCase(normalized, KEYWORD_PREFIX_CN)) {
            return StrUtil.trim(StrUtil.removePrefixIgnoreCase(normalized, KEYWORD_PREFIX_CN));
        }
        if (StrUtil.startWithIgnoreCase(normalized, KEYWORD_PREFIX)) {
            return StrUtil.trim(StrUtil.removePrefixIgnoreCase(normalized, KEYWORD_PREFIX));
        }
        return normalized;
    }

    private String normalizeRuleInstruction(String ruleContent) {
        return StrUtil.splitTrim(ruleContent, '\n').stream()
                .filter(StrUtil::isNotBlank)
                .map(line -> {
                    if (isRegexLine(line)) {
                        return stripRegexPrefix(line);
                    }
                    return stripKeywordPrefix(line);
                })
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining("；"));
    }

    private boolean isMatcherOnlyRule(String ruleContent) {
        List<String> lines = StrUtil.splitTrim(ruleContent, '\n');
        if (lines.isEmpty()) {
            return true;
        }
        return lines.stream().allMatch(line -> isRegexLine(line) && looksLikeRegexExpression(stripRegexPrefix(line)));
    }

    private boolean looksLikeRegexExpression(String text) {
        return StrUtil.containsAny(text, ".*", ".+", "|", "\\d", "\\s", "\\w", "[", "]", "(", ")", "^", "$");
    }

    private String extractAnswerSuffix(String instruction) {
        if (StrUtil.isBlank(instruction)) {
            return null;
        }
        Matcher matcher = ANSWER_SUFFIX_PATTERN.matcher(instruction);
        if (!matcher.find()) {
            return null;
        }
        String suffix = StrUtil.trim(matcher.group(1));
        if (StrUtil.isBlank(suffix)) {
            return null;
        }
        if (suffix.endsWith("。") || suffix.endsWith("；") || suffix.endsWith(";")) {
            suffix = suffix.substring(0, suffix.length() - 1).trim();
        }
        return suffix;
    }

    private String normalizeRuleLineForStorage(String line) {
        String trimmed = StrUtil.trim(line);
        if (StrUtil.isBlank(trimmed)) {
            return null;
        }
        if (StrUtil.startWithIgnoreCase(trimmed, REGEX_PREFIX_CN)) {
            return REGEX_PREFIX + stripRegexPrefix(trimmed);
        }
        if (StrUtil.startWithIgnoreCase(trimmed, KEYWORD_PREFIX_CN)) {
            return KEYWORD_PREFIX + stripKeywordPrefix(trimmed);
        }
        return trimmed;
    }

    private List<String> normalizeRuleLines(String ruleContent) {
        return StrUtil.splitTrim(ruleContent, '\n').stream()
                .map(this::normalizeRuleLineForStorage)
                .filter(StrUtil::isNotBlank)
                .toList();
    }

    private boolean canBeGovernanceInstruction(String ruleContent) {
        return StrUtil.isNotBlank(normalizeRuleInstruction(ruleContent)) && !isMatcherOnlyRule(ruleContent);
    }

    private boolean shouldTreatAsInstructionOnly(String ruleContent) {
        return canBeGovernanceInstruction(ruleContent)
                && !StrUtil.splitTrim(ruleContent, '\n').stream().anyMatch(line -> isRegexLine(line) || StrUtil.startWithIgnoreCase(line, KEYWORD_PREFIX) || StrUtil.startWithIgnoreCase(line, KEYWORD_PREFIX_CN));
    }

    private boolean matchRuleOrInstruction(String content, String lowerContent, String ruleContent) {
        if (shouldTreatAsInstructionOnly(ruleContent)) {
            return false;
        }
        return matchRule(content, lowerContent, ruleContent);
    }

    private SysAdminPreRuleConfigVO normalize(SysAdminPreRuleConfigVO config) {
        if (config.getEnabled() == null) {
            config.setEnabled(Boolean.TRUE);
        }
        if (config.getRules() == null) {
            config.setRules(new ArrayList<>());
        }
        if (StrUtil.isBlank(config.getInterceptionMessage())) {
            config.setInterceptionMessage(DEFAULT_INTERCEPTION_MESSAGE);
        }
        List<SysAdminPreRuleConfigDTO.RuleItem> normalizedRules = new ArrayList<>();
        for (SysAdminPreRuleConfigDTO.RuleItem rule : config.getRules()) {
            if (rule == null) {
                continue;
            }
            if (StrUtil.isBlank(rule.getId())) {
                rule.setId(IdUtil.fastSimpleUUID());
            }
            if (rule.getEnabled() == null) {
                rule.setEnabled(Boolean.TRUE);
            }
            if (rule.getPriority() == null) {
                rule.setPriority(100);
            }
            rule.setRuleName(StrUtil.trim(rule.getRuleName()));
            rule.setRuleContent(String.join("\n", normalizeRuleLines(rule.getRuleContent())));
            normalizedRules.add(rule);
        }
        normalizedRules.sort(Comparator.comparing(SysAdminPreRuleConfigDTO.RuleItem::getPriority, Comparator.nullsLast(Integer::compareTo)).reversed());
        config.setRules(normalizedRules);
        config.setMaxRules(MAX_RULES);
        config.setMaxRuleLength(MAX_RULE_LENGTH);
        return config;
    }

    private SysAdminPreRuleConfigVO copyConfig(SysAdminPreRuleConfigVO config) {
        return copyConfig(config, SysAdminPreRuleConfigVO.class);
    }

    private <T> T copyConfig(Object source, Class<T> type) {
        return JSONUtil.toBean(JSONUtil.toJsonStr(source), type);
    }
}
