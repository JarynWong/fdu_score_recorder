package com.jaryn.recorder.service;

import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.AdmissionScore;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.mapper.AdmissionScoreMapper;
import com.jaryn.recorder.mapper.ScoreMapper;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.jaryn.recorder.constants.Constant.Cache.*;

/**
 * @author: Jaryn
 * @date: 2024/1/2 11:18 上午
 * @description:
 */
@Service
public class ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private AdmissionScoreMapper admissionScoreMapper;

    @Resource
    private FduPostgraduateProperties fduPostgraduateProperties;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private Cache<String, Object> cache;

    /**
     * 保存分数
     * 首次登陆保存分数
     * 非首次则不用走这一步
     */
    public Score saveScore(UserInfo user) {
        // 查缓存和库
        String key = SCORE_KEY
                .concat(user.getAdmissionTicket())
                .concat(user.getName())
                .concat(String.valueOf(fduPostgraduateProperties.getYear()));
        Score score = getScore(user, key);
        if (score != null) {
            return score;
        }

        // 第一次登陆，缓存和数据库都没有
        score = mapperFacade.map(user, Score.class);
        score.setYear(fduPostgraduateProperties.getYear());
        score.calculateTotalScore();
        scoreMapper.create(score);
        cache.put(key, score);
        return score;
    }

    /**
     * 在缓存/数据库中尝试获取用户之前录分的信息
     * @param user
     * @param key
     * @return
     */
    public Score getScore(UserInfo user, String key) {
        Score score = (Score)cache.getIfPresent(key);
        if (score != null) {
            return score;
        }
        // 缓存找不到就数据库查一下，再放入缓存
        Score queryScore = new Score();
        queryScore.setAdmissionTicket(user.getAdmissionTicket());
        queryScore.setUsername(user.getUsername());
        queryScore.setYear(fduPostgraduateProperties.getYear());
        queryScore.setName(user.getName());
        score = scoreMapper.findOne(queryScore);
        if (score != null) {
            // 用户非首次登陆
            cache.put(key, score);
            return score;
        }
        return null;
    }

    /**
     * 在数据库中尝试获取用户之前录分的信息
     * @return
     */
    public Score getScoreBot(String username) {
        // 缓存找不到就数据库查一下，再放入缓存
        Score queryScore = new Score();
        queryScore.setUsername(username);
        queryScore.setYear(fduPostgraduateProperties.getYear());
        return scoreMapper.findOne(queryScore);
    }

    /**
     * 获取分数列表
     */
    public List<Score> getScores(Integer applyingMajorId) {
        Score allScoreQuery = new Score();
        allScoreQuery.setApplyingMajorId(applyingMajorId);
        allScoreQuery.setYear(fduPostgraduateProperties.getYear());
        return scoreMapper.find(allScoreQuery);
    }

    /**
     * 获取往年录取分数
     */
    public List<AdmissionScore> getAdmissionScores(Integer applyingMajorId) {
        // 缓存key
        String applyingMajorIdKey = APPLYING_MAJORS_ID_KEY.concat(String.valueOf(applyingMajorId));
        List<AdmissionScore> admissionScores = (List<AdmissionScore>)cache.getIfPresent(applyingMajorIdKey);
        if (Objects.isNull(admissionScores)) {
            AdmissionScore queryAdmissionScore = new AdmissionScore();
            queryAdmissionScore.setApplyingMajorId(applyingMajorId);
            admissionScores = admissionScoreMapper.find(queryAdmissionScore);
            cache.put(applyingMajorIdKey, admissionScores);
        }
        return admissionScores;
    }

    /**
     * 获取录取分数列表
     */
    public AdmissionScore getAdmissionScore(Integer applyingMajorId) {
        // 缓存key
        String applyingMajorIdKey = APPLYING_MAJOR_ID_KEY.concat(String.valueOf(applyingMajorId));
        AdmissionScore admissionScore = (AdmissionScore)cache.getIfPresent(applyingMajorIdKey);
        if (Objects.isNull(admissionScore)) {
            AdmissionScore queryAdmissionScore = new AdmissionScore();
            queryAdmissionScore.setApplyingMajorId(applyingMajorId);
            queryAdmissionScore.setYear(fduPostgraduateProperties.getYear());
            admissionScore = admissionScoreMapper.findOne(queryAdmissionScore);
            if (admissionScore == null) {
                return admissionScore;
            }
            cache.put(applyingMajorIdKey, admissionScore);
        }
        return admissionScore;
    }
}
