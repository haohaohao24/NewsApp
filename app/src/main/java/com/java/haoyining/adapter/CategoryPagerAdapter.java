package com.java.haoyining.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.java.haoyining.fragment.NewsListFragment;

import java.util.List;

/**
 * 用于首页新闻分类的ViewPager2适配器
 */
public class CategoryPagerAdapter extends FragmentStateAdapter {

    private final List<String> categories;

    /**
     * *** 这里是修复编译错误的关键 ***
     * 构造函数接收一个Fragment作为父级，而不是FragmentActivity。
     * 这对于在Fragment内部(HomeFragment)创建Adapter是正确的做法。
     */
    public CategoryPagerAdapter(@NonNull Fragment fragment, List<String> categories) {
        super(fragment);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return NewsListFragment.newInstance(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
