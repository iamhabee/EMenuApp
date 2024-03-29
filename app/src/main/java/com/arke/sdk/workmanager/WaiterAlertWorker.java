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
import com.arke.sdk.ui.activities.WaiterHomeActivity;
import com.arke.sdk.utilities.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Objects;

public class WaiterAlertWorker extends Worker {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";
    protected List<ParseObject> mMessages;
    private String waiterUsername;
    private String restaurantOrBarId = AppPrefs.getRestaurantOrBarId();

    @NonNull
    @Override
    public Result doWork() {

        waiterUsername = ParseUser.getCurrentUser().getString("username");

        queryPendingOrdersForDrinks();

        queryPendingOrdersForFood();

        queryPendingOrdersForRejectedOrder();

        return Result.SUCCESS;
    }

    public void queryPendingOrdersForFood() {
        ParseQuery<ParseObject> query = new ParseQuery<>("EMenuOrders");
        query.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        query.whereEqualTo("order_progress_status", '"'+"DONE"+'"');
        query.whereEqualTo("waiter_received_notify", false);
        query.whereEqualTo(Globals.FOOD_READY, true);
        query.whereEqualTo(Globals.WAITER_TAG, waiterUsername);
        query.findInBackground((objects, e) -> {
            if (e == null){
                //we found messages
                mMessages = objects;
                String username = String.valueOf(mMessages.size());
                Log.d("Waiter", String.valueOf(mMessages.size()));

                // check if response contains objects
                if(mMessages.size() > 0) {
                    // loop through the response to update
                    // their notification_received_status
                    String title = getInputData().getString(EXTRA_TITLE, "Order for food is ready");
                    String text = getInputData().getString(EXTRA_TEXT, "Click to view food order");

                    int id = (int) getInputData().getLong(Constants.KITCHEN_ID, 0);

                    sendNotification(title, text, id);

                    for (ParseObject message : mMessages) {
                        message.put("waiter_received_notify", true);
                        message.saveEventually();
                        Log.d("Waiter", String.valueOf(username));
                    }

                }

            }else{
                // error occurred
                Log.d("Waiter", e.getMessage());

            }
        });
    }

    public void queryPendingOrdersForRejectedOrder() {
        ParseQuery<ParseObject> query = new ParseQuery<>("EMenuOrders");
        if (AppPrefs.getUseType() == Globals.BAR){
            query.whereEqualTo("order_progress_status", '"'+"BAR_REJECTED"+'"');
            query.whereEqualTo(Globals.BAR_REJECTED_ORDER, true);
        }else if (AppPrefs.getUseType() == Globals.KITCHEN) {
            query.whereEqualTo("order_progress_status", '"'+"KITCHEN_REJECTED"+'"');
            query.whereEqualTo(Globals.KITCHEN_REJECTED_ORDER, true);
        }
        query.whereEqualTo("rejected_notifier", false);
        query.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        query.whereEqualTo(Globals.WAITER_TAG, waiterUsername);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    //we found messages
                    mMessages = objects;
                    String username = String.valueOf(mMessages.size());
                    Log.d("Waiter", String.valueOf(mMessages.size()));

                    // check if response contains objects
                    if(mMessages.size() > 0) {
                        // loop through the response to update
                        // their notification_received_status
                        String title = null;
                        String text = null;
                        int id = 0;
                        if (AppPrefs.getUseType() == Globals.BAR){
                            title = getInputData().getString(EXTRA_TITLE, "Drink order was rejected");
                            text = getInputData().getString(EXTRA_TEXT, "Click to view drink order");
                            id = (int) getInputData().getLong(Constants.BAR_REJECT, 0);
                        }else if (AppPrefs.getUseType() == Globals.KITCHEN) {
                            title = getInputData().getString(EXTRA_TITLE, "Food order was rejected");
                            text = getInputData().getString(EXTRA_TEXT, "Click to view food order");
                            id = (int) getInputData().getLong(Constants.KITCHEN_REJECT, 0);
                        }

                        sendNotificationOnDelete(title, text, id);

                        for (ParseObject message : mMessages) {
//                            if (AppPrefs.getUseType() == Globals.BAR){
//
//                            }
//                            if(AppPrefs.getUseType() == Globals.KITCHEN){
//                                //
//                            }
                            message.put("rejected_notifier", true);
                            message.saveEventually();
                            Log.d("Waiter", String.valueOf(username));
                        }

                    }

                }else{
                    // error occurred
                    Log.d("Waiter", e.getMessage());

                }
            }
        });
    }

    public void queryPendingOrdersForDrinks() {
        ParseQuery<ParseObject> query = new ParseQuery<>("EMenuOrders");
        query.whereEqualTo(Globals.RESTAURANT_OR_BAR_ID, restaurantOrBarId);
        query.whereEqualTo("order_progress_status", '"'+"DONE"+'"');
        query.whereEqualTo("waiter_received_notify_drink", false);
        query.whereEqualTo(Globals.DRINK_READY, true);
        query.whereEqualTo(Globals.WAITER_TAG, waiterUsername);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    //we found messages
                    mMessages = objects;
                    String username = String.valueOf(mMessages.size());
                    Log.d("Waiter", String.valueOf(mMessages.size()));

                    // check if response contains objects
                    if(mMessages.size() > 0) {
                        // loop through the response to update
                        // their notification_received_status
                        String title = getInputData().getString(EXTRA_TITLE, "Order for drink is ready");
                        String text = getInputData().getString(EXTRA_TEXT, "Click to view drink order");

                        int id = (int) getInputData().getLong(Constants.KITCHEN_ID, 0);

                        sendNotificationDrink(title, text, id);

                        for (ParseObject message : mMessages) {
                            message.put("waiter_received_notify_drink", true);
                            message.saveEventually();
                            Log.d("Waiter", String.valueOf(username));
                        }

                    }

                }else{
                    // error occurred
                    Log.d("Waiter", e.getMessage());

                }
            }
        });

    }


    private void sendNotification(String title, String text, int id) {
        Intent intent = new Intent(getApplicationContext(), WaiterHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.WAITER_ID, id);

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

    private void sendNotificationDrink(String title, String text, int id) {
        Intent intent = new Intent(getApplicationContext(), WaiterHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.WAITER_ID_DRINK, id);

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

    private void sendNotificationOnDelete(String title, String text, int id) {
        Intent intent = new Intent(getApplicationContext(), WaiterHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.WAITER_ID, id);

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
