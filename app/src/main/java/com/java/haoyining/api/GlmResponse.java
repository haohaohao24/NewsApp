package com.java.haoyining.api;

import java.util.List;

/**
 * 接收GLM API响应的数据模型。
 */
public class GlmResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    public static class Message {
        private String role;     // 添加此字段
        private String content;

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
