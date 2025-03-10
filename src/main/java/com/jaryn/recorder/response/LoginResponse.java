package com.jaryn.recorder.response;

import com.jaryn.recorder.response.pojo.RecordCountColumnChart;
import com.jaryn.recorder.response.pojo.ScoreRangeColumnChart;
import com.jaryn.recorder.response.pojo.LineChart;
import com.jaryn.recorder.response.pojo.OverallScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Jaryn
 * @date: 2024/1/1 5:00 下午
 * @description: 登陆响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    /**
     * 专业课名
     */
    private String professionalCourse1Name;
    private String professionalCourse2Name;
    /**
     * 报考院系id + 名字
     */
    private Integer applyingMajorId;
    private String applyingMajorName;

    /**
     * 你的总分
     */
    private int score;

    /**
     * 你的总分排名
     */
    private int rank;

    /**
     * 全体学生成绩
     */
    private List<OverallScore> scores;

    /**
     * 柱状图信息
     */
    private List<ScoreRangeColumnChart> columnCharts;

    /**
     * 录分人数变动情况
     */
    private List<RecordCountColumnChart> recordCountColumnCharts;

    /**
     * 折线图信息
     */
    private List<LineChart> lineCharts;
}
