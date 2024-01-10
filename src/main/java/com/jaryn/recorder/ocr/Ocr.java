package com.jaryn.recorder.ocr;

import com.jaryn.recorder.config.FduPostgraduateProperties;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author: Jaryn
 * @date: 2023/12/27 4:26 下午
 * @description:
 */
@Component
@Scope("prototype")
public class Ocr implements InitializingBean {

    private Tesseract tesseract;

    @Autowired
    private FduPostgraduateProperties fduPostgraduateProperties;


    /**
     * 字母数字图片验证码识别
     */
    public String identify(File picture){
        String result = "";
        try {
            result = tesseract.doOCR(picture);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return  result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tesseract = new Tesseract();
        // 设置tessdata文件夹的路径
        tesseract.setDatapath(fduPostgraduateProperties.getTessdataPath());
        // 同时使用英语（eng）和简体中文（chi_sim）进行文本识别。
        tesseract.setLanguage("eng+chi_sim");
    }
}
