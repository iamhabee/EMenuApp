package com.arke.sdk.models;

import com.raizlabs.android.dbflow.structure.BaseModel;

public class RestaurantOrBarInfo extends BaseModel {
    private String restaurantOrBarId;
    private String restaurantOrBarName;
    private String restaurantOrBarEmailAddress;
    private String restaurantOrBarPassword;
    private String restaurantOrBarPhotoUrl;
    private String restaurantOrBarCoverPhotoUrl;
    private int restaurantOrBarPrimaryColor;
    private int restaurantOrBarSecondaryColor;
    private int restaurantOrBarTertiaryColor;
    private String restaurantOrBarAccountDetails;
    private String restaurantOrBarAdminPassword;

    public void setRestaurantOrBarId(String restaurantOrBarId) {
        this.restaurantOrBarId = restaurantOrBarId;
    }

    public String getRestaurantOrBarId() {
        return restaurantOrBarId;
    }

    public String getRestaurantOrBarPhotoUrl() {
        return restaurantOrBarPhotoUrl;
    }

    public String getRestaurantOrBarCoverPhotoUrl() {
        return restaurantOrBarCoverPhotoUrl;
    }

    public void setRestaurantOrBarCoverPhotoUrl(String restaurantOrBarCoverPhotoUrl) {
        this.restaurantOrBarCoverPhotoUrl = restaurantOrBarCoverPhotoUrl;
    }

    public int getRestaurantOrBarPrimaryColor() {
        return restaurantOrBarPrimaryColor;
    }

    public void setRestaurantOrBarPrimaryColor(int restaurantOrBarPrimaryColor) {
        this.restaurantOrBarPrimaryColor = restaurantOrBarPrimaryColor;
    }

    public int getRestaurantOrBarSecondaryColor() {
        return restaurantOrBarSecondaryColor;
    }

    public void setRestaurantOrBarSecondaryColor(int restaurantOrBarSecondaryColor) {
        this.restaurantOrBarSecondaryColor = restaurantOrBarSecondaryColor;
    }

    public void setRestaurantOrBarTertiaryColor(int restaurantOrBarTertiaryColor) {
        this.restaurantOrBarTertiaryColor = restaurantOrBarTertiaryColor;
    }

    public int getRestaurantOrBarTertiaryColor() {
        return restaurantOrBarTertiaryColor;
    }

    public void setRestaurantOrBarPhotoUrl(String restaurantOrBarPhotoUrl) {
        this.restaurantOrBarPhotoUrl = restaurantOrBarPhotoUrl;
    }

    public String getRestaurantOrBarName() {
        return restaurantOrBarName;
    }

    public void setRestaurantOrBarName(String restaurantOrBarName) {
        this.restaurantOrBarName = restaurantOrBarName;
    }

    public String getRestaurantOrBarEmailAddress() {
        return restaurantOrBarEmailAddress;
    }

    public void setRestaurantOrBarEmailAddress(String restaurantOrBarEmailAddress) {
        this.restaurantOrBarEmailAddress = restaurantOrBarEmailAddress;
    }

    public String getRestaurantOrBarPassword() {
        return restaurantOrBarPassword;
    }

    public void setRestaurantOrBarPassword(String restaurantOrBarPassCode) {
        this.restaurantOrBarPassword = restaurantOrBarPassCode;
    }

    public void setRestaurantOrBarAccountDetails(String restaurantOrBarAccountDetails) {
        this.restaurantOrBarAccountDetails = restaurantOrBarAccountDetails;
    }

    public String getRestaurantOrBarAccountDetails() {
        return restaurantOrBarAccountDetails;
    }

    public void setRestaurantOrBarAdminPassword(String restaurantOrBarAdminPassword) {
        this.restaurantOrBarAdminPassword = restaurantOrBarAdminPassword;
    }

    public String getRestaurantOrBarAdminPassword() {
        return restaurantOrBarAdminPassword;
    }

}
