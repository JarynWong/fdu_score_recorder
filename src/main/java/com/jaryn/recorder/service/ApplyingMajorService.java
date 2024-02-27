package com.jaryn.recorder.service;

import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.AdmissionScore;
import com.jaryn.recorder.bean.ApplyingMajor;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.mapper.AdmissionScoreMapper;
import com.jaryn.recorder.mapper.ApplyingMajorMapper;
import com.jaryn.recorder.mapper.ScoreMapper;
import com.jaryn.recorder.utils.RedisUtils;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.jaryn.recorder.constants.Constant.Cache.*;

/**
 * @author: Jaryn
 * @date: 2024/1/2 11:18 上午
 * @description:
 */
@Service
public class ApplyingMajorService {

    @Autowired
    private ApplyingMajorMapper applyingMajorMapper;

    @Resource
    private FduPostgraduateProperties fduPostgraduateProperties;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取报考院系
     */
    public List<ApplyingMajor> getApplyingMajors() {
        // 加入缓存机制
        List<ApplyingMajor> applyingMajors = redisUtils.getList(APPLYING_MAJOR_ALL_KEY, ApplyingMajor.class);
        if (Objects.isNull(applyingMajors)) {
            ApplyingMajor queryApplyingMajor = new ApplyingMajor();
            applyingMajors = applyingMajorMapper.find(queryApplyingMajor);
            redisUtils.put(APPLYING_MAJOR_ALL_KEY, applyingMajors);
        }
        return applyingMajors;
    }

    /**
     * 获取指定的报考院系
     */
    public ApplyingMajor getApplyingMajorName(Integer applyingMajorId) {
        String key = APPLYING_MAJOR_KEY.concat(String.valueOf(applyingMajorId));
        ApplyingMajor applyingMajor = redisUtils.get(key, ApplyingMajor.class);
        if (Objects.isNull(applyingMajor)) {
            ApplyingMajor queryApplyingMajor = new ApplyingMajor();
            queryApplyingMajor.setApplyingMajorId(applyingMajorId);
            applyingMajor = applyingMajorMapper.findOne(queryApplyingMajor);
            redisUtils.put(key, applyingMajor);
        }
        return applyingMajor;
    }

    /**
     * 根据院系和专业 获取指定的报考院系
     */
    public ApplyingMajor getApplyingMajor(String department, String major) {
        String key = APPLYING_MAJOR_KEY.concat(department).concat(major);
        ApplyingMajor applyingMajor = redisUtils.get(key, ApplyingMajor.class);
        if (Objects.isNull(applyingMajor)) {
            ApplyingMajor queryApplyingMajor = new ApplyingMajor();
            queryApplyingMajor.setDepartmentInfo(department);
            queryApplyingMajor.setMajorInfo(major);
            applyingMajor = applyingMajorMapper.findOne(queryApplyingMajor);
            redisUtils.put(key, applyingMajor);
        }
        return applyingMajor;
    }
}
