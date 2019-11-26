package com.arke.sdk.workmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import com.arke.sdk.R;

import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.activities.KitchenHomeActivity;
import com.arke.sdk.utilities.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Objects;


public class KitchenAlertWorker extends Worker {


    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";
    protected List<ParseObject> mMessages;
    private String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();

    @NonNull
    @Override
    public Result doWork() {

        queryPendingOrders();

        return Result.SUCCESS;
    }

    public void queryPendingOrders() {
        ParseQuery<ParseObject> query = new ParseQuery<>("EMenuOrders");
        query.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        query.whereEqualTo("order_progress_status", '"'+"PENDING"+'"');
        query.whereEqualTo("kitchen_received_notify", false);
        query.whereEqualTo(Globals.HAS_FOOD, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    //we found messages
                    mMessages = objects;
                    String username = String.valueOf(mMessages.size());
                    Log.d("Kitchen", String.valueOf(mMessages.size()));

                    // check if response contains objects
                    if(mMessages.size() > 0) {
                        // loop through the response to update
                        // their notification_received_status
                        String title = getInputData().getString(EXTRA_TITLE, "A New Order was received");
                        String text = getInputData().getString(EXTRA_TEXT, "Click to view order");

                        int id = (int) getInputData().getLong(Constants.KITCHEN_ID, 0);

                        sendNotification(title, text, id);

                        for (ParseObject message : mMessages) {
                            message.put("kitchen_received_notify", true);
                            message.saveEventually();
                            Log.d("Kitchen", String.valueOf(username));
                        }

                    }

                }else{
                    // error occurred
                    Log.d("Kitchen", e.getMessage());

                }
            }
        });
    }

    private void sendNotification(String title, String text, int id) {
        Intent intent = new Intent(getApplicationContext(), KitchenHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KITCHEN_ID, id);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle(title)
                .setContentText(text)
                .setColor(Color.rgb(255, 127, 0))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_menu_notify)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);


        Objects.requireNonNull(notificationManager).notify(id, notification.build());
    }

}
