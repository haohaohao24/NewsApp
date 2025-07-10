package com.java.haoyining.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.java.haoyining.api.ApiService;
import com.java.haoyining.api.RetrofitClient;
import com.java.haoyining.model.NewsData;
import com.java.haoyining.model.NewsResponse;
import com.java.haoyining.util.CategoryManager;

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

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<NewsData>> newsListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final Map<String, CategoryDataState> categoryCache = new HashMap<>();

    // [核心改造] activeCategory现在存储的是API请求参数（如“娱乐”或“”），而不是UI显示的名称
    private String activeCategoryParam = null;
    private Call<NewsResponse> currentCall;

    private static class CategoryDataState {
        List<NewsData> newsList = new ArrayList<>();
        int currentPage = 1;
        boolean hasLoadedAll = false;
        void reset() {
            newsList.clear();
            currentPage = 1;
            hasLoadedAll = false;
        }
    }

    // --- 公开给UI层使用的方法 ---
    public LiveData<List<NewsData>> getNewsList() { return newsListLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public String getActiveCategory() { return activeCategoryParam; }

    public void onToastMessageShown() {
        toastMessage.setValue(null);
    }

    /**
     * 当用户切换分类时，由HomeFragment调用
     * @param categoryName UI上显示的分类名，如 "全部", "娱乐"
     */
    public void loadCategory(String categoryName) {
        String categoryParam = CategoryManager.FIXED_CATEGORY.equals(categoryName) ? "" : categoryName;

        // 如果分类没变，且已有数据，则不重复加载
        if (Objects.equals(categoryParam, activeCategoryParam) && newsListLiveData.getValue() != null && !newsListLiveData.getValue().isEmpty()) {
            return;
        }

        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }

        this.activeCategoryParam = categoryParam;
        CategoryDataState state = categoryCache.get(categoryParam);

        if (state != null && !state.newsList.isEmpty()) {
            newsListLiveData.setValue(new ArrayList<>(state.newsList));
        } else {
            refresh();
        }
    }

    public void refresh() {
        if (activeCategoryParam == null) return;
        CategoryDataState state = categoryCache.get(activeCategoryParam);
        if (state != null) {
            state.reset();
        }
        fetchNews(1);
    }

    public void loadMore() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || activeCategoryParam == null) return;

        CategoryDataState state = categoryCache.get(activeCategoryParam);
        if (state != null && state.hasLoadedAll) {
            toastMessage.setValue("已经到底啦！");
            return;
        }
        int nextPage = (state != null) ? state.currentPage + 1 : 1;
        fetchNews(nextPage);
    }

    private void fetchNews(final int page) {
        isLoading.setValue(true);

        if (page == 1) {
            categoryCache.put(activeCategoryParam, new CategoryDataState());
        }
        final CategoryDataState state = categoryCache.get(activeCategoryParam);
        if (state == null) {
            isLoading.setValue(false);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        final String categoryToFetch = this.activeCategoryParam;
        currentCall = apiService.getNews(15, page, "", endDate, "", categoryToFetch);
        currentCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (call.isCanceled() || !Objects.equals(categoryToFetch, activeCategoryParam)) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsData> fetchedNews = response.body().getData();
                    if (page == 1) {
                        state.newsList.clear();
                    }
                    if (fetchedNews != null) {
                        state.newsList.addAll(fetchedNews);
                    }

                    state.currentPage = page;
                    if (fetchedNews == null || fetchedNews.isEmpty() || fetchedNews.size() < 15) {
                        state.hasLoadedAll = true;
                    }

                    newsListLiveData.setValue(new ArrayList<>(state.newsList));

                } else {
                    toastMessage.setValue("加载失败，请稍后重试");
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading.setValue(false);
                toastMessage.setValue("网络连接失败");
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
