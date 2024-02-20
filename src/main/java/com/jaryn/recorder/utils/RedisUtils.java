package com.jaryn.recorder.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    public <T> T get(String key, Class<T> clz) {
        String value = redisTemplate.opsForValue().get(key);
        return JSONObject.parseObject(value, clz);
    }


    public  <T> List<T> getList(String key,Class<T> clz){
        String value = redisTemplate.opsForValue().get(key);
        return JSON.parseObject(value, new TypeReference<List<T>>(clz){});
    }


    public <T> T getAndClear(String key, Class<T> clz) {
        String value = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        return JSONObject.parseObject(value, clz);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String getAndClear(String key) {
        String value = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        return value;
    }

    public <K> void put(String key, K k, long seconds) {
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(k), seconds, TimeUnit.SECONDS);
    }

    public <K> void setIfAbsent(String key, K k, long seconds) {
        redisTemplate.opsForValue().setIfAbsent(key,JSONObject.toJSONString(k),seconds,TimeUnit.SECONDS);
    }


    public <K> void put(String key, K k) {
        // redisTemplate.opsForValue().set(key, JSONObject.toJSONString(k));
        // 默认3天
        put(key, k, 3 * 24 * 60 * 60);
    }

    /**
     * put进字符串类型
     * @param key
     * @param value
     */
    // public void put(String key, String value) {
    //     // redisTemplate.opsForValue().set(key, value);
    //     // 默认3天
    //     redisTemplate.opsForValue().set(key, value, 3, TimeUnit.DAYS);
    // }

    /**
     * put进字符串类型
     * @param key
     * @param value
     * @param seconds
     */
    public void put(String key, String value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
    }

    public void refreshTime(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }


}
