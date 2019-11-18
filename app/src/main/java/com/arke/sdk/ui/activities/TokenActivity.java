package com.arke.sdk.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.utilities.CryptoUtils;
import com.arke.sdk.utilities.NetworkClient;
import com.arke.sdk.utilities.UiUtils;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;

public class TokenActivity extends AppCompatActivity {

    private AppCompatActivity activity = TokenActivity.this;

    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etConfirmPassword)
    EditText etConfirmPassword;
    @BindView(R.id.etToken)
    EditText etToken;
    @BindView(R.id.tvResetSubtext)
    TextView tvResetLabel;
    @BindView(R.id.linearLayoutToken)
    LinearLayout layoutToken;
    @BindView(R.id.linearLayoutNewPassword)
    LinearLayout layoutNewPassword;

    private String emailAddress, restaurantOrBarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        ButterKnife.bind(this);

        String tokenSubText = "Enter the one-time password reset token that was sent to your email address. " +
                "If the email is not in your inbox by now, then check the SPAM folder.";
        tvResetLabel.setText(tokenSubText);

        Intent intent = getIntent();
        if (intent != null) {
            emailAddress = intent.getStringExtra("restaurantEmailAddress");
            restaurantOrBarName = intent.getStringExtra("restaurantOrBarName");
        }
    }

    @OnClick(R.id.btnValidateToken)
    void ValidateToken(View view) {
        String token = etToken.getText().toString().trim();
        if (TextUtils.isEmpty(token)) {
            etToken.setError("Reset Token is required");
            return;
        }

        try {
            if (NetworkClient.isOnline(activity)) {
                UiUtils.dismissKeyboard(view);
                UiUtils.showOperationsDialog(activity, "Loading...", "Please wait");
                ParseQuery<ParseObject> getUserQuery = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);
                getUserQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress);

                getUserQuery.getFirstInBackground((object, e) -> {
                    if (e != null) {
                        UiUtils.dismissProgressDialog();
                        if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            UiUtils.showErrorMessage(activity, "Oops...", "Sorry, " +
                                    "The provided token does not match your account token");
                        } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                            UiUtils.showErrorMessage(activity, "Network Error", "Sorry, " + "You're currently " +
                                    "offline, kindly check your network and try again later!");

                        } else {
                            UiUtils.showErrorMessage(activity, "Oops...", e.getMessage());
                        }
                    } else {
                        UiUtils.dismissProgressDialog();
                        String otpToken = object.getString(Globals.OTP_TOKEN);
                        Date tokenExpiringDate = object.getDate(Globals.TOKEN_EXPIRING_DATE);

                        if (CryptoUtils.getSha256Digest(token).equals(otpToken) && new Date().before(tokenExpiringDate)) {

                            // SUCCESSFUL
                            showMessage();

                        } else {
                            UiUtils.dismissProgressDialog();
                            UiUtils.showErrorMessage(activity, "Oops...", "Sorry, " +
                                    "The provided token has expired. Kindly re-initialize new password reset token!");
                        }
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

    @OnClick(R.id.btnChangePassword)
    void changePassword(View view) {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etPassword.setError("Confirm Password is required");
            return;
        }
        if (!confirmPassword.equalsIgnoreCase(password)) {
            UiUtils.showSafeToast("Password and Confirm Password does not match");
            return;
        }

        try {
            if (NetworkClient.isOnline(activity)) {
                UiUtils.dismissKeyboard(view);
                UiUtils.showOperationsDialog(activity, "Loading...", "Resetting Password");
                ParseQuery<ParseObject> getUserQuery = ParseQuery.getQuery(Globals.RESTAURANTS_AND_BARS);
                getUserQuery.whereEqualTo(Globals.RESTAURANT_OR_BAR_EMAIL_ADDRESS, emailAddress);

                getUserQuery.getFirstInBackground((object, e) -> {
                    UiUtils.dismissProgressDialog();
                    if (e != null) {
                        if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            UiUtils.showErrorMessage(activity, "Oops...", e.getMessage());
                        } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                            UiUtils.showErrorMessage(activity, "Network Error", "Sorry, " + "You're currently " +
                                    "offline, kindly check your network and try again later!");

                        } else {
                            UiUtils.showErrorMessage(activity, "Oops...", e.getMessage());
                        }
                    } else {

                        object.put(Globals.RESTAURANT_OR_BAR_PASSWORD, CryptoUtils.getSha256Digest(password));
                        object.put(Globals.RESTAURANT_OR_BAR_REVEALED_PASSWORD, CryptoUtils.getSha256Digest(password));

                        object.saveInBackground(ex -> {
                            if (ex != null) {
                                UiUtils.showErrorMessage(activity, "Failed to update new password", ex.getMessage());
                            } else {
                                showMessage2();
                            }
                        });
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

    private void showMessage() {
        LottieAlertDialog lottieAlertDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle("Alert!").setDescription("Password Reset Token validated successfully.\nPress OK to continue...")
                .setPositiveText("OK").setPositiveListener(lottieAlertDialog1 -> {
                    lottieAlertDialog1.dismiss();
                    layoutToken.setVisibility(GONE);
                    layoutNewPassword.setVisibility(View.VISIBLE);
                    tvResetLabel.setText(R.string.new_password_hint);
                })
                .build();
        if (!lottieAlertDialog.isShowing()) {
            lottieAlertDialog.setCancelable(true);
            lottieAlertDialog.show();
        }
    }

    private void showMessage2() {
        LottieAlertDialog lottieAlertDialog = new LottieAlertDialog
                .Builder(this, DialogTypes.TYPE_SUCCESS)
                .setTitle("Alert!").setDescription("Password updated successfully. \nKindly LOGIN with your new password!")
                .setPositiveText("OK").setPositiveListener(lottieAlertDialog1 -> {
                    lottieAlertDialog1.dismiss();
                    startActivity(new Intent(activity, LogInActivity.class));
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
