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

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder> {

    private final Context context;
    private final List<String> imageUrls;
    private final OnImageClickListener listener;

    // **关键修改：定义一个新的点击监听接口**
    public interface OnImageClickListener {
        void onImageClick(int position, List<String> urls);
    }

    public ImageSliderAdapter(Context context, List<String> imageUrls, OnImageClickListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new ImageSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imageView);

        // **关键修改：使用新的监听器**
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(holder.getAdapterPosition(), imageUrls);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageSliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image_view);
        }
    }
}
