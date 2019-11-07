package com.arke.sdk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.arke.sdk.workmanager.KitchenAlertWorker;

import java.util.concurrent.TimeUnit;

public class KitchenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d(TAG, "onReceive: " + intent.getParcelableExtra("downloadStatusModel").toString());

        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(KitchenAlertWorker.class, 5, TimeUnit.SECONDS)
                        .addTag("periodic_work")
                        .build();


        assert WorkManager.getInstance() != null;
        WorkManager.getInstance().enqueue(periodicWorkRequest);

    }


}
