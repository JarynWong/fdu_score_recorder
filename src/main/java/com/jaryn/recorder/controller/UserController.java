package com.jaryn.recorder.controller;

import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.request.LoginRequest;
import com.jaryn.recorder.response.LoginResponse;
import com.jaryn.recorder.service.ScoreService;
import com.jaryn.recorder.service.UserService;
import com.jaryn.recorder.utils.OkHttpUtil;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.jaryn.recorder.constants.Constant.Http.USER_TOKEN;
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.CHECK_STATE;
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.LOGIN;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private Cache<String, Object> cache;

    @GetMapping(CHECK_STATE)
    public LoginResponse checkState(HttpServletRequest request) {
        String token = OkHttpUtil.getToken(request);
        UserInfo user = (UserInfo) cache.getIfPresent(token);
        // 未过期则直接获取分数等信息
        Score score = scoreService.saveScore(user);
        return userService.getLoginResponse(score);
    }

    @PostMapping(LOGIN)
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        UserInfo user = mapperFacade.map(request, UserInfo.class);
        LoginResponse loginResponse = userService.login(user);

        String token = UUID.randomUUID().toString();
        cache.put(token, user);
        assembleCookie(response, token);
        return loginResponse;
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
