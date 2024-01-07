package com.jaryn.recorder.exception;

/**
 * @author: Jaryn
 * @date: 2024/1/4 7:49 下午
 * @description: 异常类
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
