package com.java.haoyining.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {

    // *** 新增字段 ***
    // 让Gson帮我们解析出新闻总数
    @SerializedName("total")
    private int total;

    @SerializedName("data")
    private List<NewsData> data;

    public List<NewsData> getData() {
        return data;
    }

    public void setData(List<NewsData> data) {
        this.data = data;
    }

    // *** 新增的getter/setter ***
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
