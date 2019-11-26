package com.arke.sdk.eventbuses;

import android.content.Context;

public class RefreshOrderEvent {
    private final int viewPagerIndex;
    private int user_type;
    private Context context;

    public RefreshOrderEvent(Context mContext, int userType, int viewPagerIndex){
        this.context = mContext;
        this.user_type = userType;
        this.viewPagerIndex = viewPagerIndex;
    }

    public int getUser_type() {
        return user_type;
    }

    public Context getContext() {
        return context;
    }

    public int getViewPagerIndex() {
        return viewPagerIndex;
    }
}
