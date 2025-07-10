package com.java.haoyining.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.java.haoyining.R;
import com.java.haoyining.db.AppDatabase;
import com.java.haoyining.db.NewsEntity;
import com.java.haoyining.model.NewsData;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<NewsData> newsList;
    private final OnItemClickListener listener;

    // *** 核心修改 1：接口现在传递完整的NewsData对象 ***
    public interface OnItemClickListener {
        void onItemClick(NewsData newsData);
    }

    public NewsAdapter(Context context, List<NewsData> newsList, OnItemClickListener listener) {
        this.context = context;
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsData newsItem = newsList.get(position);
        holder.bind(newsItem, context, listener);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void setNews(List<NewsData> news) {
        this.newsList.clear();
        this.newsList.addAll(news);
        notifyDataSetChanged();
    }

    public void addNews(List<NewsData> news) {
        int startPosition = this.newsList.size();
        this.newsList.addAll(news);
        notifyItemRangeInserted(startPosition, news.size());
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, sourceTextView, timeTextView;
        ImageView newsImageView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.news_title);
            sourceTextView = itemView.findViewById(R.id.news_source);
            timeTextView = itemView.findViewById(R.id.news_time);
            newsImageView = itemView.findViewById(R.id.news_image);
        }

        public void bind(final NewsData newsData, final Context context, final OnItemClickListener listener) {
            titleTextView.setText(newsData.getTitle());
            sourceTextView.setText(newsData.getPublisher());
            // 直接设置时间，不再进行验证
            timeTextView.setText(newsData.getPublishTime());
            timeTextView.setVisibility(View.VISIBLE);

            titleTextView.setTextColor(Color.BLACK);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                NewsEntity entity = AppDatabase.getDatabase(context).newsDao().getNewsByIdSync(newsData.getNewsID());
                if (entity != null && entity.isRead()) {
                    itemView.post(() -> titleTextView.setTextColor(Color.GRAY));
                }
            });

            String finalImageUrl = parseFirstImageUrl(newsData.getImage());
            if (finalImageUrl != null) {
                newsImageView.setVisibility(View.VISIBLE);
                Glide.with(context).load(finalImageUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        newsImageView.setVisibility(View.GONE);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        newsImageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(newsImageView);
            } else {
                newsImageView.setVisibility(View.GONE);
            }

            // *** 核心修改 2：点击事件传递完整的NewsData对象 ***
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(newsData);
                }
            });
        }

        private boolean isValidDate(String dateStr) {
            if (dateStr == null || dateStr.length() < 4) {
                return false;
            }
            try {
                int year = Integer.parseInt(dateStr.substring(0, 4));
                return year > 1970 && year < 2030;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private String parseFirstImageUrl(String rawUrlString) {
            if (rawUrlString == null || rawUrlString.trim().isEmpty() || rawUrlString.trim().equals("[]")) {
                return null;
            }
            String cleaned = rawUrlString.replace("[", "").replace("]", "").replace("\"", "").trim();
            if (cleaned.isEmpty()) {
                return null;
            }
            String[] urls = cleaned.split(",");
            if (urls.length > 0) {
                String firstUrl = urls[0].trim();
                if (firstUrl.startsWith("http")) {
                    return firstUrl;
                }
            }
            return null;
        }
    }
}
