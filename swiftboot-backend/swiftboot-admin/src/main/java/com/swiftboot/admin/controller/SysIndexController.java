package com.swiftboot.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swiftboot.admin.mapper.SysDeptMapper;
import com.swiftboot.admin.mapper.SysMenuMapper;
import com.swiftboot.admin.mapper.SysRoleMapper;
import com.swiftboot.admin.mapper.SysUserMapper;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@Tag(name = "首页管理")
@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
public class SysIndexController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDeptMapper deptMapper;

    @Operation(summary = "获取首页统计信息")
    @GetMapping("/stats")
    public R<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(new QueryWrapper<>()));
        stats.put("roleCount", roleMapper.selectCount(new QueryWrapper<>()));
        // 仅统计菜单类型为 C (菜单) 的数量，排除目录 (M) 和按钮 (F)
        stats.put("menuCount", menuMapper.selectCount(new QueryWrapper<com.swiftboot.admin.domain.entity.SysMenu>().eq("menu_type", "C")));
        stats.put("deptCount", deptMapper.selectCount(new QueryWrapper<>()));
        return R.ok(stats);
    }
}
