package com.arke.sdk.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.utilities.FontUtils;
import com.arke.sdk.utilities.UiUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.hanks.htextview.base.HTextView;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends BaseActivity {

    @BindView(R.id.restaurant_logo)
    ImageView restaurantLogoView;

    @BindView(R.id.welcome_view)
    HTextView welcomeView;

    @BindView(R.id.restaurant_or_bar_cover_photo_view)
    ImageView restaurantOrBarCoverPhotoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        int primaryColor = AppPrefs.getPrimaryColor();
        if (!UiUtils.whitish(primaryColor)) {
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
        } else {
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        }
        welcomeView.setTypeface(FontUtils.selectTypeface(this, 5));
        new Handler().postDelayed(this::postInitUI, 2000);
    }

    private void postInitUI() {
        restaurantLogoView.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.BounceIn)
                .duration(700)
                .playOn(restaurantLogoView);
        String restaurantOrBarName = AppPrefs.getRestaurantOrBarName();
        String restaurantOrBarProfilePhoto = AppPrefs.getRestaurantOrBarProfilePhotoUrl();
        if (StringUtils.isNotEmpty(restaurantOrBarProfilePhoto)) {
            UiUtils.loadImageIntoView(restaurantOrBarCoverPhotoView, restaurantOrBarProfilePhoto);
        }
        String welcomeMessage = "Welcome to \n" + restaurantOrBarName;
        welcomeView.setText(welcomeMessage);
        welcomeView.animateText(welcomeMessage);
        welcomeView.setAnimationListener(hTextView -> {
            boolean setup = AppPrefs.isAppSetup();
            if (setup) {
                transitionWithPreferences(AppPrefs.getUseType());
            }
        });
    }

    private void transitionWithPreferences(int useType) {
        new Handler().postDelayed(() -> {
            if (useType == Globals.UseType.USE_TYPE_KITCHEN.ordinal()) {
                transitionToKitchenHome();
            } else if (useType == Globals.UseType.USE_TYPE_WAITER.ordinal()) {
                transitionToWaiterHome();
            } else if (useType == Globals.UseType.USE_TYPE_BAR.ordinal()) {
                transitionToBarHome();
            } else {
                transitionToAdminHome();
            }
        }, 2000);
    }

    private void transitionToAdminHome() {
        Intent adminHomeIntent = new Intent(this, AdminHomeActivity.class);
        startActivity(adminHomeIntent);
        finish();
    }

    private void transitionToBarHome() {
        Intent barHomeIntent = new Intent(this, BarHomeActivity.class);
        startActivity(barHomeIntent);
        finish();
    }

    private void transitionToWaiterHome() {
        Intent waiterHomeIntent = new Intent(this, WaiterHomeActivity.class);
        startActivity(waiterHomeIntent);
        finish();
    }

    private void transitionToKitchenHome() {
        Intent kitchenHomeIntent = new Intent(this, KitchenHomeActivity.class);
        startActivity(kitchenHomeIntent);
        finish();
    }

}
