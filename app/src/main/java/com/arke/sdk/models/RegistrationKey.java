package com.arke.sdk.models;

public class RegistrationKey {
    private String id;
    private String license_key;
    private String restaurant_name;

    public String getRestaurant_name() {
        return restaurant_name;
    }

    public void setRestaurant_name(String restaurant_name) {
        this.restaurant_name = restaurant_name;
    }

    private String restaurant_email_add;
    private int user_accounts_allowed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicense_key() {
        return license_key;
    }

    public void setLicense_key(String license_key) {
        this.license_key = license_key;
    }

    public String getRestaurant_email_add() {
        return restaurant_email_add;
    }

    public void setRestaurant_email_add(String restaurant_email_add) {
        this.restaurant_email_add = restaurant_email_add;
    }

    public int getUser_accounts_allowed() {
        return user_accounts_allowed;
    }

    public void setUser_accounts_allowed(int user_accounts_allowed) {
        this.user_accounts_allowed = user_accounts_allowed;
    }
}
