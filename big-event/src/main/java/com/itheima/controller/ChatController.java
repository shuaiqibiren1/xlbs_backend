package com.itheima.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itheima.config.WenXinConfig;
import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/wenxin")
public class ChatController {

    @Resource
    private WenXinConfig wenXinConfig;

    // 历史对话，需要按照 user, assistant
    List<Map<String, String>> messages = new ArrayList<>();

    @Autowired
    private UserService userService;   // 假设已经通过依赖注入的用户服务

    @PostMapping("/info")
    public Result<String> info() throws IOException {
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        // 获取用户信息，如果没有找到用户则返回错误
        User user = userService.findByUserName(username);
        if (user == null) {
            return Result.error("用户未找到");
        }

        String question = "我的名字是 " + user.getUsername() + "之后的回答需要添加这个姓名"; // 获取用户名并构造问题
        log.info("用户询问信息: {}", question); // 日志记录

        // 调用处理用户问题的方法并返回结果
        return handleUserQuestion(question);
    }


    @PostMapping("/ask")
    public Result<String> ask(@RequestParam String question) throws IOException {
        if (question == null || question.isEmpty()) {
            return Result.error("请输入问题");
        }

        log.info("用户提问: {}", question);

        // 调用封装好的处理函数
        return handleUserQuestion(question);
    }

    private Result<String> handleUserQuestion(String question) throws IOException {
        String responseJson;
        String accessToken = wenXinConfig.flushAccessToken();
        if (accessToken != null) {
            HashMap<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);

            String requestJson = constructRequestJson(1, 0.95, 0.8, 1.0, false, messages);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
            Request request = new Request.Builder()
                    .url(wenXinConfig.ERNIE_Bot_4_0_URL + "?access_token=" + accessToken)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

            printRequest(request);

            try {
                responseJson = HTTP_CLIENT.newCall(request).execute().body().string();
                log.info("API 响应: {}", responseJson);

                // 处理API响应并构建返回结果
                return processApiResponse(responseJson);
            } catch (IOException e) {
                log.error("网络有问题", e);
                HashMap<String, String> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", "...");
                messages.add(assistantMessage);
                return Result.error("网络有问题，请稍后重试");
            }
        }
        return Result.error("获取令牌失败"); // 处理获取令牌的错误情况
    }

    private Result<String> processApiResponse(String responseJson) {
        // 将回复的内容转为一个 JSONObject
        JSONObject responseObject = JSON.parseObject(responseJson);
        // 将回复的内容添加到消息中
        HashMap<String, String> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", responseObject.getString("result"));
        messages.add(assistantMessage);

        return Result.success(responseObject.getString("result")); // 返回成功结果
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
        log.info("用户提问: {}", messages);
        return JSON.toJSONString(request);
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