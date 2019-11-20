package com.arke.sdk.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.arke.sdk.R;
import com.arke.sdk.utilities.NetworkClient;
import com.arke.sdk.utilities.UiUtils;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PasswordReset extends AppCompatActivity {

    private AppCompatActivity activity = PasswordReset.this;

    @BindView(R.id.etEmail)
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnResetPassword)
    void resetPassword(View view) {
        UiUtils.dismissKeyboard(view);
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email address is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).find()) {
            UiUtils.showSafeToast("Please enter a valid email address to reset your password");
            return;
        }

        try {
            if (NetworkClient.isOnline(activity)) {
                UiUtils.showOperationsDialog(activity, "Loading...", "Please wait");
                ParseUser.requestPasswordResetInBackground(email, e -> {
                    UiUtils.dismissProgressDialog();
                    if (e == null) {
                        successMessage();

                    } else {
                        UiUtils.showErrorMessage(activity, "Oops..Something went wrong", e.getMessage());
                    }
                });
            } else {
                UiUtils.showErrorMessage(activity, "Network Error", "You're currently " +
                        "offline, kindly check your network and try again later!");
            }
        } catch (NullPointerException ex) {
            ex.getMessage();
        }
    }

    private void successMessage() {
        LottieAlertDialog lottieAlertDialog = new LottieAlertDialog
                .Builder(activity, DialogTypes.TYPE_SUCCESS)
                .setTitle("Recovery Message Sent!").setDescription("A password recovery email was " +
                        "sent to the email provided. If the email is not in your inbox by now, then check the SPAM folder.")
                .setPositiveText("Okay")
                .setPositiveListener(lottieAlertDialog1 -> {
                    lottieAlertDialog1.dismiss();
                    startActivity(new Intent(activity, UserLoginActivity.class));
                    finish();
                })
                .build();
        if (!lottieAlertDialog.isShowing()) {
            lottieAlertDialog.setCancelable(true);
            lottieAlertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(activity);
    }
}
