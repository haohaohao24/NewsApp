package com.java.haoyining.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.java.haoyining.R;
import com.java.haoyining.adapter.NewsAdapter;
import com.java.haoyining.viewmodel.SearchViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private SearchViewModel viewModel;
    private EditText keywordEditText;
    private Spinner categorySpinner;
    private TextView startDateTextView, endDateTextView;
    private Button searchButton;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout searchControlsContainer;

    private String startDate = "";
    private String endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        initViews();
        setupCategorySpinner();
        setupDatePicker();
        setupRecyclerView();
        setupObservers();

        searchButton.setOnClickListener(v -> performSearch());
    }

    private void initViews() {
        keywordEditText = findViewById(R.id.search_keyword);
        categorySpinner = findViewById(R.id.search_category_spinner);
        startDateTextView = findViewById(R.id.search_start_date);
        endDateTextView = findViewById(R.id.search_end_date);
        searchButton = findViewById(R.id.search_button);
        recyclerView = findViewById(R.id.search_recycler_view);
        swipeRefreshLayout = findViewById(R.id.search_swipe_refresh);
        searchControlsContainer = findViewById(R.id.search_controls_container);
    }

    private void setupCategorySpinner() {
        String[] categories = {"全部", "娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupDatePicker() {
        startDateTextView.setOnClickListener(v -> showDatePickerDialog(true));
        endDateTextView.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isStartDate) {
                this.startDate = date;
                startDateTextView.setText(date);
            } else {
                this.endDate = date;
                endDateTextView.setText(date);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter(this, new ArrayList<>(), newsData -> {
            Intent intent = new Intent(this, NewsDetailActivity.class);
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refresh());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        viewModel.loadMore();
                    }
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(this, newsDataList -> {
            newsAdapter.setNews(newsDataList);
            if (newsDataList != null && !newsDataList.isEmpty() && searchControlsContainer.getVisibility() == View.VISIBLE) {
                searchControlsContainer.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
            searchButton.setEnabled(!isLoading);
        });

        viewModel.getToastMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(keywordEditText.getWindowToken(), 0);
        }

        String keyword = keywordEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        String finalEndDate = this.endDate;

        // --- 关键修复：处理各种空搜索的情况，确保endDate的正确性 ---
        boolean noKeywords = keyword.isEmpty();
        boolean noStartDate = this.startDate.isEmpty();
        boolean noEndDate = finalEndDate.isEmpty();
        boolean isAllCategory = "全部".equals(category);

        // Case 1: 所有都为空 (空搜索) -> 查最新
        // Case 2: 只选了分类，其他都为空 -> 查该分类最新，以匹配首页行为
        if (noKeywords && noStartDate && noEndDate && !isAllCategory) {
            finalEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            Toast.makeText(this, "查找“" + category + "”分类最新新闻", Toast.LENGTH_SHORT).show();
        } else if (noKeywords && noStartDate && noEndDate && isAllCategory) {
            finalEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            Toast.makeText(this, "未指定条件，载入最新新闻", Toast.LENGTH_SHORT).show();
        } else {
            // 对于其他搜索情况，如果用户只选了日期没选时间，则补充到当天最后一秒
            if (!finalEndDate.isEmpty() && !finalEndDate.contains(":")) {
                finalEndDate += " 23:59:59";
            }
        }
        // --- 修复结束 ---

        viewModel.performNewSearch(this.startDate, finalEndDate, keyword, category);
    }

    @Override
    public void onBackPressed() {
        if (searchControlsContainer.getVisibility() == View.GONE) {
            searchControlsContainer.setVisibility(View.VISIBLE);
            viewModel.clearSearchResults();
        } else {
            super.onBackPressed();
        }
    }
}
