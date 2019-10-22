package com.arke.sdk.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arke.sdk.R;
import com.arke.sdk.utilities.CryptoUtils;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PasswordUpdateActivity extends BaseActivity {

    @BindView(R.id.old_password_box)
    EditText oldPasswordBox;

    @BindView(R.id.new_password_box)
    EditText newPasswordBox;

    @BindView(R.id.password_repetition_box)
    EditText newPasswordRepetitionBox;

    @BindView(R.id.password_background_view)
    ImageView passwordBackgroundView;

    @BindView(R.id.password_confirmation_button)
    Button passwordsConfirmationButton;

    @BindView(R.id.password_error_indicator)
    TextView passwordErrorView;

    @BindView(R.id.close_password_view)
    ImageView closeActivityView;

    @BindView(R.id.screen_title)
    TextView screenTitle;

    private boolean isForAdmin = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_password_update_layout);
        ButterKnife.bind(this);
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            isForAdmin = intentExtras.getBoolean(Globals.PASSWORD_UPDATE_TYPE_ADMIN);
        }
        if (isForAdmin) {
            screenTitle.setText("Update Admin Password");
        } else {
            screenTitle.setText("Update Restaurant Password");
        }
        String restaurantPhotoUrl = AppPrefs.getRestaurantOrBarPhotoUrl();
        if (StringUtils.isNotEmpty(restaurantPhotoUrl)) {
            UiUtils.loadImageIntoView(passwordBackgroundView, restaurantPhotoUrl);
        }
        initEventHandlers();
    }

    @SuppressLint("SetTextI18n")
    private void initEventHandlers() {
        closeActivityView.setOnClickListener(view -> finish());
        passwordsConfirmationButton.setOnClickListener(view -> {
            String oldPassword = oldPasswordBox.getText().toString().trim();
            if (StringUtils.isEmpty(oldPassword)) {
                passwordErrorView.setText("Please enter your old " + (isForAdmin ? "admin" : "restaurant") + " password. Default is 12345");
                return;
            }
            if (!CryptoUtils.getSha256Digest(oldPassword).equals(isForAdmin ? AppPrefs.getRestaurantAdminPassword() : AppPrefs.getRestaurantOrBarPassword())) {
                passwordErrorView.setText("Sorry, your old password doesn't match this restaurants previous " + (isForAdmin ? "admin" : "") + " password.");
                return;
            }
            String newPassword = newPasswordBox.getText().toString().trim();
            if (StringUtils.isEmpty(newPassword)) {
                passwordErrorView.setText("Please enter your new password");
                return;
            }
            String newPasswordRepetition = newPasswordRepetitionBox.getText().toString().trim();
            if (StringUtils.isEmpty(newPasswordRepetition)) {
                passwordErrorView.setText("Please repeat new password");
                return;
            }
            if (!newPassword.equals(newPasswordRepetition)) {
                passwordErrorView.setText("Sorry, new password and repetition didn't match.");
                return;
            }
            String setPassword = CryptoUtils.getSha256Digest(newPassword);
            if (isForAdmin) {
                DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, null, null, setPassword, newPassword, null, (restaurantOrBarInfo, e) -> AppPrefs.persistRestaurantOrBarAdminPassword(newPassword, setPassword));
                AppPrefs.persistRestaurantOrBarAdminPassword(newPassword, setPassword);
            } else {
                DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, null, setPassword, null, null, newPassword, (restaurantOrBarInfo, e) -> AppPrefs.persistRestaurantOrBarPassword(setPassword));
                AppPrefs.persistRestaurantOrBarPassword(setPassword);
            }
            UiUtils.showSafeToast("Password Successfully updated");
            finish();
        });
    }

}
