package com.jaryn.recorder.mapper;

import com.jaryn.recorder.bean.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// @Mapper
public interface ScoreMapper {
    /**
     * 插入新的 Score 记录
     */
    int create(Score score);

    /**
     * 查询单个 Score 记录
     */
    Score findOne(Score score);

    /**
     * 查询所有 Score 记录
     */
    List<Score> find(Score score);
}

