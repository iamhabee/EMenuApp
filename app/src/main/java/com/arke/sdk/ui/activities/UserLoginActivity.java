package com.arke.sdk.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arke.sdk.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class UserLoginActivity extends AppCompatActivity {

    String name, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        onSignUp();
    }


    private void onSignUp(){
        ParseUser user = new ParseUser();
        name = "sikiru";
        password = "tallest";
// Set the user's username and password, which can be obtained by a forms
        user.setUsername(name);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    String userId = user.getObjectId();
                    user.put("res_id", "12345678");
                    try {
                        user.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    Intent intent = new Intent(UserLoginActivity.this, UserLoginActivity.class);
                    startActivity(intent);
                } else {
                    ParseUser.logOut();
                    Toast.makeText(UserLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
