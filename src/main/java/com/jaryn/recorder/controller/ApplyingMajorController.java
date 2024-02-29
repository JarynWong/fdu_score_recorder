package com.jaryn.recorder.controller;

import com.jaryn.recorder.response.ApplyingMajorResponse;
import com.jaryn.recorder.service.ApplyingMajorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.jaryn.recorder.constants.Constant.SERVICE_CODE.QUERY_APPLYING_MAJOR;

@RestController
public class ApplyingMajorController {

    @Resource
    private ApplyingMajorService applyingMajorService;

    @GetMapping(QUERY_APPLYING_MAJOR)
    public ApplyingMajorResponse getApplyingMajor() {
        return new ApplyingMajorResponse(applyingMajorService.getApplyingMajors());
    }


}
