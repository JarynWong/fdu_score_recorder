package com.jaryn.recorder.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jaryn
 * @date: 2024/1/2 9:50 上午
 * @description: 查分请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreQueryRequest {
    /**
     * 查询类型
     * @see com.jaryn.recorder.constants.Constant.QueryType
     */
    private String queryType;

}
