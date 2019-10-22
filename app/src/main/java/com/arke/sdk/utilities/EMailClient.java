package com.arke.sdk.utilities;

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

    @SuppressWarnings("ConstantConditions")
    public static void sendPasswordRecoveryEmail(boolean isForAdmin, String emailAddress,
                                                 String restaurantOrBarName,
                                                 String retrievedPassword,
                                                 BooleanOperationDoneCallback booleanOperationDoneCallback) {
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
            contentArrObject.put("value", getEmailContent(isForAdmin, emailAddress, retrievedPassword));
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
    }

}