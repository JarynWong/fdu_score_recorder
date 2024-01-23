package com.jaryn.recorder.controller;

import com.github.pagehelper.util.StringUtil;
import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.request.ExamineeQueryRequest;
import com.jaryn.recorder.request.LoginRequest;
import com.jaryn.recorder.response.ExamineeQueryResponse;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.jaryn.recorder.constants.Constant.Cache.*;
import static com.jaryn.recorder.constants.Constant.Http.*;
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.*;

@RestController
public class BotController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private Cache<String, Object> cache;

    @Resource
    private FduPostgraduateProperties fduPostgraduateProperties;

    @PostMapping(QUERY_EXAMINEE_EXIST)
    public ExamineeQueryResponse queryExamineeExist(@RequestBody ExamineeQueryRequest request) {
        // 检测考生编号是否被别人使用
        String examineeNumKey = EXAMINEE_KEY.concat(request.getExamineeNum());
        checkExamineeNum(examineeNumKey);

        UserInfo user = new UserInfo();
        user.setUsername(request.getExamineeNum());
        Score score = scoreService.getScore(user, SCORE_KEY
                .concat(request.getExamineeNum())
                .concat(String.valueOf(fduPostgraduateProperties.getYear())));

        boolean isExist = score != null;
        checkFailCnt(request, isExist, examineeNumKey);
        return new ExamineeQueryResponse(isExist);
    }

    /**
     * 检测考生编号是否被别人使用
     * @param examineeNumKey
     */
    private void checkExamineeNum(String examineeNumKey) {
        String qq = (String) cache.getIfPresent(examineeNumKey);
        if (qq != null) {
            throw new ServiceException("考生编号已被他人使用，QQ：" + qq);
        }
    }

    /**
     * 检测申请入群人qq的申请次数，若失败太多则
     * @param request
     * @param isExist
     */
    private void checkFailCnt(ExamineeQueryRequest request, boolean isExist, String examineeNumKey) {
        String examineeFailCntKey = QUERY_EXAMINEE_FAIL
                .concat(request.getQq())
                .concat(String.valueOf(fduPostgraduateProperties.getYear()));
        if (!isExist) {
            Integer failCnt = (Integer) cache.getIfPresent(examineeFailCntKey);
            if (failCnt == null) {
                failCnt = 0;
            } else if (failCnt >= MAX_ADD_GROUP_FAIL_CNT) {
                throw new ServiceException("过多尝试，请1-2天后重试");
            }
            cache.put(examineeFailCntKey, ++failCnt);
        } else {
            // 清空原有失败次数的cache
            Integer failCnt = (Integer) cache.getIfPresent(examineeFailCntKey);
            if (failCnt != null) {
                cache.invalidate(examineeFailCntKey);
            }
            // 加入 考生编号 - qq缓存
            cache.put(examineeNumKey, request.getQq());
        }
    }


}
