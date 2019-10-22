
package com.arke.sdk.ui.adapters;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.arke.sdk.R;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.utilities.FontUtils;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;

import java.util.ArrayList;

/**
 * @author Wan Clem
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;
    private LayoutInflater layoutInflater;
    private Context context;

    @SuppressWarnings("deprecation")
    public PagerAdapter(Context context, FragmentManager fm, ArrayList<Fragment> fragments, ArrayList<String> titles) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
        this.titles = titles;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public View getCustomTabView(int pos) {
        @SuppressWarnings("InflateParams")
        View view = layoutInflater.inflate(R.layout.tab_custom_view, null);
        TextView tabTitle = getTextView(view);
        Typeface typeface = FontUtils.selectTypeface(context, 1);
        tabTitle.setTypeface(typeface);
        tabTitle.setText(getPageTitle(pos));
        return view;
    }

    private TextView getTextView(View view) {
        int primaryColor = AppPrefs.getPrimaryColor();
        TextView tabTitle = view.findViewById(R.id.tab_title);
        if (UiUtils.whitish(primaryColor)) {
            int[][] states = new int[][]{new int[]{android.R.attr.state_selected}, new int[]{}};
            int[] colors = new int[]{Color.BLACK, ContextCompat.getColor(context, R.color.ease_gray)};
            tabTitle.setTextColor(new ColorStateList(states, colors));
        } else {
            int[][] states = new int[][]{new int[]{android.R.attr.state_selected}, new int[]{}};
            int[] colors = new int[]{Color.WHITE, ContextCompat.getColor(context, R.color.gery_inactive)};
            tabTitle.setTextColor(new ColorStateList(states, colors));
        }
        return tabTitle;
    }

}
