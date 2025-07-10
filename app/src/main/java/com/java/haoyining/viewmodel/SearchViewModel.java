package com.java.haoyining.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.java.haoyining.api.ApiService;
import com.java.haoyining.api.RetrofitClient;
import com.java.haoyining.model.NewsData;
import com.java.haoyining.model.NewsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<NewsData>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<List<NewsData>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void searchNews(int size, int page, String startDate, String endDate, String words, String category) {
        isLoading.setValue(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        String finalCategory = "全部".equals(category) ? "" : category;

        apiService.getNews(size, page, startDate, endDate, words, finalCategory).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.setValue(response.body().getData());
                } else {
                    searchResults.setValue(null);
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                searchResults.setValue(null);
                isLoading.setValue(false);
            }
        });
    }
}