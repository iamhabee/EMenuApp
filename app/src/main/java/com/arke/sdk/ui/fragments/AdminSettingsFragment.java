package com.arke.sdk.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.activities.PasswordUpdateActivity;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.greenrobot.eventbus.EventBus;

public class AdminSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static String PRIMARY_COLOR_PREF = "primary_color_pref";
    private static String SECONDARY_COLOR_PREF = "secondary_color_pref";
    private static String TERTIARY_COLOR_PREF = "tertiary_color_pref";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.admin_settings_screen, rootKey);
        ColorPreferenceCompat primaryColorPref = findPreference(PRIMARY_COLOR_PREF);
        if (primaryColorPref != null) {
            primaryColorPref.setOnPreferenceChangeListener(this);
            primaryColorPref.onColorSelected(1, AppPrefs.getPrimaryColor());
        }
        ColorPreferenceCompat secondaryColorPref = findPreference(SECONDARY_COLOR_PREF);
        if (secondaryColorPref != null) {
            secondaryColorPref.setOnPreferenceChangeListener(this);
            secondaryColorPref.onColorSelected(2, AppPrefs.getSecondaryColor());
        }
        ColorPreferenceCompat tertiaryColorPref = findPreference(TERTIARY_COLOR_PREF);
        if (tertiaryColorPref != null) {
            tertiaryColorPref.setOnPreferenceChangeListener(this);
            tertiaryColorPref.onColorSelected(3, AppPrefs.getTertiaryColor());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String RESET_PREF = "reset_prefs";
        String UPDATE_RESTAURANT_PASSWORD_PREF = "restaurant_password_update_pref";
        String UPDATE_ADMIN_PASSWORD_PREF = "admin_password_update_pref";
        if (preference.getKey().equals(RESET_PREF)) {
            initColorPreferencesReset();
            return true;
        } else if (preference.getKey().equals(UPDATE_ADMIN_PASSWORD_PREF)) {
            initAdminPasswordUpdate();
            return true;
        } else if (preference.getKey().equals(UPDATE_RESTAURANT_PASSWORD_PREF)) {
            initRestaurantPasswordUpdate();
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    private void initRestaurantPasswordUpdate() {
        Intent passwordUpdateIntent = new Intent(getActivity(), PasswordUpdateActivity.class);
        passwordUpdateIntent.putExtra(Globals.PASSWORD_UPDATE_TYPE_ADMIN, false);
        startActivity(passwordUpdateIntent);
    }

    private void initAdminPasswordUpdate() {
        Intent passwordUpdateIntent = new Intent(getActivity(), PasswordUpdateActivity.class);
        passwordUpdateIntent.putExtra(Globals.PASSWORD_UPDATE_TYPE_ADMIN, true);
        startActivity(passwordUpdateIntent);
    }

    @SuppressWarnings("ConstantConditions")
    private void initColorPreferencesReset() {
        LottieAlertDialog.Builder colorResetDialog = new LottieAlertDialog.Builder(getActivity(),
                DialogTypes.TYPE_QUESTION)
                .setTitle("Reset Color Preferences")
                .setDescription("Are you sure you really want to reset your color preferences? This would take the app back to its default color configurations.")
                .setPositiveText("RESET")
                .setNegativeText("CANCEL")
                .setPositiveListener(lottieAlertDialog -> {
                    lottieAlertDialog.dismiss();
                    lottieAlertDialog.cancel();
                    int defaultPrimary = Color.WHITE;
                    int defaultSecondary = ContextCompat.getColor(getActivity(), R.color.black);
                    int defaultTertiary = ContextCompat.getColor(getActivity(), R.color.colorGreen);
                    AppPrefs.persistRestaurantOrBarPrimaryColor(defaultPrimary);
                    AppPrefs.persistRestaurantOrBarSecondaryColor(defaultSecondary);
                    AppPrefs.persistRestaurantOrBarTertiaryColor(defaultTertiary);
                    UiUtils.showSafeToast("Done!");
                    DataStoreClient.resetAllColorsToDefault(defaultPrimary, defaultSecondary, defaultTertiary);
                }).setNegativeListener(Dialog::dismiss);
        colorResetDialog.build().show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(PRIMARY_COLOR_PREF)) {
            int selectedColor = Integer.parseInt(newValue.toString());
            AppPrefs.persistRestaurantOrBarPrimaryColor(selectedColor);
            EventBus.getDefault().post(Globals.INVALIDATE_SETTINGS);
            DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, new Globals.ModifiableColor(Globals.ModifiableColor.ColorType.PRIMARY, selectedColor), null, null, null, null, (restaurantOrBarInfo, e) -> {

            });
        } else if (preference.getKey().equals(SECONDARY_COLOR_PREF)) {
            int selectedColor = Integer.parseInt(newValue.toString());
            AppPrefs.persistRestaurantOrBarSecondaryColor(Integer.parseInt(newValue.toString()));
            EventBus.getDefault().post(Globals.INVALIDATE_SETTINGS);
            DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, new Globals.ModifiableColor(Globals.ModifiableColor.ColorType.SECONDARY, selectedColor), null, null,null,null, (restaurantOrBarInfo, e) -> {

            });
        } else if (preference.getKey().equals(TERTIARY_COLOR_PREF)) {
            int selectedColor = Integer.parseInt(newValue.toString());
            AppPrefs.persistRestaurantOrBarTertiaryColor(Integer.parseInt(newValue.toString()));
            EventBus.getDefault().post(Globals.INVALIDATE_SETTINGS);
            DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, new Globals.ModifiableColor(Globals.ModifiableColor.ColorType.TERTIARY, selectedColor), null, null, null,null,(restaurantOrBarInfo, e) -> {

            });
        }
        return true;
    }

}
