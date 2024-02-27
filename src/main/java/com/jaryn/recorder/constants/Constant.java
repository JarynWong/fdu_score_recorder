package com.jaryn.recorder.constants;

import java.util.regex.Pattern;

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
        public static final String QUERY_EXAMINEE_EXIST = "/queryexamineeexist";
    }

    public static class Http {
        public static final String VERIFY_CODE_HTTP = "https://gsas.fudan.edu.cn/captcha/imageCode";
        public static final String LOGIN_HTTP = "https://gsas.fudan.edu.cn";
        public static final String INDEX_HTTP = "https://gsas.fudan.edu.cn/sscjcx/index";
        public static final int VERIFY_CODE_MAX_THRESHOLD = 4;
        public static final int LOGIN_MAX_THRESHOLD = 4;
        public static final String VERIFY_CODE_ERROR = "验证码错误";
        public static final String PASSWORD_ERROR = "未查询到相应的成绩，请检查输入信息!";
        public static final String OPEN_TIME_ERROR = "暂未到开放时间!";
        public static final String USER_TOKEN = "USER_TOKEN";
        public static final int MAX_ERROR_CNT = 20;
        public static final int MAX_ADD_GROUP_FAIL_CNT = 8;
    }

    public static class Strings {
        public static final String EMPTY = "";
        public static final String SUFFIX_1 = "(一)";
        public static final String SUFFIX_2 = "(二)";
        public static final String SUFFIX_1_CH = "（一）";
        public static final String SUFFIX_2_CH = "（二）";
        public static final String SPACE = "&nbsp;";
    }

    public static class Cache {
        public static final String APPLYING_MAJOR_ALL_KEY = "getApplyingMajor";
        public static final String APPLYING_MAJOR_KEY = "getApplyingMajor:";
        public static final String APPLYING_MAJOR_ID_KEY = "applyingMajorId:";
        public static final String APPLYING_MAJORS_ID_KEY = "applyingMajorsId:";
        public static final String SCORE_KEY = "score:";
        public static final String USER_KEY = "user:";
        public static final String TOKEN_KEY = "token:";
        public static final String IP_KEY = "ip:";
        public static final String PASSWORD_ERROR = "passwordError:";
        public static final String QUERY_EXAMINEE_FAIL = "examineeError:";
        public static final String EXAMINEE_KEY = "examinee:";
    }

    public static class QueryType {
        public static final String TOTAL = "1";
        public static final String POLITICS = "2";
        public static final String ENGLISH = "3";
        public static final String PROFESSIONAL_COURSE1 = "4";
        public static final String PROFESSIONAL_COURSE2 = "5";
    }

    public static class Score {
        public static final int COLUMN_CHART_MIN_SCORE = 321;
    }

    public static class PatternConstant {
        public static final Pattern ACTION_PATTERN = Pattern.compile("form action=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
        public static final Pattern SCORE_PATTERN = Pattern.compile(
                "准考证号</td>\\s*<td[^>]*>(\\d+)</td>.*?" +
                        "姓名</td>\\s*<td[^>]*>([^<]+)</td>.*?" +
                        "报考院系</td>.*?<td[^>]*>([^<]+)</td>.*?" +
                        "报考专业</td>.*?<td[^>]*>([^<]+)</td>.*?" +
                        "101 思想政治理论</td>\\s*<td[^>]*>([^<]+)</td>.*?" +
                        "20.*? 英语.*?</td>\\s*<td[^>]*>([^<]+)</td>.*?" +
                        "<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?" +
                        "<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?"+
                        "<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?" +
                        "<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?" +
                        "总分</td>\\s*<td[^>]*>([^<]+)</td>.*?" ,
                Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS);
    }
}
