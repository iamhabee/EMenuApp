package com.arke.sdk.models;

import com.raizlabs.android.dbflow.structure.BaseModel;

public class EMenuItemCategory extends BaseModel {

    private String objectId;
    private String category;
    private String restaurantOrBarId;
    private String categoryPhotoUrl;

    public void setCategoryPhotoUrl(String categoryPhotoUrl) {
        this.categoryPhotoUrl = categoryPhotoUrl;
    }

    public String getCategoryPhotoUrl() {
        return categoryPhotoUrl;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRestaurantOrBarId() {
        return restaurantOrBarId;
    }

    public void setRestaurantOrBarId(String restaurantOrEmailAddress) {
        this.restaurantOrBarId = restaurantOrEmailAddress;
    }

}
