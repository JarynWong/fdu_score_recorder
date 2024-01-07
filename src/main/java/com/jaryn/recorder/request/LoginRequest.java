package com.jaryn.recorder.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: Jaryn
 * @date: 2024/1/2 9:50 上午
 * @description: 登陆请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest  {
    /**
     * 院系id、准考证、密码
     */
    private String applyingMajorId;
    private String username;
    private String password;

}
