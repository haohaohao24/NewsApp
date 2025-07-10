package com.java.haoyining.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.java.haoyining.model.Category;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryManager {
    private static final String PREFS_NAME = "news_app_prefs";
    private static final String KEY_USER_CATEGORIES = "user_categories";

    // 这是所有可供用户选择的分类
    private static final List<String> ALL_AVAILABLE_CATEGORIES = Arrays.asList(
            "娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"
    );

    public static final String FIXED_CATEGORY = "全部";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveUserCategories(Context context, List<Category> categories) {
        List<String> categoryNames = categories.stream().map(c -> c.name).collect(Collectors.toList());
        String json = new Gson().toJson(categoryNames);
        getPrefs(context).edit().putString(KEY_USER_CATEGORIES, json).apply();
    }

    public static List<Category> getUserCategories(Context context) {
        String json = getPrefs(context).getString(KEY_USER_CATEGORIES, null);
        List<String> userCategoryNames;
        if (json == null) {
            // [核心修复] 首次启动时，默认添加所有可用分类
            userCategoryNames = new ArrayList<>(ALL_AVAILABLE_CATEGORIES);
            // 并确保 "全部" 分类在第一位
            userCategoryNames.add(0, FIXED_CATEGORY);
        } else {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            userCategoryNames = new Gson().fromJson(json, type);
            // [健壮性检查] 确保 "全部" 分类始终存在且在第一位
            if (userCategoryNames.contains(FIXED_CATEGORY)) {
                userCategoryNames.remove(FIXED_CATEGORY);
            }
            userCategoryNames.add(0, FIXED_CATEGORY);
        }
        return userCategoryNames.stream().map(name -> new Category(name, true)).collect(Collectors.toList());
    }

    public static List<Category> getOtherCategories(Context context) {
        List<String> userCategoryNames = getUserCategories(context).stream().map(c -> c.name).collect(Collectors.toList());
        List<Category> otherCategories = new ArrayList<>();
        // [核心修复] 确保 "全部" 分类不会出现在“更多分类”中
        for (String catName : ALL_AVAILABLE_CATEGORIES) {
            if (!userCategoryNames.contains(catName)) {
                otherCategories.add(new Category(catName, false));
            }
        }
        return otherCategories;
    }
}
