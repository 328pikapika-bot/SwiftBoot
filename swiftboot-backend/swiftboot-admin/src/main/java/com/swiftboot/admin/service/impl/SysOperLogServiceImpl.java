package com.swiftboot.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.mapper.SysOperLogMapper;
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

/**
 * 操作日志 Service 实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    private final ApplicationContext applicationContext;
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
        return page(page, wrapper);
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
