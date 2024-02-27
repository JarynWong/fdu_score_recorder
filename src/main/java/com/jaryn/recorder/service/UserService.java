package com.jaryn.recorder.service;

import com.google.common.cache.Cache;
import com.jaryn.recorder.bean.AdmissionScore;
import com.jaryn.recorder.bean.ApplyingMajor;
import com.jaryn.recorder.bean.Score;
import com.jaryn.recorder.bean.UserInfo;
import com.jaryn.recorder.config.FduPostgraduateProperties;
import com.jaryn.recorder.constants.Constant;
import com.jaryn.recorder.exception.ServiceException;
import com.jaryn.recorder.ocr.Ocr;
import com.jaryn.recorder.response.LoginResponse;
import com.jaryn.recorder.response.pojo.ColumnChart;
import com.jaryn.recorder.response.pojo.LineChart;
import com.jaryn.recorder.response.pojo.OverallScore;
import com.jaryn.recorder.utils.OkHttpUtil;
import com.jaryn.recorder.utils.RedisUtils;
import com.jaryn.recorder.utils.Util;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jaryn.recorder.constants.Constant.Cache.USER_KEY;
import static com.jaryn.recorder.constants.Constant.Http.*;
import static com.jaryn.recorder.constants.Constant.PatternConstant.SCORE_PATTERN;
import static com.jaryn.recorder.constants.Constant.QueryType.TOTAL;
import static com.jaryn.recorder.constants.Constant.Score.COLUMN_CHART_MIN_SCORE;
import static com.jaryn.recorder.constants.Constant.Strings.SPACE;

/**
 * @author: Jaryn
 * @date: 2024/1/2 11:28 上午
 * @description:
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ApplyingMajorService applyingMajorService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private FduPostgraduateProperties fduPostgraduateProperties;

    /**
     * 登陆
     *
     * @param user
     */
    public LoginResponse login(UserInfo user) {
        checkParam(user);
        log.info("开始登陆，登陆用户名：{}", user.getUsername());
        // 登陆并将信息封装到user
        login4AssembleUser(user);
        // 登陆成功后将用户的四门成绩入库
        Score score = scoreService.saveScore(user);
        return getLoginResponse(score);
    }

    /**
     * 检测参数
     * @param user
     */
    private void checkParam(UserInfo user) {
        if (user.getApplyingMajorId() == null || user.getUsername() == null || user.getPassword() == null) {
            throw new ServiceException("参数错误");
        }
        if (user.getUsername().length() < 6 || user.getPassword().length() < 6 || !Util.isNumeric(user.getUsername())) {
            throw new ServiceException("用户名或密码错误");
        }
        String key = PASSWORD_ERROR
                .concat(user.getUsername())
                .concat(String.valueOf(fduPostgraduateProperties.getYear()));
        Integer errorCnt = redisUtils.get(key, Integer.class);
        if (errorCnt != null && errorCnt >= MAX_ERROR_CNT) {
            throw new ServiceException("密码错误次数过多，请1-2天后重试");
        }
    }

    /**
     * 登陆并将信息封装到user
     *
     * @param user
     */
    private void login4AssembleUser(UserInfo user) {
        String websiteRes = null;
        for (int i = 0; i < LOGIN_MAX_THRESHOLD; i++) {
            // 先获取验证码信息
            String action = generateVerifyCode(user);
            if (user.getValidateCode() == null || user.getValidateCode().length() != 4) {
                if (i + 1 == LOGIN_MAX_THRESHOLD) {
                    // 最后一次失败了，让用户重新输入
                    throw new ServiceException("出现异常！请重新输入信息！");
                }
                continue;
            }

            // 开始登陆
            Map<String, String> formMap = new HashMap<>();
            formMap.put("nd", String.valueOf(fduPostgraduateProperties.getYear()));
            formMap.put("username", user.getUsername());
            formMap.put("password", user.getPassword());
            formMap.put("validateCode", user.getValidateCode());
            Map<String, String> headers = OkHttpUtil.getLoginHeaders(user.getCookies());
            websiteRes = OkHttpUtil.doFormBodyPost(LOGIN_HTTP.concat(action), formMap, headers);

            String errorInfo = Util.getErrorInfo(websiteRes);
            String passwordErrorCntKey = Constant.Cache.PASSWORD_ERROR
                    .concat(user.getUsername())
                    .concat(String.valueOf(fduPostgraduateProperties.getYear()));
            if (VERIFY_CODE_ERROR.equals(errorInfo)) {
                // 验证码错误，继续重试
                log.error("验证码错误，错误次数 : {}", i + 1);
                if (i + 1 == LOGIN_MAX_THRESHOLD) {
                    // 最后一次失败了，让用户重新输入
                    throw new ServiceException("出现异常！请重新输入信息！");
                }
            } else if (PASSWORD_ERROR.equals(errorInfo)) {
                // 密码错误五次以上冻结
                Integer errorCnt = redisUtils.get(passwordErrorCntKey, Integer.class);
                if (errorCnt == null) {
                    errorCnt = 0;
                }
                redisUtils.put(passwordErrorCntKey, ++errorCnt);
                throw new ServiceException(errorInfo);
            }
            else if (OPEN_TIME_ERROR.equals(errorInfo)) {
                throw new ServiceException(errorInfo);
            }
            else {
                // 登陆成功，封装四门单科信息
                assembleScore(user, websiteRes);
                user.setPassword(null);
                // 清空密码错误次数
                Integer errorCnt = redisUtils.get(passwordErrorCntKey, Integer.class);
                if (errorCnt != null) {
                    redisUtils.invalidate(passwordErrorCntKey);
                }
                break;
            }
        }
    }

    /**
     * 获取登陆响应
     */
    public LoginResponse getLoginResponse(Score score) {
        LoginResponse loginResponse = new LoginResponse();
        // 院系信息
        loginResponse.setApplyingMajorId(score.getApplyingMajorId());
        ApplyingMajor applyingMajorInfo = applyingMajorService.getApplyingMajorName(score.getApplyingMajorId());
        loginResponse.setApplyingMajorName(applyingMajorInfo.getApplyingMajorName());

        List<Score> allScores = scoreService.getScores(score.getApplyingMajorId(), score.getProfessionalCourse2Name());

        // 根据总分逆序
        StringBuilder rankStr = new StringBuilder();
        List<OverallScore> overallScores = getOverallScoresOrderly(score, allScores, TOTAL, rankStr);
        loginResponse.setRank(Integer.parseInt(rankStr.toString()));

        loginResponse.setScores(overallScores);

        loginResponse.setProfessionalCourse1Name(score.getProfessionalCourse1Name());
        loginResponse.setProfessionalCourse2Name(score.getProfessionalCourse2Name());

        // 柱状图信息计算
        AdmissionScore admissionScore = scoreService.getAdmissionScore(score.getApplyingMajorId());
        loginResponse.setColumnCharts(obtainColumnCharts(TOTAL, overallScores, admissionScore));

        // 计算平均分
        OverallScore averageScore = getAverageScore(overallScores);
        // 计算过线平均分
        OverallScore averageOverScore = getAverageOverScore(overallScores, admissionScore);
        // 最后第二个是平均分
        overallScores.add(averageScore);
        // 最后一个是过线平均分
        overallScores.add(averageOverScore);

        // 总分
        loginResponse.setScore(Util.getIntFunctionByQueryType(TOTAL).applyAsInt(score));

        // 折线图
        List<LineChart> lineCharts = scoreService.getAdmissionScores(score.getApplyingMajorId())
                .stream()
                .map(admissionScoreInfo -> mapperFacade.map(admissionScoreInfo, LineChart.class))
                .sorted(Comparator.comparingInt(LineChart::getYear))
                .collect(Collectors.toList());
        loginResponse.setLineCharts(lineCharts);
        return loginResponse;
    }

    /**
     * 获取平均分
     */
    public OverallScore getAverageScore(List<OverallScore> overallScores) {
        OverallScore averageScore = new OverallScore();
        averageScore.setPolitics((int) overallScores.stream().mapToInt(OverallScore::getPolitics).average().getAsDouble());
        averageScore.setEnglish((int) overallScores.stream().mapToInt(OverallScore::getEnglish).average().getAsDouble());
        averageScore.setProfessionalCourse1Score((int) overallScores.stream().mapToInt(OverallScore::getProfessionalCourse1Score).average().getAsDouble());
        averageScore.setProfessionalCourse2Score((int) overallScores.stream().mapToInt(OverallScore::getProfessionalCourse2Score).average().getAsDouble());
        averageScore.setTotalScore((int) overallScores.stream().mapToInt(OverallScore::getTotalScore).average().getAsDouble());
        return averageScore;
    }

    /**
     * 获取过线平均分，前提是总分平均分大于等于录取平均分
     * 查询总分才走这个
     */
    public OverallScore getAverageOverScore(List<OverallScore> overallScores, AdmissionScore admissionScore) {
        OverallScore averageOverScore = new OverallScore();
        if (admissionScore == null) {
            return averageOverScore;
        }
        averageOverScore.setPolitics((int) overallScores.stream()
                .filter(overallScore -> overallScore.getTotalScore() >= admissionScore.getMinScore())
                .mapToInt(OverallScore::getPolitics).average().getAsDouble());
        averageOverScore.setEnglish((int) overallScores.stream()
                .filter(overallScore -> overallScore.getTotalScore() >= admissionScore.getMinScore())
                .mapToInt(OverallScore::getEnglish).average().getAsDouble());
        averageOverScore.setProfessionalCourse1Score((int) overallScores.stream()
                .filter(overallScore -> overallScore.getTotalScore() >= admissionScore.getMinScore())
                .mapToInt(OverallScore::getProfessionalCourse1Score).average().getAsDouble());
        averageOverScore.setProfessionalCourse2Score((int) overallScores.stream()
                .filter(overallScore -> overallScore.getTotalScore() >= admissionScore.getMinScore())
                .mapToInt(OverallScore::getProfessionalCourse2Score).average().getAsDouble());
        averageOverScore.setTotalScore((int) overallScores.stream()
                .filter(overallScore -> overallScore.getTotalScore() >= admissionScore.getMinScore())
                .mapToInt(OverallScore::getTotalScore).average().getAsDouble());
        return averageOverScore;
    }


    /**
     * 根据分数逆序，自定义分数类型
     *
     * @see com.jaryn.recorder.constants.Constant.QueryType
     * 分数相同则排名相同
     */
    public List<OverallScore> getOverallScoresOrderly(Score score, List<Score> allScores, String queryType, StringBuilder rankStr) {
        ToIntFunction<OverallScore> functionByQueryType = Util.getIntFunctionByQueryType(queryType);
        List<OverallScore> overallScores = mapperFacade.mapAsList(allScores, OverallScore.class).stream()
                .sorted(Comparator.comparingInt(functionByQueryType).reversed())
                .collect(Collectors.toList());

        // 计算所有人的排名，其中分数相等排名一致
        int rank = 1;
        // 不存在overallScores为空的情况，因为前面一定插入过数据
        overallScores.get(0).setRank(rank);
        // 前一位的分数一致的人数
        int cnt = 0;
        for (int i = 1; i < overallScores.size(); i++) {

            if (functionByQueryType.applyAsInt(overallScores.get(i)) < functionByQueryType.applyAsInt(overallScores.get(i - 1))) {
                // 分比前一名小，那么rank++，否则前面分数一样的人cnt+1
                rank++;
                // +cnt是要算上前面分数相同的人，比如排名1 2 2 4，那么++2 + 1 = 4
                rank += cnt;
                cnt = 0;
            } else {
                cnt++;
            }
            // 比如400 399 399 397，那么排名是1 2 2 4
            overallScores.get(i).setRank(rank);
            // 计算当前用户的排名
            if (functionByQueryType.applyAsInt(score) == functionByQueryType.applyAsInt(overallScores.get(i))) {
                if (Constant.Strings.EMPTY.equals(rankStr.toString().trim())) {
                    rankStr.append(rank);
                }
            }
        }
        // i = 0的情况没考虑，最高分，第一名
        if (functionByQueryType.applyAsInt(score) == functionByQueryType.applyAsInt(overallScores.get(0))) {
            // 这里的代码表示将rankStr设为1，防止与上面的rankStr.append(rank)冲突
            rankStr.setLength(1);
            rankStr.setCharAt(0, '1');
        }
        return overallScores;
    }

    public static void main(String[] args) {
        StringBuilder rankStr = new StringBuilder();
        rankStr.setLength(1);
        rankStr.setCharAt(0, '1');
        System.err.println(rankStr);
    }

    /**
     * 获取柱状图
     */
    public List<ColumnChart> obtainColumnCharts(String queryType, List<OverallScore> overallScores, AdmissionScore admissionScore) {
        ToIntFunction<OverallScore> functionByQueryType = Util.getIntFunctionByQueryType(queryType);

        List<ColumnChart> columnCharts = new ArrayList<>();

        // 最高分段柱状图计算
        int maxScore = functionByQueryType.applyAsInt(overallScores.get(0));
        ColumnChart maxScoreColumnChart = new ColumnChart();
        maxScoreColumnChart.setMax(maxScore);
        // 如果分数%10=1那么x坐标就显示这个分数，其次分数%10=0，那么显示（分数/10-1）*10+1 到 这个分数的分数段，最后否则显示分数/10*10+1 到 这个分数的分数段
        if (maxScore % 10 == 1) {
            maxScoreColumnChart.setMin(maxScore);
        } else if (maxScore % 10 == 0) {
            maxScoreColumnChart.setMin((maxScore / 10 - 1) * 10 + 1);
        } else {
            maxScoreColumnChart.setMin(maxScore / 10 * 10 + 1);
        }
        calculateColumnChartCount(overallScores, maxScoreColumnChart, queryType);

        // 最低分段柱状图计算，
        int minScore = functionByQueryType.applyAsInt(overallScores.get(overallScores.size() - 1));
        // minScore = 未出分前的最低分 或 出分后的录取分数(总分才有)
        if (TOTAL.equals(queryType)) {
            // 统计总分时，321分以下就不统计了
            minScore = Math.max(minScore, COLUMN_CHART_MIN_SCORE);
            if (admissionScore != null) {
                // 出分了
                minScore = admissionScore.getMinScore();
            }
        }


        ColumnChart minScoreColumnChart = new ColumnChart();
        minScoreColumnChart.setMin(minScore);
        // 如果分数%10=0那么x坐标就显示这个分数，否则显示 这个分数到（分数/10+1）*10的分数段
        if (minScore % 10 == 0) {
            minScoreColumnChart.setMax(minScore);
        } else {
            minScoreColumnChart.setMax((minScore / 10 + 1) * 10);
        }
        calculateColumnChartCount(overallScores, minScoreColumnChart, queryType);
        if (minScore < maxScore) {
            columnCharts.add(minScoreColumnChart);
        }

        // 中间分段柱状图计算
        int midMin = minScoreColumnChart.getMax() + 1;
        int midMax = maxScoreColumnChart.getMin() - 1;
        // 中间分数段个数=midmax/10到midmin/10的差值
        int midColumnChartCnt = midMax / 10 - midMin / 10;
        for (int i = 0; i < midColumnChartCnt; i++) {
            ColumnChart midScoreColumnChart = new ColumnChart();
            midScoreColumnChart.setMin(midMin);
            // 351 + 9 = 360
            midScoreColumnChart.setMax(midMin + 9);
            calculateColumnChartCount(overallScores, midScoreColumnChart, queryType);
            columnCharts.add(midScoreColumnChart);

            midMin += 10;
        }
        columnCharts.add(maxScoreColumnChart);
        return columnCharts;
    }

    /**
     * 封装ck
     *
     * @param response
     * @param token
     */
    public void assembleCookie(HttpServletResponse response, String token) {
        // 创建一个新的 Cookie 来存储会话 ID
        Cookie sessionCookie = new Cookie(USER_TOKEN, token);
        // 设置 cookie 过期时间为 2天
        sessionCookie.setMaxAge(60 * 60 * 24 * 2);
        // 防止 JavaScript 访问此 cookie
        sessionCookie.setHttpOnly(true);
        // 设置 cookie 应用的路径
        sessionCookie.setPath("/");
        // 安全标志，只在HTTPS下发送
        // sessionCookie.setSecure(true);
        // 将 Cookie 添加到响应中
        response.addCookie(sessionCookie);

        response.addCookie(new Cookie("SameSite", "None"));
    }


    /**
     * 封装四门单科信息
     *
     * @param user
     * @param res
     */
    private void assembleScore(UserInfo user, String res) {
        // 处理res字符串，获取四门单科信息
        Matcher matcher = SCORE_PATTERN.matcher(res);

        if (matcher.find()) {
            // // Extract and print the matched groups
            // System.out.println("准考证号: " + matcher.group(1));
            // System.out.println("姓名: " + matcher.group(2));
            // System.out.println("报考院系: " + matcher.group(3).replaceAll("&nbsp;", " ").trim());
            // System.out.println("报考专业: " + matcher.group(4).replaceAll("&nbsp;", " ").trim());
            // System.out.println("政治成绩: " + matcher.group(5).trim());
            // System.out.println("英语成绩: " + matcher.group(6).trim());
            // System.out.println("专业课1: " + matcher.group(7).trim());
            // System.out.println("专业课1成绩: " + matcher.group(8).trim());
            // System.out.println("专业课2: " + matcher.group(9).trim());
            // System.out.println("专业课2成绩: " + matcher.group(10).trim());
            // System.out.println("总分: " + matcher.group(11).trim());

            user.setAdmissionTicket(matcher.group(1));
            user.setName(matcher.group(2));
            user.setPolitics(Integer.parseInt(matcher.group(5).trim()));
            user.setEnglish(Integer.parseInt(matcher.group(6).trim()));
            user.setProfessionalCourse1Score(Integer.parseInt(matcher.group(8).trim()));
            user.setProfessionalCourse1Name(matcher.group(7).trim());
            user.setProfessionalCourse2Score(Integer.parseInt(matcher.group(10).trim()));
            user.setProfessionalCourse2Name(matcher.group(9).trim());

            ApplyingMajor applyingMajor = applyingMajorService.getApplyingMajor(matcher.group(3).replaceAll(SPACE, " ").trim(),
                    matcher.group(4).replaceAll(SPACE, " ").trim());
            if (applyingMajor == null) {
                throw new ServiceException("院系专业信息查询失败，请联系作者配置");
            }
            user.setApplyingMajorId(applyingMajor.getApplyingMajorId());
        } else {
            throw new ServiceException("分数解析失败");
        }

        // 测试：生成随机准考证，防止唯一索引冲突
        // Random random = new Random();
        // int numberOfDigits = 7;
        // int lowerBound = (int) Math.pow(10, numberOfDigits - 1);
        // int upperBound = (int) Math.pow(10, numberOfDigits) - 1;
        //
        // user.setAdmissionTicket(random.nextInt(upperBound - lowerBound + 1) + lowerBound + "");
        // user.setName("十多好");
        // user.setPolitics(60 + random.nextInt(41));
        // user.setEnglish(60 + random.nextInt(41));
        // user.setProfessionalCourse1Score(60 + random.nextInt(41));
        // user.setProfessionalCourse1Name("302 数学 （二）");
        // user.setProfessionalCourse2Score(60 + random.nextInt(41));
        // user.setProfessionalCourse2Name("408 计算机专业基础");
    }

    /**
     * 计算柱状图分数段人数
     */
    private void calculateColumnChartCount(List<OverallScore> overallScores, ColumnChart columnChart, String queryType) {
        // 计算分数段人数
        long count = overallScores.stream()
                .map(Util.getFunctionByQueryType(queryType))
                .filter(score -> score >= columnChart.getMin() && score <= columnChart.getMax())
                .count();
        columnChart.setCount((int) count);
    }

    /**
     * 获取图片验证码
     *
     * @return
     */
    private String generateVerifyCode(UserInfo user) {
        Ocr ocr = context.getBean(Ocr.class);
        String action = Constant.Strings.EMPTY;
        for (int i = 0; i < VERIFY_CODE_MAX_THRESHOLD; i++) {
            // 获取验证码id_ck
            Map<String, String> resMap = OkHttpUtil.doGet(INDEX_HTTP);
            String cookies = resMap.get("cookies");
            action = resMap.get("action");
            user.setCookies(cookies);
            Map<String, String> headers = OkHttpUtil.getCookieHeaders(cookies);

            // 绑定验证码id_ck，生成验证码
            String verifyCode = getVerifyCode(ocr, headers);

            log.info("识别验证码中...");
            // code 不为4位就重新识别
            // 位数识别正确就直接退出循环，共三次试错机会
            if (verifyCode.length() == 4) {
                log.info("识别验证码成功：{}", verifyCode);
                user.setValidateCode(verifyCode);
                break;
            }
            log.info("识别验证码失败：{}", verifyCode);
        }
        return action;
    }

    /**
     * 获取图片验证码
     * @param ocr
     * @param headers
     * @return
     */
    private String getVerifyCode(Ocr ocr, Map<String, String> headers) {
        File verifyCodeFile = OkHttpUtil.doGetVerifyCode(VERIFY_CODE_HTTP, headers);
        String verifyCode = ocr.identify(verifyCodeFile).trim();
        try {
            Files.delete(verifyCodeFile.toPath());
        } catch (IOException e) {
            log.error("删除图片验证码出错：{}", e.toString());
            throw new ServiceException("删除图片验证码出错");
        }
        return verifyCode;
    }
}
