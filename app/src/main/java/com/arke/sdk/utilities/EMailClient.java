package com.arke.sdk.utilities;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.arke.sdk.contracts.BooleanOperationDoneCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EMailClient {

    private static String getEmailContent(boolean isForAdmin, String emailAddress, String password) {
        return "Dear EMenu User, <br/> A password recovery process was initiated on your EMenu Account.<br/>Here are your existing credentials:<br/>Email Address: <b>" + emailAddress + "</b><br/>" + (isForAdmin ? "Admin " : "") + "Password:<b>" + password + "</b>.<br/>Login into the app and update your details as necessary.<br/><br/>Regards,<br/>The EMenu Team.";
    }

    private static String getEmailContent(String token, String expiringDate) {
        return "Dear EMenu User, <br/> A password recovery process was initiated on your EMenu Account.<br/>" +
                "Kindly copy the below generated TOKEN, go back to your mobile application and paste " +
                "it before it expires:<br/>TOKEN: <b>" + token + "</b><br/>" + "Expiring Time:<b>" + expiringDate + "</b>.<br/>Regards,<br/>The EMenu Team.";
    }

    @SuppressWarnings("ConstantConditions")
    public static void sendPasswordRecoveryEmail(boolean isPasswordReset, String emailAddress,
                                                 String restaurantOrBarName,
                                                 String token,
                                                 String expiringDate,
                                                 BooleanOperationDoneCallback booleanOperationDoneCallback) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        Log.d("res# handleMessage", "handleMessage() called");
        mHandler.postDelayed(() -> {
            try {
                JSONObject dataObject = new JSONObject();
                JSONArray personalizations = new JSONArray();
                JSONObject personalizationsFirstObject = new JSONObject();
                JSONArray toArr = new JSONArray();

                JSONObject toData = new JSONObject();
                toData.put("email", emailAddress);
                toData.put("name", restaurantOrBarName);
                toArr.put(toData);

                personalizationsFirstObject.put("to", toArr);
                personalizationsFirstObject.put("subject", "EMenu Password Recovery");
                personalizations.put(personalizationsFirstObject);
                dataObject.put("personalizations", personalizations);

                JSONArray contentArray = new JSONArray();
                JSONObject contentArrObject = new JSONObject();
                contentArrObject.put("type", "text/html");
                if (isPasswordReset)
                    contentArrObject.put("value", getEmailContent(token, expiringDate));
                else
                    contentArrObject.put("value", getEmailContent(isPasswordReset, emailAddress, CryptoUtils.getSha256Digest(token)));
                contentArray.put(contentArrObject);

                dataObject.put("content", contentArray);

                JSONObject fromObject = new JSONObject();
                fromObject.put("email", "emenuspprt@gmail.com");
                fromObject.put("name", "EMenu Support");

                dataObject.put("from", fromObject);

                JSONObject replyToObject = new JSONObject();
                replyToObject.put("email", "emenuspprt@gmail.com");
                replyToObject.put("name", "EMenu Support");
                dataObject.put("reply_to", replyToObject);

                OkHttpClient okHttpClient = NetworkClient.getOkHttpClient();
                HttpUrl.Builder builder = HttpUrl.parse("https://api.sendgrid.com/v3/mail/send").newBuilder();
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), dataObject.toString());
                Request request = NetworkClient.getHeaders().url(builder.build().toString()).post(requestBody).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        EMenuLogger.d("SendGridData", "Failed to send Email due to " + e.getMessage());
                        booleanOperationDoneCallback.done(false, e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        Log.d("res# sendGrid", response.toString());
                        int responseCode = response.code();
                        if (responseCode != 202) {
                            booleanOperationDoneCallback.done(false, new Exception("Sorry, failed to initiate a password recovery at this time. Please try again."));
                        } else {
                            booleanOperationDoneCallback.done(true, null);
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, 0);
    }

}
