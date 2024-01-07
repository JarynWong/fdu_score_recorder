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

        boolean hasValid = checkTokenValid(OkHttpUtil.getToken(request));

        if (!hasValid) {
            // 如果 cookie 无效或不存在，执行特定操作，如重定向到登录页面
            throw new ServiceException("登陆过期");
        }

        return true;
    }

    private boolean checkTokenValid(String sessionId) {
        Object user = cache.getIfPresent(sessionId);
        if (user != null) {
            log.info("用户姓名：" + ((UserInfo)user).getName());
        }
        return user != null;
    }

}

