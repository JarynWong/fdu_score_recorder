package com.jaryn.recorder.bean;

import com.jaryn.recorder.constants.Constant;
import com.jaryn.recorder.response.pojo.OverallScore;
import com.jaryn.recorder.utils.Util;
import com.sun.deploy.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score extends OverallScore implements Serializable {

    /**
     * 注释见表备注
     */
    private Integer id;
    private String name;
    private String admissionTicket;
    private String username;
    private Integer politics;
    private Integer english;
    private Integer professionalCourse1Score;
    private String professionalCourse1Name;
    private Integer professionalCourse2Score;
    private String professionalCourse2Name;
    private Integer totalScore;
    private Integer applyingMajorId;
    private Integer year;

    public void calculateTotalScore() {
        totalScore = politics + english + professionalCourse1Score + professionalCourse2Score;
    }

    /**
     * 专业课1，提取302 数学 （二）的数学二字
     * @param professionalCourse1Name
     */
    public void setProfessionalCourse1Name(String professionalCourse1Name) {
        String prefixNumber = Util.extractNumber(professionalCourse1Name);
        this.professionalCourse1Name = professionalCourse1Name.replace(prefixNumber, Constant.Strings.EMPTY)
                .replace(Constant.Strings.SUFFIX_1, Constant.Strings.EMPTY)
                .replace(Constant.Strings.SUFFIX_2, Constant.Strings.EMPTY)
                .replace(Constant.Strings.SUFFIX_1_CH, Constant.Strings.EMPTY)
                .replace(Constant.Strings.SUFFIX_2_CH, Constant.Strings.EMPTY).trim();
        // 专业课名字超过2个字则显示课程代码
        if (this.professionalCourse1Name.length() > 2 && !Util.isNumeric(this.professionalCourse1Name)) {
            this.professionalCourse1Name = Util.extractNumber(professionalCourse1Name);
        }
    }

    /**
     * 专业课2，提取408 计算机专业基础的408代码
     * @param professionalCourse2Name
     */
    public void setProfessionalCourse2Name(String professionalCourse2Name) {
        if (professionalCourse2Name.contains("--")) {
            // 对于只考一门专业课的院系
            this.professionalCourse2Name = "无";
        } else {
            this.professionalCourse2Name = Util.extractNumber(professionalCourse2Name);
        }
    }
}
