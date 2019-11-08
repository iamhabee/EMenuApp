package com.arke.sdk.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.LogInCallback;

import androidx.appcompat.app.AppCompatActivity;

import com.arke.sdk.R;
import com.parse.ParseException;
import com.parse.ParseUser;


public class UserLoginActivity extends AppCompatActivity {

    EditText username;
    EditText passwordd;
    TextView welcomeText;
    Button signIn;
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

        switchAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppPrefs.setUp(false);
                AppPrefs.setUseType(Globals.UseType.USE_TYPE_NONE);
                AppPrefs.persistRestaurantOrBarId(null);
                AppPrefs.persistRestaurantOrBarEmailAddress(null);
                Intent switchA = new Intent(UserLoginActivity.this, OnBoardingActivity.class);
                switchA.putExtra("overrideAppSetup", true);
                startActivity(switchA);
                finish();
            }
        });

        welcomeText.setText("Welcome to "+ AppPrefs.getRestaurantOrBarName()+"!");

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });
//        onSignUp();
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


        showOperationsDialog("Logging You In " + AppPrefs.getRestaurantOrBarName(), "Please Wait");


        ParseUser.logInInBackground(editUsername, editPassword, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                dismissProgressDialog();
                if (parseUser != null) {
                    // check if the current user is registered under the current restaurant
                    Log.d("App RES ID", AppPrefs.getRestaurantOrBarId());
                    Log.d("Server RES ID", parseUser.get("res_id").toString());
                    if(parseUser.get("res_id").toString().equals(AppPrefs.getRestaurantOrBarId())) {
                        // get user's permission level and designation (waiter, bar, etc)
                        int user_type = parseUser.getInt("user_type");
                        // grant user access WRT permission
                        switch (user_type){
                            case 2:
                                // kitchen
                                AppPrefs.setUseType(Globals.UseType.USE_TYPE_KITCHEN);
                                Intent kitchenHomeIntent = new Intent(UserLoginActivity.this, KitchenHomeActivity.class);
                                startActivity(kitchenHomeIntent);
                                finish();
                                break;
                            case 3:
                                // bar
                                AppPrefs.setUseType(Globals.UseType.USE_TYPE_BAR);
                                Intent barHomeIntent = new Intent(UserLoginActivity.this, BarHomeActivity.class);
                                startActivity(barHomeIntent);
                                finish();
                                break;
                            case 263389:
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
                    }else {
                        // else log user out and notify the user
                        ParseUser.logOut();
                        Toast.makeText(UserLoginActivity.this, "Sorry, you are not assigned to "+AppPrefs.getRestaurantOrBarName(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    ParseUser.logOut();
                    Toast.makeText(UserLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

}
