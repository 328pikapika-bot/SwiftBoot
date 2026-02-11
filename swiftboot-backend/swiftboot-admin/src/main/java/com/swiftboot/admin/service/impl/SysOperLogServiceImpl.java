package com.swiftboot.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.mapper.SysOperLogMapper;
import com.swiftboot.admin.mapper.SysUserMapper;
import com.swiftboot.admin.service.SysOperLogService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.log.annotation.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作日志 Service 实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    private final ApplicationContext applicationContext;
    private final SysUserMapper userMapper;
    private Set<String> cachedModuleNames;

    @Override
    public Page<SysOperLog> selectOperLogPage(SysOperLog operLog, PageQuery pageQuery) {
        Page<SysOperLog> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(operLog.getTitle() != null, SysOperLog::getTitle, operLog.getTitle());
        wrapper.eq(operLog.getBusinessType() != null, SysOperLog::getBusinessType, operLog.getBusinessType());
        wrapper.like(operLog.getOperName() != null, SysOperLog::getOperName, operLog.getOperName());
        wrapper.eq(operLog.getStatus() != null, SysOperLog::getStatus, operLog.getStatus());
        wrapper.orderByDesc(SysOperLog::getOperTime);
        
        Page<SysOperLog> result = page(page, wrapper);

        // 尝试将 operName (如果是ID) 转换为昵称
        if (CollUtil.isNotEmpty(result.getRecords())) {
            Set<Long> userIds = new HashSet<>();
            
            for (SysOperLog log : result.getRecords()) {
                // 如果是纯数字，认为是用户ID
                if (NumberUtil.isLong(log.getOperName())) {
                    userIds.add(Long.parseLong(log.getOperName()));
                }
            }
            
            if (CollUtil.isNotEmpty(userIds)) {
                List<SysUser> users = userMapper.selectBatchIds(userIds);
                Map<String, String> userMap = users.stream().collect(Collectors.toMap(u -> u.getId().toString(), SysUser::getNickname));
                
                for (SysOperLog logItem : result.getRecords()) {
                    if (userMap.containsKey(logItem.getOperName())) {
                        String nickname = userMap.get(logItem.getOperName());
                        if (StrUtil.isNotBlank(nickname)) {
                            logItem.setOperName(nickname);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void deleteOperLogByIds(List<Long> operLogIds) {
        removeByIds(operLogIds);
    }

    @Override
    public void cleanOperLog() {
        baseMapper.cleanOperLog();
    }

    @Override
    public void saveOperLog(SysOperLog operLog) {
        save(operLog);
    }

    @Override
    public List<String> selectOperLogModuleList() {
        Set<String> modules = new HashSet<>(baseMapper.selectOperLogModuleList());

        // 自动扫描 Controller 中的 @Log 注解获取模块名称（缓存以提升性能）
        if (cachedModuleNames == null) {
            cachedModuleNames = new HashSet<>();
            try {
                Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RestController.class);
                for (Object bean : beans.values()) {
                    Class<?> targetClass = AopUtils.getTargetClass(bean);
                    Method[] methods = targetClass.getMethods();
                    for (Method method : methods) {
                        Log log = method.getAnnotation(Log.class);
                        if (log != null && StrUtil.isNotBlank(log.title())) {
                            cachedModuleNames.add(log.title());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to scan @Log annotations", e);
            }
        }
        modules.addAll(cachedModuleNames);
        return new ArrayList<>(modules);
    }
}
