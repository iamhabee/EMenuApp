package com.arke.sdk.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.util.printer.Printer;
import com.arke.sdk.utilities.UiUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import java.util.List;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            prepareAndTransitionAppropriately();
                        } else {
                            UiUtils.showSafeToast("Sorry, we can't proceed unless you grant us permissions.");
                            finish();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permanently, navigate user to app settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(error -> UiUtils.showSafeToast("Error occurred! " + error.toString()))
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {

        this.runOnUiThread(() -> {
            try {
                LottieAlertDialog errorCreationErrorDialog = new LottieAlertDialog
                        .Builder(this, DialogTypes.TYPE_ERROR)
                        .setTitle("Need Permissions").setDescription("This app needs permission to " +
                                "use this feature. You can grant them in app settings.")
                        .setPositiveText("GOTO SETTINGS").setPositiveListener(dialog -> {
                            dialog.dismiss();
                            openSettings();
                        })
                        .build();
                if (!errorCreationErrorDialog.isShowing()) {
                    errorCreationErrorDialog.setCancelable(false);
                    errorCreationErrorDialog.show();
                }

            } catch (NullPointerException ex) {
                ex.getMessage();
            }
        });
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @SuppressLint("HardwareIds")
    private void prepareAndTransitionAppropriately() {
        int primaryColor = AppPrefs.getPrimaryColor();
        if (!UiUtils.whitish(primaryColor)) {
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
        } else {
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        }
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#" + Integer.toHexString(primaryColor))));
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String deviceId = AppPrefs.getDeviceId();

        Log.d("res# deviceId", "id: " + deviceId);

        if (deviceId == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                assert mTelephonyManager != null;
                deviceId = mTelephonyManager.getImei();
            } else {
                assert mTelephonyManager != null;
                deviceId = mTelephonyManager.getDeviceId();
            }
            AppPrefs.setDeviceId(deviceId);
            // Create a global webView to load print template
            Printer.initWebView(SplashActivity.this);

            proceedWithTransition();

        } else {
            proceedWithTransition();
        }
    }

    private void proceedWithTransition() {
        boolean setup = AppPrefs.isAppSetup();
        if (!setup) {
            navigateToOnBoardingScreen();
        } else {
            navigateToWelcomeScreen();
        }
    }

    private void navigateToWelcomeScreen() {
        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }

    private void navigateToOnBoardingScreen() {
        Intent onBoardingIntent = new Intent(this, OnBoardingActivity.class);
        startActivity(onBoardingIntent);
        finish();
    }

}
