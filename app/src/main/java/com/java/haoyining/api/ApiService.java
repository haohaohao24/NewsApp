package com.java.haoyining.api;

import com.java.haoyining.model.NewsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * 查询新闻列表
     * *** 这里是修改点 ***
     * 我们把接口文档里所有的查询参数都加上，让这个方法更完整、更健壮。
     * 即使暂时不用，也可以传入空字符串""。
     *
     * @param size 每页数量
     * @param page 页码
     * @param startDate 开始日期 (格式 yyyy-MM-dd HH:mm:ss)
     * @param endDate 结束日期 (格式 yyyy-MM-dd HH:mm:ss)
     * @param words 关键词
     * @param categories 分类
     * @return Retrofit的Call对象
     */

    @GET("svc/news/queryNewsList")
    Call<NewsResponse> getNews(
            @Query("size") int size,
            @Query("page") int page,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("words") String words,
            @Query("categories") String categories
    );

    // 在ApiService接口中添加
    @POST("api/v2/text/chat")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json; charset=UTF-8"
    })
    Call<GlmResponse> getGlmSummary(@Header("Authorization") String auth, @Body GlmRequest body);
}
