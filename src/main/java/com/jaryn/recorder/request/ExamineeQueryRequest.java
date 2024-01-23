package com.jaryn.recorder.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jaryn
 * @date: 2024/1/23 9:50 上午
 * @description: 查询考生编号是否录分-请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamineeQueryRequest {
    /**
     * 考生编号 也就是username
     */
    private String examineeNum;
    /**
     * 申请人QQ
     */
    private String qq;

}
