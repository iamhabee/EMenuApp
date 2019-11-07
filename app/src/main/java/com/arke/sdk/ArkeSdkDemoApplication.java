package com.arke.sdk;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.arke.sdk.api.DeviceService;
import com.arke.sdk.companions.Credentials;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.database.EMenuDb;
import com.arke.sdk.eventbuses.EMenuItemCreatedEvent;
import com.arke.sdk.eventbuses.EMenuItemDeletedEvent;
import com.arke.sdk.eventbuses.EMenuItemUpdatedEvent;
import com.arke.sdk.eventbuses.RefreshEMenuOrder;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.receivers.KitchenReceiver;
import com.arke.sdk.ui.activities.BarHomeActivity;
import com.arke.sdk.ui.activities.KitchenHomeActivity;
import com.arke.sdk.util.printer.Printer;
import com.arke.sdk.utilities.AppNotifier;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.livequery.LiveQueryException;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.ParseLiveQueryClientCallbacks;
import com.parse.livequery.SubscriptionHandling;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier;
import com.usdk.apiservice.aidl.UDeviceService;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * Application entry.
 */

public class ArkeSdkDemoApplication extends MultiDexApplication {

    private static final String TAG = "ArkeSdkDemoApplication";
    private static final String USDK_ACTION_NAME = "com.usdk.apiservice";
    private static final String USDK_PACKAGE_NAME = "com.usdk.apiservice";
    private static DeviceService deviceService;
    private static Context context;

    @SuppressLint("StaticFieldLeak")
    private static Context _INSTANCE;
    private static ParseLiveQueryClient parseLiveQueryClient;
    private static ParseQuery<ParseObject> notificationsQuery;

    public static final String CHANNEL_ORDER_COMPLETED_ID = "orderCompleted";
    public static final String CHANNEL_ORDER_RECEIVED_ID = "orderReceived";
    public static final String CHANNEL_PAYMENT_SUCCESSFUL_ID = "paymentSuccessful";
    public static final String CHANNEL_PAYMENT_FAILED_ID = "paymentFailed";
    public static final String CHANNEL_PURCHASE_COMPLETED_ID = "purchaseCompleted";
    public static final String CHANNEL_PURCHASE_FAILED_ID = "purchaseFailed";
    public static String EMENU_NOTIFICATION = "emenu.notification";

    /**
     * Create.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // Bind sdk device service.
        bindSdkDeviceService();

        // Create a global webView to load print template
        Printer.initWebView(context);

        Completable.complete().delay(3, TimeUnit.SECONDS)
                .andThen(Printer.preLoadHtml("multi-languages-template", "{}"))
                .subscribe();

        // Debug for errors
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        initContext();
        MultiDex.install(getBaseContext());
        setupDatabase();
        initParse();
        listenToIncomingNotifications();
        //create notification with channel id
        createNotificationChannels();

//        KitchenReceiver receiver = new KitchenReceiver();
//
//        IntentFilter filter = new IntentFilter(EMENU_NOTIFICATION);
//
//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        try {
            deviceService.unregister();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(serviceConnection);
        System.exit(0);
    }

    /**
     * Get context.
     */
    public static Context getContext() {
        if (context == null) {
            throw new RuntimeException("Initiate context failed");
        }

        return context;
    }

    /**
     * Get device service instance.
     */
    public static DeviceService getDeviceService() {
        if (deviceService == null) {
            throw new RuntimeException("SDK service is still not connected.");
        }

        return deviceService;
    }

    /**
     * Bind sdk service.
     */
    private void bindSdkDeviceService() {
        Intent intent = new Intent();
        intent.setAction(USDK_ACTION_NAME);
        intent.setPackage(USDK_PACKAGE_NAME);

        Log.d(TAG, "binding sdk device service...");
        boolean flag = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!flag) {
            Log.d(TAG, "SDK service binding failed.");
            return;
        }

        Log.d(TAG, "SDK service binding successfully.");
    }

    /**
     * Service connection.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "SDK service disconnected.");
            deviceService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "SDK service connected.");

            try {
                deviceService = new DeviceService(UDeviceService.Stub.asInterface(service));
                deviceService.register();
                deviceService.debugLog(true, true);
                Log.d(TAG, "SDK deviceService initiated version:" + deviceService.getVersion() + ".");
            } catch (RemoteException e) {
                throw new RuntimeException("SDK deviceService initiating failed.", e);
            }

            try {
                linkToDeath(service);
            } catch (RemoteException e) {
                throw new RuntimeException("SDK service link to death error.", e);
            }
        }

        private void linkToDeath(IBinder service) throws RemoteException {
            service.linkToDeath(() -> {
                Log.d(TAG, "SDK service is dead. Reconnecting...");
                bindSdkDeviceService();
            }, 0);
        }
    };

    private void initContext() {
        if (_INSTANCE == null) {
            _INSTANCE = this;
        }
    }

    public static void listenToIncomingNotifications() {
        String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();
        if (StringUtils.isNotEmpty(restaurantOrBarId)) {
            listenToNewIncomingNotifications(restaurantOrBarId);
        }
    }

    public static void silenceIncomingNotifications() {
        if (parseLiveQueryClient == null) {
            return;
        }
        if (notificationsQuery != null) {
            parseLiveQueryClient.unsubscribe(notificationsQuery);
        }
    }

    private static void listenToNewIncomingNotifications(String restaurantOrBarId) {
        if (getParseLiveQueryClient() == null) {
            return;
        }
        notificationsQuery = ParseQuery.getQuery(Globals.NOTIFICATIONS);
        notificationsQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        SubscriptionHandling<ParseObject> subscriptionHandling = getParseLiveQueryClient()
                .subscribe(notificationsQuery);
        subscriptionHandling.handleEvents((query, event, object) -> {
            if (event == SubscriptionHandling.Event.CREATE || event == SubscriptionHandling.Event.UPDATE) {
                notifyListenersOfNewNotification(object);
            }
        });
    }

    public static boolean isAllDrinks(List<EMenuItem> items) {
        boolean allDrinks = true;
        for (EMenuItem eMenuItem : items) {
            String parentCategory = eMenuItem.getParentCategory();
            if (!StringUtils.containsIgnoreCase(parentCategory, Globals.DRINKS)) {
                allDrinks = false;
            }
        }
        return allDrinks;
    }

    public static Pair<Boolean, Boolean> containsDrinks(EMenuOrder eMenuOrder) {
        boolean containsDrinks = false;
        boolean containsOnlyDrinks = true;
        List<EMenuItem> eMenuItems = eMenuOrder.getItems();
        if (eMenuItems != null) {
            for (EMenuItem eMenuItem : eMenuItems) {
                String category = eMenuItem.getParentCategory();
                if (StringUtils.containsIgnoreCase(category, Globals.DRINKS)) {
                    containsDrinks = true;
                }
            }
            containsOnlyDrinks = isAllDrinks(eMenuItems);
        }
        return new Pair<>(containsDrinks, containsOnlyDrinks);
    }

    @SuppressWarnings({"UnnecessaryUnboxing", "ConstantConditions"})
    private static void notifyListenersOfNewNotification(ParseObject notificationObject) {
        String notificationType = notificationObject.getString(Globals.NOTIFICATION_TYPE);
        String updateType = notificationObject.getString(Globals.UPDATE_TYPE);
        boolean deleted = updateType.equals(Globals.DELETED);
        if (notificationType.equals(Globals.EMENU_ORDER_NOTIFICATION)) {
            String orderGSON = notificationObject.getString(Globals.NOTIFICATION_DATA);
            Type objectTpe = new TypeToken<EMenuOrder>() {
            }.getType();
            EMenuOrder eMenuOrder = new Gson().fromJson(orderGSON, objectTpe);
            if (eMenuOrder != null) {
                Pair<Boolean, Boolean> drinkContainment = containsDrinks(eMenuOrder);
                int useType = AppPrefs.getUseType();
                if (useType != Globals.UseType.USE_TYPE_NONE.ordinal()) {
                    if (useType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
                        //If it's not all drinks, send a notification
                        if (!drinkContainment.second.booleanValue()) {
                            if (updateType.equals(Globals.UPDATE_TYPE_NEW_INSERTION) || updateType.equals(Globals.UPDATE_TYPE_UPDATE)) {
                                if (!KitchenHomeActivity.ACTIVE) {
                                    EMenuLogger.d("NotifLogger", "Sending a new none delete notification for kitchen orders");
                                    AppNotifier.getInstance().sendSingleNotification();
                                } else {
                                    EventBus.getDefault().post(new RefreshEMenuOrder(eMenuOrder).setDeleted(deleted));
                                }
                            } else {
                                EventBus.getDefault().post(new RefreshEMenuOrder(eMenuOrder).setDeleted(deleted));
                            }
                        }
                    } else if (useType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
                        if (!drinkContainment.first.booleanValue()) {
                            if (updateType.equals(Globals.UPDATE_TYPE_NEW_INSERTION) || updateType.equals(Globals.UPDATE_TYPE_UPDATE)) {
                                if (!BarHomeActivity.ACTIVE) {
                                    EMenuLogger.d("NotifLogger", "Sending a new none delete notification for kitchen orders");
                                    AppNotifier.getInstance().sendSingleNotification();
                                } else {
                                    EventBus.getDefault().post(new RefreshEMenuOrder(eMenuOrder).setDeleted(deleted));
                                }
                            } else {
                                EventBus.getDefault().post(new RefreshEMenuOrder(eMenuOrder).setDeleted(deleted));
                            }
                        }
                    } else {
                        EventBus.getDefault().post(new RefreshEMenuOrder(eMenuOrder).setDeleted(deleted));
                    }
                }
            }
        } else if (notificationType.equals(Globals.EMENU_ITEM_NOTIFICATION)) {
            String notificationData = notificationObject.getString(Globals.NOTIFICATION_DATA);
            if (notificationData != null) {
                EMenuItem eMenuItem = new Gson().fromJson(notificationData, new TypeToken<EMenuItem>() {
                }.getType());
                if (deleted) {
                    EventBus.getDefault().post(new EMenuItemDeletedEvent(eMenuItem));
                } else {
                    if (updateType.equals(Globals.UPDATE_TYPE_UPDATE)) {
                        EventBus.getDefault().post(new EMenuItemUpdatedEvent(eMenuItem));
                    } else {
                        EventBus.getDefault().post(new EMenuItemCreatedEvent(eMenuItem));
                    }
                }
            }
        }
    }

    public static Context getInstance() {
        return _INSTANCE;
    }

    private void setupDatabase() {
        FlowManager.init(new FlowConfig.Builder(this)
                .addDatabaseConfig(new DatabaseConfig.Builder(EMenuDb.class)
                        .modelNotifier(DirectModelNotifier.get())
                        .build()).build());
    }

    public static void initParse() {
        Parse.initialize(new Parse.Configuration.Builder(_INSTANCE)
                .applicationId(Credentials.PARSE_APPLICATION_ID)
                .clientKey(Credentials.PARSE_CLIENT_KEY)
                .server(Credentials.PARSE_SERVER_URL)
                .enableLocalDataStore()
                .clientBuilder(getOkHttpClientBuilder())
                .build());
        initParseLiveQueryClient();
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> EMenuLogger.d(DataStoreClient.TAG, message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .addInterceptor(logging);
    }

    private static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ArkeSdkDemoApplication.getInstance().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null;
    }

    private static void initParseLiveQueryClient() {
        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient((new URI(Credentials.PARSE_SERVER_URL)));
            parseLiveQueryClient.registerListener(new ParseLiveQueryClientCallbacks() {
                @Override
                public void onLiveQueryClientConnected(ParseLiveQueryClient client) {
                    EMenuLogger.d("ParseLiveQueryClient", "Client Connected");
                }

                @Override
                public void onLiveQueryClientDisconnected(ParseLiveQueryClient client, boolean userInitiated) {
//                    attemptLiveQueryReconnection();
                }

                @Override
                public void onLiveQueryError(ParseLiveQueryClient client, LiveQueryException reason) {
//                    attemptLiveQueryReconnection();
                }

                @Override
                public void onSocketError(ParseLiveQueryClient client, Throwable reason) {
//                    attemptLiveQueryReconnection();
                }
            });
        } catch (URISyntaxException e) {
            EMenuLogger.d("ParseLiveQueryClient", "Exception = " + e.getMessage());
            e.printStackTrace();
        }
    }

//    private static void attemptLiveQueryReconnection() {
//        if (parseLiveQueryClient != null) {
//            new Thread(() -> {
//                try {
//                    if (isConnected()) {
//                        parseLiveQueryClient.reconnect();
//                    }
//                } catch (NullPointerException ignored) {
//                }
//            }).start();
//        }
//    }

    public static ParseLiveQueryClient getParseLiveQueryClient() {
        return parseLiveQueryClient;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Order completed notification id */
            NotificationChannel orderCompleted = new NotificationChannel(
                    CHANNEL_ORDER_COMPLETED_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderCompleted.setDescription("This is Channel 1");

            /* Order received notification id */
            NotificationChannel orderReceived = new NotificationChannel(
                    CHANNEL_ORDER_RECEIVED_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderReceived.setDescription("This is Channel 2");

            /* Payment successful notification id */
            NotificationChannel paymentSuccessful = new NotificationChannel(
                    CHANNEL_PAYMENT_SUCCESSFUL_ID,
                    "Channel 3",
                    NotificationManager.IMPORTANCE_HIGH
            );
            paymentSuccessful.setDescription("This is Channel 3");

            /* Payment failed notification id */
            NotificationChannel paymentFailed = new NotificationChannel(
                    CHANNEL_PAYMENT_FAILED_ID,
                    "Channel 4",
                    NotificationManager.IMPORTANCE_HIGH
            );
            paymentFailed.setDescription("This is Channel 4");


            /* Purchase completed notification id */
            NotificationChannel purchaseCompleted = new NotificationChannel(
                    CHANNEL_PURCHASE_COMPLETED_ID,
                    "Channel 5",
                    NotificationManager.IMPORTANCE_HIGH
            );
            purchaseCompleted.setDescription("This is Channel 5");

            /* Purchase failed notification id */
            NotificationChannel purchaseFailed = new NotificationChannel(
                    CHANNEL_PURCHASE_FAILED_ID,
                    "Channel 6",
                    NotificationManager.IMPORTANCE_HIGH
            );
            purchaseFailed.setDescription("This is Channel 6");


            NotificationManager manager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(manager).createNotificationChannel(orderCompleted);
            manager.createNotificationChannel(orderReceived);
            manager.createNotificationChannel(paymentSuccessful);
            manager.createNotificationChannel(paymentFailed);
            manager.createNotificationChannel(purchaseCompleted);
            manager.createNotificationChannel(purchaseFailed);

        }
    }



}
