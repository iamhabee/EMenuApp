package com.arke.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.eventbuses.DeviceConnectedToInternetEvent;

import org.greenrobot.eventbus.EventBus;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            reInitParse();
        }
    }

    private void reInitParse() {
        EventBus.getDefault().post(new DeviceConnectedToInternetEvent(true));
        ArkeSdkDemoApplication.listenToIncomingNotifications();
    }

}
