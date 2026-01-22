package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysDict;
import com.swiftboot.admin.domain.entity.SysDictData;
import com.swiftboot.admin.service.SysDictDataService;
import com.swiftboot.admin.service.SysDictService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;
    private final SysDictDataService dictDataService;

    // ========== 字典类型 ==========

    @Operation(summary = "分页查询字典类型列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type/list")
    public R<PageResult<SysDict>> typeList(SysDict dict, PageQuery pageQuery) {
        Page<SysDict> page = dictService.selectDictPage(dict, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询字典类型详情")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/type/{dictId}")
    public R<SysDict> getTypeInfo(@PathVariable Long dictId) {
        SysDict dict = dictService.getById(dictId);
        return R.ok(dict);
    }

    @Operation(summary = "新增字典类型")
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/type")
    public R<Void> addType(@Valid @RequestBody SysDict dict) {
        dictService.insertDict(dict);
        return R.ok();
    }

    @Operation(summary = "修改字典类型")
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PutMapping("/type")
    public R<Void> editType(@Valid @RequestBody SysDict dict) {
        dictService.updateDict(dict);
        return R.ok();
    }

    @Operation(summary = "删除字典类型")
    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/type/{dictIds}")
    public R<Void> removeType(@PathVariable List<Long> dictIds) {
        dictService.deleteDictByIds(dictIds);
        return R.ok();
    }

    @Operation(summary = "刷新字典缓存")
    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/type/refreshCache")
    public R<Void> refreshCache() {
        dictService.refreshCache();
        return R.ok();
    }

    // ========== 字典数据 ==========

    @Operation(summary = "根据字典类型查询字典数据")
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dictDataByType(@PathVariable String dictType) {
        List<SysDictData> dataList = dictService.selectDictDataByType(dictType);
        return R.ok(dataList);
    }

    @Operation(summary = "分页查询字典数据列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/data/list")
    public R<PageResult<SysDictData>> dataList(SysDictData dictData, PageQuery pageQuery) {
        Page<SysDictData> page = dictDataService.selectDictDataPage(dictData, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询字典数据详情")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/data/{dictDataId}")
    public R<SysDictData> getDataInfo(@PathVariable Long dictDataId) {
        SysDictData dictData = dictDataService.getById(dictDataId);
        return R.ok(dictData);
    }

    @Operation(summary = "新增字典数据")
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public R<Void> addData(@Valid @RequestBody SysDictData dictData) {
        dictDataService.insertDictData(dictData);
        return R.ok();
    }

    @Operation(summary = "修改字典数据")
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping("/data")
    public R<Void> editData(@Valid @RequestBody SysDictData dictData) {
        dictDataService.updateDictData(dictData);
        return R.ok();
    }

    @Operation(summary = "删除字典数据")
    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{dictDataIds}")
    public R<Void> removeData(@PathVariable List<Long> dictDataIds) {
        dictDataService.deleteDictDataByIds(dictDataIds);
        return R.ok();
    }
}
