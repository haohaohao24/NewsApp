package com.java.haoyining.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.java.haoyining.model.NewsData;

@Entity(tableName = "news_history")
public class NewsEntity {

    @PrimaryKey
    @NonNull
    private String newsID;

    private String title;
    private String publisher;
    private String publishTime;
    private String image;
    private String content;
    private String video;
    private boolean isRead;
    private boolean isFavorite;
    private String summary;

    public NewsEntity(@NonNull String newsID, String title, String publisher, String publishTime, String image, String content, String video) {
        this.newsID = newsID;
        this.title = title;
        this.publisher = publisher;
        this.publishTime = publishTime;
        this.image = image;
        this.content = content;
        this.video = video;
        this.isRead = false;
        this.isFavorite = false;
        this.summary = "";
    }

    // --- Getters and Setters ---
    @NonNull
    public String getNewsID() { return newsID; }
    public void setNewsID(@NonNull String newsID) { this.newsID = newsID; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    /**
     * 将数据库实体对象转换为UI数据对象
     * @return 一个NewsData实例
     */
    public NewsData toNewsData() {
        NewsData data = new NewsData();
        data.setNewsID(this.getNewsID());
        data.setTitle(this.getTitle());
        data.setPublisher(this.getPublisher());
        data.setPublishTime(this.getPublishTime());
        data.setImage(this.getImage());
        data.setContent(this.getContent());
        data.setVideo(this.getVideo());
        return data;
    }
}
