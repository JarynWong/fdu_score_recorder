package com.jaryn.recorder.interceptor;

/**
 * @author: Jaryn
 * @date: 2024/1/1 2:36 下午
 * @description: 日志拦截器
 */
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.exception.ServiceException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class LimiterInterceptor implements HandlerInterceptor, InitializingBean {

    @Autowired
    private FduPostgraduateProperties fduPostgraduateProperties;

    /**
     * 限流桶
     */
    private Bucket bucket;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            // 预检请求（OPTIONS请求），不走该拦截器，很重要！！！！
            return true;
        }
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            throw new ServiceException("并发限流中，请稍后再试");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 定义带宽：每秒生成1个令牌，桶的容量为getPermitsPerSecond()
        Bandwidth limit = Bandwidth.classic(fduPostgraduateProperties.getPermitsPerSecond(), Refill.greedy(1, Duration.ofSeconds(1)));
        bucket = Bucket4j.builder().addLimit(limit).build();
    }
}

