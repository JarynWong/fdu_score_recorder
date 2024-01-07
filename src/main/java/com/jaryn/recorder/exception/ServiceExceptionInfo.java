package com.jaryn.recorder.exception;

import lombok.Data;

/**
 * @author: Jaryn
 * @date: 2024/1/5 11:28 上午
 * @description:
 */
@Data
public class ServiceExceptionInfo {
    private String code = "ex";
    private String msg;

}
