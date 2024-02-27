package com.jaryn.recorder.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jaryn.recorder.constants.Constant;
import com.jaryn.recorder.exception.ServiceException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jaryn.recorder.constants.Constant.Http.USER_TOKEN;
import static com.jaryn.recorder.constants.Constant.PatternConstant.ACTION_PATTERN;

/**
 * http util
 */
public class OkHttpUtil {

    private static final Logger log = LoggerFactory.getLogger(OkHttpUtil.class.getName());

    public static final MediaType UTF_8 = MediaType.parse("charset=utf-8");
    /**
     * text/xml
     */
    public static final MediaType TEXT_XML = MediaType.parse("text/xml;charset=utf-8");
    /**
     * form-data
     */
    private static final MediaType FROM_DATA = MediaType.parse("multipart/form-data");
    /**
     * xml
     */
    public static final MediaType XML_TYPE = MediaType.parse("application/xml;charset=utf-8");
    /**
     * json
     */
    public static final MediaType JSON_TYPE = MediaType.parse("application/json;charset=utf-8");
    /**
     * file
     */
    public static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream;charset=utf-8");
    /**
     * form表单，有中文时的请求格式
     */
    public static final MediaType FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");

    /**
     * 设置连接超时时间 单位:s 在OkHttpClient中设置单位
     */
    private static final int CONNECT_TIMEOUT = 60;
    /**
     * 设置读取超时时间 单位:s 在OkHttpClient中设置单位
     */
    private static final int READ_TIMEOUT = 60;
    /**
     * 设置写入超时时间 单位:s 在OkHttpClient中设置单位
     */
    private static final int WRITE_TIMEOUT = 60;

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build();

    /**
     * @param url       请求路径
     * @param jsonParam 请求参数
     * @return 响应结果
     */
    public static String doRequestBodyPost(String url, String jsonParam) {
        RequestBody requestBody = RequestBody.create(OkHttpUtil.JSON_TYPE, jsonParam);
        Request request = new Request.Builder().post(requestBody).url(url).build();
        //响应结果
        return execute(client, request);
    }


    public static String getToken(HttpServletRequest request) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (USER_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return Constant.Strings.EMPTY;
    }

    /**
     * @param url       请求路径
     * @param jsonParam 请求参数
     * @param mediaType MIME类型
     * @return 响应结果
     */
    public static String doRequestBodyPost(String url, String jsonParam, MediaType mediaType) {
        RequestBody requestBody = RequestBody.create(mediaType, jsonParam);
        Request request = new Request.Builder().post(requestBody).url(url).build();
        //响应结果
        return execute(client, request);
    }

    /**
     * @param url       请求路径
     * @param headers   请求头
     * @param jsonParam 请求参数
     * @param mediaType MIME类型
     * @return 响应结果
     */
    public static String doRequestBodyPost(String url, Map<String, String> headers, String jsonParam, MediaType mediaType) {
        RequestBody requestBody = RequestBody.create(mediaType, jsonParam);
        //请求头
        Headers.Builder hBuilder = new Headers.Builder();
        if (headers != null) {
            headers = headers.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            headers.forEach(hBuilder::add);
        }
        Request request = new Request.Builder().headers(hBuilder.build()).post(requestBody).url(url).build();
        //响应结果
        return execute(client, request);
    }

    /**
     * from表单，参数无中文时
     *
     * @param url     请求路径
     * @param formMap 请求参数
     * @return 响应结果
     */
    public static String doFormBody(String url, Map<String, String> formMap) {
        log.info("http 请求,form 表单数据格式 url :{},请求参数:{}", url, formMap);
        String res = doFormBodyPost(url, formMap, null);
        JSONObject parse = JSONObject.parseObject(res);
        String message = parse.getString("message");
        log.info("http 请求,form 表单数据格式 url :{},响应参数:{}", url, message);
        return message;
    }

    /**
     * from表单，参数无中文时
     *
     * @param url     请求路径
     * @param formMap 请求参数
     * @return 响应结果
     */
    public static String doFormBodyPost(String url, Map<String, String> formMap) {
        return doFormBodyPost(url, formMap, null);
    }


    /**
     * from表单，参数无中文时
     *
     * @param url       请求路径
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @return 响应结果
     */
    public static String doFormBodyPost(String url, Map<String, String> formMap, Map<String, String> headerMap) {
        //请求头
        Headers.Builder hBuilder = new Headers.Builder();
        if (headerMap != null) {
            headerMap = headerMap.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            headerMap.forEach(hBuilder::add);
        }
        //请求体
        FormBody.Builder fBuilder = new FormBody.Builder();
        if (formMap != null) {
            formMap = formMap.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            formMap.forEach(fBuilder::add);
        }
        Request request = new Request.Builder()
                .headers(hBuilder.build())
                .post(fBuilder.build())
                .url(url)
                .build();
        //响应结果
        return execute(client, request);
    }

    /**
     * form表单，参数有中文时
     *
     * @param url     请求路径
     * @param formMap 请求参数
     * @return 响应结果
     */
    public static String doRequestBodyPost(String url, Map<String, Object> formMap) {
        return doRequestBodyPost(url, formMap, null);
    }

    /**
     * form表单有中文时
     *
     * @param url       请求路径
     * @param formMap   请求参数
     * @param headerMap 请求头
     * @return 响应结果
     */
    public static String doRequestBodyPost(String url, Map<String, Object> formMap, Map<String, String> headerMap) {
        //请求头
        Headers.Builder hBuilder = new Headers.Builder();
        if (headerMap != null) {
            headerMap = headerMap.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            headerMap.forEach(hBuilder::add);
        }
        StringJoiner joiner = new StringJoiner("&");
        if (formMap != null) {
            formMap = formMap.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            formMap.forEach((k, v) -> {
                joiner.add(k + "=" + v);
            });
        }
        //请求体
        RequestBody requestBody = RequestBody.create(OkHttpUtil.FORM_URLENCODED, joiner.toString().getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .headers(hBuilder.build())
                .url(url)
                .post(requestBody)
                .build();
        //响应结果
        return execute(client, request);
    }

    /**
     * @param url 请求路径
     * @return 响应结果
     */
    public static Map<String, String> doGet(String url) {
        return doGet(url, null);
    }

    /**
     * @param url 请求路径
     * @return 响应结果
     */
    // public static String doGet(String url) {
    //     return execute(url);
    // }

    /**
     * @param url 请求路径
     * @return 响应结果
     */
    public static File doGetVerifyCode(String url, Map<String, String> headerMap) {

        Headers.Builder hBuilder = new Headers.Builder();
        //请求头
        if (headerMap != null) {
            headerMap = headerMap.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            headerMap.forEach(hBuilder::add);
        }

        Request request = new Request.Builder()
                .headers(hBuilder.build())
                .url(url)
                .build();

        File file = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                // 将图片读入内存
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream inputStream = responseBody.byteStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                }

                // 创建临时文件
                Path tempFile = Files.createTempFile("downloaded_image", ".jpg");
                Files.write(tempFile, baos.toByteArray());
                file = tempFile.toFile();
                log.debug("生成验证码图片路径：{}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("生成验证码图片出错：{}", e.toString());
            throw new ServiceException("生成验证码图片出错");
        }
        return file;
    }

    /**
     * @param url       请求路径
     * @param headerMap 请求头
     * @return 响应结果
     */
    public static Map<String, String> doGet(String url, Map<String, String> headerMap) {
        Headers.Builder hBuilder = new Headers.Builder();
        //请求头
        if (headerMap != null) {
            headerMap = headerMap.entrySet().stream().filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            headerMap.forEach(hBuilder::add);
        }
        Request request = new Request.Builder()
                .headers(hBuilder.build())
                .url(url)
                .get()
                .build();
        //响应结果
        return execute(request);
    }

    public static String doGetWithParams(String url, Map<String, String> params) {
        log.info("doGet - url1:{}, params:{}", url, params);
        if (params != null) {
            url = getUrl(url, params);
        }
        log.info("doGet - url2:{}", url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        //响应结果
        return execute(client, request);
    }

    /**
     * 构造get url
     *
     * @param url
     * @param params
     * @return
     */
    public static String getUrl(String url, Map<String, String> params) {
        // 添加url参数
        if (params != null && params.size() != 0) {
            Iterator<String> it = params.keySet().iterator();
            StringBuffer sb = null;
            while (it.hasNext()) {
                String key = it.next();
                String value = String.valueOf(params.get(key));
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            url += sb.toString();
        }
        return url;
    }

    private static String execute(OkHttpClient client, Request request) {
        Response res = null;
        try {
            res = client.newCall(request).execute();
            if (res == null) {
                log.error("响应信息为空");
                return null;
            }
            if (res.isSuccessful()) {
                //请求成功
                return res.body().string();
            } else {
                //请求失败
                throw new ServiceException("请求失败");
            }
        } catch (Exception e) {
            log.error("http请求异常,请求参数:{},错误信息:", request.toString(), e);
        } finally {
            Optional.ofNullable(res).ifPresent(Response::close);
        }
        return "";
    }

    private static Map<String, String> execute(Request request) {
        Response res = null;
        // 存放ck和form action="/sscjcx/28198B369067E88DAB9FEFE85484DBF4"
        Map<String, String> resMap = new HashMap<>();
        String cookies = Constant.Strings.EMPTY;
        try {
            res = client.newCall(request).execute();
            if (res == null) {
                log.error("响应信息为空");
                resMap.put("cookies", cookies);
                return resMap;
            }
            if (res.isSuccessful()) {
                //请求成功
                List<String> cookieList = res.headers("Set-Cookie");
                for (String cookie : cookieList) {
                    log.debug("cookie: {}", cookie);
                    cookies = cookies.concat(cookie).concat(";");
                }

                Matcher matcher = ACTION_PATTERN.matcher(res.body().string());
                if (matcher.find()) {
                    resMap.put("action", matcher.group(1).trim());
                } else {
                    throw new ServiceException("action获取失败");
                }

            } else {
                //请求失败
                throw new ServiceException("请求失败");
            }
        } catch (Exception e) {
            log.error("http请求异常,请求参数:{},错误信息:", request.toString(), e);
        } finally {
            Optional.ofNullable(res).ifPresent(Response::close);
        }
        resMap.put("cookies", cookies);
        return resMap;
    }

    // /**
    //  * 请求
    //  *
    //  * @param request
    //  * @return response
    //  */
    // public static Response execute(Request request) {
    //     Response res = null;
    //     try {
    //         res = client.newCall(request).execute();
    //         if (res == null) {
    //             log.error("响应信息为空");
    //             return null;
    //         }
    //     } catch (Exception e) {
    //         log.error("http请求异常,请求参数:{},错误信息:", request.toString(), e);
    //     }
    //     return res;
    // }


    public static byte[] doGetWithByteRepsonse(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response res = null;
        try {
            res = client.newCall(request).execute();
            if (res.isSuccessful()) {
                return res.body().bytes();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Optional.ofNullable(res).ifPresent(Response::close);
        }
        return null;
    }

    public static String doPostFile(String url, String fileKey, byte[] fileValue, String fileName, Map<String, String> params) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (String param : params.keySet()) {
            builder.addFormDataPart(param, params.get(param));
        }
        RequestBody fileBody = RequestBody.create(FILE_TYPE, fileValue);
        builder.addFormDataPart(fileKey, fileName, fileBody);
        builder.setType(FROM_DATA);
        MultipartBody body = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(client, request);

    }

    /**
     * gzip请求处理
     *
     * @param url
     * @param jsonParam
     * @return
     */
    public static String doGzipPostBody(String url, String jsonParam) {
        RequestBody requestBody = RequestBody.create(JSON_TYPE, jsonParam);
        Request request = new Request
                .Builder()
                .header("Content-Encoding", "gzip")
                .post(requestBody)
                .url(url)
                .build();
        //响应结果
        return executeGzip(client, request);
    }

    private static String executeGzip(OkHttpClient client, Request request) {
        Response res = null;
        try {
            res = client.newCall(request).execute();
            if (Objects.isNull(res)) {
                return null;
            }
            log.info("调用返回值:{},httpStatus:{},message:{},body:{}", JSON.toJSONString(res), res.code(), res.message());
            return res.body().string();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        } finally {
            Optional.ofNullable(res).ifPresent(Response::close);
        }
    }


    public static Map<String, String> getCookieHeaders(String cks) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cks);
        return headers;
    }

    public static Map<String, String> getLoginHeaders(String cks) {
        Map<String, String> headers = getCookieHeaders(cks);
        headers.put("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Host", "gsas.fudan.edu.cn");
        headers.put("Referer", "https://gsas.fudan.edu.cn/logon");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.put("Origin", "https://gsas.fudan.edu.cn");
        headers.put("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Connection", "keep-alive");
        headers.put("Cache-Control", "max-age=0");
        return headers;
    }

}
