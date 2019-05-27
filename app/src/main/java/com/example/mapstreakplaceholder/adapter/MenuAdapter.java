package com.example.mapstreakplaceholder.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;


public class MenuAdapter extends ArrayAdapter<String> {

    private static final String[] MENU_ITEMS = {"Map", "Profile", "High scores", "Logout"};

    public MenuAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId, MENU_ITEMS);
    }

    public static String[] getMenuItems() {
        return MENU_ITEMS;
    }
}
