package com.java.haoyining.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.java.haoyining.R;
import com.java.haoyining.adapter.CategoryAdapter;
import com.java.haoyining.model.Category;
import com.java.haoyining.util.CategoryManager;
import java.util.Collections;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView userRv, otherRv;
    private CategoryAdapter userAdapter, otherAdapter;
    private List<Category> userCategories, otherCategories;
    private boolean isEditMode = false;
    private boolean hasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);
        // 显示返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        userCategories = CategoryManager.getUserCategories(this);
        otherCategories = CategoryManager.getOtherCategories(this);

        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        userRv = findViewById(R.id.user_category_rv);
        otherRv = findViewById(R.id.other_category_rv);

        userAdapter = new CategoryAdapter(userCategories, position -> {
            // 只有在编辑模式下，并且点击的不是 "全部" 分类，才执行删除操作
            if (isEditMode && !CategoryManager.FIXED_CATEGORY.equals(userCategories.get(position).name)) {
                moveCategory(userAdapter, otherAdapter, position);
            }
        });

        otherAdapter = new CategoryAdapter(otherCategories, position -> {
            // 点击下方分类，直接添加到上方
            moveCategory(otherAdapter, userAdapter, position);
        });

        userRv.setLayoutManager(new GridLayoutManager(this, 4));
        userRv.setAdapter(userAdapter);

        otherRv.setLayoutManager(new GridLayoutManager(this, 4));
        otherRv.setAdapter(otherAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 只允许在编辑模式下，对“我的分类”列表进行拖拽
                if (recyclerView.getAdapter() != userAdapter || !isEditMode) return makeMovementFlags(0, 0);
                // “全部”分类（位置0）不允许拖动
                if (viewHolder.getAdapterPosition() == 0) return makeMovementFlags(0, 0);
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                // 不允许拖动到“全部”分类的位置
                if (toPosition == 0) return false;
                Collections.swap(userCategories, fromPosition, toPosition);
                userAdapter.notifyItemMoved(fromPosition, toPosition);
                hasChanged = true;
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(userRv);
    }

    private void moveCategory(CategoryAdapter fromAdapter, CategoryAdapter toAdapter, int fromPosition) {
        hasChanged = true;
        List<Category> fromList = (fromAdapter == userAdapter) ? userCategories : otherCategories;
        List<Category> toList = (toAdapter == userAdapter) ? userCategories : otherCategories;

        Category category = fromList.get(fromPosition);
        fromList.remove(fromPosition);
        fromAdapter.notifyItemRemoved(fromPosition);

        toList.add(category);
        toAdapter.notifyItemInserted(toList.size() - 1);
    }

    // [核心] 加载菜单布局，让“编辑”按钮显示出来
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    // [核心] 在显示菜单前，动态修改按钮文字（“编辑”或“完成”）
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.action_edit);
        if (editItem != null) {
            editItem.setTitle(isEditMode ? "完成" : "编辑");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // [核心] 处理“编辑/完成”按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            isEditMode = !isEditMode;
            invalidateOptionsMenu(); // 通知系统重新调用 onPrepareOptionsMenu 来更新按钮文字
            userAdapter.setEditMode(isEditMode); // 通知适配器进入或退出编辑模式
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // [核心] 在关闭页面时，如果数据有变动，则保存并通知主页刷新
    @Override
    public void finish() {
        if (hasChanged) {
            CategoryManager.saveUserCategories(this, userCategories);
            setResult(RESULT_OK); // 设置一个成功的结果码
        } else {
            setResult(RESULT_CANCELED); // 设置一个取消的结果码
        }
        super.finish();
    }
}