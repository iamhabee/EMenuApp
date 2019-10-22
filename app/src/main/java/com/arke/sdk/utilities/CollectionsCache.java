package com.arke.sdk.utilities;

import com.arke.sdk.contracts.EMenuItemCategoriesFetchDoneCallback;
import com.arke.sdk.contracts.EMenuItemsFetchDoneCallBack;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuItemCategory;
import com.arke.sdk.preferences.AppPrefs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CollectionsCache {

    public static CollectionsCache getInstance() {
        return new CollectionsCache();
    }

    public void clearCache(String fileName) {
        String restaurantOrBarEmail = AppPrefs.getRestaurantOrBarEmailAddress();
        restaurantOrBarEmail = restaurantOrBarEmail != null ? restaurantOrBarEmail.split("@")[0] : "";
        AppPrefs.cacheCollectionsString(fileName + restaurantOrBarEmail, "");
    }

    public void cacheEMenuItems(String fileName, List<EMenuItem> eMenuItemsToCache) {
        Type collectionsType = new TypeToken<List<EMenuItem>>() {
        }.getType();
        Gson gson = new Gson();
        String cacheSerialization = gson.toJson(eMenuItemsToCache, collectionsType);
        String restaurantOrBarEmail = AppPrefs.getRestaurantOrBarEmailAddress();
        restaurantOrBarEmail = restaurantOrBarEmail != null ? restaurantOrBarEmail.split("@")[0] : "";
        AppPrefs.cacheCollectionsString(fileName + restaurantOrBarEmail, cacheSerialization);
    }

    public void fetchEMenuFromCache(String fileName, EMenuItemsFetchDoneCallBack eMenuItemEMenuItemsFetchDoneCallBack) {
        Type collectionsType = new TypeToken<List<EMenuItem>>() {
        }.getType();
        Gson gson = new Gson();
        String restaurantOrBarEmail = AppPrefs.getRestaurantOrBarEmailAddress();
        restaurantOrBarEmail = restaurantOrBarEmail != null ? restaurantOrBarEmail.split("@")[0] : "";
        String cachedDataString = AppPrefs.getCachedDataString(fileName + restaurantOrBarEmail);
        if (cachedDataString == null) {
            eMenuItemEMenuItemsFetchDoneCallBack.done(new ArrayList<>(), null);
        } else {
            eMenuItemEMenuItemsFetchDoneCallBack.done(gson.fromJson(cachedDataString, collectionsType), null);
        }
    }

    public void cacheEMenuCategories(String fileName, List<EMenuItemCategory> eMenuItemsToCache) {
        Type collectionsType = new TypeToken<List<EMenuItemCategory>>() {
        }.getType();
        Gson gson = new Gson();
        String cacheSerialization = gson.toJson(eMenuItemsToCache, collectionsType);
        String restaurantOrBarEmail = AppPrefs.getRestaurantOrBarEmailAddress();
        restaurantOrBarEmail = restaurantOrBarEmail != null ? restaurantOrBarEmail.split("@")[0] : "";
        AppPrefs.cacheCollectionsString(fileName + restaurantOrBarEmail, cacheSerialization);
    }

    public void fetchEMenuCategoriesFromCache(String fileName, EMenuItemCategoriesFetchDoneCallback eMenuItemCategoriesFetchDoneCallback) {
        Type collectionsType = new TypeToken<List<EMenuItemCategory>>() {
        }.getType();
        Gson gson = new Gson();
        String restaurantOrBarEmail = AppPrefs.getRestaurantOrBarEmailAddress();
        restaurantOrBarEmail = restaurantOrBarEmail != null ? restaurantOrBarEmail.split("@")[0] : "";
        String cachedDataString = AppPrefs.getCachedDataString(fileName + restaurantOrBarEmail);
        if (cachedDataString == null) {
            eMenuItemCategoriesFetchDoneCallback.done(new ArrayList<>(), null);
        } else {
            eMenuItemCategoriesFetchDoneCallback.done(gson.fromJson(cachedDataString, collectionsType), null);
        }
    }

}
