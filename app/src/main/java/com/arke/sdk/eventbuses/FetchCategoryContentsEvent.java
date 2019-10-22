package com.arke.sdk.eventbuses;

public class FetchCategoryContentsEvent {
    private String categoryName;

    public FetchCategoryContentsEvent(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

}
