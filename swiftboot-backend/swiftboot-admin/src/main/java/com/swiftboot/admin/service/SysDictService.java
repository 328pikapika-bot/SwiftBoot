package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysDict;
import com.swiftboot.admin.domain.entity.SysDictData;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 字典 Service
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 分页查询字典类型列表
     */
    Page<SysDict> selectDictPage(SysDict dict, PageQuery pageQuery);

    /**
     * 根据字典类型查询字典数据
     */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 新增字典类型
     */
    void insertDict(SysDict dict);

    /**
     * 修改字典类型
     */
    void updateDict(SysDict dict);

    /**
     * 删除字典类型
     */
    void deleteDictByIds(List<Long> dictIds);

    /**
     * 刷新字典缓存
     */
    void refreshCache();

    /**
     * 校验字典类型是否唯一
     */
    boolean checkDictTypeUnique(String dictType, Long dictId);
}
