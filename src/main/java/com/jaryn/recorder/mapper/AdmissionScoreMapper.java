package com.jaryn.recorder.mapper;

import com.jaryn.recorder.bean.AdmissionScore;
import com.jaryn.recorder.bean.Score;

import java.util.List;

// @Mapper
public interface AdmissionScoreMapper {

    List<AdmissionScore> find(AdmissionScore info);
    AdmissionScore findOne(AdmissionScore info);

}

