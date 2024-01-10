package com.jaryn.recorder.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jaryn
 */
@Component
@ConfigurationProperties(prefix = "fdu.postgraduate")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FduPostgraduateProperties {

    /**
     * 报考年份
     */
    private int year;

    /**
     * 总限流，1秒放行几个请求
     */
    private int permitsPerSecond;

    /**
     * ip限流，1分钟放行几个请求
     */
    private int ipPermitsPerMin;

    /**
     * 前端部署ip，跨域设置用
     */
    private String frontEndIp;

    /**
     * tessdata路径
     */
    private String tessdataPath;
}
