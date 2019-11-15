package com.arke.sdk;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.arke.sdk.api.DeviceService;
import com.arke.sdk.companions.Credentials;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.database.EMenuDb;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.models.EMenuOrder;

import com.arke.sdk.util.printer.Printer;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.EMenuLogger;
import com.parse.Parse;
import com.parse.livequery.LiveQueryException;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.ParseLiveQueryClientCallbacks;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier;
import com.usdk.apiservice.aidl.UDeviceService;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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



    /**
     * Create.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        // Bind sdk device service.
        bindSdkDeviceService();

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

                }

                @Override
                public void onLiveQueryError(ParseLiveQueryClient client, LiveQueryException reason) {

                }

                @Override
                public void onSocketError(ParseLiveQueryClient client, Throwable reason) {

                }
            });
        } catch (URISyntaxException e) {
            EMenuLogger.d("ParseLiveQueryClient", "Exception = " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static ParseLiveQueryClient getParseLiveQueryClient() {
        return parseLiveQueryClient;
    }





}
