package com.jaryn.recorder.response;

import com.jaryn.recorder.bean.ApplyingMajor;
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
public class ApplyingMajorResponse {

    /**
     * 柱状图信息
     */
    private List<ApplyingMajor> applyingMajors;
}
