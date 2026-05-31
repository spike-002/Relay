package com.spike.relay.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.spike.relay.dto.OrderPublishDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiOrderParserService {

    // 从 application.yaml 里读取配置
    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    // 我们刚才设计好的“超级系统提示词”
    private static final String SYSTEM_PROMPT = "你是一个专业的校园跑腿订单智能助理。\n" +
            "你的任务是提取关键信息，并检查【必填项】是否缺失。\n" +
            "必填项包括：手机号(phone)、取件地点(pickupCabinet)、目标楼栋(targetBuilding)、具体房间号(targetAddress)、跑腿费(fee)。\n" +
            "\n" +
            "你必须严格输出如下格式的 JSON（绝不输出任何废话或 markdown 标记）：\n" +
            "{\n" +
            "  \"isComplete\": true, // 只有所有必填项都提供时才为 true，否则为 false\n" +
            "  \"replyMsg\": \"\", // 如果 isComplete 为 false，请用幽默、友好的客服口吻告诉用户缺少了什么信息。如果为 true，留空。\n" +
            "  \"orderData\": {\n" +
            "    \"phone\": \"提取的手机号\",\n" +
            "    \"pickupCabinet\": \"取件地点\",\n" +
            "    \"targetBuilding\": \"目标楼栋\",\n" +
            "    \"targetAddress\": \"房间号\",\n" +
            "    \"fee\": 10.0 // 数字\n" +
            "  }\n" +
            "}";
    /**
     * 将用户的自然语言大白话，解析为标准的 DTO 对象
     */
    /**
     * 将用户的自然语言大白话，解析为带有状态的 JSON 对象
     */
    public JSONObject parseOrderFromText(String userText) {
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        requestBody.set("temperature", 0.1);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", SYSTEM_PROMPT));
        messages.add(new JSONObject().set("role", "user").set("content", userText));
        requestBody.set("messages", messages);

        String responseStr = HttpRequest.post(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute()
                .body();

        JSONObject responseJson = JSONUtil.parseObj(responseStr);
        String aiOutput = responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getStr("content");

        // 直接返回 AI 构建的这颗完整的 JSON 树
        return JSONUtil.parseObj(aiOutput);
    }
}