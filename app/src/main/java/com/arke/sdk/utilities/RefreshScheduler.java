package com.arke.sdk.utilities;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.arke.sdk.workmanager.NotificationWorker;

import java.util.concurrent.TimeUnit;

public class RefreshScheduler {

    public static void refreshPeriodicWork() {

        //define constraints
        Constraints myConstraints = new Constraints.Builder()
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();

        Data source = new Data.Builder()
                .putString("workType", "PeriodicTime")
                .build();

        PeriodicWorkRequest refreshCpnWork =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 16, TimeUnit.SECONDS)
                        .setConstraints(myConstraints)
                        .setInputData(source)
                        .build();

        WorkManager.getInstance().enqueue(refreshCpnWork);
    }
}
