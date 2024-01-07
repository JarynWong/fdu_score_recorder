package com.jaryn.recorder.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionScore implements Serializable {

    /**
     * 注释见表备注
     */
    private Integer id;
    private Integer minScore;
    private Integer year;
    private Integer applyingMajorId;

}
