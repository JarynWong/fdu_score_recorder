package com.jaryn.recorder.utils;

import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.constants.Constant;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.response.pojo.OverallScore;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jaryn.recorder.constants.Constant.QueryType.*;
import static com.jaryn.recorder.constants.Constant.Score.FORMATTER;

/**
 * @author: Jaryn
 * @date: 2023/12/27 3:15 下午
 * @description:
 */
@Slf4j
public class Util {

    private static final Map<String, ToIntFunction<OverallScore>> SUBJECT_INT_FUNCTION_MAP = new HashMap<>();
    private static final Map<String, Function<OverallScore, Integer>> SUBJECT_FUNCTION_MAP = new HashMap<>();

    static {
        SUBJECT_INT_FUNCTION_MAP.put(TOTAL, OverallScore::getTotalScore);
        SUBJECT_INT_FUNCTION_MAP.put(POLITICS, OverallScore::getPolitics);
        SUBJECT_INT_FUNCTION_MAP.put(ENGLISH, OverallScore::getEnglish);
        SUBJECT_INT_FUNCTION_MAP.put(PROFESSIONAL_COURSE1, OverallScore::getProfessionalCourse1Score);
        SUBJECT_INT_FUNCTION_MAP.put(PROFESSIONAL_COURSE2, OverallScore::getProfessionalCourse2Score);

        SUBJECT_FUNCTION_MAP.put(TOTAL, OverallScore::getTotalScore);
        SUBJECT_FUNCTION_MAP.put(POLITICS, OverallScore::getPolitics);
        SUBJECT_FUNCTION_MAP.put(ENGLISH, OverallScore::getEnglish);
        SUBJECT_FUNCTION_MAP.put(PROFESSIONAL_COURSE1, OverallScore::getProfessionalCourse1Score);
        SUBJECT_FUNCTION_MAP.put(PROFESSIONAL_COURSE2, OverallScore::getProfessionalCourse2Score);
    }

    /**
     * 根据查分类型，查询对应的
     */
    public static ToIntFunction<OverallScore> getIntFunctionByQueryType(String queryType) {
        ToIntFunction<OverallScore> function = SUBJECT_INT_FUNCTION_MAP.get(queryType);
        if (function == null) {
            throw new ServiceException("参数错误");
        }
        return function;
    }

    /**
     * 根据查分类型，查询对应的
     */
    public static Function<OverallScore, Integer> getFunctionByQueryType(String queryType) {
        Function<OverallScore, Integer> function = SUBJECT_FUNCTION_MAP.get(queryType);
        if (function == null) {
            throw new ServiceException("参数错误");
        }
        return function;
    }


    /**
     * 获取网站错误信息
     * @param websiteRes
     * @return
     */
    public static String getErrorInfo(String websiteRes){
        // 定义正则表达式
        String regex = "<div class=\"form-group\" id=\"errorInfo\"[^>]*>\\s*(.*?)\\s*</div>";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(websiteRes);

        // 检查是否有匹配项
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 从字符串的开头提取数字
     * 比如302 数学 （二）提取302这几个数字
     */
    public static String extractNumber(String str) {
        if (str != null && !str.isEmpty()) {
            // 使用正则表达式匹配开头的数字
            Matcher matcher = Pattern.compile("^\\d+").matcher(str);
            if (matcher.find()) {
                // 将匹配到的数字字符串转换为整数
                return matcher.group();
            }
        }
        return Constant.Strings.EMPTY;
    }

    /**
     * 判断字符串是不是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        return str.matches("\\d+");
    }

    /**
     * 判断两天是不是同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        return FORMATTER.format(date1).equals(FORMATTER.format(date2));
    }


}
