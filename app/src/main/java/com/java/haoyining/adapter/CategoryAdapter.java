package com.java.haoyining.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.java.haoyining.R;
import com.java.haoyining.model.Category;
import com.java.haoyining.util.CategoryManager; // 导入 CategoryManager
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener listener;
    private boolean isEditMode = false;

    public interface OnCategoryClickListener {
        void onCategoryClick(int position);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_drag, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textView.setText(category.name);

        // [修改] 检查是否是固定的 "全部" 分类
        boolean isFixed = CategoryManager.FIXED_CATEGORY.equals(category.name);

        if (isEditMode && category.isAdded && !isFixed) {
            holder.deleteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.deleteIcon.setVisibility(View.GONE);
        }

        if(!category.isAdded){
            holder.textView.setText(String.format("+ %s", category.name));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onCategoryClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView deleteIcon;

        CategoryViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.category_text);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}
