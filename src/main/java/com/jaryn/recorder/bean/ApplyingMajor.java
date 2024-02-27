package com.jaryn.recorder.bean;

import com.jaryn.recorder.constants.Constant;
import com.jaryn.recorder.utils.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyingMajor implements Serializable {

    /**
     * 注释见表备注
     */
    private Integer applyingMajorId;
    private String applyingMajorName;
    private String departmentInfo;
    private String majorInfo;

}
