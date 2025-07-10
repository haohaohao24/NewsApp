package com.java.haoyining.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.java.haoyining.R;
import com.java.haoyining.adapter.FullScreenImageAdapter;

import java.util.ArrayList;

public class FullScreenImageActivity extends AppCompatActivity {

    // 用于从Intent中获取数据的键
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_IMAGE_POSITION = "image_position";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // 从布局中获取ViewPager2和关闭按钮
        ViewPager2 viewPager = findViewById(R.id.full_screen_view_pager);
        ImageButton closeButton = findViewById(R.id.close_button);

        // 从启动该Activity的Intent中获取图片URL列表和初始位置
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        int currentPosition = getIntent().getIntExtra(EXTRA_IMAGE_POSITION, 0);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            // 创建并设置适配器 (Adapter)
            // 我们将一个点击监听器传递给Adapter，当用户点击任何一张图片时，这个监听器会被调用
            FullScreenImageAdapter adapter = new FullScreenImageAdapter(this, imageUrls, v -> finish());
            viewPager.setAdapter(adapter);

            // 让ViewPager2直接显示用户点击的那张图片
            viewPager.setCurrentItem(currentPosition, false);
        } else {
            // 如果没有有效的图片数据，则提示用户并关闭页面
            Toast.makeText(this, "无法加载图片", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 为关闭按钮设置点击事件，点击后关闭当前Activity
        closeButton.setOnClickListener(v -> finish());
    }

    // 覆盖物理返回键的默认行为，确保它也能关闭当前Activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
