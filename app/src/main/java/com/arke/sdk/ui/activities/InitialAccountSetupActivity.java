package com.arke.sdk.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arke.sdk.R;
import com.arke.sdk.preferences.AppPrefs;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class InitialAccountSetupActivity extends AppCompatActivity {

    TextView mWelcomText;
    TextView mInfoText;
    EditText mUsername;
    EditText mEmail;
    EditText mPassword;
    EditText mRetypePassword;
    Button mSubmit;
    private int res_id;



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
        res_id = Integer.parseInt(AppPrefs.getRestaurantOrBarId());
        mWelcomText.setText("Welcome to "+ AppPrefs.getRestaurantOrBarName()+ "!");

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onSignUp();
            }
        });
    }



    private void onSignUp(){
        String aName = mUsername.getText().toString();
        String aEmail = mEmail.getText().toString();
        String aPassword = mPassword.getText().toString().trim();
        String aRetypePassword = mRetypePassword.getText().toString().trim();
        if(Validate()){
            // check if passwords match
            if(aPassword.equals(aRetypePassword)){
                ParseUser user = new ParseUser();
                // Set the user's username and password, which can be obtained by a forms
                user.setUsername(aName);
                user.setPassword(aEmail);
                user.setPassword(aPassword);
                user.setPassword(aRetypePassword);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            try {
                                user.put("res_id", res_id); // restaurant ID
                                user.put("account_type", "Admin");
                                user.put("user_type", 263389);
                                user.save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            Intent intent = new Intent(InitialAccountSetupActivity.this, UserLoginActivity.class);
                            startActivity(intent);
                        } else {
                            ParseUser.logOut();
                            Toast.makeText(InitialAccountSetupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                // password mis-match
            }
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
