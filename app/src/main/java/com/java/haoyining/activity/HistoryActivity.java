package com.java.haoyining.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.java.haoyining.R;
import com.java.haoyining.adapter.NewsAdapter;
import com.java.haoyining.db.NewsEntity;
import com.java.haoyining.model.NewsData;
import com.java.haoyining.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 这是一个简化的Activity，它自己处理所有逻辑，以减少由Fragment引起的潜在错误。
public class HistoryActivity extends AppCompatActivity {

    public static final String EXTRA_PAGE_TYPE = "page_type";
    public static final String TYPE_HISTORY = "history";
    public static final String TYPE_FAVORITES = "favorites";

    private ProfileViewModel viewModel;
    private NewsAdapter newsAdapter;

    public static void start(Context context, String pageType) {
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra(EXTRA_PAGE_TYPE, pageType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用一个为此Activity专门准备的布局文件
        setContentView(R.layout.activity_history);

        // --- 步骤1：获取页面类型并设置Toolbar ---
        String pageType = getIntent().getStringExtra(EXTRA_PAGE_TYPE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // 确保你有这个返回图标
        toolbar.setNavigationOnClickListener(v -> finish()); // 点击返回按钮，关闭当前Activity

        // --- 步骤2：设置RecyclerView ---
        RecyclerView recyclerView = findViewById(R.id.list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, new ArrayList<>(), newsData -> {
            Intent intent = new Intent(this, NewsDetailActivity.class);
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        // --- 步骤3：设置ViewModel并观察数据 ---
        // 这是在Activity中创建AndroidViewModel的正确方式
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        LiveData<List<NewsEntity>> dataToObserve;
        if (TYPE_HISTORY.equals(pageType)) {
            getSupportActionBar().setTitle("历史记录");
            dataToObserve = viewModel.getHistoryNews();
        } else {
            getSupportActionBar().setTitle("我的收藏");
            dataToObserve = viewModel.getFavoriteNews();
        }

        // 观察数据库的数据变化
        dataToObserve.observe(this, entities -> {
            if (entities != null) {
                List<NewsData> newsDataList = entities.stream()
                        .map(NewsEntity::toNewsData)
                        .collect(Collectors.toList());
                newsAdapter.setNews(newsDataList);
            }
        });
    }
}
