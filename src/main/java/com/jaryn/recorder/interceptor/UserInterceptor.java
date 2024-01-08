package com.jaryn.recorder.interceptor;

/**
 * @author: Jaryn
 * @date: 2024/1/1 2:36 下午
 * @description: 用户拦截器，主要判断ck是否过期
 */
import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.utils.OkHttpUtil;
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
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.LOGIN;

@Component
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private Cache<String, Object> cache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (LOGIN.equals(request.getServletPath())) {
            // 登陆不走该拦截器
            return true;
        }
        if ("OPTIONS".equals(request.getMethod())) {
            // 预检请求（OPTIONS请求），不走该拦截器，很重要！！！！
            return true;
        }



        boolean hasValid = checkTokenValid(OkHttpUtil.getToken(request));

        if (!hasValid) {
            // 如果 cookie 无效或不存在，执行特定操作，如重定向到登录页面
            throw new ServiceException("登陆过期");
        }
        // assembleCookie(response, OkHttpUtil.getToken(request));
        return true;
    }

    private boolean checkTokenValid(String sessionId) {
        Object user = cache.getIfPresent(sessionId);
        if (user != null) {
            log.info("用户姓名：" + ((UserInfo)user).getName());
        }
        return user != null;
    }

    /**
     * 封装ck
     *
     * @param response
     * @param token
     */
    private void assembleCookie(HttpServletResponse response, String token) {
        // 创建一个新的 Cookie 来存储会话 ID
        Cookie sessionCookie = new Cookie(USER_TOKEN, token);
        // 设置 cookie 过期时间为 5天
        sessionCookie.setMaxAge(60 * 60 * 24 * 5);
        // 防止 JavaScript 访问此 cookie
        sessionCookie.setHttpOnly(true);
        // 设置 cookie 应用的路径
        sessionCookie.setPath("/");
        // 安全标志，只在HTTPS下发送
        sessionCookie.setSecure(true);
        // 将 Cookie 添加到响应中
        response.addCookie(sessionCookie);

        response.addCookie(new Cookie("SameSite", "None"));
    }

}

