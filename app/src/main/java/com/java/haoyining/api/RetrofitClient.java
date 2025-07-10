package com.java.haoyining.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit客户端单例类。
 * 为什么用单例？ 整个App中我们只需要一个Retrofit实例就够了，重复创建会浪费资源。
 * 单例模式保证了全局只有一个实例。
 */
public class RetrofitClient {
    // API的根地址
    private static final String BASE_URL = "https://api2.newsminer.net/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // 设置服务器根地址
                    // 添加Gson转换器，它会自动帮我们完成JSON和Java对象的转换
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
