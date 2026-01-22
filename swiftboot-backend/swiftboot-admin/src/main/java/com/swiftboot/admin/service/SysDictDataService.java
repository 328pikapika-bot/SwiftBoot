package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysDictData;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 字典数据 Service
 */
public interface SysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据列表
     */
    Page<SysDictData> selectDictDataPage(SysDictData dictData, PageQuery pageQuery);

    /**
     * 新增字典数据
     */
    void insertDictData(SysDictData dictData);

    /**
     * 修改字典数据
     */
    void updateDictData(SysDictData dictData);

    /**
     * 删除字典数据
     */
    void deleteDictDataByIds(List<Long> dictDataIds);
}
