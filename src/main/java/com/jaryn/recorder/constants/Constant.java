package com.jaryn.recorder.constants;

/**
 * @author: Jaryn
 * @date: 2023/12/27 4:49 下午
 * @description:
 */
public class Constant {

    public static class SERVICE_CODE {
        public static final String LOGIN = "/login";
        public static final String CHECK_STATE = "/checkstate";
        public static final String QUERY_SCORE = "/queryscore";
        public static final String QUERY_APPLYING_MAJOR = "/queryapplyingmajor";
    }

    public static class Http {
        public static final String VERIFY_CODE_HTTP = "https://gsas.fudan.edu.cn/captcha/imageCode";
        public static final String LOGIN_HTTP = "https://gsas.fudan.edu.cn/logon";
        public static final int VERIFY_CODE_MAX_THRESHOLD = 4;
        public static final int LOGIN_MAX_THRESHOLD = 4;
        public static final String VERIFY_CODE_ERROR = "验证码错误";
        public static final String PASSWORD_ERROR = "用户名或密码有误，请重新输入!";
        public static final String OPEN_TIME_ERROR = "暂未到开放时间!";
        public static final String USER_TOKEN = "USER_TOKEN";
    }

    public static class Strings {
        public static final String EMPTY = "";
        public static final String SUFFIX_1 = "(一)";
        public static final String SUFFIX_2 = "(二)";
        public static final String SUFFIX_1_CH = "（一）";
        public static final String SUFFIX_2_CH = "（二）";
    }

    public static class Cache {
        public static final String APPLYING_MAJOR_ALL_KEY = "getApplyingMajor";
        public static final String APPLYING_MAJOR_KEY = "getApplyingMajor:";
        public static final String APPLYING_MAJOR_ID_KEY = "applyingMajorId:";
        public static final String SCORE_KEY = "score:";
        public static final String USER_KEY = "user:";
        public static final String IP_KEY = "ip:";
    }

    public static class QueryType {
        public static final String TOTAL = "1";
        public static final String POLITICS = "2";
        public static final String ENGLISH = "3";
        public static final String PROFESSIONAL_COURSE1 = "4";
        public static final String PROFESSIONAL_COURSE2 = "5";
    }

    public static class Score {
        // TODO
        // public static final int COLUMN_CHART_MIN_SCORE = 321;
        public static final int COLUMN_CHART_MIN_SCORE = 221;
    }
}
