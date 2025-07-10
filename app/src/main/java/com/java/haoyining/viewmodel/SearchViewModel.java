package com.java.haoyining.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.java.haoyining.api.ApiService;
import com.java.haoyining.api.RetrofitClient;
import com.java.haoyining.model.NewsData;
import com.java.haoyining.model.NewsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<NewsData>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    private String currentWords = "", currentCategory = "", currentStartDate = "", currentEndDate = "";
    private int currentPage = 1;
    private boolean hasLoadedAll = false;
    private Call<NewsResponse> currentCall;

    public LiveData<List<NewsData>> getSearchResults() {
        return searchResults;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void clearSearchResults() {
        searchResults.setValue(new ArrayList<>());
    }

    public void performNewSearch(String startDate, String endDate, String words, String category) {
        this.currentStartDate = startDate;
        this.currentEndDate = endDate;
        this.currentWords = words;
        this.currentCategory = "全部".equals(category) ? "" : category;

        this.currentPage = 1;
        this.hasLoadedAll = false;
        this.searchResults.setValue(new ArrayList<>());
        fetchNews(true);
    }

    public void refresh() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;
        this.currentPage = 1;
        this.hasLoadedAll = false;
        fetchNews(true);
    }

    public void loadMore() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || hasLoadedAll) {
            if(hasLoadedAll){
                toastMessage.postValue("已经到底啦！");
            }
            return;
        }
        fetchNews(false);
    }

    private void fetchNews(final boolean isRefresh) {
        isLoading.setValue(true);

        if (!isRefresh) {
            currentPage++;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // --- 关键修复：调整关键词逻辑 ---
        // 只有当用户同时输入了关键词并选择了分类时，我们才将分类附加到关键词后。
        // 如果用户只选择了分类，关键词(words)将保持为空，确保API调用与首页分类模块完全一致。
        String finalWords = this.currentWords;
        if (!this.currentCategory.isEmpty() && !this.currentWords.isEmpty()) {
            finalWords = this.currentWords + " " + this.currentCategory;
        }
        // --- 修复结束 ---

        if (currentCall != null) {
            currentCall.cancel();
        }

        currentCall = apiService.getNews(15, currentPage, currentStartDate, currentEndDate, finalWords, currentCategory);
        currentCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (call.isCanceled()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<NewsData> fetchedNews = response.body().getData();
                    List<NewsData> currentList = searchResults.getValue();
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                    }

                    if (isRefresh) {
                        currentList.clear();
                    }
                    currentList.addAll(fetchedNews);
                    searchResults.setValue(currentList);

                    if (fetchedNews.isEmpty() || currentList.size() >= response.body().getTotal()) {
                        hasLoadedAll = true;
                    }

                } else {
                    if (!isRefresh) currentPage--;
                    toastMessage.postValue("加载失败");
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                if (!isRefresh) currentPage--;
                isLoading.setValue(false);
                toastMessage.postValue("网络错误");
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
