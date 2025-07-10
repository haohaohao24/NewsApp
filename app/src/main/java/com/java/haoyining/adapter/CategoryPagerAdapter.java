package com.java.haoyining.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.java.haoyining.fragment.NewsListFragment;
import java.util.List;

public class CategoryPagerAdapter extends FragmentStateAdapter {

    private final List<String> categories;

    public CategoryPagerAdapter(@NonNull Fragment fragment, List<String> categories) {
        super(fragment);
        this.categories = categories;
    }

    /**
     * 公共方法，用于安全地获取指定位置的分类名称。
     */
    public String getCategoryAt(int position) {
        if (position >= 0 && position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 直接从内部列表中获取分类，确保数据最新
        return NewsListFragment.newInstance(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
