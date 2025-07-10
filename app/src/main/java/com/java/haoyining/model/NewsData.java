package com.java.haoyining.model;

import java.io.Serializable;

// *** 核心修复 1: 必须实现 Serializable 接口 ***
// 这样才能通过 Intent 在 Activity 之间传递对象
public class NewsData implements Serializable {

    // *** 核心修复 2: 确保所有字段都存在 ***
    private String newsID;
    private String title;
    private String publisher;
    private String publishTime;
    private String image;
    private String content;
    private String video;

    // --- Getters and Setters ---
    // 确保每个字段都有对应的 get 和 set 方法

    public String getNewsID() {
        return newsID;
    }

    public void setNewsID(String newsID) {
        this.newsID = newsID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
