package com.arke.sdk.workmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.arke.sdk.R;
import com.arke.sdk.receivers.ClearNotificationReceiver;

import static com.arke.sdk.ArkeSdkDemoApplication.CHANNEL_ORDER_COMPLETED_ID;

public class NotificationWorker extends Worker {

    private static final String WORK_RESULT = "work_result";
    Context context;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
//        Data taskData = getInputData();
//        String taskDataString = taskData.getString(KitchenHomeActivity.MESSAGE_STATUS);
//
//        showNotification("WorkManager", taskDataString != null ? taskDataString : "Message has been Sent");
//
//        Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
//
//        return Result.success(outputData);
        notificationNewOrder();

        return Result.success();
    }

    private void notificationNewOrder() {
        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


        Intent broadcastIntent = new Intent(context, ClearNotificationReceiver.class);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(context,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ORDER_COMPLETED_ID)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("A New Order was received")
                .setContentText("Click to view order")
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine(context.getString(R.string.open_notify))
//                        .setBigContentTitle("A New Order was received")
//                        .setSummaryText("Click to view order")
                .setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(cancelIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();

        assert notificationManager != null;
        notificationManager.notify(1, notification);
    }

    private void showNotification(String task, String desc) {

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


        String channelId = "task_channel";
        String channelName = "task_name";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher);

        manager.notify(1, builder.build());

    }
}
