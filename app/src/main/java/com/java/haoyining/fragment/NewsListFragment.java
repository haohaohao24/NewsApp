package com.java.haoyining.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.java.haoyining.R;
import com.java.haoyining.activity.NewsDetailActivity;
import com.java.haoyining.adapter.NewsAdapter;
import com.java.haoyining.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class NewsListFragment extends Fragment {

    private HomeViewModel sharedViewModel;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String fragmentCategory;

    public static NewsListFragment newInstance(String category) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        String categoryParam = "推荐".equals(category) ? "" : category;
        args.putString("category", categoryParam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从父Fragment(HomeFragment)获取共享的ViewModel
        sharedViewModel = new ViewModelProvider(requireParentFragment()).get(HomeViewModel.class);
        if (getArguments() != null) {
            fragmentCategory = getArguments().getString("category");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        setupViews(view);
        setupObservers();
        return view;
    }

    private void setupViews(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // *** 核心修改：点击时传递完整的NewsData对象 ***
        newsAdapter = new NewsAdapter(getContext(), new ArrayList<>(), newsData -> {
            Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
            // 我们现在传递整个对象，确保详情页能立刻显示内容
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        // ... (滚动和刷新监听保持不变) ...
    }

    private void setupObservers() {
        sharedViewModel.getNewsList().observe(getViewLifecycleOwner(), newsList -> {
            // *** 终极保险：只在分类匹配时才更新UI ***
            if (Objects.equals(fragmentCategory, sharedViewModel.getActiveCategory())) {
                newsAdapter.setNews(newsList);
            }
        });

        sharedViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Objects.equals(fragmentCategory, sharedViewModel.getActiveCategory())) {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });

        sharedViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            // 检查Fragment是否对用户可见，避免后台的Fragment也弹Toast
            if (message != null && !message.isEmpty() && isResumed() && Objects.equals(fragmentCategory, sharedViewModel.getActiveCategory())) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (newsAdapter != null) {
            newsAdapter.notifyDataSetChanged();
        }
    }
}
