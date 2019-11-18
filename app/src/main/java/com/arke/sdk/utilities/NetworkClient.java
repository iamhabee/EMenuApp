package com.arke.sdk.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.arke.sdk.companions.Credentials;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public class NetworkClient {
    private static final String TAG = "SendGrid";
    private static final String VERSION = "3.0.0";
    private static final String USER_AGENT = "sendgrid/" + VERSION + ";java";

    public static Request.Builder getHeaders() {
        Request.Builder request = new Request.Builder();
        request.addHeader("Authorization", "Bearer " + Credentials.SEND_GRID_API_KEY);
        request.addHeader("Accept", "application/json");
        request.addHeader("User-agent", USER_AGENT);
        request.addHeader("Content-Type", "application/json");
        return request;
    }

    public static OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> EMenuLogger.d(TAG, message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .addInterceptor(logging)
                .build();
    }

    public static boolean isOnline(Context context) {
        //return true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting() && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected();
    }

}
