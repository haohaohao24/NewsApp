package com.java.haoyining.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.java.haoyining.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MaterialCardView historyCard = view.findViewById(R.id.card_history);
        MaterialCardView favoritesCard = view.findViewById(R.id.card_favorites);

        historyCard.setOnClickListener(v -> {
            // 使用Fragment管理器进行跳转
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new HistoryFragment())
                    .addToBackStack(null) // 允许用户按返回键回到这个页面
                    .commit();
        });

        favoritesCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new FavoritesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}