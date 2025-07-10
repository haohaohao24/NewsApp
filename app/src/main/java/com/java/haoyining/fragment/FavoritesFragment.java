package com.java.haoyining.fragment;

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

public class FavoritesFragment extends Fragment {

    private ProfileViewModel viewModel;
    private NewsAdapter newsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_display, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("我的收藏");
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        RecyclerView recyclerView = view.findViewById(R.id.list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        newsAdapter = new NewsAdapter(getContext(), new ArrayList<>(), newsData -> {
            Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        viewModel.getFavoriteNews().observe(getViewLifecycleOwner(), this::updateUI);

        return view;
    }

    private void updateUI(List<NewsEntity> entities) {
        List<NewsData> newsDataList = entities.stream().map(entity -> {
            NewsData data = new NewsData();
            data.setNewsID(entity.getNewsID());
            data.setTitle(entity.getTitle());
            data.setPublisher(entity.getPublisher());
            data.setPublishTime(entity.getPublishTime());
            data.setImage(entity.getImage());
            data.setContent(entity.getContent());
            data.setVideo(entity.getVideo());
            return data;
        }).collect(Collectors.toList());
        newsAdapter.setNews(newsDataList);
    }
}