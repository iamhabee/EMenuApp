package com.arke.sdk.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.arke.sdk.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.utilities.DataStoreClient;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;
import com.labters.lottiealertdialoglibrary.DialogTypes;
import com.labters.lottiealertdialoglibrary.LottieAlertDialog;

import org.greenrobot.eventbus.EventBus;

@SuppressWarnings("FieldCanBeLocal")
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final int REQUEST_CODE_ALERT_RINGTONE = 0x10;
    private static String RINGTONE_PREF = "ringtone_pref";
    private static String PRIMARY_COLOR_PREF = "primary_color_pref";
    private static String SECONDARY_COLOR_PREF = "secondary_color_pref";
    private static String TERTIARY_COLOR_PREF = "tertiary_color_pref";
    private static String RESET_PREF = "reset_prefs";
    private Preference ringtonePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.customization_settings_screen, rootKey);
        ringtonePreference = findPreference(RINGTONE_PREF);
        checkAndLoadRingtonePrefValue(AppPrefs.getIncomingNotificationRingtoneUri());
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
        if (preference.getKey().equals(RINGTONE_PREF)) {
            selectRingTone();
            return true;
        } else if (preference.getKey().equals(RESET_PREF)) {
            initColorPreferencesReset();
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
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

    private void selectRingTone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
        String existingValue = getRingtonePreferenceValue();
        if (existingValue != null) {
            if (existingValue.length() == 0) {
                // Select "Silent"
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            } else {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
            }
        } else {
            // No ringtone has been selected, set to the default
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtone != null) {
                setRingtonePreferenceValue(ringtone.toString());
            } else {
                setRingtonePreferenceValue("");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setRingtonePreferenceValue(String newPreference) {
        AppPrefs.persistIncomingNotificationRingtoneUri(newPreference);
        checkAndLoadRingtonePrefValue(newPreference);
    }

    private void checkAndLoadRingtonePrefValue(String newPreference) {
        if (newPreference != null) {
            String path = Uri.parse(newPreference).getPath();
            if (path != null) {
                ringtonePreference.setSummary(path);
            }
        }
    }

    private String getRingtonePreferenceValue() {
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(PRIMARY_COLOR_PREF)) {
            int selectedColor = Integer.parseInt(newValue.toString());
            AppPrefs.persistRestaurantOrBarPrimaryColor(selectedColor);
            EventBus.getDefault().post(Globals.INVALIDATE_SETTINGS);
            DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, new Globals.ModifiableColor(Globals.ModifiableColor.ColorType.PRIMARY, selectedColor), null, null,null,null, (restaurantOrBarInfo, e) -> {

            });
        } else if (preference.getKey().equals(SECONDARY_COLOR_PREF)) {
            int selectedColor = Integer.parseInt(newValue.toString());
            AppPrefs.persistRestaurantOrBarSecondaryColor(Integer.parseInt(newValue.toString()));
            EventBus.getDefault().post(Globals.INVALIDATE_SETTINGS);
            DataStoreClient.updateRestaurantOrBarInfo(null, null, null, null, new Globals.ModifiableColor(Globals.ModifiableColor.ColorType.SECONDARY, selectedColor), null, null, null,null,(restaurantOrBarInfo, e) -> {

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
