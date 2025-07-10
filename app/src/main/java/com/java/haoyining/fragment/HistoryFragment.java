package com.java.haoyining.fragment;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.java.haoyining.R;
import com.java.haoyining.activity.NewsDetailActivity;
import com.java.haoyining.adapter.NewsAdapter;
import com.java.haoyining.db.NewsEntity;
import com.java.haoyining.model.NewsData;
import com.java.haoyining.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

    private ProfileViewModel viewModel;
    private NewsAdapter newsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 因为ProfileViewModel继承自AndroidViewModel，需要Application上下文，所以必须这样创建
        Application application = requireActivity().getApplication();
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(application))
                .get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 确保加载的是包含Toolbar和RecyclerView的正确布局文件
        View view = inflater.inflate(R.layout.fragment_list_display, container, false);

        // 从加载的视图中查找控件
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        RecyclerView recyclerView = view.findViewById(R.id.list_recycler_view);

        // 设置Toolbar
        toolbar.setTitle("历史记录");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // 确保你有这个返回图标
        toolbar.setNavigationOnClickListener(v -> requireActivity().finish());

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsAdapter = new NewsAdapter(getContext(), new ArrayList<>(), newsData -> {
            Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        // 观察数据变化
        viewModel.getHistoryNews().observe(getViewLifecycleOwner(), this::updateUI);

        return view;
    }

    private void updateUI(List<NewsEntity> entities) {
        List<NewsData> newsDataList = entities.stream()
                .map(NewsEntity::toNewsData)
                .collect(Collectors.toList());
        newsAdapter.setNews(newsDataList);
    }
}
