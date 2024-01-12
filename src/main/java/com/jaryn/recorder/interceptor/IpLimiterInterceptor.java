package com.jaryn.recorder.interceptor;

/**
 * @author: Jaryn
 * @date: 2024/1/1 2:36 下午
 * @description: ip限流器
 */
import com.google.common.cache.Cache;
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
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.jaryn.recorder.constants.Constant.Cache.IP_KEY;

@Component
public class IpLimiterInterceptor implements HandlerInterceptor {

    @Autowired
    private Cache<String, Object> cache;

    @Autowired
    private FduPostgraduateProperties fduPostgraduateProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            // 预检请求（OPTIONS请求），不走该拦截器，很重要！！！！
            return true;
        }
        String ipKey = IP_KEY.concat(request.getRemoteHost());
        ConcurrentLinkedDeque<Long> requestTimestampDeque = (ConcurrentLinkedDeque<Long>)cache.getIfPresent(ipKey);
        if (requestTimestampDeque == null) {
            requestTimestampDeque = new ConcurrentLinkedDeque<>();
            cache.put(ipKey, requestTimestampDeque);
        }

        long currentTimestamp = System.currentTimeMillis();

        // 清理1分钟之外的时间戳
        while (!requestTimestampDeque.isEmpty() && requestTimestampDeque.peek() <= currentTimestamp - 60000) {
            requestTimestampDeque.poll();
        }

        // 检查请求数量
        if (requestTimestampDeque.size() >= fduPostgraduateProperties.getIpPermitsPerMin()) {
            throw new ServiceException("操作频繁");
            // throw new ServiceException("IP限流中");
        }

        // 记录请求时间戳
        requestTimestampDeque.offer(currentTimestamp);
        return true;
    }

}

