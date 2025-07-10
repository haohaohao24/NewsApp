package com.java.haoyining.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.java.haoyining.R;
import com.java.haoyining.adapter.NewsAdapter;
import com.java.haoyining.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {

    private SearchViewModel viewModel;
    private EditText keywordEditText;
    private Spinner categorySpinner;
    private TextView startDateTextView, endDateTextView;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NewsAdapter newsAdapter;

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
        progressBar = findViewById(R.id.search_progress_bar);
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
            String date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isStartDate) {
                startDate = date;
                startDateTextView.setText(date);
            } else {
                endDate = date;
                endDateTextView.setText(date);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, new ArrayList<>(), newsData -> {
            Intent intent = new Intent(this, NewsDetailActivity.class);
            intent.putExtra("news_data", newsData);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(this, newsDataList -> {
            if (newsDataList != null) {
                newsAdapter.setNews(newsDataList);
                if (newsDataList.isEmpty()) {
                    Toast.makeText(this, "没有找到相关新闻", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "搜索失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            searchButton.setEnabled(!isLoading);
        });
    }

    private void performSearch() {
        String keyword = keywordEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        String finalEndDate = endDate.isEmpty() ? "" : endDate + " 23:59:59";

        viewModel.searchNews(50, 1, startDate, finalEndDate, keyword, category);
    }
}