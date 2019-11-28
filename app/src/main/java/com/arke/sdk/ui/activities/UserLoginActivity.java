package com.arke.sdk.ui.activities;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.contracts.BuildConfig;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.NetworkClient;
import com.arke.sdk.utilities.UiUtils;
//import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.LogInCallback;

import androidx.appcompat.app.AppCompatActivity;

import com.arke.sdk.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Objects;


public class UserLoginActivity extends AppCompatActivity {

    private AppCompatActivity activity = UserLoginActivity.this;

    EditText username;
    EditText passwordd;
    TextView welcomeText;
    Button signIn, btnForgotPassword;
    TextView switchAccount;

    String name, password, editUsername, editPassword;



    private LottieAlertDialog logInOperationProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        checkOsVersion();

        username = findViewById(R.id.username);
        passwordd = findViewById(R.id.password);
        signIn= findViewById(R.id.signIn);
        welcomeText = findViewById(R.id.welcome_text);
        switchAccount = findViewById(R.id.switchAccount);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        switchAccount.setOnClickListener(view -> {
            AppPrefs.setUp(false);
            AppPrefs.setUseType(Globals.UseType.USE_TYPE_NONE);
            AppPrefs.persistRestaurantOrBarId(null);
            AppPrefs.persistRestaurantOrBarEmailAddress(null);
            AppPrefs.persistLicenseKey(null);
            AppPrefs.persistLicenseAllowedUserAccounts(0);
            AppPrefs.persistLicenseKeyId(null);
            finish();
            Intent switchA = new Intent(UserLoginActivity.this, OnBoardingActivity.class);
            switchA.putExtra("overrideAppSetup", true);
            startActivity(switchA);
        });

        btnForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(activity, PasswordReset.class));
            Animatoo.animateSlideLeft(activity);
        });

        welcomeText.setText(AppPrefs.getRestaurantOrBarName());

        signIn.setOnClickListener(view -> logIn());
    }


    public void checkOsVersion() {
        String packageName =  BuildConfig.APPLICATION_ID;
        String versionName = BuildConfig.VERSION_NAME;
        String tid = "123456";
        String url = "https://terminal.efulltech.com.ng/api/checkOsVersion?terminalId="+tid+"&package="+packageName+"&version="+versionName;

        Log.d("Checking OS Version ", url);
        RequestQueue requestQueue = Volley.newRequestQueue(UserLoginActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Update Response", response);
                        if(!response.equals("true")){
//                            navigate to update page
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response)));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Cloud DB Error", error.toString());
                        showErrorMessage("Error", error.getMessage());
                    }
                }
        );
//      set retry policy to determine how long volley should wait before resending a failed request
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        add jsonObjectRequest to the queue
        requestQueue.add(stringRequest);
    }


    private void showErrorMessage(String title, String description) {
        LottieAlertDialog errorCreationErrorDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_ERROR)
                .setTitle(title).setDescription(description)
                .setPositiveText("OK").setPositiveListener(Dialog::dismiss)
                .build();
        errorCreationErrorDialog.setCancelable(true);
        errorCreationErrorDialog.show();
    }


    private void showOperationsDialog(String title, String description) {
        logInOperationProgressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        logInOperationProgressDialog.setCancelable(false);
        logInOperationProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (logInOperationProgressDialog != null) {
            logInOperationProgressDialog.dismiss();
            logInOperationProgressDialog = null;
        }
    }

    private void logIn(){
        editPassword = passwordd.getText().toString();
        editUsername = username.getText().toString();

        if (TextUtils.isEmpty(editUsername)) {
            username.setError("Username is Required");
            return;
        }
        if (TextUtils.isEmpty(editPassword)) {
            passwordd.setError("Password is Required");
            return;
        }

        if (NetworkClient.isOnline(activity)) {
            showOperationsDialog("Logging you in to " + AppPrefs.getRestaurantOrBarName(), "Please Wait");

            ParseUser.logInInBackground(editUsername, editPassword, (parseUser, e) -> {
                dismissProgressDialog();
                if (parseUser != null) {
                    // check if the restaurant's license is still active
                    String resId = parseUser.getString("res_id");
                    ParseQuery<ParseObject> orderQuery = ParseQuery.getQuery(Globals.LICENSE_KEYS);
                    orderQuery.whereEqualTo(Globals.LICENSE_KEY, AppPrefs.getLicenseKey());
                    orderQuery.getFirstInBackground((object, ex) -> {
                        if(ex == null){
                            if(object != null){
                                // check if the license is active
                                if(object.getBoolean("license_active")){
                                    // check if the current user is registered under the current restaurant
                                    Log.d("App RES ID", AppPrefs.getRestaurantOrBarId());
                                    Log.d("Server RES ID", Objects.requireNonNull(parseUser.get("res_id")).toString());
                                    if (Objects.requireNonNull(parseUser.get("res_id")).toString().equals(AppPrefs.getRestaurantOrBarId())) {
                                        // get user's permission level and designation (waiter, bar, etc)
                                        int user_type = parseUser.getInt("user_type");
                                        // grant user access WRT permission

                                        switch (user_type) {
                                            case Globals.KITCHEN:
                                                // kitchen
                                                AppPrefs.setUseType(Globals.UseType.USE_TYPE_KITCHEN);
                                                Intent kitchenHomeIntent = new Intent(UserLoginActivity.this, KitchenHomeActivity.class);
                                                startActivity(kitchenHomeIntent);
                                                finish();
                                                break;
                                            case Globals.BAR:
                                                // bar
                                                AppPrefs.setUseType(Globals.UseType.USE_TYPE_BAR);
                                                Intent barHomeIntent = new Intent(UserLoginActivity.this, BarHomeActivity.class);
                                                startActivity(barHomeIntent);
                                                finish();
                                                break;
                                            case Globals.ADMIN_TAG_ID:
                                                // admin
                                                AppPrefs.setUseType(Globals.UseType.USE_TYPE_ADMIN);
                                                Intent adminHomeIntent = new Intent(UserLoginActivity.this, AdminHomeActivity.class);
                                                startActivity(adminHomeIntent);
                                                finish();
                                                break;
                                            default:
                                                // waiter
                                                AppPrefs.setUseType(Globals.UseType.USE_TYPE_WAITER);
                                                Intent waiterHomeIntent = new Intent(UserLoginActivity.this, WaiterHomeActivity.class);
                                                startActivity(waiterHomeIntent);
                                                finish();
                                                break;
                                        }
                                    } else {
                                        // else log user out and notify the user
                                        ParseUser.logOut();
                                        Toast.makeText(UserLoginActivity.this, "Sorry, you are not assigned to " + AppPrefs.getRestaurantOrBarName(), Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                 showErrorMessage("Login Error", "Your restaurant's license key is invalid");
                                }
                            }else{
                                showErrorMessage("Login Error", "This account is not tied to a valid License key");
                            }
                        }else{
                            showErrorMessage("Login Error", ex.getMessage());
                        }
                    });
                } else {
                    ParseUser.logOut();
                    Toast.makeText(UserLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            UiUtils.showErrorMessage(activity, "Network Error", DataStoreClient.getNetworkErrorMessage());
        }

    }

    @Override
    public void onBackPressed() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.close_app_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button yes = dialog.findViewById(R.id.yes);
        Button no = dialog.findViewById(R.id.no);

        yes.setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });

        no.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

}
