package com.java.haoyining.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class NewsData implements Serializable {

    @SerializedName("newsID")
    private String newsID;
    @SerializedName("title")
    private String title;
    @SerializedName("publishTime")
    private String publishTime;
    @SerializedName("publisher")
    private String publisher;
    @SerializedName("image")
    private String image;

    // *** 新增字段 ***
    @SerializedName("content")
    private String content;
    @SerializedName("video")
    private String video;

    // --- Getters and Setters ---
    // (省略已有的getter/setter)

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

    // 已有的 getter/setter ...
    public String getNewsID() { return newsID; }
    public void setNewsID(String newsID) { this.newsID = newsID; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
