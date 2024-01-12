package com.jaryn.recorder.response.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jaryn
 * @date: 2024/1/2 10:18 上午
 * @description: 往年录取分-折线图信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineChart {
    /**
     * 折线图x轴的年份、录取分
     */
    private Integer year;
    private Integer minScore;
}
