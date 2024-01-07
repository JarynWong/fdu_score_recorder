package com.jaryn.recorder.mapper;

import com.jaryn.recorder.bean.ApplyingMajor;
import com.jaryn.recorder.bean.Score;

import java.util.List;

// @Mapper
public interface ApplyingMajorMapper {

    /**
     * 查询单个 Score 记录
     */
    ApplyingMajor findOne(ApplyingMajor applyingMajor);

    /**
     * 查询所有 Score 记录
     */
    List<ApplyingMajor> find(ApplyingMajor applyingMajor);
}

