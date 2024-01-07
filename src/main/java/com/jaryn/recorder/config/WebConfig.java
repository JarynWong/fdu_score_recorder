package com.jaryn.recorder.config;

import com.jaryn.recorder.interceptor.LoggingInterceptor;
import com.jaryn.recorder.interceptor.LimiterInterceptor;
import com.jaryn.recorder.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * @author: Jaryn
 * @date: 2024/1/1 2:36 下午
 * @description:
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private LimiterInterceptor rateLimiterInterceptor;

    @Autowired
    private UserInterceptor userInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(rateLimiterInterceptor);
        registry.addInterceptor(loggingInterceptor);

        registry.addInterceptor(userInterceptor);
    }

    /**
     * 跨源资源共享（CORS）的问题。当您尝试从一个源（在这个案例中是 http://127.0.0.1:4000）
     * 通过 JavaScript 发起 AJAX 请求访问另一个源（http://127.0.0.1:8080）的资源时，
     * 浏览器出于安全考虑会阻止这样的请求，除非目标源明确地通过 HTTP 头信息允许该请求。
     * @return
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:4000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
