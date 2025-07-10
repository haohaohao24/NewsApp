package com.java.haoyining.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.java.haoyining.api.ApiService;
import com.java.haoyining.api.RetrofitClient;
import com.java.haoyining.model.NewsData;
import com.java.haoyining.model.NewsResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 共享的、唯一的“中央数据大脑”
 * 它拥有一个无参数的构造函数，可以被ViewModelProvider正确创建。
 */
public class HomeViewModel extends ViewModel {

    // 这些LiveData是给UI看的“公告板”
    private final MutableLiveData<List<NewsData>> newsListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    // 这是“中央大脑”的“记忆柜”，用Map给每个分类一个专属“小抽屉”
    private final Map<String, CategoryDataState> categoryCache = new HashMap<>();
    private String activeCategory = null; // 当前激活的分类
    private Call<NewsResponse> currentCall; // 当前正在进行的网络请求

    // 内部类，定义了每个“小抽屉”里要存放的东西
    private static class CategoryDataState {
        List<NewsData> newsList = new ArrayList<>();
        int currentPage = 1;
        int totalCount = 0;
        boolean hasLoadedAll = false;
    }

    // --- 公开给UI层使用的方法 ---
    public LiveData<List<NewsData>> getNewsList() { return newsListLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public String getActiveCategory() { return activeCategory; }

    /**
     * 当用户切换分类时，由HomeFragment调用
     */
    public void loadCategory(String category) {
        if (Objects.equals(category, activeCategory)) return; // 如果是同一个分类，不重复加载

        // 取消上一个分类还没完成的请求，防止数据串线
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }

        this.activeCategory = category;
        CategoryDataState state = categoryCache.get(category);

        if (state != null) {
            // 如果这个分类的“小抽屉”里有数据，直接显示
            newsListLiveData.setValue(state.newsList);
        } else {
            // 如果是全新的分类，加载第一页
            refresh();
        }
    }

    public void refresh() {
        fetchNews(1);
    }

    public void loadMore() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;

        CategoryDataState state = categoryCache.get(activeCategory);
        if (state != null && state.hasLoadedAll) {
            toastMessage.setValue("已经到底啦！");
            return;
        }
        int nextPage = (state != null) ? state.currentPage + 1 : 1;
        fetchNews(nextPage);
    }

    private void fetchNews(final int page) {
        isLoading.setValue(true);

        // 如果是第一页，就创建一个新的“小抽屉”
        if (page == 1) {
            categoryCache.put(activeCategory, new CategoryDataState());
        }
        final CategoryDataState state = categoryCache.get(activeCategory);
        if (state == null) { // 安全检查
            isLoading.setValue(false);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        final String categoryToFetch = this.activeCategory;
        currentCall = apiService.getNews(15, page, "", endDate, "", categoryToFetch);
        currentCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (call.isCanceled() || !Objects.equals(categoryToFetch, activeCategory)) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsData> fetchedNews = response.body().getData();
                    if (fetchedNews != null && !fetchedNews.isEmpty()) {
                        state.newsList.addAll(fetchedNews);
                        state.totalCount = response.body().getTotal();
                        state.currentPage = page;
                        if (state.newsList.size() >= state.totalCount) {
                            state.hasLoadedAll = true;
                        }
                    } else {
                        state.hasLoadedAll = true; // 没有返回数据，也认为到底了
                    }
                    newsListLiveData.setValue(new ArrayList<>(state.newsList));
                } else {
                    toastMessage.setValue("加载失败");
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading.setValue(false);
                toastMessage.setValue("网络错误");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
