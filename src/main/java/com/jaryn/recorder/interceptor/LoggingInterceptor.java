package com.jaryn.recorder.interceptor;

/**
 * @author: Jaryn
 * @date: 2024/1/1 2:36 下午
 * @description: 日志拦截器
 */
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 在请求开始时生成 trace_id
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.info("request path：{}", request.getServletPath());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束时清除 trace_id
        MDC.clear();
    }
}

