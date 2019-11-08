package com.arke.sdk.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.arke.sdk.R;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
import com.arke.sdk.preferences.AppPrefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseActivity extends AppCompatActivity {

    protected String primaryColorHex, secondaryColorHex;
    protected int primaryColorInt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        checkAndRegisterEventBus();
        initColors();
    }

    protected int getBlackBackButton() {
        return R.drawable.ic_arrow_back_black_24dp;
    }

    protected int getWhiteBackButton() {
        return R.drawable.white_back_button;
    }

    protected void tintToolbarAndTabLayout(int colorPrimary) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(UiUtils.darker(colorPrimary, 0.9f));
        }
    }

    protected void initColors() {
        primaryColorInt = AppPrefs.getPrimaryColor();
        EMenuLogger.d("ColorTag", "Primary Color Int=" + primaryColorInt);
        primaryColorHex = "#" + Integer.toHexString(primaryColorInt);
        EMenuLogger.d("ColorTag", "Primary Color Hex=" + primaryColorHex);
        secondaryColorHex = "#" + Integer.toHexString(AppPrefs.getSecondaryColor());
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegisterEventBus();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAndRegisterEventBus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRegisterEventBus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkAndUnRegisterEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAndUnRegisterEventBus();
    }

    private void checkAndRegisterEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAndUnRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventMainThread(Object event) {

    }

}
