package com.arke.sdk.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arke.sdk.companions.Globals;
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
            Intent switchA = new Intent(UserLoginActivity.this, OnBoardingActivity.class);
            switchA.putExtra("overrideAppSetup", true);
            startActivity(switchA);
            finish();
        });

        btnForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(activity, PasswordReset.class));
            Animatoo.animateSlideLeft(activity);
        });

        welcomeText.setText(getString(R.string.welcome_to).concat(AppPrefs.getRestaurantOrBarName()).concat("!"));

        signIn.setOnClickListener(view -> logIn());
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

}
