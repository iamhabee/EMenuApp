package com.arke.sdk.ui.activities;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;


public class InitialAccountSetupActivity extends AppCompatActivity {

    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;


    TextView mWelcomText;
    TextView mInfoText;
    EditText mUsername;
    EditText mEmail;
    EditText mPassword;
    EditText mRetypePassword;
    Button mSubmit;
    private int res_id;
    int user_type = 0;
    String account_type;

    private LottieAlertDialog regInOperationProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_account_setup);

        mWelcomText = findViewById(R.id.welcome_text);
        mInfoText = findViewById(R.id.info_text);
        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRetypePassword = findViewById(R.id.retypePassword);
        mSubmit = findViewById(R.id.submit);

        rbGroup = findViewById(R.id.radio_group);
        rb1 =  findViewById(R.id.radio_button1);
        rb2 =  findViewById(R.id.radio_button2);
        rb3 =  findViewById(R.id.radio_button3);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onSignUp();
            }
        });
    }


    private void showOperationsDialog(String title, String description) {
        regInOperationProgressDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_LOADING)
                .setTitle(title).setDescription(description).build();
        regInOperationProgressDialog.setCancelable(false);
        regInOperationProgressDialog.show();
    }


    private void dismissProgressDialog() {
        if (regInOperationProgressDialog != null) {
            regInOperationProgressDialog.dismiss();
            regInOperationProgressDialog = null;
        }
    }

    private void onSignUp(){
        String aPassword = mPassword.getText().toString().trim();
        String aRetypePassword = mRetypePassword.getText().toString().trim();
        if(Validate()){
            // check if passwords match
            if(aPassword.equals(aRetypePassword)){
                showOperationsDialog("Registration in Progress " + AppPrefs.getRestaurantOrBarName(), "Please Wait");
                // check if the number of user accounts tied to the license has not been exceeded
                // get license key data
                ParseQuery<ParseObject> licenseKeyData = ParseQuery.getQuery(Globals.LICENSE_KEYS);
                licenseKeyData.whereEqualTo(Globals.LICENSE_KEY, AppPrefs.getLicenseKey());
                licenseKeyData.getFirstInBackground((object, e) -> {
                    if (e == null) {
                        // check the number of user accounts with the license key
                        int allowedAccounts = object.getInt(Globals.USER_ACCOUNTS_ALLOWED);
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("res_id", object.getString("restaurant_id"));
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> users, ParseException e) {
                                if (e == null) {
                                    if (!users.isEmpty()) {
                                        if(users.size() < allowedAccounts){
                                            createAccount();
                                        }else{
                                            // show error message
                                            dismissProgressDialog();
                                            showErrorMessage("Account Creation Error", "You have exceeded the number of user accounts for your license");
                                        }
                                    }
                                }else{
                                    dismissProgressDialog();
                                    showErrorMessage("Account Creation Error", e.getMessage());
                                }
                            }
                        });
                    }
                    else {
                        dismissProgressDialog();
                        showErrorMessage("Account Creation Error", e.getMessage());
                    }
                });
            }else{
                // password mis-match
                dismissProgressDialog();
                showErrorMessage("Account Creation Error", "Password mismatch");
            }
        }
    }


    public void createAccount(){
        String aName = mUsername.getText().toString();
        String aEmail = mEmail.getText().toString();
        String aPassword = mPassword.getText().toString().trim();
        String aRetypePassword = mRetypePassword.getText().toString().trim();
        ParseUser user = new ParseUser();
        // Set the user's username and password, which can be obtained by a forms
        user.setUsername(aName);
        user.setEmail(aEmail);
        user.setPassword(aPassword);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dismissProgressDialog();
                if (e == null) {
                    try {
                        user.put("res_id", AppPrefs.getRestaurantOrBarId()); // restaurant ID
                        user.put("account_type", account_type);
                        user.put("user_type", user_type);
                        user.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    showSuccessMessage("Success", "Account created successfully");
                } else {
                    ParseUser.logOut();
                    showErrorMessage("Account creation Error", e.getMessage());
                }
            }
        });
    }


    private void showSuccessMessage(String title, String description) {
        LottieAlertDialog operationsSuccessDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle(title).setDescription(description)
                .setPositiveText("Done")
                .setPositiveListener((dialog) -> {
                    dialog.dismiss();
                    finish();
                    Intent intent = new Intent(InitialAccountSetupActivity.this, AdminHomeActivity.class);
                    startActivity(intent);
                }).build();
        operationsSuccessDialog.setCancelable(false);
        operationsSuccessDialog.show();
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



    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_button1:
                if (checked)
                    // admin
                    account_type = "Admin";
                    user_type = Globals.ADMIN_TAG_ID;

                    break;
            case R.id.radio_button2:
                if (checked)
                    account_type = "Kitchen";
                    user_type = Globals.KITCHEN;

                break;
            case R.id.radio_button3:
                if (checked)
                    account_type = "Waiter";
                    user_type = Globals.WAITER;
                    break;

            case R.id.radio_button4:
                if (checked)
                    account_type = "Bar";
                    user_type = Globals.BAR;
                break;
        }
    }

    /* set edit text to error when empty */
    public static boolean hasText(EditText editText) {

        return hasText(editText, "Required");
    }

    /* check edit text length and set error message for required edit text
     * Custom Message */
    public static boolean hasText(EditText editText, String error_message) {

        String text = editText.getText().toString().trim();
        editText.setError(null);

        // length 0 means there is no text
        if (text.length() == 0) {
            editText.setError(error_message);
            return false;
        }

        return true;
    }



    private boolean Validate() {
        /* Validate all required edit text */
        // check if username is not null
        // check if password is not null
        // check if res is true;;
        boolean check = true;
        if (hasText(mUsername)) {
            if (hasText(mEmail)){
                if (hasText(mPassword)){
                    if (hasText(mRetypePassword)){
                        check = true;
                    }else {
                        check = false;
                    }
                }else {
                    check = false;
                }
            }else {
                check = false;
            }
        }else{
            check = false;
        }
        return check;
    }

}
