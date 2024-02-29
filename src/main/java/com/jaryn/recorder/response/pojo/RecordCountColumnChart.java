package com.jaryn.recorder.response.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jaryn
 * @date: 2024/2/29 10:18 上午
 * @description: 录分人数柱状图信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordCountColumnChart {
    /**
     * 2.26开始
     */
    private String date;
    /**
     * 录分人数
     */
    private int count;
}
