package com.arke.sdk.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.contracts.RuntimePermissionsGrantedCallBack;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.preferences.AppPrefs;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

public class SplashActivity extends BaseActivity {

    private String deviceId;
    private TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareAndTransitionAppropriately();
    }

    private void prepareAndTransitionAppropriately() {
        int primaryColor = AppPrefs.getPrimaryColor();
        if (!UiUtils.whitish(primaryColor)) {
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
        } else {
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        }
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#" + Integer.toHexString(primaryColor))));
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        deviceId = AppPrefs.getDeviceId();
        if (deviceId == null) {
            pickDeviceId(granted -> {
                if (granted) {
                    proceedWithTransition();
                } else {
                    UiUtils.showSafeToast("Sorry, we can't proceed unless you grant us permissions.");
                    finish();
                }
            });
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

    private void pickDeviceId(RuntimePermissionsGrantedCallBack runtimePermissionsGrantedCallBack) {
        Permissions.check(this/*context*/, Manifest.permission.READ_PHONE_STATE, null, new PermissionHandler() {
            @SuppressLint({"MissingPermission", "HardwareIds"})

            @Override
            public void onGranted() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephonyManager.getImei();
                } else {
                    deviceId = mTelephonyManager.getDeviceId();
                }
                AppPrefs.setDeviceId(deviceId);
                if (runtimePermissionsGrantedCallBack != null) {
                    runtimePermissionsGrantedCallBack.onGrantStatus(true);
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                UiUtils.showSafeToast("Sorry, we can't proceed orders unless you grant us permissions.");
                finish();
            }

        });

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
