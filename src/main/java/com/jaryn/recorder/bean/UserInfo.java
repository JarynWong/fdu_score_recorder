package com.jaryn.recorder.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Jaryn
 * @date: 2023/12/27 10:10 下午
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    /**
     * type=2为研究生初试成绩查询.其他为博士成绩等
     */
    private String type = "2";
    private String username = "12345677";
    private String password = "asdasdasd";
    private String validateCode;
    private String cookies;

    /**
     * 名字和准考证号和报考专业
     */
    private String name;
    private String admissionTicket;
    private Integer applyingMajorId;

    /**
     * 四门科目成绩
     */
    private Integer politics;
    private Integer english;
    private Integer professionalCourse1Score;
    private String professionalCourse1Name;
    private Integer professionalCourse2Score;
    private String professionalCourse2Name;

}
