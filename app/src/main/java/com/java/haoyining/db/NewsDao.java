package com.java.haoyining.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NewsEntity newsEntity);

    @Query("SELECT * FROM news_history WHERE newsID = :newsId")
    NewsEntity getNewsByIdSync(String newsId);

    @Query("SELECT * FROM news_history WHERE newsID = :newsId")
    LiveData<NewsEntity> getNewsById(String newsId);

    @Update
    void update(NewsEntity newsEntity);

    @Query("UPDATE news_history SET isFavorite = :isFavorite WHERE newsID = :newsId")
    void updateFavoriteStatus(String newsId, boolean isFavorite);

    @Query("SELECT * FROM news_history WHERE isFavorite = 1 ORDER BY publishTime DESC")
    LiveData<List<NewsEntity>> getFavoriteNews();

    @Query("SELECT * FROM news_history WHERE isRead = 1 ORDER BY publishTime DESC")
    LiveData<List<NewsEntity>> getHistoryNews();
}