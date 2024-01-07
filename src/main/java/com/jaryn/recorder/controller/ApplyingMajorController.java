package com.jaryn.recorder.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jaryn.recorder.bean.ApplyingMajor;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.mapper.ApplyingMajorMapper;
import com.jaryn.recorder.mapper.ScoreMapper;
import com.jaryn.recorder.ocr.Ocr;
import com.jaryn.recorder.request.LoginRequest;
import com.jaryn.recorder.response.ApplyingMajorResponse;
import com.jaryn.recorder.response.LoginResponse;
import com.jaryn.recorder.response.pojo.ColumnChart;
import com.jaryn.recorder.response.pojo.OverallScore;
import com.jaryn.recorder.service.ApplyingMajorService;
import com.jaryn.recorder.utils.OkHttpUtil;
import com.jaryn.recorder.utils.Util;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jaryn.recorder.constants.Constant.Cache.APPLYING_MAJOR_ALL_KEY;
import static com.jaryn.recorder.constants.Constant.Http.*;

@RestController
public class ApplyingMajorController {

    @Resource
    private ApplyingMajorService applyingMajorService;

    @GetMapping("/applyingMajor")
    public ApplyingMajorResponse getApplyingMajor() {
        return new ApplyingMajorResponse(applyingMajorService.getApplyingMajors());
    }


}
