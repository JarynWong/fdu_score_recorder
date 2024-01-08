package com.jaryn.recorder.controller;

import com.github.pagehelper.util.StringUtil;
import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
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

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.jaryn.recorder.constants.Constant.Cache.SCORE_KEY;
import static com.jaryn.recorder.constants.Constant.Cache.USER_KEY;
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

    @Resource
    private FduPostgraduateProperties fduPostgraduateProperties;

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
        // 处理ck
        copeWithTokenInCache(response, user);
        return loginResponse;
    }

    /**
     * 处理ck
     * @param response
     * @param user
     */
    private void copeWithTokenInCache(HttpServletResponse response, UserInfo user) {
        // 禁止多处登录：登录成功后，将用户的三件套作为key，从cache中get，若存在token，则用这个token作为key，删除对应的user。最后ck作为value，存入缓存
        String key = USER_KEY
                .concat(user.getAdmissionTicket())
                .concat(user.getName())
                .concat(String.valueOf(fduPostgraduateProperties.getYear()));
        String oldToken = (String)cache.getIfPresent(key);
        if (StringUtil.isNotEmpty(oldToken)) {
            // 在cache删除user信息，那么其他处的ck就失效了
            cache.invalidate(oldToken);
        }

        String token = UUID.randomUUID().toString();
        // 第一个用于存储用户token和信息，第二个用于防止多地登陆
        cache.put(token, user);
        cache.put(key, token);
        userService.assembleCookie(response, token);
    }


}
