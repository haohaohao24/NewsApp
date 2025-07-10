package com.java.haoyining.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.java.haoyining.R;
import com.java.haoyining.adapter.ImageSliderAdapter;
import com.java.haoyining.api.GlmRequest;
import com.java.haoyining.api.GlmResponse;
import com.java.haoyining.db.AppDatabase;
import com.java.haoyining.db.NewsEntity;
import com.java.haoyining.model.NewsData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class NewsDetailActivity extends AppCompatActivity {

    private TextView titleTextView, publisherTextView, timeTextView, contentTextView, imageIndicatorTextView;
    private ViewPager2 imageViewPager;
    private FrameLayout imageGalleryContainer;
    private FloatingActionButton fabFavorite;
    private AppDatabase db;
    private boolean isCurrentlyFavorite = false;
    private PlayerView playerView;
    private ExoPlayer player;
    private TextView summaryTextView;
    private ProgressBar summaryProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        db = AppDatabase.getDatabase(this);
        initViews();

        Intent intent = getIntent();
        NewsData newsData = (NewsData) intent.getSerializableExtra("news_data");

        if (newsData != null) {
            populateUI(newsData);
            markNewsAsRead(newsData);
            setupFavoriteButton(newsData);
            fetchRealAiSummary(newsData);
        } else {
            Toast.makeText(this, "无法加载新闻详情", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        titleTextView = findViewById(R.id.detail_title);
        publisherTextView = findViewById(R.id.detail_publisher);
        timeTextView = findViewById(R.id.detail_time);
        contentTextView = findViewById(R.id.detail_content);
        imageGalleryContainer = findViewById(R.id.image_gallery_container);
        imageViewPager = findViewById(R.id.image_view_pager);
        imageIndicatorTextView = findViewById(R.id.image_indicator);
        fabFavorite = findViewById(R.id.fab_favorite);
        playerView = findViewById(R.id.detail_video_player);
        summaryTextView = findViewById(R.id.detail_summary);
        summaryProgressBar = findViewById(R.id.summary_progress_bar);
    }

    private void setupFavoriteButton(final NewsData newsData) {
        db.newsDao().getNewsById(newsData.getNewsID()).observe(this, entity -> {
            if (entity != null) {
                isCurrentlyFavorite = entity.isFavorite();
                updateFabIcon();
            }
        });

        fabFavorite.setOnClickListener(v -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.newsDao().updateFavoriteStatus(newsData.getNewsID(), !isCurrentlyFavorite);
            });
        });
    }

    private void updateFabIcon() {
        if (isCurrentlyFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void markNewsAsRead(final NewsData newsData) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            NewsEntity existingEntity = db.newsDao().getNewsByIdSync(newsData.getNewsID());
            if (existingEntity != null) {
                if (!existingEntity.isRead()) {
                    existingEntity.setRead(true);
                    db.newsDao().update(existingEntity);
                }
            } else {
                NewsEntity newEntity = new NewsEntity(
                        newsData.getNewsID(), newsData.getTitle(), newsData.getPublisher(),
                        newsData.getPublishTime(), newsData.getImage(), newsData.getContent(), newsData.getVideo()
                );
                newEntity.setRead(true);
                db.newsDao().insert(newEntity);
            }
        });
    }

    private void populateUI(NewsData newsData) {
        titleTextView.setText(newsData.getTitle());
        publisherTextView.setText(newsData.getPublisher());
        timeTextView.setText(newsData.getPublishTime());

        String originalContent = newsData.getContent();
        if (originalContent != null && !originalContent.isEmpty()) {
            String processedContent = originalContent.trim().replaceAll("\\s*\\n\\s*", "\n\n\u3000\u3000");
            contentTextView.setText("\u3000\u3000" + processedContent);
        } else {
            contentTextView.setText("");
        }

        // **关键修改：调用新的去重方法**
        List<String> imageUrls = parseAndDeduplicateImageUrls(newsData.getImage());
        if (!imageUrls.isEmpty()) {
            imageGalleryContainer.setVisibility(View.VISIBLE);
            // **关键修改：为Adapter添加点击监听器**
            ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls, (position, urls) -> {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_URLS, new ArrayList<>(urls));
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_POSITION, position);
                startActivity(intent);
            });
            imageViewPager.setAdapter(adapter);

            if (imageUrls.size() > 1) {
                imageIndicatorTextView.setVisibility(View.VISIBLE);
                imageIndicatorTextView.setText(String.format(Locale.getDefault(), "1 / %d", imageUrls.size()));
                imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        imageIndicatorTextView.setText(String.format(Locale.getDefault(), "%d / %d", position + 1, imageUrls.size()));
                    }
                });
            } else {
                imageIndicatorTextView.setVisibility(View.GONE);
            }
        } else {
            imageGalleryContainer.setVisibility(View.GONE);
        }

        String videoUrl = newsData.getVideo();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            initializePlayer(videoUrl);
            playerView.setVisibility(View.VISIBLE);
        } else {
            playerView.setVisibility(View.GONE);
        }
    }

    // ... (省略 fetchRealAiSummary, callGlmApi, cacheSummary, initializePlayer, onStop 等未改变的方法)
    private void fetchRealAiSummary(final NewsData newsData) {
        summaryProgressBar.setVisibility(View.VISIBLE);
        summaryTextView.setVisibility(View.GONE);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            NewsEntity entity = db.newsDao().getNewsByIdSync(newsData.getNewsID());
            if (entity != null && entity.getSummary() != null && !entity.getSummary().isEmpty()) {
                runOnUiThread(() -> {
                    summaryTextView.setText(entity.getSummary());
                    summaryProgressBar.setVisibility(View.GONE);
                    summaryTextView.setVisibility(View.VISIBLE);
                });
            } else {
                callGlmApi(newsData);
            }
        });
    }

    private void callGlmApi(final NewsData newsData) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            String prompt = "请为以下新闻正文生成一段50字左右的摘要：" + newsData.getContent();
            GlmRequest.Message userMessage = new GlmRequest.Message("user", prompt);

            GlmRequest glmRequest = new GlmRequest("glm-4", Arrays.asList(userMessage), 0.95f, 0.7f, newsData.getNewsID());

            Gson gson = new Gson();
            String jsonBody = gson.toJson(glmRequest);

            MediaType mediaType = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, jsonBody);

            Request request = new Request.Builder()
                    .url("https://open.bigmodel.cn/api/paas/v4/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "6b25274f10dc4c6ca6fbba4d5a033e1d.etOevnTZz2dD8hbZ") // *** 注意：这里需要填入真实的API KEY ***
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (okhttp3.Response response = client.newCall(request).execute()) {
                final String summary;
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().string();
                    GlmResponse glmResponse = gson.fromJson(result, GlmResponse.class);
                    if (glmResponse != null && glmResponse.getChoices() != null && !glmResponse.getChoices().isEmpty()) {
                        summary = glmResponse.getChoices().get(0).getMessage().getContent();
                        cacheSummary(newsData.getNewsID(), summary);
                    } else {
                        summary = "AI摘要解析失败。";
                    }
                } else {
                    summary = "AI摘要获取失败，错误码: " + response.code();
                }

                runOnUiThread(() -> {
                    summaryProgressBar.setVisibility(View.GONE);
                    summaryTextView.setText(summary);
                    summaryTextView.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    summaryProgressBar.setVisibility(View.GONE);
                    summaryTextView.setText("网络错误，无法获取AI摘要。");
                    summaryTextView.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void cacheSummary(String newsId, String summary) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            NewsEntity entity = db.newsDao().getNewsByIdSync(newsId);
            if (entity != null) {
                entity.setSummary(summary);
                db.newsDao().update(entity);
            }
        });
    }

    private void initializePlayer(String videoUrl) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }


    /**
     * **关键新增方法：解析并去除重复的图片URL**
     * 使用LinkedHashSet来保证URL的唯一性，并维持插入顺序。
     */
    private List<String> parseAndDeduplicateImageUrls(String rawUrlString) {
        // 使用Set来自动处理重复项
        Set<String> urls = new LinkedHashSet<>();
        if (rawUrlString != null && !rawUrlString.trim().isEmpty() && !rawUrlString.trim().equals("[]")) {
            String cleaned = rawUrlString.replace("[", "").replace("]", "").replace("\"", "").trim();
            if (!cleaned.isEmpty()) {
                String[] urlArray = cleaned.split(",");
                for (String url : urlArray) {
                    String trimmedUrl = url.trim();
                    if (!trimmedUrl.isEmpty() && trimmedUrl.startsWith("http")) {
                        urls.add(trimmedUrl);
                    }
                }
            }
        }
        return new ArrayList<>(urls);
    }
}
