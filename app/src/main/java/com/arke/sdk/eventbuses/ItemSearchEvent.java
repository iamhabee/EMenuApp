package com.arke.sdk.eventbuses;

public class ItemSearchEvent {
    private String searchString;
    private int viewPagerIndex;

    public ItemSearchEvent(String searchString, int viewPagerIndex) {
        this.searchString = searchString;
        this.viewPagerIndex = viewPagerIndex;
    }

    public int getViewPagerIndex() {
        return viewPagerIndex;
    }

    public String getSearchString() {
        return searchString;
    }

}
