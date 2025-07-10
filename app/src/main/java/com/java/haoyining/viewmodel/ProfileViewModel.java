package com.java.haoyining.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.java.haoyining.db.AppDatabase;
import com.java.haoyining.db.NewsDao;
import com.java.haoyining.db.NewsEntity;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final NewsDao newsDao;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        newsDao = db.newsDao();
    }

    public LiveData<List<NewsEntity>> getHistoryNews() {
        return newsDao.getHistoryNews();
    }

    public LiveData<List<NewsEntity>> getFavoriteNews() {
        return newsDao.getFavoriteNews();
    }
}