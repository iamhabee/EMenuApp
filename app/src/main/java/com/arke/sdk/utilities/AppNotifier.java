package com.arke.sdk.utilities;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;

import androidx.core.app.NotificationCompat;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.activities.BarHomeActivity;
import com.arke.sdk.ui.activities.KitchenHomeActivity;

import org.apache.commons.lang3.text.WordUtils;

public class AppNotifier {

    public static AppNotifier getInstance() {
        return new AppNotifier();
    }

    public void blowNewIncomingOrderNotification(Context context) {
        MediaPlayer.create(context, R.raw.notification_sound);
    }

    public void sendSingleNotification() {
        int useType = AppPrefs.getUseType();
        Intent notificationIntent = new Intent(ArkeSdkDemoApplication.getInstance(),
                useType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()
                ? KitchenHomeActivity.class : BarHomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ArkeSdkDemoApplication.getInstance(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ArkeSdkDemoApplication.getInstance(), ArkeSdkDemoApplication.getInstance().getString(R.string.default_channel_id));
        NotificationHelper notificationHelper = new NotificationHelper(ArkeSdkDemoApplication.getInstance().getApplicationContext(), getDefaultChanneld(), getDefaultChannelName());
        builder.setContentTitle(WordUtils.capitalize(AppPrefs.getRestaurantOrBarName()));
        builder.setContentText("A New Order was received");
        builder.setTicker("A New Order was received");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLights(Color.parseColor("blue"), 500, 1000);
        Bitmap notificationInitiatorBitmap = BitmapFactory.decodeResource(ArkeSdkDemoApplication.getInstance().getResources(), R.mipmap.ic_launcher);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setLargeIcon(notificationInitiatorBitmap);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setColor(Color.parseColor("#00628F"));
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText("A New Order was received")
                .setBigContentTitle(WordUtils.capitalize(AppPrefs.getRestaurantOrBarName())).setSummaryText("1 New Message"));
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        if (pendingIntent != null) {
            notificationHelper.notify(Globals.NEW_MESSAGE_NOTIFICATION_ID, notification);
        }
    }

    private String getDefaultChannelName() {
        return WordUtils.capitalize(AppPrefs.getRestaurantOrBarName());
    }

    private String getDefaultChanneld() {
        return WordUtils.capitalize(AppPrefs.getRestaurantOrBarEmailAddress());
    }

}
