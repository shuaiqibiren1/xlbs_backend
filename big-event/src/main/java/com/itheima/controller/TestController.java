package com.itheima.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itheima.config.WenXinConfig;
import com.itheima.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/test")


public class TestController {

    @Resource
    private WenXinConfig wenXinConfig;

    // 历史对话，需要按照 user, assistant
    List<Map<String, String>> messages = new ArrayList<>();

    /**
     * 非流式问答
     */
    @PostMapping("/ask")
    public Result<String> test1(@RequestParam String question) throws IOException {
        if (question == null || question.isEmpty()) {
            return Result.error("请输入问题");
        }

        log.info("用户提问: {}", question);

        String responseJson;
        String accessToken = wenXinConfig.flushAccessToken();
        if (accessToken != null) {
            HashMap<String, String> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", question);
            messages.add(user);

            String requestJson = constructRequestJson(1, 0.95, 0.8, 1.0, false, messages);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
            Request request = new Request.Builder()
                    .url(wenXinConfig.ERNIE_Bot_4_0_URL + "?access_token=" + accessToken)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            printRequest(request);
            OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

            try {
                responseJson = HTTP_CLIENT.newCall(request).execute().body().string();
                log.info("API 响应: {}", responseJson);

                // 将回复的内容转为一个 JSONObject
                JSONObject responseObject = JSON.parseObject(responseJson);
                // 将回复的内容添加到消息中
                HashMap<String, String> assistant = new HashMap<>();
                assistant.put("role", "assistant");
                assistant.put("content", responseObject.getString("result"));
                messages.add(assistant);

                return Result.success(responseObject.getString("result"));  // 返回成功结果
            } catch (IOException e) {
                log.error("网络有问题", e);
                return Result.error("网络有问题，请稍后重试");
            }
        }
        return Result.error("获取令牌失败");  // 处理获取令牌的错误情况
    }

    /**
     * 构造请求的请求参数
     */
    public String constructRequestJson(Integer userId,
                                       Double temperature,
                                       Double topP,
                                       Double penaltyScore,
                                       boolean stream,
                                       List<Map<String, String>> messages) {
        Map<String, Object> request = new HashMap<>();
        request.put("user_id", userId.toString());
        request.put("temperature", temperature);
        request.put("top_p", topP);
        request.put("penalty_score", penaltyScore);
        request.put("stream", stream);
        request.put("messages", messages);

        String jsonRequest = JSON.toJSONString(request);
        log.info("构造的请求 JSON: {}", jsonRequest);
        return jsonRequest;
    }

    /**
     * 打印 Request
     */
    private void printRequest(Request request) {
        System.out.println("Request Details:");
        System.out.println("Method: " + request.method());
        System.out.println("URL: " + request.url());
        System.out.println("Headers: " + request.headers());

        // 如果请求体存在且不是空的，打印请求体
        if (request.body() != null) {
            // 注意：需要手动读取请求体内容，通常需要在发送请求前就读取
            // 这里示范性地打印出内容
            System.out.println("Request body exists.");
        }
    }
}