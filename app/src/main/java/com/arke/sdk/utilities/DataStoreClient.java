package com.arke.sdk.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;
import com.arke.sdk.contracts.AcceptedOrder;
import com.arke.sdk.contracts.RejectedOrder;
import com.arke.sdk.eventbuses.EMenuItemRemovedFromOrderEvent;
import com.arke.sdk.eventbuses.OrderPaidForEvent;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
//import com.elitepath.android.emenu.R;


import com.arke.sdk.companions.Globals;
import com.arke.sdk.contracts.BaseModelOperationDoneCallback;
import com.arke.sdk.contracts.BooleanOperationDoneCallback;
import com.arke.sdk.contracts.EMenuCustomerOrderCallBack;
import com.arke.sdk.contracts.EMenuItemCategoriesFetchDoneCallback;
import com.arke.sdk.contracts.EMenuItemUpdateDoneCallback;
import com.arke.sdk.contracts.EMenuItemsFetchDoneCallBack;
import com.arke.sdk.contracts.EMenuOrdersFetchDoneCallBack;
import com.arke.sdk.contracts.OrderUpdateDoneCallback;
import com.arke.sdk.contracts.PaymentDoneCallBack;
import com.arke.sdk.contracts.RestaurantUpdateDoneCallback;
import com.arke.sdk.contracts.UnProcessedOrderPushCallBack;
import com.arke.sdk.contracts.WaitersFetchDoneCallBack;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuItemCategory;
import com.arke.sdk.models.EMenuOrder;
//import com.elitepath.android.emenu.models.EMenuOrder_Table;
import com.arke.sdk.models.EMenuOrder_Table;
import com.arke.sdk.models.RestaurantOrBarInfo;
import com.arke.sdk.preferences.AppPrefs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("ConstantConditions")
public class DataStoreClient {

    public static String TAG = DataStoreClient.class.getSimpleName();
     SharedPreferences preferences;
    SharedPreferences.Editor editor;
     Context mContext;

    public DataStoreClient(Context context) {
        this.preferences = preferences;
        this.mContext = context;

        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = preferences.edit();
    }

    public static void logInAccount(BaseModelOperationDoneCallback baseModelOperationDoneCallback) {
        String emailAddress = AppPrefs.getRestaurantOrBarEmailAddress();
        String passCode = AppPrefs.getRestaurantOrBarPassword();
        if (StringUtils.isEmpty(emailAddress) || StringUtils.isEmpty(passCode)) {
            baseModelOperationDoneCallback.done(null, getException("Sorry, we can't proceed. Required fields are empty. Please fill all fields."));
            return;
        }
        ParseQuery<ParseObject> restaurantsAndBars = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);
        restaurantsAndBars.whereEqualTo(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress.trim());
        restaurantsAndBars.getFirstInBackground((object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    baseModelOperationDoneCallback.done(null, getException("Sorry, Your Restaurant/Bar has probably not being setup yet. Consider Creating an account first."));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    baseModelOperationDoneCallback.done(null, getException(getNetworkErrorMessage()));
                } else {
                    baseModelOperationDoneCallback.done(null, getException(e.getMessage()));
                }
            } else {
                String existingPassCode = object.getString(Globals.RESTAURANT_OR_BAR_PASSWORD);
                if (CryptoUtils.getSha256Digest(passCode).equals(existingPassCode)) {
                    RestaurantOrBarInfo result = loadParseObjectIntoRestaurantOrBarModel(object);
                    baseModelOperationDoneCallback.done(result, null);
                } else {
                    baseModelOperationDoneCallback.done(null, getException("Sorry, The password you provided does not match the password for your Restaurant/Bar"));
                }
            }
        });
    }

    public static void checkIfEmailAddressIsAlreadyRegistered(boolean isForAdmin, BaseModelOperationDoneCallback baseModelOperationDoneCallback) {
        String emailAddress = AppPrefs.getRestaurantOrBarEmailAddress();
        ParseQuery<ParseObject> restaurantsAndBars = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);
        restaurantsAndBars.whereEqualTo(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress.trim());
        restaurantsAndBars.getFirstInBackground((object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    baseModelOperationDoneCallback.done(null, getException("Sorry, email address not associated with any existing account."));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    baseModelOperationDoneCallback.done(null, getException(getNetworkErrorMessage()));
                } else {
                    baseModelOperationDoneCallback.done(null, getException(e.getMessage()));
                }
            } else {
                String existingPassCode = object.getString(Globals.RESTAURANT_OR_BAR_REVEALED_PASSWORD);
                if (isForAdmin) {
                    existingPassCode = object.getString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD_REVEALED);
                    if (existingPassCode==null){
                        existingPassCode = AppPrefs.getRestaurantAdminPasswordRevealed();
                    }
                }
                String restaurantOrBarName = object.getString(Globals.RESTAURANT_OR_BAR_NAME);
                String restaurantEmailAddress = object.getString(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS);
                RestaurantOrBarInfo restaurantOrBarInfo = new RestaurantOrBarInfo();
                restaurantOrBarInfo.setRestaurantOrBarName(restaurantOrBarName);
                restaurantOrBarInfo.setRestaurantOrBarPassword(existingPassCode);
                restaurantOrBarInfo.setRestaurantOrBarEmailAddress(restaurantEmailAddress);
                baseModelOperationDoneCallback.done(restaurantOrBarInfo, null);
            }
        });
    }

    private static String getNetworkErrorMessage() {
        return "A network glitch happened. Please review your data connection and try again.";
    }

    private static long fromMidNight(long mills) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(mills);
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);
        return currentCalendar.getTimeInMillis();
    }

    public static void fetchOrdersFromWaiter(String waiterTag, int skip, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> eMenuOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        eMenuOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        eMenuOrdersQuery.whereEqualTo(Globals.WAITER_TAG, waiterTag);
        eMenuOrdersQuery.whereExists(Globals.ORDER_PAYMENT_STATUS);
        if (skip != 0) {
            eMenuOrdersQuery.setSkip(skip);
        }
        eMenuOrdersQuery.setLimit(30);
        eMenuOrdersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedData = new ArrayList<>();
                for (ParseObject parseObject : objects) {
                    EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(parseObject);
                    if (!retrievedData.contains(eMenuOrder)) {
                        retrievedData.add(eMenuOrder);
                    }
                }
                eMenuOrdersFetchDoneCallBack.done(retrievedData, null);
            } else {
                eMenuOrdersFetchDoneCallBack.done(null, e);
            }
        });
    }

    public static void fetchOrdersBetweenDates(Date from, Date to, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> eMenuOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        eMenuOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        eMenuOrdersQuery.whereExists(Globals.ORDER_PAYMENT_STATUS);
        long fromTime = fromMidNight(from.getTime());
        eMenuOrdersQuery.whereGreaterThanOrEqualTo(Globals.CREATED_AT, new Date(fromTime));
        eMenuOrdersQuery.whereLessThanOrEqualTo(Globals.CREATED_AT, new Date(to.getTime()));
        eMenuOrdersQuery.whereExists(Globals.ORDER_PAYMENT_STATUS);
        eMenuOrdersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedData = new ArrayList<>();
                for (ParseObject parseObject : objects) {
                    EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(parseObject);
                    if (!retrievedData.contains(eMenuOrder)) {
                        retrievedData.add(eMenuOrder);
                    }
                }
                eMenuOrdersFetchDoneCallBack.done(retrievedData, null);
            } else {
                eMenuOrdersFetchDoneCallBack.done(null, e);
            }
        });
    }

    public static void registerAccount(BaseModelOperationDoneCallback baseModelOperationDoneCallback) {
        String restaurantOrBarName = AppPrefs.getRestaurantOrBarName();
        String emailAddress = AppPrefs.getRestaurantOrBarEmailAddress();
        String passCode = AppPrefs.getRestaurantOrBarPassword();
        if (StringUtils.isEmpty(emailAddress) || StringUtils.isEmpty(passCode)) {
            baseModelOperationDoneCallback.done(null, getException("Sorry, we can't proceed. Required fields are empty. Please fill all fields."));
            return;
        }
        ParseQuery<ParseObject> restaurantsAndBars = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);
        restaurantsAndBars.whereEqualTo(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress);
        restaurantsAndBars.getFirstInBackground((object, e) -> {
            if (object != null) {
                baseModelOperationDoneCallback.done(null, getException("A Restaurant/Bar with the provided email address already exists. Consider login in"));
            } else {
                createNewRestaurantOrBar(baseModelOperationDoneCallback, restaurantOrBarName, emailAddress, passCode);
            }
        });
    }

    private static void createNewRestaurantOrBar(BaseModelOperationDoneCallback baseModelOperationDoneCallback, String restaurantOrBarName, String emailAddress, String passCode) {
        ParseObject newRestaurantOrBar = new ParseObject(Globals.RESTAURANTS_AND_BARS);
        if (StringUtils.isNotEmpty(restaurantOrBarName)) {
            newRestaurantOrBar.put(Globals.RESTAURANT_OR_BAR_NAME, restaurantOrBarName);
        }
        newRestaurantOrBar.put(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress);
        newRestaurantOrBar.put(Globals.RESTAURANT_OR_BAR_PASSWORD, CryptoUtils.getSha256Digest(passCode));
        newRestaurantOrBar.put(Globals.RESTAURANT_OR_BAR_REVEALED_PASSWORD, passCode);
        newRestaurantOrBar.saveInBackground(e -> {
            if (e == null) {
                RestaurantOrBarInfo result = loadParseObjectIntoRestaurantOrBarModel(newRestaurantOrBar);
                // create a default admin account using the provided details
                createNewAdminAccount(result, newRestaurantOrBar);
                baseModelOperationDoneCallback.done(result, null);
            } else {
                if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    baseModelOperationDoneCallback.done(null, getException(getNetworkErrorMessage()));
                } else {
                    baseModelOperationDoneCallback.done(null, getException("Error creating new Restaurant/Bar. Please try again"));
                }
            }
        });
    }

    private static void createNewAdminAccount(RestaurantOrBarInfo restaurantOrBarInfo, ParseObject restaurant){
        String passCode = restaurant.getString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD_REVEALED);

        ParseUser user = new ParseUser();
        // Set the user's username and password, which can be obtained by a forms
        user.setUsername(restaurantOrBarInfo.getRestaurantOrBarEmailAddress());
        user.setEmail(restaurantOrBarInfo.getRestaurantOrBarEmailAddress());
        user.setPassword(Globals.DEFAULT_PWD);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    try {
                        user.put("res_id", restaurantOrBarInfo.getRestaurantOrBarId()); // restaurant ID
                        user.put("account_type", "Admin");
                        user.put("user_type", Globals.ADMIN_TAG_ID);
                        user.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private static RestaurantOrBarInfo loadParseObjectIntoRestaurantOrBarModel(ParseObject parseObject) {
        RestaurantOrBarInfo restaurantOrBarInfo = new RestaurantOrBarInfo();
        String objectId = parseObject.getObjectId();
        String restaurantOrBarPassword = parseObject.getString(Globals.RESTAURANT_OR_BAR_PASSWORD);
        if (StringUtils.isNotEmpty(restaurantOrBarPassword)) {
            restaurantOrBarInfo.setRestaurantOrBarPassword(restaurantOrBarPassword);
            AppPrefs.persistRestaurantOrBarPassword(restaurantOrBarPassword);
        }
        restaurantOrBarInfo.setRestaurantOrBarId(objectId);
        AppPrefs.persistRestaurantOrBarId(objectId);

        String emailAddress = parseObject.getString(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS);
        restaurantOrBarInfo.setRestaurantOrBarEmailAddress(emailAddress);
        AppPrefs.persistRestaurantOrBarEmailAddress(emailAddress);

        String restaurantOrBarName = parseObject.getString(Globals.RESTAURANT_OR_BAR_NAME);
        restaurantOrBarInfo.setRestaurantOrBarName(restaurantOrBarName);
        AppPrefs.persistRestaurantOrBarName(restaurantOrBarName);

        int restaurantOrBarPrimaryColor = parseObject.getInt(Globals.RESTAURANT_OR_BAR_PRIMARY_COLOR);
        restaurantOrBarInfo.setRestaurantOrBarPrimaryColor(restaurantOrBarPrimaryColor);
        AppPrefs.persistRestaurantOrBarPrimaryColor(restaurantOrBarPrimaryColor);
        if (restaurantOrBarPrimaryColor == 0) {
            int defaultPrimaryColor = Color.WHITE;
            restaurantOrBarInfo.setRestaurantOrBarPrimaryColor(defaultPrimaryColor);
            AppPrefs.persistRestaurantOrBarPrimaryColor(defaultPrimaryColor);
        }
        int restaurantOrBarSecondaryColor = parseObject.getInt(Globals.RESTAURANT_OR_BAR_SECONDARY_COLOR);
        restaurantOrBarInfo.setRestaurantOrBarSecondaryColor(restaurantOrBarSecondaryColor);
        AppPrefs.persistRestaurantOrBarSecondaryColor(restaurantOrBarSecondaryColor);
        if (restaurantOrBarSecondaryColor == 0) {
            int defaultSecondaryColor = ContextCompat.getColor(ArkeSdkDemoApplication.getInstance(), R.color.black);
            restaurantOrBarInfo.setRestaurantOrBarSecondaryColor(defaultSecondaryColor);
            AppPrefs.persistRestaurantOrBarSecondaryColor(defaultSecondaryColor);
        }
        int restaurantOrBarTertiaryColor = parseObject.getInt(Globals.RESTAURANT_OR_BAR_TERTIARY_COLOR);
        restaurantOrBarInfo.setRestaurantOrBarTertiaryColor(restaurantOrBarTertiaryColor);
        AppPrefs.persistRestaurantOrBarTertiaryColor(restaurantOrBarTertiaryColor);
        if (restaurantOrBarTertiaryColor == 0) {
            int defaultTertiaryColor = ContextCompat.getColor(ArkeSdkDemoApplication.getInstance(), R.color.colorGreen);
            restaurantOrBarInfo.setRestaurantOrBarTertiaryColor(defaultTertiaryColor);
            AppPrefs.persistRestaurantOrBarTertiaryColor(defaultTertiaryColor);
        }
        String restaurantOrBarProfilePhotoUrl = parseObject.getString(Globals.RESTAURANT_OR_BAR_PROFILE_PHOTO_URL);
        if (StringUtils.isNotEmpty(restaurantOrBarProfilePhotoUrl)) {
            restaurantOrBarInfo.setRestaurantOrBarPhotoUrl(restaurantOrBarProfilePhotoUrl);
            AppPrefs.persistRestaurantOrBarProfilePhoto(restaurantOrBarProfilePhotoUrl);
        }
        String restaurantOrBarCoverPhotoUrl = parseObject.getString(Globals.RESTAURANT_OR_BAR_COVER_PHOTO_URL);
        if (StringUtils.isNotEmpty(restaurantOrBarCoverPhotoUrl)) {
            restaurantOrBarInfo.setRestaurantOrBarCoverPhotoUrl(restaurantOrBarCoverPhotoUrl);
            AppPrefs.persistRestaurantOrBarCoverPhotoUrl(restaurantOrBarCoverPhotoUrl);
        }
        String restaurantOrBarAccountDetails = parseObject.getString(Globals.RESTAURANT_OR_BAR_ACCOUNT_DETAILS);
        if (StringUtils.isNotEmpty(restaurantOrBarAccountDetails)) {
            restaurantOrBarInfo.setRestaurantOrBarAccountDetails(restaurantOrBarAccountDetails);
            AppPrefs.persistRestaurantOrBarAccountDetails(restaurantOrBarAccountDetails);
        }
        String restaurantOrBarAdminPassword = parseObject.getString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD);
        String restaurantOrBarAdminPasswordRevealed = parseObject.getString(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD_REVEALED);
        if (StringUtils.isNotEmpty(restaurantOrBarAdminPassword)) {
            restaurantOrBarInfo.setRestaurantOrBarAdminPassword(restaurantOrBarAdminPassword);
            AppPrefs.persistRestaurantOrBarAdminPassword(restaurantOrBarAdminPasswordRevealed, restaurantOrBarAdminPassword);
        }
        return restaurantOrBarInfo;
    }

    public static void fetchMenuCategories(EMenuItemCategoriesFetchDoneCallback eMenuItemCategoriesFetchDoneCallback) {
        ParseQuery<ParseObject> categoriesQuery = ParseQuery.getQuery(Globals.EMENU_ITEMS_CATEGORIES);
        categoriesQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
        categoriesQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuItemCategory> eMenuItemCategories = new ArrayList<>();
                for (ParseObject parseObject : objects) {
                    EMenuItemCategory eMenuItemCategory = loadParseObjectIntoEMenuItemCategory(parseObject);
                    if (!eMenuItemCategories.contains(eMenuItemCategory)) {
                        eMenuItemCategories.add(eMenuItemCategory);
                    }
                }
                if (!eMenuItemCategories.isEmpty()) {
                    eMenuItemCategoriesFetchDoneCallback.done(eMenuItemCategories, null);
                } else {
                    eMenuItemCategoriesFetchDoneCallback.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            } else {
                int exceptionCode = e.getCode();
                if (exceptionCode == ParseException.OBJECT_NOT_FOUND) {
                    eMenuItemCategoriesFetchDoneCallback.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (exceptionCode == ParseException.CONNECTION_FAILED) {
                    eMenuItemCategoriesFetchDoneCallback.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuItemCategoriesFetchDoneCallback.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    public static void fetchDrinks(int skip, EMenuItemsFetchDoneCallBack fetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> eMenuItemsQuery = ParseQuery.getQuery(Globals.EMenuItems);
        eMenuItemsQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        eMenuItemsQuery.whereEqualTo(Globals.DESTINATION_ID, AppPrefs.getUseType());
        eMenuItemsQuery.setLimit(100);

        if (skip != 0) {
            eMenuItemsQuery.setSkip(skip);
        }
        eMenuItemsQuery.findInBackground((objects, e) -> {
            if (e != null) {
                int exceptionCode = e.getCode();
                if (exceptionCode == ParseException.OBJECT_NOT_FOUND) {
                    fetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (exceptionCode == ParseException.CONNECTION_FAILED) {
                    fetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    fetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            } else {
                List<EMenuItem> availableItems = loadParseObjectsIntoEMenuItemObjects(objects);
                if (!availableItems.isEmpty()) {
                    fetchDoneCallBack.done(availableItems, null);
                } else {
                    fetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            }
        });

    }

    private static String stringifyEMenuItem(EMenuItem eMenuItem) {
        return new Gson().toJson(eMenuItem, new TypeToken<EMenuItem>() {
        }.getType());
    }

    public static void addWaiterIfNotExisting(String waiterTag) {
        ParseQuery<ParseObject> waiterQuery = ParseQuery.getQuery(Globals.WAITERS);
        waiterQuery.whereEqualTo(Globals.WAITER_TAG, waiterTag);
        waiterQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
        waiterQuery.getFirstInBackground((object, e) -> {
            if (e != null) {
                int errorCode = e.getCode();
                if (errorCode == ParseException.OBJECT_NOT_FOUND) {
                    ParseObject newWaiter = new ParseObject(Globals.WAITERS);
                    newWaiter.put(Globals.WAITER_TAG, waiterTag);
                    newWaiter.put(Globals.SEARCHABLE_TAG, waiterTag.toLowerCase());
                    newWaiter.put(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
                    newWaiter.saveInBackground();
                }
            }
        });
    }

    public static void fetchWaiters(WaitersFetchDoneCallBack waitersFetchDoneCallBack) {
        ParseQuery<ParseObject> waitersQuery = ParseQuery.getQuery(Globals.WAITERS);
        waitersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
        waitersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                if (!objects.isEmpty()) {
                    List<String> waitersTagList = new ArrayList<>();
                    for (ParseObject parseObject : objects) {
                        String waiterTag = parseObject.getString(Globals.WAITER_TAG);
                        if (!waitersTagList.contains(waiterTag)) {
                            waitersTagList.add(waiterTag);
                        }
                    }
                    CharSequence[] waiters = new CharSequence[waitersTagList.size()];
                    for (int i = 0; i < waiters.length; i++) {
                        waiters[i] = waitersTagList.get(i);
                    }
                    waitersFetchDoneCallBack.done(null, waiters);
                } else {
                    waitersFetchDoneCallBack.done(getException("No waiters recorded found"), (CharSequence) null);
                }
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    waitersFetchDoneCallBack.done(getException("No waiters recorded found"), (CharSequence) null);
                } else {
                    waitersFetchDoneCallBack.done(e, (CharSequence) null);
                }
            }
        });
    }

    public static void setQuantityAvailableInStockForItem(int qtyInStock, String itemId, EMenuItemUpdateDoneCallback eMenuItemUpdateDoneCallback) {
        ParseQuery<ParseObject> itemQuery = ParseQuery.getQuery(Globals.EMenuItems);
        itemQuery.getInBackground(itemId, (object, e) -> {
            if (e == null) {
                object.put(Globals.QTY_IN_STOCK, qtyInStock);
                if (qtyInStock == 0) {
                    object.put(Globals.IN_STOCK, false);
                } else {
                    object.put(Globals.IN_STOCK, true);
                }
                object.saveInBackground(e1 -> {
                    if (e1 == null) {
                        eMenuItemUpdateDoneCallback.done(loadParseObjectIntoEMenuItem(object), null);
                        sendOutNotification(1, Globals.EMENU_ITEM_NOTIFICATION, stringifyEMenuItem(loadParseObjectIntoEMenuItem(object)), Globals.UPDATE_TYPE_UPDATE);
                    } else {
                        eMenuItemUpdateDoneCallback.done(null, e1);
                    }
                });
            } else {
                eMenuItemUpdateDoneCallback.done(null, e);
            }
        });
    }

    public static void stockItem(boolean stock, String itemId, EMenuItemUpdateDoneCallback eMenuItemUpdateDoneCallback) {
        ParseQuery<ParseObject> itemQuery = ParseQuery.getQuery(Globals.EMenuItems);
        itemQuery.getInBackground(itemId, (object, e) -> {
            if (e == null) {
                object.put(Globals.IN_STOCK, stock);
                object.saveInBackground(e1 -> {
                    if (e1 == null) {
                        eMenuItemUpdateDoneCallback.done(loadParseObjectIntoEMenuItem(object), null);
                        sendOutNotification(1, Globals.EMENU_ITEM_NOTIFICATION, stringifyEMenuItem(loadParseObjectIntoEMenuItem(object)), Globals.UPDATE_TYPE_UPDATE);
                    } else {
                        eMenuItemUpdateDoneCallback.done(null, e1);
                    }
                });
            } else {
                eMenuItemUpdateDoneCallback.done(null, e);
            }
        });
    }

    public static void rejectEmenuOrder(String orderId, Boolean rejected, RejectedOrder rejectedOrder){

    // Connect to the parse server
        ParseQuery<ParseObject> orderQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        String deviceId = AppPrefs.getDeviceId();
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        // Setting query clauses
        orderQuery.whereEqualTo(Globals.ORDER_ID, orderId);
        orderQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        orderQuery.getFirstInBackground((object, e) -> {
            if (object != null) {
                int appUseType = AppPrefs.getUseType();
                String rejectionStatus = null;
                if (appUseType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
                    rejectionStatus = object.getString(Globals.KITCHEN_ATTENDANT_DEVICE_ID);
                } else if (appUseType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
                    rejectionStatus = object.getString(Globals.BAR_ATTENDANT_DEVICE_ID);
                }
                if (deviceId != null && rejectionStatus != null) {
                    String errorMessage = " Order was rejected";
                    rejectedOrder.done(true, getException(errorMessage));
                } else {
                    if (deviceId != null) {
                        //
                        object.put(appUseType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()
                                        ? Globals.KITCHEN_ATTENDANT_ID
                                        : Globals.BAR_ATTENDANT_ID,
                                deviceId);
                        Boolean orderRejectionState = true;
                        Boolean orderAcceptedState = false;
                        Boolean rejectedNotifier = false;
                        object.put(Globals.REJECTED_ORDER, orderRejectionState);
                        object.put(Globals.ACCEPTED_ORDER, orderAcceptedState);
                        object.put(Globals.REJECTED_NOTIFIER, rejectedNotifier);
                        object.put(Globals.ORDER_PROGRESS_STATUS, '"' + "REJECTED" + '"');
                    }
                    object.saveInBackground(e1 -> {
                        if (e1 == null) {
                            rejectedOrder.done(true, null);
                        } else {
                            rejectedOrder.done(true, e1);
                        }
                    });
                }
            }

            // Send notification
            EMenuOrder insertToRejected = loadParseObjectIntoEMenuOrder(object);
            sendOutNotification(1, Globals.EMENU_ORDER_NOTIFICATION, serializeEMenuOrder(insertToRejected),
                Globals.REJECTED_ORDER);
        });
    }

    public static void deleteEMenuOrderRemotely(String orderId, BooleanOperationDoneCallback deleteDoneCallBack) {
        ParseQuery<ParseObject> orderQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        orderQuery.whereEqualTo(Globals.ORDER_ID, orderId);
        orderQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
        orderQuery.getFirstInBackground((object, e) -> {
            if (e == null) {
                if (object != null) {
                    object.deleteInBackground(e1 -> {
                        if (e1 == null) {
                            deleteDoneCallBack.done(true, null);
                            sendOutNotification(1, Globals.EMENU_ORDER_NOTIFICATION, serializeEMenuOrder(loadParseObjectIntoEMenuOrder(object)), Globals.DELETED);
                        } else {
                            deleteDoneCallBack.done(false, e1);
                        }
                    });
                } else {
                    deleteDoneCallBack.done(false, getException("Error deleting item.Please try again."));
                }
            } else {
                deleteDoneCallBack.done(false, e);
            }
        });
    }

    public static void deleteEMenuItem(String itemId,
                                       BooleanOperationDoneCallback booleanOperationDoneCallback) {
        ParseQuery<ParseObject> itemQuery = ParseQuery.getQuery(Globals.EMenuItems);
        itemQuery.getInBackground(itemId, (object, e) -> {
            if (object != null) {
                object.deleteInBackground(e1 -> {
                    if (e1 == null) {
                        booleanOperationDoneCallback.done(true, null);
                        sendOutNotification(1, Globals.EMENU_ITEM_NOTIFICATION, stringifyEMenuItem(loadParseObjectIntoEMenuItem(object)), Globals.DELETED);
                    } else {
                        booleanOperationDoneCallback.done(false, e1);
                    }
                });
            } else {
                booleanOperationDoneCallback.done(false, e);
            }
        });
    }



//    This method fetches all available menu  items for waiter, kitchen and bar
    public static void fetchAvailableEMenuItemsForRestaurant(int skip,
                                                             EMenuItemsFetchDoneCallBack fetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> eMenuItemsQuery = ParseQuery.getQuery(Globals.EMenuItems);
        eMenuItemsQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        if(AppPrefs.getUseType() != 1) {
            eMenuItemsQuery.whereEqualTo(Globals.DESTINATION_ID, AppPrefs.getUseType());
        }
        eMenuItemsQuery.setLimit(100);
        if (skip != 0) {
            eMenuItemsQuery.setSkip(skip);
        }
        eMenuItemsQuery.findInBackground((objects, e) -> {
            if (e != null) {
                int exceptionCode = e.getCode();
                if (exceptionCode == ParseException.OBJECT_NOT_FOUND) {
                    fetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (exceptionCode == ParseException.CONNECTION_FAILED) {
                    fetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    fetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            } else {
                List<EMenuItem> availableItems = loadParseObjectsIntoEMenuItemObjects(objects);
                if (!availableItems.isEmpty()) {
                    fetchDoneCallBack.done(availableItems, null);
                } else {
                    fetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            }
        });

    }

    public static void fetchContentsInEMenuItemsCategory(String category,
                                                         int skip, EMenuItemsFetchDoneCallBack fetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> eMenuItemsQuery = ParseQuery.getQuery(Globals.EMenuItems);
        eMenuItemsQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        eMenuItemsQuery.setLimit(100);
        if (skip != 0) {
            eMenuItemsQuery.setSkip(skip);
        }
        if (category != null) {
            eMenuItemsQuery.whereEqualTo(Globals.EMENU_ITEM_PARENT_CATEGORY, category.toLowerCase());
        }
        eMenuItemsQuery.findInBackground((objects, e) -> {
            if (e != null) {
                int exceptionCode = e.getCode();
                if (exceptionCode == ParseException.OBJECT_NOT_FOUND) {
                    fetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (exceptionCode == ParseException.CONNECTION_FAILED) {
                    fetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    fetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            } else {
                List<EMenuItem> availableItems = loadParseObjectsIntoEMenuItemObjects(objects);
                if (!availableItems.isEmpty()) {
                    fetchDoneCallBack.done(availableItems, null);
                } else {
                    fetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            }
        });

    }

    private static List<EMenuItem> loadParseObjectsIntoEMenuItemObjects(List<ParseObject> objects) {
        List<EMenuItem> availableItems = new ArrayList<>();
        for (ParseObject parseObject : objects) {
            EMenuItem eMenuItem = loadParseObjectIntoEMenuItem(parseObject);
            if (!availableItems.contains(eMenuItem)) {
                availableItems.add(eMenuItem);
            }
        }
        return availableItems;
    }

    private static EMenuItem loadParseObjectIntoEMenuItem(ParseObject parseObject) {
        String restaurantOrBarName = parseObject.getString(Globals.RESTAURANT_OR_BAR_NAME);
        String emenuItemPhotoUrl = parseObject.getString(Globals.EMENU_ITEM_PHOTO_URL);
        String emenuItemName = parseObject.getString(Globals.EMENU_ITEM_NAME);
        String emenuItemDescription = parseObject.getString(Globals.EMENU_ITEM_DESCRIPTION);
        String emenuItemParentCategory = parseObject.getString(Globals.EMENU_ITEM_PARENT_CATEGORY);
        String emenuItemSubParentCategory = parseObject.getString(Globals.EMENU_ITEM_SUB_PARENT_CATEGORY);
        List<String> emenuItemIngredientsList = parseObject.getList(Globals.EMENU_ITEM_INGREDIENTS_LIST);
        int quantityAvailableInStock = parseObject.getInt(Globals.QTY_IN_STOCK);
        String emenuItemId = parseObject.getObjectId();
        Date createdAt = parseObject.getCreatedAt();
        Date updatedAt = parseObject.getUpdatedAt();
        String emenuItemPrice = parseObject.getString(Globals.EMENU_ITEM_PRICE);
        boolean available = parseObject.getBoolean(Globals.IN_STOCK);
        String emenuItemCreatorTag = parseObject.getString(Globals.EMENU_ITEM_CREATOR_TAG);
        String restaurantOrBarId = parseObject.getString(Globals.RESTAURANT_OR_BAR_ID);

        EMenuItem eMenuItem = new EMenuItem();
        eMenuItem.setRestaurantOrBarId(restaurantOrBarId);
        eMenuItem.setQuantityAvailableInStock(quantityAvailableInStock);
        eMenuItem.setRestaurantOrBarName(restaurantOrBarName);
        eMenuItem.setMenuItemName(emenuItemName);
        eMenuItem.setMenuItemDisplayPhotoUrl(emenuItemPhotoUrl);
        eMenuItem.setMenuItemDescription(emenuItemDescription);
        eMenuItem.setMenuItemId(emenuItemId);
        eMenuItem.setMenuItemIngredientsList(emenuItemIngredientsList);
        eMenuItem.setParentCategory(emenuItemParentCategory);
        eMenuItem.setSubParentCategory(emenuItemSubParentCategory);
        eMenuItem.setCreatedAt(createdAt.getTime());
        eMenuItem.setUpdatedAt(updatedAt.getTime());
        eMenuItem.setInStock(available);
        eMenuItem.setMenuItemPrice(emenuItemPrice);
        eMenuItem.setCreatorTag(emenuItemCreatorTag);
        return eMenuItem;
    }

    private static EMenuItemCategory loadParseObjectIntoEMenuItemCategory(ParseObject parseObject) {
        EMenuItemCategory eMenuItemCategory = new EMenuItemCategory();
        String objectId = parseObject.getObjectId();
        String categoryName = parseObject.getString(Globals.EMENU_ITEM_CATEGORY);
        String categoryPhotoUrl = parseObject.getString(Globals.MENU_CATEGORY_PHOTO_URL);
        String restaurantOrBarId = parseObject.getString(Globals.RESTAURANT_OR_BAR_ID);
        eMenuItemCategory.setObjectId(objectId);
        eMenuItemCategory.setRestaurantOrBarId(restaurantOrBarId);
        eMenuItemCategory.setCategory(WordUtils.capitalize(categoryName));
        if (StringUtils.isNotEmpty(categoryPhotoUrl)) {
            eMenuItemCategory.setCategoryPhotoUrl(categoryPhotoUrl);
        }
        return eMenuItemCategory;
    }

    public static void searchEMenuItems(String searchString, EMenuItemsFetchDoneCallBack eMenuItemsFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> searchQuery = ParseQuery.getQuery(Globals.EMenuItems);
        searchQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        if(searchString.length() > 0) {
            searchQuery.whereContains(Globals.EMENU_ITEM_NAME, searchString.toLowerCase());
        }
        searchQuery.whereEqualTo(Globals.DESTINATION_ID, AppPrefs.getUseType());
        searchQuery.findInBackground((objects, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuItemsFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuItemsFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuItemsFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            } else {
                List<EMenuItem> availableItems = loadParseObjectsIntoEMenuItemObjects(objects);
                if (!availableItems.isEmpty()) {
                    EMenuLogger.d("SearchResultsTag", availableItems.toString());
                    eMenuItemsFetchDoneCallBack.done(availableItems, null);
                } else {
                    eMenuItemsFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            }
        });
    }

    public static void searchDrinks(String searchString, EMenuItemsFetchDoneCallBack eMenuItemsFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> searchQuery = ParseQuery.getQuery(Globals.EMenuItems);
        searchQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        searchQuery.whereContains(Globals.EMENU_ITEM_NAME, searchString.toLowerCase());
        searchQuery.whereContains(Globals.EMENU_ITEM_PARENT_CATEGORY, Globals.DRINKS);
        searchQuery.findInBackground((objects, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuItemsFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuItemsFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuItemsFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            } else {
                List<EMenuItem> availableItems = loadParseObjectsIntoEMenuItemObjects(objects);
                if (!availableItems.isEmpty()) {
                    EMenuLogger.d("SearchResultsTag", availableItems.toString());
                    eMenuItemsFetchDoneCallBack.done(availableItems, null);
                } else {
                    eMenuItemsFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            }
        });
    }



//    create menu item

    public static void createNewMenuItem(String itemName, String itemDescription, int stockNumber, String
            itemPrice, String itemParentCategory, String itemPhotoUrl, EMenuItemUpdateDoneCallback upsertionDoneCallBack) {

        ParseObject newMenuItem = new ParseObject(Globals.EMenuItems);
        newMenuItem.put(Globals.EMENU_ITEM_NAME, itemName.toLowerCase());
        newMenuItem.put(Globals.EMENU_ITEM_DESCRIPTION, itemDescription);
        newMenuItem.put(Globals.EMENU_ITEM_PARENT_CATEGORY, itemParentCategory.trim().toLowerCase());
        newMenuItem.put(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
        newMenuItem.put(Globals.EMENU_ITEM_PRICE, itemPrice);
        newMenuItem.put(Globals.IN_STOCK, true);
        newMenuItem.put(Globals.QTY_IN_STOCK, stockNumber);
        newMenuItem.put(Globals.DESTINATION_ID, AppPrefs.getUseType());


        if (itemPhotoUrl != null) {
            newMenuItem.put(Globals.EMENU_ITEM_PHOTO_URL, itemPhotoUrl);
        }
        newMenuItem.saveInBackground(e -> {
            if (e != null) {
                int errorCode = e.getCode();
                if (errorCode == ParseException.CONNECTION_FAILED) {
                    upsertionDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    upsertionDoneCallBack.done(null, getException(e.getMessage()));
                }
            } else {
                EMenuItem newlyCreatedEMenuItem = loadParseObjectIntoEMenuItem(newMenuItem);
                upsertionDoneCallBack.done(newlyCreatedEMenuItem, null);
                sendOutNotification(1, Globals.EMENU_ITEM_NOTIFICATION, stringifyEMenuItem(newlyCreatedEMenuItem),
                        Globals.UPDATE_TYPE_NEW_INSERTION);
            }
        });
    }

    public static void updateRestaurantOrBarInfo(String newRestaurantName,
                                                 String newRestaurantEmail,
                                                 String profilePhotoUrl,
                                                 String coverPhotoUrl,
                                                 Globals.ModifiableColor modifiableColor,
                                                 String restaurantPassword,
                                                 String adminPassword,
                                                 String adminPasswordRevealed,
                                                 String restaurantPasswordRevealed,
                                                 RestaurantUpdateDoneCallback restaurantUpdateDoneCallback) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();

//        this means selecting all from the table EmenuRestaurantAndBars
        ParseQuery<ParseObject> restaurantOrBarQuery = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);

//        object holds the response while e means error
        restaurantOrBarQuery.getInBackground(restaurantOrBarId, (object, e) -> {
            if (e == null && object != null) {
                if (StringUtils.isNotEmpty(newRestaurantName)) {

//                    this means that the field in the database has been updated using the newRestaurantName variable
                    object.put(Globals.RESTAURANT_OR_BAR_NAME, newRestaurantName);
                }
                if (StringUtils.isNotEmpty(newRestaurantEmail)) {
                    object.put(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, newRestaurantEmail);
                }
                if (modifiableColor != null) {
                    if (modifiableColor.getColorType() == Globals.ModifiableColor.ColorType.PRIMARY) {
                        object.put(Globals.RESTAURANT_OR_BAR_PRIMARY_COLOR, modifiableColor.getColor());
                    } else if (modifiableColor.getColorType() == Globals.ModifiableColor.ColorType.SECONDARY) {
                        object.put(Globals.RESTAURANT_OR_BAR_SECONDARY_COLOR, modifiableColor.getColor());
                    } else {
                        object.put(Globals.RESTAURANT_OR_BAR_TERTIARY_COLOR, modifiableColor.getColor());
                    }
                }
                if (StringUtils.isNotEmpty(profilePhotoUrl)) {
                    object.put(Globals.RESTAURANT_OR_BAR_PROFILE_PHOTO_URL, profilePhotoUrl);
                }
                if (StringUtils.isNotEmpty(coverPhotoUrl)) {
                    object.put(Globals.RESTAURANT_OR_BAR_COVER_PHOTO_URL, coverPhotoUrl);
                }
                if (StringUtils.isNotEmpty(restaurantPassword)) {
                    object.put(Globals.RESTAURANT_OR_BAR_PASSWORD, restaurantPassword);
                    object.put(Globals.RESTAURANT_OR_BAR_REVEALED_PASSWORD, restaurantPasswordRevealed);
                }
                if (StringUtils.isNotEmpty(adminPassword)) {
                    object.put(Globals.RESTAURANT_OR_BAR_ADMIN_PASSWORD, adminPassword);
                    object.put(Globals.RESTAURANT_OR_BAR_REVEALED_PASSWORD, adminPasswordRevealed);
                }
                object.saveInBackground(e1 -> {
                    if (e1 == null) {
                        RestaurantOrBarInfo result = loadParseObjectIntoRestaurantOrBarModel(object);
                        restaurantUpdateDoneCallback.done(result, null);
                    } else {
                        restaurantUpdateDoneCallback.done(null, e1);
                        object.saveEventually();
                    }
                });
            } else {
                restaurantUpdateDoneCallback.done(null, e);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void updateEMenuItem(String itemId, String itemName,
                                       String itemDescription,
                                       String itemPrice,
                                       String itemParentCategory,
                                       String itemPhotoUrl,
                                       EMenuItemUpdateDoneCallback upsertionDoneCallBack) {
        ParseQuery<ParseObject> existingObjectQuery = ParseQuery.getQuery(Globals.EMenuItems);
        existingObjectQuery.getInBackground(itemId, (retrievedObject, e) -> {
            if (e == null) {
                retrievedObject.put(Globals.EMENU_ITEM_NAME, itemName.toLowerCase());
                retrievedObject.put(Globals.EMENU_ITEM_DESCRIPTION, itemDescription);
                retrievedObject.put(Globals.EMENU_ITEM_PARENT_CATEGORY, itemParentCategory.trim().toLowerCase());
                retrievedObject.put(Globals.RESTAURANT_OR_BAR_ID, AppPrefs.getRestaurantOrBarId());
                retrievedObject.put(Globals.EMENU_ITEM_PRICE, itemPrice);
                if (itemPhotoUrl != null) {
                    retrievedObject.put(Globals.EMENU_ITEM_PHOTO_URL, itemPhotoUrl);
                }
                retrievedObject.saveInBackground(e1 -> {
                    if (e1 != null) {
                        int errorCode = e1.getCode();
                        if (errorCode == ParseException.CONNECTION_FAILED) {
                            upsertionDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                        } else {
                            upsertionDoneCallBack.done(null, getException(e1.getMessage()));
                        }
                    } else {
                        EMenuItem newlyCreatedEMenuItem = loadParseObjectIntoEMenuItem(retrievedObject);
                        upsertionDoneCallBack.done(newlyCreatedEMenuItem, null);
                        sendOutNotification(1, Globals.EMENU_ITEM_NOTIFICATION, stringifyEMenuItem(newlyCreatedEMenuItem), Globals.UPDATE_TYPE_UPDATE);
                    }
                });
            } else {
                int errorCode = e.getCode();
                if (errorCode == ParseException.CONNECTION_FAILED) {
                    upsertionDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    upsertionDoneCallBack.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    public static void checkAndCreateNewCategory(String newCategory, String entryUploadUrl) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> categoriesQuery = ParseQuery.getQuery(Globals.EMENU_ITEMS_CATEGORIES);
        categoriesQuery.whereEqualTo(Globals.EMENU_ITEM_CATEGORY, newCategory.trim().toLowerCase());
        categoriesQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        categoriesQuery.getFirstInBackground((object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    createNewItemCategory(newCategory, entryUploadUrl, restaurantOrBarId);
                } else {
                    EMenuLogger.d(TAG, "Category check error happened with message = " + e.getMessage());
                }
            }
        });
    }

    private static void createNewItemCategory(String newCategory, String entryUploadUrl,
                                              String restaurantOrBarId) {
        ParseObject newEMenuItemCategoryObject = new ParseObject(Globals.EMENU_ITEMS_CATEGORIES);
        newEMenuItemCategoryObject.put(Globals.EMENU_ITEM_CATEGORY, newCategory.trim().toLowerCase());
        if (entryUploadUrl != null) {
            newEMenuItemCategoryObject.put(Globals.MENU_CATEGORY_PHOTO_URL, entryUploadUrl);
        }
        if (StringUtils.containsIgnoreCase(newCategory, "drinks")) {
            newEMenuItemCategoryObject.put(Globals.EMENU_ITEM_SUB_CATEGORY, Globals.DRINKS);
        } else {
            newEMenuItemCategoryObject.put(Globals.EMENU_ITEM_SUB_CATEGORY, Globals.NON_DRINKS);
        }
        newEMenuItemCategoryObject.put(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        newEMenuItemCategoryObject.saveInBackground();
    }

    public static void suggestAvailableCategories(String searchTerm,
                                                  EMenuItemCategoriesFetchDoneCallback categoriesFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> categoriesQuery = ParseQuery.getQuery(Globals.EMENU_ITEMS_CATEGORIES);
        categoriesQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        categoriesQuery.whereContains(Globals.EMENU_ITEM_CATEGORY, searchTerm.toLowerCase());
        categoriesQuery.findInBackground((objects, e) -> {
            if (objects != null && !objects.isEmpty()) {
                List<EMenuItemCategory> eMenuItemCategories = new ArrayList<>();
                for (ParseObject parseObject : objects) {
                    EMenuItemCategory eMenuItemCategory = loadParseObjectIntoEMenuItemCategory(parseObject);
                    if (!eMenuItemCategories.contains(eMenuItemCategory)) {
                        eMenuItemCategories.add(eMenuItemCategory);
                    }
                }
                categoriesFetchDoneCallBack.done(eMenuItemCategories, null);
            }
        });
    }

    public void addEMenuItemToCustomerCart(String deviceId,
                                           String tableTagValue,
                                           String customerTagValue,
                                           String waiterTagValue,
                                           int increment,
                                           EMenuItem eMenuItem,
                                           EMenuCustomerOrderCallBack eMenuCustomerOrderCallBack) {
        EMenuOrder customerOrder = getCustomerOrder(tableTagValue, customerTagValue);
        if (customerOrder == null) {
            customerOrder = createNewOrderInLocalDataStore(deviceId, tableTagValue, customerTagValue, waiterTagValue, eMenuItem);
            eMenuCustomerOrderCallBack.done(customerOrder, eMenuItem, null);
        } else {
            List<EMenuItem> existingItems = customerOrder.getItems();
            if (existingItems == null) {
                existingItems = new ArrayList<>();
            }
            if (existingItems.contains(eMenuItem)) {
                int indexOfEMenuItem = existingItems.indexOf(eMenuItem);
                eMenuItem = existingItems.get(indexOfEMenuItem);
                int previouslyOrderedQuantity = eMenuItem.getOrderedQuantity();
                int quantityIncrement = previouslyOrderedQuantity + increment;


                eMenuItem.setOrderedQuantity(quantityIncrement);
                eMenuItem.setTableTag(tableTagValue);
                eMenuItem.setCustomerTag(customerTagValue);
                eMenuItem.setWaiterTag(waiterTagValue);
                existingItems.set(indexOfEMenuItem, eMenuItem);

            } else {


                existingItems.add(eMenuItem);
            }
            customerOrder.setItems(existingItems);
            customerOrder.setDirty(true);
            customerOrder.update();
            eMenuCustomerOrderCallBack.done(customerOrder, eMenuItem, null);
        }
    }

    public static void decrementEMenuItemFromCustomerOrder(int forcedQuantity,
                                                           EMenuOrder eMenuOrder,
                                                           EMenuItem eMenuItem,
                                                           EMenuCustomerOrderCallBack eMenuCustomerOrderCallBack) {
        List<EMenuItem> items = eMenuOrder.getItems();
        if (items.contains(eMenuItem)) {
            int indexOfItem = items.indexOf(eMenuItem);
            EMenuLogger.d("QuantityLogger", "Item Index =" + indexOfItem);
            int existingQuantity = eMenuItem.getOrderedQuantity();
            EMenuLogger.d("QuantityLogger", "Existing Quantity=" + existingQuantity);
            int newQuantity;
            if (forcedQuantity != -1) {
                newQuantity = forcedQuantity;
            } else {
                newQuantity = existingQuantity - 1;
            }
            EMenuLogger.d("QuantityLogger", "New Quantity=" + newQuantity);
            if (newQuantity <= 0) {
                if (items.size() == 1) {
                    eMenuOrder.delete();
                } else {
                    items.remove(eMenuItem);
                    eMenuOrder.setItems(items);
                    eMenuOrder.setDirty(true);
                    eMenuOrder.update();
                    EventBus.getDefault().post(new EMenuItemRemovedFromOrderEvent(eMenuOrder, eMenuItem, eMenuOrder.getCustomerTag()));
                }
            } else {
                eMenuItem.setOrderedQuantity(newQuantity);
                items.set(indexOfItem, eMenuItem);
                eMenuOrder.setItems(items);
                eMenuOrder.setDirty(true);
                eMenuOrder.update();
            }
            eMenuCustomerOrderCallBack.done(eMenuOrder, eMenuItem, null);
        } else {
            eMenuCustomerOrderCallBack.done(eMenuOrder, eMenuItem, getException("Not found for delete"));
        }
    }

    public static EMenuOrder getCustomerOrder(String tableTagValue, String customerTagValue) {
        return SQLite
                .select()
                .from(EMenuOrder.class)
                .where(EMenuOrder_Table.tableTag.eq(tableTagValue))
                .and(EMenuOrder_Table.customerTag.eq(customerTagValue))
                .querySingle();
    }

    private static EMenuOrder createNewOrderInLocalDataStore(String deviceId,
                                                             String tableTagValue,
                                                             String customerTagValue,
                                                             String waiterTagValue,
                                                             EMenuItem eMenuItem) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        EMenuOrder newEMenuOrder = new EMenuOrder();
        newEMenuOrder.setRestaurantOrBarId(restaurantOrBarId);
        newEMenuOrder.setEMenuOrderId(generateRandomSha256Digest());
        newEMenuOrder.setTableTag(tableTagValue);
        newEMenuOrder.setWaiterTag(waiterTagValue);
        newEMenuOrder.setCustomerTag(customerTagValue);
        newEMenuOrder.setWaiterDeviceId(deviceId);
        newEMenuOrder.setCreatedAt(getCurrentTime());
        newEMenuOrder.setUpdatedAt(getCurrentTime());
        newEMenuOrder.setOrderProgressStatus(Globals.OrderProgressStatus.NOT_YET_SENT);
        newEMenuOrder.setDirty(true);
        List<EMenuItem> items = new ArrayList<>();
        items.add(eMenuItem);
        newEMenuOrder.setItems(items);
        newEMenuOrder.save();
        return newEMenuOrder;
    }

    private static long getCurrentTime() {
        return new Date().getTime();
    }

    private static String generateRandomSha256Digest() {
        return RandomStringUtils
                .random(64, true, false)
                .toLowerCase(Locale.getDefault());
    }

    public static boolean areUnProcessedOrdersAvailable() {
        return SQLite
                .select()
                .from(EMenuOrder.class)
                .where(EMenuOrder_Table.dirty.eq(true))
                .querySingle() != null;
    }

    public static void fetchAllUnProcessedOrders(EMenuOrdersFetchDoneCallBack unProcessedOrdersFetchDoneCallBack) {
        List<EMenuOrder> eMenuOrders = SQLite
                .select()
                .from(EMenuOrder.class)
                .where(EMenuOrder_Table.dirty.eq(true))
                .queryList();
        if (!eMenuOrders.isEmpty()) {
            unProcessedOrdersFetchDoneCallBack.done(eMenuOrders, null);
        } else {
            unProcessedOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
        }
    }

    public static void pushOrdersToKitchenOrBar(List<EMenuOrder> orders) {
        for (EMenuOrder eMenuOrder : orders) {
            if (eMenuOrder.getOrderProgressStatus() == Globals.OrderProgressStatus.NOT_YET_SENT) {
                eMenuOrder.setOrderProgressStatus(Globals.OrderProgressStatus.PENDING);
            }else if (eMenuOrder.getOrderProgressStatus() == Globals.OrderProgressStatus.REJECTED){
                eMenuOrder.setOrderProgressStatus(Globals.OrderProgressStatus.REJECTED);
            }
            checkAndPushOrder(eMenuOrder, (eMenuOrder1, exists, e) -> {
                if (e == null) {
                    eMenuOrder.setDirty(false);
                    eMenuOrder.update();
                    sendOutNotification(orders.size(), Globals.EMENU_ORDER_NOTIFICATION, serializeEMenuOrder(eMenuOrder), exists ? Globals.UPDATE_TYPE_UPDATE : Globals.UPDATE_TYPE_NEW_INSERTION);
                }
            });
        }
    }

    private static ParseObject createParseObjectFromOrder(ParseObject existingObject, EMenuOrder eMenuOrder) {
        boolean has_drink = false, has_food = false;
        // loop through the list of eMenuItems
        List<EMenuItem> eMenuItems = eMenuOrder.getItems();
        for(EMenuItem eMenuItem: eMenuItems){
            if (eMenuItem.parentCategory.equals("drinks")) {
                has_drink = true;
            }else{
                has_food = true;
            }
        }

        ParseObject newOrderObject;
        if (existingObject != null) {
            newOrderObject = existingObject;
        } else {
            newOrderObject = new ParseObject(Globals.EMENU_ORDERS);
        }
        newOrderObject.put(Globals.CUSTOMER_TAG, eMenuOrder.getCustomerTag());
        newOrderObject.put(Globals.WAITER_TAG, eMenuOrder.getWaiterTag());
        newOrderObject.put(Globals.TABLE_TAG, eMenuOrder.getTableTag());
        newOrderObject.put(Globals.ORDER_ID, eMenuOrder.getOrderId());
        newOrderObject.put("kitchen_received_notify", false);
        newOrderObject.put("waiter_received_notify", false);
        newOrderObject.put("waiter_received_notify_drink", false);
        newOrderObject.put("bar_received_notify", false);

        newOrderObject.put(Globals.HAS_DRINK, has_drink);
        newOrderObject.put(Globals.HAS_FOOD, has_food);
        newOrderObject.put(Globals.FOOD_READY, false);
        newOrderObject.put(Globals.DRINK_READY, false);
        String waiterDeviceId = eMenuOrder.getWaiterDeviceId();
        if (waiterDeviceId != null) {
            newOrderObject.put(Globals.WAITER_DEVICE_ID, waiterDeviceId);
        }
        newOrderObject.put(Globals.RESTAURANT_OR_BAR_ID, eMenuOrder.getRestaurantOrBarId());
        String kitchenAttendantTag = eMenuOrder.getKitchenAttendantTag();
        if (kitchenAttendantTag != null) {
            newOrderObject.put(Globals.KITCHEN_ATTENDANT_TAG, kitchenAttendantTag);
        }
        String kitchenAttendantDeviceId = eMenuOrder.getKitchenAttendantDeviceId();
        if (kitchenAttendantDeviceId != null) {
            newOrderObject.put(Globals.KITCHEN_ATTENDANT_DEVICE_ID, kitchenAttendantDeviceId);
        }
        String barAttendantTag = eMenuOrder.getBarAttendantTag();
        if (barAttendantTag != null) {
            newOrderObject.put(Globals.BAR_ATTENDANT_TAG, barAttendantTag);
        }
        String barAttendantDeviceId = eMenuOrder.getBarAttendantDeviceId();
        if (barAttendantDeviceId != null) {
            newOrderObject.put(Globals.BAR_ATTENDANT_DEVICE_ID, barAttendantDeviceId);
        }
        Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();
        if (orderProgressStatus != null) {
            String orderProgressStatusString = serializeOrderProgress(orderProgressStatus);
            newOrderObject.put(Globals.ORDER_PROGRESS_STATUS, orderProgressStatusString);
        }
        Globals.OrderPaymentStatus orderPaymentStatus = eMenuOrder.getOrderPaymentStatus();
        if (orderPaymentStatus != null) {
            String orderPaymentStatusString = serializeOrderPayment(orderPaymentStatus);
            newOrderObject.put(Globals.ORDER_PAYMENT_STATUS, orderPaymentStatusString);
        }
        List<EMenuItem> orderedItems = eMenuOrder.getItems();
        if (orderedItems != null && !orderedItems.isEmpty()) {
            String orderedItemsString = serializeEMenuItems(orderedItems);
            newOrderObject.put(Globals.ORDERED_ITEMS, orderedItemsString);
        }
        return newOrderObject;
    }

    private static EMenuOrder loadParseObjectIntoEMenuOrder(ParseObject parseObject) {
        EMenuOrder eMenuOrder = new EMenuOrder();
        String tableTag = parseObject.getString(Globals.TABLE_TAG);
        String customerTag = parseObject.getString(Globals.CUSTOMER_TAG);
        String waiterTag = parseObject.getString(Globals.WAITER_TAG);
        String waiterDeviceId = parseObject.getString(Globals.WAITER_DEVICE_ID);
        String orderId = parseObject.getString(Globals.ORDER_ID);
        String restaurantOrBarId = parseObject.getString(Globals.RESTAURANT_OR_BAR_ID);
        String kitchenAttendantTag = parseObject.getString(Globals.KITCHEN_ATTENDANT_TAG);
        String kitchenAttendantDeviceId = parseObject.getString(Globals.KITCHEN_ATTENDANT_DEVICE_ID);
        String barAttendantTag = parseObject.getString(Globals.BAR_ATTENDANT_TAG);
        String barAttendantDeviceId = parseObject.getString(Globals.BAR_ATTENDANT_DEVICE_ID);
        String orderProgressStatusString = parseObject.getString(Globals.ORDER_PROGRESS_STATUS);
        Globals.OrderProgressStatus orderProgressStatus = null;
        if (orderProgressStatusString != null) {
            orderProgressStatus = getBackOrderProgressFromString(orderProgressStatusString);
        }
        Globals.OrderPaymentStatus orderPaymentStatus = null;
        String orderPaymentStatusString = parseObject.getString(Globals.ORDER_PAYMENT_STATUS);
        if (orderPaymentStatusString != null) {
            orderPaymentStatus = getBackPaymentStatusFromString(orderPaymentStatusString);
        }
        List<EMenuItem> orderedItems = new ArrayList<>();
        String itemsString = parseObject.getString(Globals.ORDERED_ITEMS);
        if (itemsString != null) {
            orderedItems = getBackEMenuItemsFromString(itemsString);
        }
        eMenuOrder.setOrderId(orderId);
        eMenuOrder.setTableTag(tableTag);
        eMenuOrder.setCustomerTag(customerTag);
        eMenuOrder.setWaiterTag(waiterTag);
        eMenuOrder.setRestaurantOrBarId(restaurantOrBarId);
        eMenuOrder.setKitchenAttendantTag(kitchenAttendantTag);
        eMenuOrder.setBarAttendantTag(barAttendantTag);
        eMenuOrder.setCreatedAt(parseObject.getCreatedAt().getTime());
        eMenuOrder.setUpdatedAt(parseObject.getUpdatedAt().getTime());
        eMenuOrder.setKitchenAttendantDeviceId(kitchenAttendantDeviceId);
        eMenuOrder.setBarAttendantDeviceId(barAttendantDeviceId);
        eMenuOrder.setOrderProgressStatus(orderProgressStatus);
        eMenuOrder.setOrderPaymentStatus(orderPaymentStatus);
        eMenuOrder.setItems(orderedItems);
        eMenuOrder.setWaiterDeviceId(waiterDeviceId);
        eMenuOrder.setDirty(false);
        return eMenuOrder;
    }

    private static String serializeOrderProgress(Globals.OrderProgressStatus orderProgressStatus) {
        return new Gson().toJson(orderProgressStatus, new TypeToken<Globals.OrderProgressStatus>() {
        }.getType());
    }

    private static Globals.OrderProgressStatus getBackOrderProgressFromString(String serialized) {
        return new Gson().fromJson(serialized, new TypeToken<Globals.OrderProgressStatus>() {
        }.getType());
    }

    private static String serializeOrderPayment(Globals.OrderPaymentStatus orderPaymentStatus) {
        return new Gson().toJson(orderPaymentStatus, new TypeToken<Globals.OrderPaymentStatus>() {
        }.getType());
    }

    private static Globals.OrderPaymentStatus getBackPaymentStatusFromString(String serialized) {
        return new Gson().fromJson(serialized, new TypeToken<Globals.OrderPaymentStatus>() {
        }.getType());
    }

    private static String serializeEMenuItems(List<EMenuItem> eMenuItems) {
        return new Gson().toJson(eMenuItems, new TypeToken<List<EMenuItem>>() {
        }.getType());
    }

    private static List<EMenuItem> getBackEMenuItemsFromString(String serialized) {
        return new Gson().fromJson(serialized, new TypeToken<List<EMenuItem>>() {
        }.getType());
    }

    public static void updateOrderPaymentStatus(String orderId, Globals.OrderPaymentStatus orderPaymentStatus, PaymentDoneCallBack paymentDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> emenuOrderQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        emenuOrderQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        emenuOrderQuery.whereEqualTo(Globals.ORDER_ID, orderId);
        emenuOrderQuery.getFirstInBackground((object, e) -> {
            if (e == null) {
                String orderPaymentStatusString = serializeOrderPayment(orderPaymentStatus);
                String progressStatusString = serializeOrderProgress(Globals.OrderProgressStatus.DONE);
                object.put(Globals.ORDER_PAYMENT_STATUS, orderPaymentStatusString);
                object.put(Globals.ORDER_PROGRESS_STATUS, progressStatusString);
                object.saveInBackground(e1 -> {
                    if (e1 == null) {
                        EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(object);
                        eMenuOrder.setOrderPaymentStatus(orderPaymentStatus);
                        String objectGSON = serializeEMenuOrder(eMenuOrder);
                        paymentDoneCallBack.done(orderPaymentStatus, null);
                        sendOutNotification(1, Globals.EMENU_ORDER_NOTIFICATION, objectGSON, Globals.UPDATE_TYPE_UPDATE);
                        EventBus.getDefault().post(new OrderPaidForEvent(eMenuOrder));
                        eMenuOrder.delete();
                    } else {
                        paymentDoneCallBack.done(null, e1);
                    }
                });
            } else {
                paymentDoneCallBack.done(null, getException("Sorry, failed to finalize payment. Please try again"));
            }
        });
    }

    private static String serializeEMenuOrder(EMenuOrder eMenuOrder) {
        return new Gson().toJson(eMenuOrder, new TypeToken<EMenuOrder>() {
        }.getType());
    }

    public static void updateEMenuOrderProgress(String orderId, Globals.OrderProgressStatus orderProgressStatus) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> emenuOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        emenuOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        emenuOrdersQuery.whereEqualTo(Globals.ORDER_ID, orderId);
        emenuOrdersQuery.getFirstInBackground((object, e) -> {
            if (e == null) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    String orderProgressString = serializeOrderProgress(orderProgressStatus);

                    //iterate through and check if has food/drink or food and drink
                    Boolean hasFood = (Boolean) object.get(Globals.HAS_FOOD);
                    Boolean hasDrink = (Boolean) object.get(Globals.HAS_DRINK);
                    Boolean foodReady = (Boolean) object.get(Globals.FOOD_READY);
                    Boolean drinkReady = (Boolean) object.get(Globals.DRINK_READY);

                    if (hasDrink && hasFood){
                        // check if the user is a bar or kitchen attendant
                        if(currentUser.getInt("user_type") == 3 || currentUser.getInt("user_type") == Globals.ADMIN_TAG_ID){
                            // bar attendant or admin
                            if(foodReady){
                                // set status to done
                                object.put(Globals.ORDER_PROGRESS_STATUS, '"'+"DONE"+'"');
                            }else{
                                // set status to almost done
                                object.put(Globals.ORDER_PROGRESS_STATUS, '"'+"ALMOST DONE"+'"');
                            }
                            object.put(Globals.DRINK_READY, true);
                        }
                        if(currentUser.getInt("user_type") == 2 || currentUser.getInt("user_type") == Globals.ADMIN_TAG_ID){
                            // kitchen attendant or admin
                            if(drinkReady){
                                // set status to done
                                object.put(Globals.ORDER_PROGRESS_STATUS, '"'+"DONE"+'"');
                            }else{
                                // set status to almost done
                                object.put(Globals.ORDER_PROGRESS_STATUS, '"'+"ALMOST DONE"+'"');
                            }
                            object.put(Globals.FOOD_READY, true);
                        }
                    }
                     else if (hasDrink){
                         if(orderProgressString.equals('"'+"DONE"+'"')){
                             object.put(Globals.DRINK_READY, true);
                         }
                        object.put(Globals.ORDER_PROGRESS_STATUS, orderProgressString);
                    }
                    else if (hasFood){
                        if(orderProgressString.equals('"'+"DONE"+'"') ) {
                            object.put(Globals.FOOD_READY, true);
                        }
                        object.put(Globals.ORDER_PROGRESS_STATUS, orderProgressString);
                    }
                } else {
                    // show the signup or login screen
                }
                object.saveInBackground(e1 -> {
                    if (e1 == null) {
                        EMenuOrder updatedOrder = loadParseObjectIntoEMenuOrder(object);
                        EventBus.getDefault().post(new OrderUpdatedEvent(updatedOrder, false));
                    }
                });
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private static void checkAndPushOrder(EMenuOrder eMenuOrder,
                                          UnProcessedOrderPushCallBack unProcessedOrderPushCallBack) {
        String deviceId = AppPrefs.getDeviceId();
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> ordersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        ordersQuery.whereEqualTo(Globals.ORDER_ID, eMenuOrder.getOrderId());
        ordersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        ordersQuery.getFirstInBackground((object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    pushNewOrder(eMenuOrder, unProcessedOrderPushCallBack, deviceId);
                } else {
                    unProcessedOrderPushCallBack.done(null, false, getException(e.getMessage()));
                }
            } else {
                ParseObject retrievedObject = createParseObjectFromOrder(object, eMenuOrder);
                retrievedObject.saveInBackground(ex -> {
                    if (ex == null) {
                        eMenuOrder.setDirty(false);
                        eMenuOrder.update();
                        unProcessedOrderPushCallBack.done(eMenuOrder, true, null);
                    } else {
                        unProcessedOrderPushCallBack.done(null, true, ex);
                    }
                });
            }
        });
    }

    private static void pushNewOrder(EMenuOrder eMenuOrder, UnProcessedOrderPushCallBack unProcessedOrderPushCallBack, String deviceId) {
        Globals.OrderProgressStatus orderProgressStatus = eMenuOrder.getOrderProgressStatus();
        eMenuOrder.setOrderProgressStatus(orderProgressStatus == null ? Globals.OrderProgressStatus.PENDING : orderProgressStatus);
        eMenuOrder.setDirty(false);
        ParseObject newOrder = createParseObjectFromOrder(null, eMenuOrder);
        if (deviceId != null) {
            newOrder.put(Globals.WAITER_DEVICE_ID, deviceId);
        }
        newOrder.saveInBackground(e -> {
            if (e == null) {
                unProcessedOrderPushCallBack.done(eMenuOrder, false, null);
            } else {
                unProcessedOrderPushCallBack.done(null, false, e);
            }
        });
    }

    private static void sendOutNotification(int count, String notificationType, String notificationData, String updateType) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> newOrdersNotificationQuery = ParseQuery.getQuery(Globals.NOTIFICATIONS);
        newOrdersNotificationQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        newOrdersNotificationQuery.getFirstInBackground((object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    ParseObject newNotificationObject = new ParseObject(Globals.NOTIFICATIONS);
                    newNotificationObject.put(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
                    newNotificationObject.put(Globals.NOTIFICATION_DATA, notificationData);
                    newNotificationObject.put(Globals.NOTIF_COUNT, count);
                    newNotificationObject.put(Globals.NOTIFICATION_TYPE, notificationType);
                    newNotificationObject.put(Globals.UPDATE_TYPE, updateType);
                    newNotificationObject.saveInBackground();
                }
            } else {
                object.put(Globals.NOTIFICATION_DATA, notificationData);
                object.put(Globals.NOTIF_COUNT, count);
                object.put(Globals.UPDATE_TYPE, updateType);
                object.put(Globals.NOTIFICATION_TYPE, notificationType);
                object.saveInBackground();
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void fetchIncomingOrdersContainingDrinks(int skip, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        String deviceId = AppPrefs.getDeviceId();
        ParseQuery<ParseObject> drinkOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        drinkOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        drinkOrdersQuery.whereDoesNotExist(Globals.ORDER_PAYMENT_STATUS);
        drinkOrdersQuery.whereEqualTo(Globals.HAS_DRINK, true);
        drinkOrdersQuery.orderByDescending("createdAt");
        if (skip != 0) {
            drinkOrdersQuery.setSkip(skip);
        }
        drinkOrdersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedOrders = new ArrayList<>();
                if (!objects.isEmpty()) {
                    for (ParseObject orderObject : objects) {
                        EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(orderObject);
                        String barAttendantDeviceId = eMenuOrder.getBarAttendantDeviceId();
                        if (ArkeSdkDemoApplication.containsDrinks(eMenuOrder).first) {
                            if (barAttendantDeviceId == null) {
                                if (!retrievedOrders.contains(eMenuOrder)) {
                                    retrievedOrders.add(eMenuOrder);
                                }
                            } else {
                                if (barAttendantDeviceId.equals(deviceId)) {
                                    if (!retrievedOrders.contains(eMenuOrder)) {
                                        retrievedOrders.add(eMenuOrder);
                                    }
                                }
                            }
                        }
                    }
                    eMenuOrdersFetchDoneCallBack.done(retrievedOrders, null);
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void fetchOutgoingOrders(int skip, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        String deviceId = AppPrefs.getDeviceId();
        ParseQuery<ParseObject> eMenuOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        eMenuOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        eMenuOrdersQuery.whereEqualTo(Globals.WAITER_TAG, ParseUser.getCurrentUser().getObjectId()); // get orders WRT logged in user
        eMenuOrdersQuery.whereDoesNotExist(Globals.ORDER_PAYMENT_STATUS);
        eMenuOrdersQuery.orderByDescending("createdAt");
        if (skip != 0) {
            eMenuOrdersQuery.setSkip(skip);
        }
        //Ideally, we should only fetch orders taken on the current terminal
//        if (deviceId != null) {
//            eMenuOrdersQuery.whereEqualTo(Globals.WAITER_DEVICE_ID, deviceId);
//        }
        eMenuOrdersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedOrders = new ArrayList<>();
                if (!objects.isEmpty()) {
                    for (ParseObject orderObject : objects) {
                        EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(orderObject);
                        if (!retrievedOrders.contains(eMenuOrder)) {
                            retrievedOrders.add(eMenuOrder);
                        }
                    }
                    eMenuOrdersFetchDoneCallBack.done(retrievedOrders, null);
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void fetchIncomingKitchenOrders(int skip, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        String deviceId = AppPrefs.getDeviceId();
        ParseQuery<ParseObject> eMenuOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        eMenuOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        eMenuOrdersQuery.whereDoesNotExist(Globals.ORDER_PAYMENT_STATUS);
        eMenuOrdersQuery.whereEqualTo(Globals.HAS_FOOD, true);
        eMenuOrdersQuery.orderByDescending("createdAt");
        if (skip != 0) {
            eMenuOrdersQuery.setSkip(skip);
        }
        eMenuOrdersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedOrders = new ArrayList<>();
                if (!objects.isEmpty()) {
                    for (ParseObject orderObject : objects) {
                        EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(orderObject);
                        String kitchenAttendantDeviceId = eMenuOrder.getKitchenAttendantDeviceId();
                        if (kitchenAttendantDeviceId == null) {
                            if (!retrievedOrders.contains(eMenuOrder)) {
                                retrievedOrders.add(eMenuOrder);
                            }
                        } else {
                            // fetching orders WRT kitchen device id
                            if (kitchenAttendantDeviceId.equals(deviceId)) {
                                if (!retrievedOrders.contains(eMenuOrder)) {
                                    retrievedOrders.add(eMenuOrder);
                                }
                            }
                        }
                    }
                    eMenuOrdersFetchDoneCallBack.done(retrievedOrders, null);
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    private static Exception getException(String message) {
        if (message.toLowerCase().contains("nullpointerexception")) {
            message = "Unresolvable Error";
        }
        return new Exception(message);
    }

    public static void searchOutgoingOrders(int skip, String searchString, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        String deviceId = AppPrefs.getDeviceId();
        ParseQuery<ParseObject> customerTagQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        if (searchString != null) {
            customerTagQuery.whereContains(Globals.CUSTOMER_TAG, searchString);
        }
        ParseQuery<ParseObject> tableTagQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        if (searchString != null) {
            tableTagQuery.whereContains(Globals.TABLE_TAG, searchString);
        }
        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(customerTagQuery);
        queries.add(tableTagQuery);
        ParseQuery<ParseObject> resultantQuery = ParseQuery.or(queries);
        resultantQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        if (deviceId != null) {
            //This ensures that the waiter can only search for orders sent out from the current terminal
            resultantQuery.whereEqualTo(Globals.WAITER_DEVICE_ID, deviceId);
        }
        resultantQuery.whereDoesNotExist(Globals.ORDER_PAYMENT_STATUS);
        resultantQuery.orderByDescending("createdAt");
        if (skip != 0) {
            resultantQuery.setSkip(skip);
        }
        resultantQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedOrders = new ArrayList<>();
                if (!objects.isEmpty()) {
                    for (ParseObject orderObject : objects) {
                        EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(orderObject);
                        if (!retrievedOrders.contains(eMenuOrder)) {
                            retrievedOrders.add(eMenuOrder);
                        }
                    }
                    eMenuOrdersFetchDoneCallBack.done(retrievedOrders, null);
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void searchIncomingOrders(String searchString, int skip, EMenuOrdersFetchDoneCallBack eMenuOrdersFetchDoneCallBack) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> eMenuOrdersQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        int useType = AppPrefs.getUseType();
        String deviceId = AppPrefs.getDeviceId();
        eMenuOrdersQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        if (searchString != null) {
            eMenuOrdersQuery.whereContains(Globals.CUSTOMER_TAG, searchString);
        }
        eMenuOrdersQuery.whereDoesNotExist(Globals.ORDER_PAYMENT_STATUS);
        eMenuOrdersQuery.orderByDescending("createdAt");
        if (skip != 0) {
            eMenuOrdersQuery.setSkip(skip);
        }
        eMenuOrdersQuery.findInBackground((objects, e) -> {
            if (e == null) {
                List<EMenuOrder> retrievedOrders = new ArrayList<>();
                if (!objects.isEmpty()) {
                    for (ParseObject orderObject : objects) {
                        EMenuOrder eMenuOrder = loadParseObjectIntoEMenuOrder(orderObject);
                        String attendanceDeviceId;
                        if (useType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
                            attendanceDeviceId = eMenuOrder.getKitchenAttendantDeviceId();
                        } else {
                            attendanceDeviceId = eMenuOrder.getBarAttendantDeviceId();
                        }
                        if (attendanceDeviceId == null) {
                            if (!retrievedOrders.contains(eMenuOrder)) {
                                retrievedOrders.add(eMenuOrder);
                            }
                        } else {
                            if (attendanceDeviceId.equals(deviceId)) {
                                if (!retrievedOrders.contains(eMenuOrder)) {
                                    retrievedOrders.add(eMenuOrder);
                                }
                            }
                        }
                    }
                    eMenuOrdersFetchDoneCallBack.done(retrievedOrders, null);
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                }
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(Globals.EMPTY_PLACEHOLDER_ERROR_MESSAGE));
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(getNetworkErrorMessage()));
                } else {
                    eMenuOrdersFetchDoneCallBack.done(null, getException(e.getMessage()));
                }
            }
        });
    }

    public static void markItemAsTaken(String orderId,
                                       OrderUpdateDoneCallback orderUpdateDoneCallback) {
        ParseQuery<ParseObject> orderQuery = ParseQuery.getQuery(Globals.EMENU_ORDERS);
        String deviceId = AppPrefs.getDeviceId();
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        orderQuery.whereEqualTo(Globals.ORDER_ID, orderId);
        orderQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        orderQuery.getFirstInBackground((object, e) -> {
            if (object != null) {
                int appUseType = AppPrefs.getUseType();
                String takenStatus = null;
                if (appUseType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
                    takenStatus = object.getString(Globals.KITCHEN_ATTENDANT_DEVICE_ID);
                } else if (appUseType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
                    takenStatus = object.getString(Globals.BAR_ATTENDANT_DEVICE_ID);
                }
                if (deviceId != null && takenStatus != null) {
                    String errorMessage = " Order already taken cared of by another attendant";
                    orderUpdateDoneCallback.done(null, getException(errorMessage));
                } else {
                    if (deviceId != null) {
                        //
                        object.put(appUseType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()
                                        ? Globals.KITCHEN_ATTENDANT_ID
                                        : Globals.BAR_ATTENDANT_ID,
                                deviceId);
                        String orderProgressString = serializeOrderProgress(Globals.OrderProgressStatus.PROCESSING);
                        object.put(Globals.ORDER_PROGRESS_STATUS, orderProgressString);
                    }
                    object.saveInBackground(e1 -> {
                        if (e1 == null) {
                            orderUpdateDoneCallback.done(loadParseObjectIntoEMenuOrder(object), null);
                        } else {
                            orderUpdateDoneCallback.done(null, e1);
                        }
                    });
                }
            }
        });
    }

    public static void resetAllColorsToDefault(int defaultPrimary, int defaultSecondary, int defaultTertiary) {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        ParseQuery<ParseObject> restaurantOrBarQuery = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);
        restaurantOrBarQuery.getInBackground(restaurantOrBarId, (object, e) -> {
            if (e == null && object != null) {
                object.put(Globals.RESTAURANT_OR_BAR_PRIMARY_COLOR, defaultPrimary);
                object.put(Globals.RESTAURANT_OR_BAR_SECONDARY_COLOR, defaultSecondary);
                object.put(Globals.RESTAURANT_OR_BAR_TERTIARY_COLOR, defaultTertiary);
                object.saveInBackground(e1 -> loadParseObjectIntoRestaurantOrBarModel(object));
            }
        });
    }

}
