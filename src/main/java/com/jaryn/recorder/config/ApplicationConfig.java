package com.jaryn.recorder.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class ApplicationConfig {

    @Bean
    public MapperFacade mapperFacadeBean() {
        return new DefaultMapperFactory.Builder().build().getMapperFacade();
    }

    @Bean
    public Cache<String, Object> cacheBean() {
        // 创建一个缓存实例，最多100个元素，写入 1 小时后过期
        Cache<String, Object> cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.DAYS)
                .build();
        return cache;
    }
}
