package com.java.haoyining.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.haoyining.R;
import com.java.haoyining.activity.CategoryActivity;
import com.java.haoyining.adapter.CategoryPagerAdapter;
import com.java.haoyining.util.CategoryManager;
import com.java.haoyining.viewmodel.HomeViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ActivityResultLauncher<Intent> categoryLauncher;
    private TabLayoutMediator tabLayoutMediator;

    // [最终修复] 使用一个标志位来安全地触发刷新
    private boolean needsRefresh = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        categoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // [核心] 不在这里直接操作视图，只设置一个标志
                        // 这个标志会在此Fragment的onResume生命周期中被检查
                        needsRefresh = true;
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.news_view_pager);
        ImageView manageButton = view.findViewById(R.id.category_manage_button);

        manageButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CategoryActivity.class);
            categoryLauncher.launch(intent);
        });

        // 首次加载时，直接设置
        setupViewPager();
    }

    @Override
    public void onResume() {
        super.onResume();
        // [核心] 在 onResume 中检查标志位并执行刷新，这是最安全的时机
        if (needsRefresh) {
            needsRefresh = false; // 重置标志，防止重复刷新
            setupViewPager(); // 使用简化的硬刷新方法
        }
    }

    /**
     * [最终重构] 采用最简单、最可靠的“硬刷新”逻辑。
     * 此方法会彻底重建ViewPager和TabLayout，确保100%稳定。
     */
    private void setupViewPager() {
        // 1. 从存储中获取最新的分类列表
        List<String> currentCategories = CategoryManager.getUserCategories(requireContext())
                .stream()
                .map(c -> c.name)
                .collect(Collectors.toList());

        // 2. 创建一个全新的适配器
        CategoryPagerAdapter newAdapter = new CategoryPagerAdapter(this, currentCategories);
        viewPager.setAdapter(newAdapter);

        // 3. 重新连接TabLayout和ViewPager
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // 安全地从新适配器获取标题
                    String title = newAdapter.getCategoryAt(position);
                    if (title != null) {
                        tab.setText(title);
                    }
                }
        );
        tabLayoutMediator.attach();

        // 4. 清理旧的监听器，并注册新的
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        // 5. 确保ViewModel加载当前显示页面的数据
        // 因为是硬刷新，我们总是加载第一个标签页的数据
        if (!currentCategories.isEmpty()) {
            viewModel.loadCategory(currentCategories.get(0));
        }
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            if (viewPager == null) return;
            CategoryPagerAdapter adapter = (CategoryPagerAdapter) viewPager.getAdapter();
            if (adapter != null) {
                String selectedCategory = adapter.getCategoryAt(position);
                if (selectedCategory != null) {
                    viewModel.loadCategory(selectedCategory);
                }
            }
        }
    };

    @Override
    public void onDestroyView() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }
        // 清理引用，防止内存泄漏
        tabLayoutMediator = null;
        viewPager = null;
        tabLayout = null;
        super.onDestroyView();
    }
}
