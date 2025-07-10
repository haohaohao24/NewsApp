package com.java.haoyining.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.haoyining.R;
import com.java.haoyining.adapter.CategoryPagerAdapter;
import com.java.haoyining.viewmodel.HomeViewModel;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private final List<String> categories = Arrays.asList(
            "推荐", "娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用默认的Provider创建ViewModel，因为我们的ViewModel现在是无参构造了
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.news_view_pager);

        CategoryPagerAdapter adapter = new CategoryPagerAdapter(this, categories);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(categories.get(position))
        ).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String selectedCategory = categories.get(position);
                String categoryParam = "推荐".equals(selectedCategory) ? "" : selectedCategory;
                viewModel.loadCategory(categoryParam);
            }
        });

        // 首次进入时，手动加载第一个分类
        if (savedInstanceState == null && viewModel.getActiveCategory() == null) {
            viewModel.loadCategory(""); // "推荐"分类对应空字符串
        }

        return view;
    }
}
