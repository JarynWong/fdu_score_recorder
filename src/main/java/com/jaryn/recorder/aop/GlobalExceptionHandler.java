package com.jaryn.recorder.aop;

import com.alibaba.fastjson.JSON;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.exception.ServiceExceptionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author: Jaryn
 * @date: 2024/1/4 7:45 下午
 * @description:
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex) {
        logError(ex);
        ServiceExceptionInfo serviceExceptionInfo = new ServiceExceptionInfo();
        if (ex instanceof ServiceException) {
            // 创建并返回统一的错误响应
            serviceExceptionInfo.setMsg(ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(JSON.toJSONString(serviceExceptionInfo));
        }
        // 创建并返回统一的错误响应
        serviceExceptionInfo.setMsg("出现异常");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(JSON.toJSONString(serviceExceptionInfo));
    }

    private void logError(Exception ex) {
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        StringBuilder exStr = new StringBuilder(ex.getMessage() + "\n");
        for (StackTraceElement element : stackTraceElements) {
            exStr.append(element.toString()).append("\n");
        }
        log.error(exStr.toString());
    }
}
