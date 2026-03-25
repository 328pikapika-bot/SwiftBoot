package com.swiftboot.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swiftboot.admin.domain.dto.SysAiBlockCategoryDTO;
import com.swiftboot.admin.domain.dto.SysAiBlockWordBatchDTO;
import com.swiftboot.admin.domain.dto.SysAiBlockWordDTO;
import com.swiftboot.admin.domain.entity.SysAiBlockCategory;
import com.swiftboot.admin.domain.entity.SysAiBlockHitLog;
import com.swiftboot.admin.domain.entity.SysDictData;
import com.swiftboot.admin.domain.entity.SysAiBlockWord;
import com.swiftboot.admin.domain.vo.SysAiBlockCategoryVO;
import com.swiftboot.admin.domain.vo.SysAiBlockOverviewVO;
import com.swiftboot.admin.domain.vo.SysAiBlockWordVO;
import com.swiftboot.admin.mapper.SysAiBlockCategoryMapper;
import com.swiftboot.admin.mapper.SysAiBlockHitLogMapper;
import com.swiftboot.admin.mapper.SysAiBlockWordMapper;
import com.swiftboot.admin.mapper.SysDictDataMapper;
import com.swiftboot.admin.service.SysAiBlockWordService;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.security.domain.LoginUser;
import com.swiftboot.common.security.utils.SecurityUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI block word service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysAiBlockWordServiceImpl implements SysAiBlockWordService {

    private static final String BLOCK_WORD_CACHE_KEY = "ai:block:words";
    private static final String DEFAULT_BLOCK_MESSAGE = "当前问题包含受限关键词，无法回答，请调整提问内容后重试。";
    private static final String BLOCK_CATEGORY_DICT_TYPE = "ai_block_category";

    private final SysAiBlockCategoryMapper categoryMapper;
    private final SysAiBlockWordMapper wordMapper;
    private final SysAiBlockHitLogMapper blockHitLogMapper;
    private final SysDictDataMapper dictDataMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private volatile CachedBlockWordData cachedData = new CachedBlockWordData();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public SysAiBlockOverviewVO getOverview() {
        List<SysAiBlockCategory> categories = listAllCategories();
        List<SysAiBlockWord> words = listAllWords();

        Map<Long, Long> countMap = words.stream()
                .collect(Collectors.groupingBy(SysAiBlockWord::getCategoryId, Collectors.counting()));
        Map<Long, List<String>> previewMap = words.stream()
                .collect(Collectors.groupingBy(
                        SysAiBlockWord::getCategoryId,
                        Collectors.mapping(SysAiBlockWord::getWordText, Collectors.toList())
                ));

        SysAiBlockOverviewVO overview = new SysAiBlockOverviewVO();
        overview.setTotalCategoryCount(categories.size());
        overview.setEnabledCategoryCount((int) categories.stream().filter(category -> isEnabled(category.getStatus())).count());
        overview.setTotalWordCount(words.size());
        overview.setEnabledWordCount((int) words.stream().filter(word -> isEnabled(word.getStatus())).count());
        overview.setCategories(categories.stream().map(category -> toCategoryVO(category, countMap, previewMap)).toList());

        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(SysAiBlockCategory::getId, SysAiBlockCategory::getCategoryName, (left, right) -> left));
        overview.setWords(words.stream().map(word -> toWordVO(word, categoryNameMap)).toList());
        return overview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(SysAiBlockCategoryDTO dto) {
        validateCategory(dto);
        SysAiBlockCategory entity = dto.getId() == null ? new SysAiBlockCategory() : categoryMapper.selectById(dto.getId());
        if (dto.getId() != null && entity == null) {
            throw new BusinessException("屏蔽词分类不存在");
        }
        DictBinding dictBinding = resolveDictBinding(dto);
        entity.setDictDataId(dictBinding.dictDataId());
        entity.setCategoryName(dictBinding.categoryName());
        entity.setCategoryCode(dictBinding.categoryCode());
        entity.setStatus(dto.getStatus());
        entity.setSort(dto.getSort());
        entity.setRemark(StrUtil.trim(dto.getRemark()));
        if (dto.getId() == null) {
            categoryMapper.insert(entity);
        } else {
            categoryMapper.updateById(entity);
        }
        refreshCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("分类ID不能为空");
        }
        long wordCount = wordMapper.selectCount(new LambdaQueryWrapper<SysAiBlockWord>()
                .eq(SysAiBlockWord::getCategoryId, categoryId));
        if (wordCount > 0) {
            throw new BusinessException("该分类下仍有屏蔽词，请先清空词条后再删除");
        }
        categoryMapper.deleteById(categoryId);
        refreshCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWord(SysAiBlockWordDTO dto) {
        validateWord(dto);
        SysAiBlockWord entity = dto.getId() == null ? new SysAiBlockWord() : wordMapper.selectById(dto.getId());
        if (dto.getId() != null && entity == null) {
            throw new BusinessException("屏蔽词不存在");
        }
        entity.setCategoryId(dto.getCategoryId());
        entity.setWordText(StrUtil.trim(dto.getWordText()));
        entity.setMatchType("contains");
        entity.setStatus(dto.getStatus());
        entity.setSort(dto.getSort());
        entity.setRemark(StrUtil.trim(dto.getRemark()));
        if (dto.getId() == null) {
            wordMapper.insert(entity);
        } else {
            wordMapper.updateById(entity);
        }
        refreshCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveWords(SysAiBlockWordBatchDTO dto) {
        if (dto.getCategoryId() == null) {
            throw new BusinessException("请选择分类");
        }
        SysAiBlockCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("所选分类不存在");
        }

        List<String> incomingWords = splitWordLines(dto.getWordLines());
        if (incomingWords.isEmpty()) {
            throw new BusinessException("请至少输入一个屏蔽词");
        }

        Set<String> existingWords = wordMapper.selectList(new LambdaQueryWrapper<SysAiBlockWord>()
                        .eq(SysAiBlockWord::getCategoryId, dto.getCategoryId()))
                .stream()
                .map(SysAiBlockWord::getWordText)
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());

        int nextSort = wordMapper.selectList(new LambdaQueryWrapper<SysAiBlockWord>()
                        .eq(SysAiBlockWord::getCategoryId, dto.getCategoryId())
                        .orderByDesc(SysAiBlockWord::getSort)
                        .last("limit 1"))
                .stream()
                .findFirst()
                .map(word -> word.getSort() == null ? 100 : word.getSort())
                .orElse(100);

        int inserted = 0;
        for (String wordText : incomingWords) {
            if (existingWords.contains(wordText)) {
                continue;
            }
            SysAiBlockWord entity = new SysAiBlockWord();
            entity.setCategoryId(dto.getCategoryId());
            entity.setWordText(wordText);
            entity.setMatchType("contains");
            entity.setStatus(0);
            nextSort += 10;
            entity.setSort(nextSort);
            entity.setRemark(StrUtil.trim(dto.getRemark()));
            wordMapper.insert(entity);
            existingWords.add(wordText);
            inserted++;
        }

        if (inserted == 0) {
            throw new BusinessException("输入的屏蔽词均已存在，无需重复添加");
        }
        refreshCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWord(Long wordId) {
        if (wordId == null) {
            throw new BusinessException("屏蔽词ID不能为空");
        }
        wordMapper.deleteById(wordId);
        refreshCache();
    }

    @Override
    public String checkBlocked(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        CachedBlockWordData data = cachedData;
        if (data.words().isEmpty()) {
            return null;
        }
        String normalized = StrUtil.trim(content);
        String lower = normalized.toLowerCase(Locale.ROOT);
        CachedBlockWord hitWord = matchBlockedWord(data.root(), lower);
        if (hitWord != null) {
            saveHitLog(hitWord, normalized);
            log.info("[BlockWord] blocked by category={}, word={}", hitWord.categoryName(), hitWord.wordText());
            return DEFAULT_BLOCK_MESSAGE;
        }
        return null;
    }

    private void validateCategory(SysAiBlockCategoryDTO dto) {
        String name = StrUtil.trim(dto.getCategoryName());
        String code = StrUtil.trim(dto.getCategoryCode()).toLowerCase(Locale.ROOT);
        if (dto.getDictDataId() == null) {
            if (StrUtil.isBlank(name)) {
                throw new BusinessException("分类名称不能为空");
            }
            if (StrUtil.isBlank(code)) {
                throw new BusinessException("分类编码不能为空");
            }
        } else {
            SysDictData dictData = dictDataMapper.selectById(dto.getDictDataId());
            if (dictData == null || dictData.getDeleted() == 1 || dictData.getStatus() != 0 || !BLOCK_CATEGORY_DICT_TYPE.equals(dictData.getDictType())) {
                throw new BusinessException("所选字典项不可用");
            }
            name = StrUtil.trim(dictData.getDictLabel());
            code = StrUtil.trim(dictData.getDictValue()).toLowerCase(Locale.ROOT);
        }
        long sameCodeCount = categoryMapper.selectCount(new LambdaQueryWrapper<SysAiBlockCategory>()
                .eq(SysAiBlockCategory::getCategoryCode, code)
                .ne(dto.getId() != null, SysAiBlockCategory::getId, dto.getId()));
        if (sameCodeCount > 0) {
            throw new BusinessException("分类编码已存在");
        }
    }

    private void validateWord(SysAiBlockWordDTO dto) {
        if (dto.getCategoryId() == null) {
            throw new BusinessException("请选择分类");
        }
        SysAiBlockCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("所选分类不存在");
        }
        String wordText = StrUtil.trim(dto.getWordText());
        if (StrUtil.isBlank(wordText)) {
            throw new BusinessException("屏蔽词不能为空");
        }
        long sameWordCount = wordMapper.selectCount(new LambdaQueryWrapper<SysAiBlockWord>()
                .eq(SysAiBlockWord::getCategoryId, dto.getCategoryId())
                .eq(SysAiBlockWord::getWordText, wordText)
                .ne(dto.getId() != null, SysAiBlockWord::getId, dto.getId()));
        if (sameWordCount > 0) {
            throw new BusinessException("同一分类下该屏蔽词已存在");
        }
    }

    private void refreshCache() {
        List<SysAiBlockCategory> enabledCategories = categoryMapper.selectList(new LambdaQueryWrapper<SysAiBlockCategory>()
                .eq(SysAiBlockCategory::getStatus, 0)
                .orderByAsc(SysAiBlockCategory::getSort)
                .orderByAsc(SysAiBlockCategory::getId));
        if (enabledCategories.isEmpty()) {
            cachedData = new CachedBlockWordData();
            stringRedisTemplate.delete(BLOCK_WORD_CACHE_KEY);
            return;
        }
        Set<Long> enabledCategoryIds = enabledCategories.stream().map(SysAiBlockCategory::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = enabledCategories.stream()
                .collect(Collectors.toMap(SysAiBlockCategory::getId, SysAiBlockCategory::getCategoryName, (left, right) -> left));
        List<SysAiBlockWord> enabledWords = wordMapper.selectList(new LambdaQueryWrapper<SysAiBlockWord>()
                .eq(SysAiBlockWord::getStatus, 0)
                .in(SysAiBlockWord::getCategoryId, enabledCategoryIds)
                .orderByAsc(SysAiBlockWord::getSort)
                .orderByAsc(SysAiBlockWord::getId));

        List<CachedBlockWord> cacheWords = enabledWords.stream()
                .map(word -> new CachedBlockWord(
                        word.getCategoryId(),
                        categoryNameMap.getOrDefault(word.getCategoryId(), ""),
                        StrUtil.trim(word.getWordText()),
                        StrUtil.blankToDefault(word.getMatchType(), "contains"),
                        StrUtil.trim(word.getWordText()).toLowerCase(Locale.ROOT)
                ))
                .filter(word -> StrUtil.isNotBlank(word.wordText()))
                .toList();

        cachedData = new CachedBlockWordData(cacheWords, buildMatcher(cacheWords));
        stringRedisTemplate.opsForValue().set(BLOCK_WORD_CACHE_KEY, JSONUtil.toJsonStr(cacheWords));
    }

    private List<SysAiBlockCategory> listAllCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<SysAiBlockCategory>()
                .orderByAsc(SysAiBlockCategory::getSort)
                .orderByAsc(SysAiBlockCategory::getId));
    }

    private List<SysAiBlockWord> listAllWords() {
        return wordMapper.selectList(new LambdaQueryWrapper<SysAiBlockWord>()
                .orderByAsc(SysAiBlockWord::getSort)
                .orderByAsc(SysAiBlockWord::getId));
    }

    private SysAiBlockCategoryVO toCategoryVO(SysAiBlockCategory entity, Map<Long, Long> countMap, Map<Long, List<String>> previewMap) {
        SysAiBlockCategoryVO vo = new SysAiBlockCategoryVO();
        vo.setId(entity.getId());
        vo.setDictDataId(entity.getDictDataId());
        vo.setCategoryName(entity.getCategoryName());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());
        vo.setWordCount(countMap.getOrDefault(entity.getId(), 0L).intValue());
        vo.setPreviewWords(previewMap.getOrDefault(entity.getId(), List.of()).stream().limit(3).toList());
        return vo;
    }

    private SysAiBlockWordVO toWordVO(SysAiBlockWord entity, Map<Long, String> categoryNameMap) {
        SysAiBlockWordVO vo = new SysAiBlockWordVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setCategoryName(categoryNameMap.getOrDefault(entity.getCategoryId(), ""));
        vo.setWordText(entity.getWordText());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private boolean isEnabled(Integer status) {
        return status != null && status == 0;
    }

    private List<String> splitWordLines(String wordLines) {
        return new ArrayList<>(new LinkedHashSet<>(StrUtil.splitTrim(StrUtil.blankToDefault(wordLines, ""), '\n').stream()
                .map(StrUtil::trim)
                .filter(StrUtil::isNotBlank)
                .toList()));
    }

    private void saveHitLog(CachedBlockWord word, String questionContent) {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            SysAiBlockHitLog logEntity = new SysAiBlockHitLog();
            if (loginUser != null) {
                logEntity.setUserId(loginUser.getUserId());
                logEntity.setUsername(loginUser.getUsername());
                logEntity.setNickname(loginUser.getNickname());
                logEntity.setLoginIp(loginUser.getLoginIp());
            }
            logEntity.setCategoryId(word.categoryId());
            logEntity.setCategoryName(word.categoryName());
            logEntity.setWordText(word.wordText());
            logEntity.setQuestionContent(StrUtil.maxLength(questionContent, 500));
            logEntity.setRemark("屏蔽词命中日志");
            blockHitLogMapper.insert(logEntity);
        } catch (Exception ex) {
            log.warn("Failed to save block hit log", ex);
        }
    }

    private DictBinding resolveDictBinding(SysAiBlockCategoryDTO dto) {
        if (dto.getDictDataId() == null) {
            return new DictBinding(
                    null,
                    StrUtil.trim(dto.getCategoryName()),
                    StrUtil.trim(dto.getCategoryCode()).toLowerCase(Locale.ROOT)
            );
        }
        SysDictData dictData = dictDataMapper.selectById(dto.getDictDataId());
        if (dictData == null || dictData.getDeleted() == 1 || dictData.getStatus() != 0 || !BLOCK_CATEGORY_DICT_TYPE.equals(dictData.getDictType())) {
            throw new BusinessException("所选字典项不可用");
        }
        return new DictBinding(
                dictData.getId(),
                StrUtil.trim(dictData.getDictLabel()),
                StrUtil.trim(dictData.getDictValue()).toLowerCase(Locale.ROOT)
        );
    }

    private CachedBlockWord matchBlockedWord(MatchNode root, String lowerContent) {
        if (root == null || StrUtil.isBlank(lowerContent)) {
            return null;
        }
        MatchNode node = root;
        for (int i = 0; i < lowerContent.length(); i++) {
            char current = lowerContent.charAt(i);
            while (node != root && !node.children.containsKey(current)) {
                node = node.fail;
            }
            node = node.children.getOrDefault(current, root);
            if (!node.outputs.isEmpty()) {
                return node.outputs.get(0);
            }
        }
        return null;
    }

    private MatchNode buildMatcher(List<CachedBlockWord> words) {
        MatchNode root = new MatchNode();
        for (CachedBlockWord word : words) {
            MatchNode current = root;
            for (int i = 0; i < word.normalizedWord().length(); i++) {
                char ch = word.normalizedWord().charAt(i);
                current = current.children.computeIfAbsent(ch, key -> new MatchNode());
            }
            current.outputs.add(word);
        }

        ArrayList<MatchNode> queue = new ArrayList<>();
        root.fail = root;
        root.children.values().forEach(child -> {
            child.fail = root;
            queue.add(child);
        });

        for (int idx = 0; idx < queue.size(); idx++) {
            MatchNode current = queue.get(idx);
            for (Map.Entry<Character, MatchNode> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                MatchNode child = entry.getValue();
                MatchNode failNode = current.fail;
                while (failNode != root && !failNode.children.containsKey(ch)) {
                    failNode = failNode.fail;
                }
                if (failNode.children.containsKey(ch) && failNode.children.get(ch) != child) {
                    child.fail = failNode.children.get(ch);
                } else {
                    child.fail = root;
                }
                if (!child.fail.outputs.isEmpty()) {
                    child.outputs.addAll(child.fail.outputs);
                }
                queue.add(child);
            }
        }
        return root;
    }

    private record CachedBlockWord(Long categoryId, String categoryName, String wordText, String matchType, String normalizedWord) {
    }

    private static final class MatchNode {
        private final Map<Character, MatchNode> children = new HashMap<>();
        private final List<CachedBlockWord> outputs = new ArrayList<>();
        private MatchNode fail;
    }

    private record CachedBlockWordData(List<CachedBlockWord> words, MatchNode root) {
        private CachedBlockWordData() {
            this(new ArrayList<>(), new MatchNode());
        }
    }

    private record DictBinding(Long dictDataId, String categoryName, String categoryCode) {
    }
}
