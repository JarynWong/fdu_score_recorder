package com.jaryn.recorder.controller;

import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.AdmissionScore;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.ocr.Ocr;
import com.jaryn.recorder.request.LoginRequest;
import com.jaryn.recorder.request.ScoreQueryRequest;
import com.jaryn.recorder.response.LoginResponse;
import com.jaryn.recorder.response.ScoreQueryResponse;
import com.jaryn.recorder.response.pojo.ColumnChart;
import com.jaryn.recorder.response.pojo.OverallScore;
import com.jaryn.recorder.service.UserService;
import com.jaryn.recorder.utils.OkHttpUtil;
import com.jaryn.recorder.utils.Util;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import com.jaryn.recorder.service.ScoreService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.jaryn.recorder.constants.Constant.Http.*;
import static com.jaryn.recorder.constants.Constant.QueryType.*;
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.LOGIN;
import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.QUERY_SCORE;

@RestController
public class ScoreController {

    @Autowired
    private UserService userService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private Cache<String, Object> cache;

    /**
     * 查分
     * 总分，四门单科
     */
    @PostMapping(QUERY_SCORE)
    public ScoreQueryResponse queryScore(@RequestBody ScoreQueryRequest request, HttpServletRequest servletRequest) {
        String token = OkHttpUtil.getToken(servletRequest);
        UserInfo user = (UserInfo) cache.getIfPresent(token);

        List<Score> scores = scoreService.getScores(user.getApplyingMajorId());
        Score userScore = scoreService.saveScore(user);

        // 排序
        StringBuilder rankStr = new StringBuilder();
        List<OverallScore> overallScoresOrderly = userService.getOverallScoresOrderly(userScore, scores, request.getQueryType(), rankStr);
        ScoreQueryResponse response = new ScoreQueryResponse();
        response.setRank(Integer.parseInt(rankStr.toString()));
        response.setScores(overallScoresOrderly);
        // 柱状图
        response.setColumnCharts(userService.obtainColumnCharts(user.getApplyingMajorId(), request.getQueryType(), overallScoresOrderly));
        return response;
    }

}
