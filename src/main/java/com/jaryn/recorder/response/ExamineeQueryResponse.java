package com.jaryn.recorder.response;

import com.jaryn.recorder.bean.ApplyingMajor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Jaryn
 * @date: 2024/1/1 5:00 下午
 * @description: 查询考生编号是否录分-响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamineeQueryResponse {

    /**
     * 查询考生编号是否录分
     */
    private boolean existFlag;
}
