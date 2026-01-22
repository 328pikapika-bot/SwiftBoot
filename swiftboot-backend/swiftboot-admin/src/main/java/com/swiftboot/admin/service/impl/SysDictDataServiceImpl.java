package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysDictData;
import com.swiftboot.admin.mapper.SysDictDataMapper;
import com.swiftboot.admin.service.SysDictDataService;
import com.swiftboot.common.core.constant.Constants;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final RedisService redisService;

    @Override
    public Page<SysDictData> selectDictDataPage(SysDictData dictData, PageQuery pageQuery) {
        Page<SysDictData> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dictData.getDictType() != null, SysDictData::getDictType, dictData.getDictType());
        wrapper.like(dictData.getDictLabel() != null, SysDictData::getDictLabel, dictData.getDictLabel());
        wrapper.eq(dictData.getStatus() != null, SysDictData::getStatus, dictData.getStatus());
        wrapper.orderByAsc(SysDictData::getSort);
        return page(page, wrapper);
    }

    @Override
    public void insertDictData(SysDictData dictData) {
        save(dictData);
        refreshCache(dictData.getDictType());
    }

    @Override
    public void updateDictData(SysDictData dictData) {
        updateById(dictData);
        refreshCache(dictData.getDictType());
    }

    @Override
    public void deleteDictDataByIds(List<Long> dictDataIds) {
        for (Long id : dictDataIds) {
            SysDictData dictData = getById(id);
            if (dictData != null) {
                removeById(id);
                refreshCache(dictData.getDictType());
            }
        }
    }

    /**
     * 刷新缓存
     */
    private void refreshCache(String dictType) {
        redisService.delete(Constants.DICT_KEY + dictType);
    }
}
