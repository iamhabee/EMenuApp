package com.arke.sdk.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.ui.fragments.AdminSettingsFragment;
import com.arke.sdk.ui.fragments.SettingsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.action_header)
    TextView actionHeaderView;

    @BindView(R.id.close_activity)
    AppCompatImageView closeActivityView;

    @BindView(R.id.top_panel)
    View topPanelView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        ButterKnife.bind(this);
        invalidateUI();
        closeActivityView.setOnClickListener(view -> {
            UiUtils.blinkView(view);
            finish();
        });
        Bundle intentExtras = getIntent().getExtras();
        boolean displayAdminSettings = false;
        if (intentExtras != null) {
            displayAdminSettings = intentExtras.getBoolean(Globals.DISPLAY_ADMIN_SETTINGS);
        }
        if (displayAdminSettings) {
            getSupportFragmentManager().beginTransaction().add(R.id.settings_content, new AdminSettingsFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.settings_content, new SettingsFragment()).commit();
        }
    }

    @Override
    public void onEventMainThread(Object event) {
        if (event instanceof String) {
            String data = (String) event;
            runOnUiThread(() -> {
                if (data.equals(Globals.INVALIDATE_SETTINGS)) {
                    invalidateUI();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateUI();
    }

    private void invalidateUI() {
        if (UiUtils.whitish(primaryColorInt)) {
            closeActivityView.setImageResource(getBlackBackButton());
            actionHeaderView.setTextColor(Color.BLACK);
            topPanelView.setBackgroundColor(Color.WHITE);
            tintToolbarAndTabLayout(ContextCompat.getColor(this, R.color.ease_gray));
        } else {
            closeActivityView.setImageResource(getWhiteBackButton());
            topPanelView.setBackgroundColor(Color.parseColor(primaryColorHex));
            actionHeaderView.setTextColor(Color.WHITE);
            tintToolbarAndTabLayout(Color.parseColor(primaryColorHex));
        }
    }

}
