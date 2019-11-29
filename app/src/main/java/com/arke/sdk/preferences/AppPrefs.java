package com.arke.sdk.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;
import com.arke.sdk.utilities.CryptoUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.parse.ParseUser;

@SuppressWarnings("unused")
public class AppPrefs {

    private static SharedPreferences appSharedPreferences;

    private static SharedPreferences getAppPreferences() {
        if (appSharedPreferences == null) {
            appSharedPreferences = ArkeSdkDemoApplication.getInstance()
                    .getSharedPreferences(Globals.APP_PREFS_NAME, Context.MODE_PRIVATE);
        }
        return appSharedPreferences;
    }

    public static String getTableTag(){
        return getAppPreferences().getString(Globals.TABLE_TAG, null);
    }

    @SuppressLint("ApplySharedPref")
    public static void setTableTag(String tableTag) {
        getAppPreferences().edit().putString(Globals.TABLE_TAG, tableTag).commit();
    }

    public static String getCustomerTag(){
        return getAppPreferences().getString(Globals.CUSTOMER_TAG, null);
    }

    @SuppressLint("ApplySharedPref")
    public static void setCustomerTag(String tableTag) {
        getAppPreferences().edit().putString(Globals.CUSTOMER_TAG, tableTag).commit();
    }

    public static int getUseType() {
        return getAppPreferences().getInt(Globals.USE_TYPE, Globals.UseType.USE_TYPE_NONE.ordinal());
    }

    @SuppressLint("ApplySharedPref")
    public static void setUseType(Globals.UseType useType) {
        getAppPreferences().edit().putInt(Globals.USE_TYPE, useType.ordinal()).commit();
    }

    public static String getRestaurantOrBarName() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_NAME, "Your EMenu");
    }

    public static boolean isAppSetup() {
        return getAppPreferences().getBoolean(Globals.IS_APP_SETUP, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void persistRestaurantOrBarName(String name) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_NAME, name).commit();
    }

    @SuppressLint("ApplySharedPref")
    public static void persistRestaurantOrBarEmailAddress(String emailAddress) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress).commit();
    }

    @SuppressLint("ApplySharedPref")
    public static void persistRestaurantOrBarPassword(String password) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_PASSWORD, password).commit();
    }

    public static String getRestaurantOrBarEmailAddress() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, null);
    }

    public static String getRestaurantOrBarPassword() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_PASSWORD, null);
    }

    @SuppressLint("ApplySharedPref")
    public static void setUp(boolean value) {
        getAppPreferences().edit().putBoolean(Globals.IS_APP_SETUP, value).commit();
    }

    public static String getPreviousAuthPassword() {
        return getAppPreferences().getString(Globals.PREVIOUS_PASSWORD, "");
    }

    @SuppressLint("ApplySharedPref")
    public static void persistPreviousAuthPassword(String previousPassword) {
        getAppPreferences().edit().putString(Globals.PREVIOUS_PASSWORD, previousPassword).commit();
    }

    public static int getMaximumQuantityOrder() {
        return getAppPreferences().getInt(Globals.RESTAURANT_EMENU_MAXIMUM_QUANTITY_ORDER, 10);
    }

    @SuppressLint("ApplySharedPref")
    public static void setEMenuMaximumQuantityOrder(int maximumValue) {
        getAppPreferences().edit().putInt(Globals.RESTAURANT_EMENU_MAXIMUM_QUANTITY_ORDER, maximumValue).commit();
    }

    public static void cacheCollectionsString(String fileName, String collectionsSerializationString) {
        getAppPreferences().edit().putString(fileName, collectionsSerializationString).apply();
    }

    public static String getCachedDataString(String fileName) {
        return getAppPreferences().getString(fileName, null);
    }

    public static void setDeviceId(String deviceId) {
        getAppPreferences().edit().putString(Globals.WAITER_DEVICE_ID, deviceId).apply();
        getAppPreferences().edit().putString(Globals.BAR_ATTENDANT_DEVICE_ID, deviceId).apply();
        getAppPreferences().edit().putString(Globals.KITCHEN_ATTENDANT_DEVICE_ID, deviceId).apply();
        getAppPreferences().edit().putString(Globals.ADMIN_DEVICE_ID, deviceId).apply();
    }

    public static String getDeviceId() {
        int appUseType = getUseType();
        if (appUseType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
            return getAppPreferences().getString(Globals.KITCHEN_ATTENDANT_DEVICE_ID, null);
        } else if (appUseType == Globals.UseType.USE_TYPE_WAITER.ordinal()) {
            return getAppPreferences().getString(Globals.WAITER_DEVICE_ID, null);
        } else if (appUseType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
            return getAppPreferences().getString(Globals.BAR_ATTENDANT_DEVICE_ID, null);
        } else {
            return getAppPreferences().getString(Globals.ADMIN_DEVICE_ID, null);
        }
    }

    public static void persistWaiterTag(String waiterTag) {
        getAppPreferences().edit().putString(Globals.WAITER_TAG, waiterTag).apply();
    }

    public static String getCurrentWaiterTag() {
        return getAppPreferences().getString(Globals.WAITER_TAG, null);
    }

    public static String getKitchenTag() {
        return getAppPreferences().getString(Globals.KITCHEN_TAG, null);
    }

    public static String getBarTag() {
        return getAppPreferences().getString(Globals.BAR_TAG, null);
    }

    public static String getRestaurantOrBarPhotoUrl() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_PROFILE_PHOTO_URL, null);
    }

    public static void persistRestaurantOrBarProfilePhoto(String profilePhoto) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_PROFILE_PHOTO_URL, profilePhoto).apply();
    }

    public static String getRestaurantOrBarProfilePhotoUrl() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_PROFILE_PHOTO_URL, null);
    }

    public static void persistRestaurantOrBarCoverPhotoUrl(String newCoverPhotoURl) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_COVER_PHOTO_URL, newCoverPhotoURl).apply();
    }

    public static String getCurrentRestaurantOrBarCoverPhotoUrl() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_COVER_PHOTO_URL, null);
    }

    public static void persistRestaurantOrBarPrimaryColor(int restaurantOrBarPrimaryColor) {
        getAppPreferences().edit().putInt(Globals.RESTAURANT_OR_BAR_PRIMARY_COLOR, restaurantOrBarPrimaryColor).apply();
    }

    public static void persistRestaurantOrBarSecondaryColor(int secondaryColor) {
        getAppPreferences().edit().putInt(Globals.RESTAURANT_OR_BAR_SECONDARY_COLOR, secondaryColor).apply();
    }

    public static int getPrimaryColor() {
        int primaryColor = getAppPreferences().getInt(Globals.RESTAURANT_OR_BAR_PRIMARY_COLOR, Color.WHITE);
        if (primaryColor == 0) {
            primaryColor = Color.WHITE;
        }
        return primaryColor;
    }

    public static int getSecondaryColor() {
        int secondaryColor = getAppPreferences().getInt(Globals.RESTAURANT_OR_BAR_SECONDARY_COLOR, ContextCompat.getColor(getContext(), R.color.black));
        if (secondaryColor == 0) {
            secondaryColor = ContextCompat.getColor(getContext(), R.color.black);
        }
        return secondaryColor;
    }

    public static String getRestaurantOrBarAccountDetails() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_ACCOUNT_DETAILS, "your restaurant/bar account details");
    }

    public static int getTertiaryColor() {
        int tertiaryColor = getAppPreferences().getInt(Globals.RESTAURANT_OR_BAR_TERTIARY_COLOR,
                ContextCompat.getColor(getContext(), R.color.colorGreen));
        if (tertiaryColor == 0) {
            tertiaryColor = ContextCompat.getColor(getContext(), R.color.colorGreen);
        }
        return tertiaryColor;
    }

    public static void persistRestaurantOrBarId(String restaurantOrBarId) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId).apply();
    }

    public static String getRestaurantOrBarId() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_ID, getRestaurantOrBarEmailAddress());
    }

    public static void persistRestaurantOrBarTertiaryColor(int tertiaryColor) {
        getAppPreferences().edit().putInt(Globals.RESTAURANT_OR_BAR_TERTIARY_COLOR, tertiaryColor).apply();
    }

    private static Context getContext() {
        return ArkeSdkDemoApplication.getInstance();
    }

    public static void persistIncomingNotificationRingtoneUri(String newPreference) {
        getAppPreferences().edit().putString(Globals.INCOMING_NOTIFICATION_RINGTONE_URI, newPreference).apply();
    }

    public static String getIncomingNotificationRingtoneUri() {
        return getAppPreferences().getString(Globals.INCOMING_NOTIFICATION_RINGTONE_URI, null);
    }

    public static String getRestaurantAdminPassword() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD, CryptoUtils.getSha256Digest("12345"));
    }

    public static String getRestaurantAdminPasswordRevealed() {
        return getAppPreferences().getString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD_REVEALED, "12345");
    }

    public static void persistRestaurantOrBarAccountDetails(String restaurantOrBarAccountDetails) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_ACCOUNT_DETAILS, restaurantOrBarAccountDetails).apply();
    }

    public static void persistRestaurantOrBarAdminPassword(String plainPassword, String restaurantOrBarAdminPassword) {
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD, restaurantOrBarAdminPassword).apply();
        getAppPreferences().edit().putString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD_REVEALED, restaurantOrBarAdminPassword).apply();
    }

    public static void persistCurrentCardProcessorData(String data) {
        getAppPreferences().edit().putString(Globals.CURRENT_CARD_DATA, data).apply();
    }

    public static String getCurrentCardData() {
        return getAppPreferences().getString(Globals.CURRENT_CARD_DATA, null);
    }

    public static void setAppToured(boolean b) {
        getAppPreferences().edit().putBoolean(Globals.APP_TOURED, b).apply();
    }

    public static boolean isAppToured() {
        return getAppPreferences().getBoolean(Globals.APP_TOURED, false);
    }

    public static void persistBarAttendantTag(String barTag) {
        getAppPreferences().edit().putString(Globals.BAR_TAG, barTag).apply();
    }

    public static void persistLicenseKeyId(String id) {
        getAppPreferences().edit().putString(Globals.LICENSE_KEY_ID, id).apply();
    }

    public static String getLicenseKeyId() {
        return getAppPreferences().getString(Globals.LICENSE_KEY_ID, null);
    }


    public static void persistLicenseKey(String license_key) {
        getAppPreferences().edit().putString(Globals.LICENSE_KEY, license_key).apply();
    }

    public static String getLicenseKey() {
        return getAppPreferences().getString(Globals.LICENSE_KEY, null);
    }


    public static void persistLicenseAllowedUserAccounts(int user_accounts_allowed) {
        getAppPreferences().edit().putInt(Globals.USER_ACCOUNTS_ALLOWED, user_accounts_allowed).apply();
    }

    public static int getLicenseAllowedUserAccounts() {
        return getAppPreferences().getInt(Globals.USER_ACCOUNTS_ALLOWED, 0);
    }

    public static int getUserId(){
        return ParseUser.getCurrentUser().getInt("res_id");
    }
}
