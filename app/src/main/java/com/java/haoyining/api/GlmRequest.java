package com.java.haoyining.api;

import java.util.List;

/**
 * 发送给GLM API的请求体数据模型。
 */
public class GlmRequest {
    private String model;
    private List<Message> messages;
    private float temperature;
    private float top_p;
    private String request_id;

    public GlmRequest(String model, List<Message> messages, float temperature, float top_p, String request_id) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.top_p = top_p;
        this.request_id = request_id;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
