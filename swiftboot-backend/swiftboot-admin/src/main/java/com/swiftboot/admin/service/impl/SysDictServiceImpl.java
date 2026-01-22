package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysDict;
import com.swiftboot.admin.domain.entity.SysDictData;
import com.swiftboot.admin.mapper.SysDictDataMapper;
import com.swiftboot.admin.mapper.SysDictMapper;
import com.swiftboot.admin.service.SysDictService;
import com.swiftboot.common.core.constant.Constants;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import com.swiftboot.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    private final SysDictDataMapper dictDataMapper;
    private final RedisService redisService;

    @Override
    public Page<SysDict> selectDictPage(SysDict dict, PageQuery pageQuery) {
        Page<SysDict> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dict.getDictName() != null, SysDict::getDictName, dict.getDictName());
        wrapper.like(dict.getDictType() != null, SysDict::getDictType, dict.getDictType());
        wrapper.eq(dict.getStatus() != null, SysDict::getStatus, dict.getStatus());
        wrapper.orderByDesc(SysDict::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        // 先从缓存获取
        String cacheKey = Constants.DICT_KEY + dictType;
        List<SysDictData> dataList = redisService.get(cacheKey);
        if (dataList != null) {
            return dataList;
        }

        // 从数据库查询
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType);
        wrapper.eq(SysDictData::getStatus, 0);
        wrapper.orderByAsc(SysDictData::getSort);
        dataList = dictDataMapper.selectList(wrapper);

        // 存入缓存
        redisService.set(cacheKey, dataList);
        return dataList;
    }

    @Override
    public void insertDict(SysDict dict) {
        if (!checkDictTypeUnique(dict.getDictType(), null)) {
            throw new BusinessException(ResultCode.DICT_TYPE_EXISTS);
        }
        save(dict);
    }

    @Override
    public void updateDict(SysDict dict) {
        if (!checkDictTypeUnique(dict.getDictType(), dict.getId())) {
            throw new BusinessException(ResultCode.DICT_TYPE_EXISTS);
        }
        SysDict oldDict = getById(dict.getId());
        updateById(dict);

        // 如果字典类型变更，更新字典数据的类型
        if (!oldDict.getDictType().equals(dict.getDictType())) {
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictData::getDictType, oldDict.getDictType());
            List<SysDictData> dataList = dictDataMapper.selectList(wrapper);
            for (SysDictData data : dataList) {
                data.setDictType(dict.getDictType());
                dictDataMapper.updateById(data);
            }
            // 删除旧缓存
            redisService.delete(Constants.DICT_KEY + oldDict.getDictType());
        }
        // 刷新缓存
        redisService.delete(Constants.DICT_KEY + dict.getDictType());
    }

    @Override
    public void deleteDictByIds(List<Long> dictIds) {
        for (Long dictId : dictIds) {
            SysDict dict = getById(dictId);
            if (dict != null) {
                // 删除字典数据
                LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysDictData::getDictType, dict.getDictType());
                dictDataMapper.delete(wrapper);
                // 删除缓存
                redisService.delete(Constants.DICT_KEY + dict.getDictType());
            }
        }
        removeByIds(dictIds);
    }

    @Override
    public void refreshCache() {
        // 删除所有字典缓存
        redisService.delete(redisService.keys(Constants.DICT_KEY + "*"));
    }

    @Override
    public boolean checkDictTypeUnique(String dictType, Long dictId) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictType);
        if (dictId != null) {
            wrapper.ne(SysDict::getId, dictId);
        }
        return count(wrapper) == 0;
    }
}
