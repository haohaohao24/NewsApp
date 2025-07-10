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
import com.java.haoyining.util.CategoryManager;
import com.java.haoyining.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class NewsListFragment extends Fragment {

    private HomeViewModel sharedViewModel;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String fragmentCategoryParam;
    private boolean isLoadingMore = false;

    public static NewsListFragment newInstance(String categoryName) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        String categoryParam = CategoryManager.FIXED_CATEGORY.equals(categoryName) ? "" : categoryName;
        args.putString("category_param", categoryParam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 从 Activity 获取共享的 ViewModel，与 HomeFragment 保持一致，彻底杜绝闪退
        sharedViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        if (getArguments() != null) {
            fragmentCategoryParam = getArguments().getString("category_param");
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsAdapter = new NewsAdapter(getContext(), new ArrayList<>(), newsData -> {
            Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (Objects.equals(fragmentCategoryParam, sharedViewModel.getActiveCategory())) {
                sharedViewModel.refresh();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && !isLoadingMore && Objects.equals(fragmentCategoryParam, sharedViewModel.getActiveCategory())) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null && lm.findLastCompletelyVisibleItemPosition() == newsAdapter.getItemCount() - 1) {
                        isLoadingMore = true;
                        sharedViewModel.loadMore();
                    }
                }
            }
        });
    }

    private void setupObservers() {
        sharedViewModel.getNewsList().observe(getViewLifecycleOwner(), newsList -> {
            if (Objects.equals(fragmentCategoryParam, sharedViewModel.getActiveCategory())) {
                newsAdapter.setNews(newsList);
            }
        });

        sharedViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Objects.equals(fragmentCategoryParam, sharedViewModel.getActiveCategory())) {
                swipeRefreshLayout.setRefreshing(isLoading);
                if (!isLoading) {
                    isLoadingMore = false;
                }
            }
        });

        sharedViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty() && isResumed() &&
                    Objects.equals(fragmentCategoryParam, sharedViewModel.getActiveCategory())) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // [最终修复] 通过“公开业务窗口”通知ViewModel，而不是自己动手
                sharedViewModel.onToastMessageShown();
            }
        });
    }
}
