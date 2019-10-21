package com.arke.sdk;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.arke.sdk.api.DeviceService;
import com.arke.sdk.util.printer.Printer;
import com.usdk.apiservice.aidl.UDeviceService;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import timber.log.Timber;

/**
 * Application entry.
 */

public class ArkeSdkDemoApplication extends Application {

    private static final String TAG = "ArkeSdkDemoApplication";
    private static final String USDK_ACTION_NAME = "com.usdk.apiservice";
    private static final String USDK_PACKAGE_NAME = "com.usdk.apiservice";
    private static DeviceService deviceService;
    private static Context context;

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

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
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
}
