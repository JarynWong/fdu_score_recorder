package com.jaryn.recorder.response.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jaryn
 * @date: 2024/1/2 10:18 上午
 * @description: 柱状图信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnChart {
    /**
     * 柱状图x轴的下限、上限、人数
     */
    private int min;
    private int max;
    private int count;
}
