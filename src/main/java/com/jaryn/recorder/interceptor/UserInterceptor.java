package com.jaryn.recorder.interceptor;

/**
 * @author: Jaryn
 * @date: 2024/1/1 2:36 下午
 * @description: 用户拦截器，主要判断ck是否过期
 */
import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.service.UserService;
import com.jaryn.recorder.utils.OkHttpUtil;
import com.jaryn.recorder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static com.jaryn.recorder.constants.Constant.Http.USER_TOKEN;
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.*;

@Component
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (LOGIN.equals(request.getServletPath()) || QUERY_APPLYING_MAJOR.equals(request.getServletPath()) || QUERY_EXAMINEE_EXIST.equals(request.getServletPath())) {
            // 登陆不走该拦截器
            return true;
        }

        if ("OPTIONS".equals(request.getMethod())) {
            // 预检请求（OPTIONS请求），不走该拦截器，很重要！！！！
            return true;
        }


        String token = OkHttpUtil.getToken(request);
        UserInfo userInfo = getUserInfo(token);
        boolean hasValid = userInfo != null;

        if (!hasValid) {
            // 如果 cookie 无效或不存在，执行特定操作，如重定向到登录页面
            throw new ServiceException("登陆过期");
        }
        // ck刷新 + cache刷新
        userService.assembleCookie(response, token);
        redisUtils.put(token, userInfo);
        // log.debug("Cache个数：{}", cache.size());
        return true;
    }

    private UserInfo getUserInfo(String token) {
        UserInfo user = redisUtils.get(token, UserInfo.class);
        if (user != null) {
            log.info("用户姓名：" + user.getName());
        }
        return user;
    }

}

