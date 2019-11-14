package com.arke.sdk.eventbuses;

import android.content.Context;

public class ItemSearchEvent {
    private String searchString;
    private int viewPagerIndex;
    private Context mContext;

    public ItemSearchEvent(String searchString, int viewPagerIndex) {
        this.searchString = searchString;
        this.viewPagerIndex = viewPagerIndex;
    }

    public ItemSearchEvent(Context context, int viewPagerIndex) {
        this.mContext = context;
        this.viewPagerIndex = viewPagerIndex;
    }

    public int getViewPagerIndex() {
        return viewPagerIndex;
    }

    public Context getmContext() { return  mContext; }

    public String getSearchString() {
        return searchString;
    }

}
