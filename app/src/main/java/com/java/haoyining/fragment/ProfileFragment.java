package com.java.haoyining.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.java.haoyining.R;
import com.java.haoyining.activity.HistoryActivity; // **关键修复：导入正确的Activity**

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button historyButton = view.findViewById(R.id.btn_history);
        Button favoritesButton = view.findViewById(R.id.btn_favorites);

        historyButton.setOnClickListener(v -> {
            // **关键修复：调用正确的 HistoryActivity**
            HistoryActivity.start(requireContext(), HistoryActivity.TYPE_HISTORY);
        });

        favoritesButton.setOnClickListener(v -> {
            // **关键修复：调用正确的 HistoryActivity**
            HistoryActivity.start(requireContext(), HistoryActivity.TYPE_FAVORITES);
        });

        return view;
    }
}
