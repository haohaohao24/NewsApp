package com.java.haoyining.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.java.haoyining.R;

import java.util.List;

public class FullScreenImageAdapter extends RecyclerView.Adapter<FullScreenImageAdapter.FullScreenViewHolder> {

    private final Context context;
    private final List<String> imageUrls;
    private final View.OnClickListener onClickListener;

    public FullScreenImageAdapter(Context context, List<String> imageUrls, View.OnClickListener onClickListener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public FullScreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 全屏预览的布局很简单，只有一个ImageView，所以可以复用item_image_slider
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new FullScreenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenViewHolder holder, int position) {
        Glide.with(context)
                .load(imageUrls.get(position))
                .fitCenter() // 确保图片完整显示
                .into(holder.imageView);
        // 设置点击监听，用于退出预览
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class FullScreenViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public FullScreenViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image_view);
        }
    }
}
