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
import com.jaryn.recorder.utils.RedisUtils;
import com.jaryn.recorder.utils.Util;
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
    private RedisUtils redisUtils;

    @Resource
    private FduPostgraduateProperties fduPostgraduateProperties;

    @PostMapping(QUERY_EXAMINEE_EXIST)
    public ExamineeQueryResponse queryExamineeExist(@RequestBody ExamineeQueryRequest request) {
        // 检测考生编号是否被别人使用
        String examineeNumKey = EXAMINEE_KEY.concat(request.getExamineeNum());
        checkExamineeNum(request, examineeNumKey);

        Score score = scoreService.getScoreBot(request.getExamineeNum());

        boolean isExist = score != null;
        checkFailCnt(request, isExist, examineeNumKey);
        if (!isExist) {
            throw new ServiceException("请先进行录分");
        }
        return new ExamineeQueryResponse(true);
    }

    /**
     * 检测考生编号是否被别人使用
     */
    private void checkExamineeNum(ExamineeQueryRequest request, String examineeNumKey) {
        if (request.getExamineeNum().length() < 6) {
            throw new ServiceException("请先进行录分");
        }
        String qq = redisUtils.get(examineeNumKey, String.class);
        if (qq != null) {
            throw new ServiceException("编号已被使用," + qq);
        }
        String examineeFailCntKey = QUERY_EXAMINEE_FAIL
                .concat(request.getQq())
                .concat(String.valueOf(fduPostgraduateProperties.getYear()));
        Integer failCnt = redisUtils.get(examineeFailCntKey, Integer.class);
        if (failCnt != null && failCnt >= MAX_ADD_GROUP_FAIL_CNT) {
            throw new ServiceException("过多尝试，请1-2天后重试");
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
            Integer failCnt = redisUtils.get(examineeFailCntKey, Integer.class);
            if (failCnt == null) {
                failCnt = 0;
            }
            redisUtils.put(examineeFailCntKey, ++failCnt, 30 * 24 * 60 * 60);
        } else {
            // 清空原有失败次数的cache
            Integer failCnt = redisUtils.get(examineeFailCntKey, Integer.class);
            if (failCnt != null) {
                redisUtils.invalidate(examineeFailCntKey);
            }
            // 加入 考生编号 - qq缓存
            redisUtils.put(examineeNumKey, request.getQq(), 30 * 24 * 60 * 60);
        }
    }


}
