package com.swiftboot.common.log.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.event.OperLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ApplicationEventPublisher eventPublisher;

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Before("@annotation(controllerLog)")
    public void before(JoinPoint joinPoint, Log controllerLog) {
        START_TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void afterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(pointcut = "@annotation(controllerLog)", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    private void handleLog(JoinPoint joinPoint, Log controllerLog, Exception e, Object jsonResult) {
        try {
            OperLogEvent operLog = new OperLogEvent();
            operLog.setStatus(0);
            operLog.setOperTime(LocalDateTime.now());

            // 计算耗时
            Long startTime = START_TIME.get();
            if (startTime != null) {
                operLog.setCostTime(System.currentTimeMillis() - startTime);
                START_TIME.remove();
            }

            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperIp(JakartaServletUtil.getClientIP(request));
            }

            // 处理注解信息
            operLog.setTitle(controllerLog.title());
            operLog.setBusinessType(controllerLog.businessType().ordinal());

            // 获取方法名
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            // 获取操作人
            try {
                if (StpUtil.isLogin()) {
                    operLog.setOperName(StpUtil.getLoginIdAsString());
                }
            } catch (Exception ignored) {
            }

            // 保存请求参数
            if (controllerLog.saveRequestData()) {
                String params = getRequestParams(joinPoint);
                operLog.setOperParam(StrUtil.sub(params, 0, 2000));
            }

            // 保存响应数据
            if (controllerLog.saveResponseData() && jsonResult != null) {
                operLog.setJsonResult(StrUtil.sub(JSONUtil.toJsonStr(jsonResult), 0, 2000));
            }

            // 异常处理
            if (e != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            }

            // 发布事件
            eventPublisher.publishEvent(operLog);
        } catch (Exception ex) {
            log.error("日志记录异常", ex);
        }
    }

    private String getRequestParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            // 过滤不需要序列化的对象
            if (arg instanceof HttpServletRequest ||
                    arg instanceof HttpServletResponse ||
                    arg instanceof MultipartFile ||
                    arg instanceof Collection<?> collection && collection.stream().anyMatch(item -> item instanceof MultipartFile)) {
                continue;
            }
            try {
                params.append(JSONUtil.toJsonStr(arg)).append(" ");
            } catch (Exception e) {
                params.append(arg.toString()).append(" ");
            }
        }
        return params.toString().trim();
    }
}
