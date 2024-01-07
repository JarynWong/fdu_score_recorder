package com.jaryn.recorder.response.pojo;

import com.jaryn.recorder.bean.Score;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jaryn
 * @date: 2024/1/1 5:02 下午
 * @description: 综合成绩，用于查询返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverallScore  {
    /**
     * 排名
     */
    private int rank;
    private Integer politics;
    private Integer english;
    private Integer professionalCourse1Score;
    private Integer professionalCourse2Score;
    private Integer totalScore;
}
