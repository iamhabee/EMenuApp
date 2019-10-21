package com.arke.sdk.util.silentinstall;

import android.content.Context;

import com.landicorp.silentinstallation.DeleteApkObserver;
import com.landicorp.silentinstallation.InstallApkObserver;
import com.landicorp.silentinstallation.SilentInstallInterface;

/**
 * Silent install util.
 */

public class SilentInstallUtil {

    private SilentInstallInterface silentInstall;
    private static SilentInstallUtil instance;

    /**
     * Constructor.
     */
    private SilentInstallUtil() {
        silentInstall = new SilentInstallInterface();
    }

    /**
     * Get instance.
     */
    public static SilentInstallUtil getInstance() {
        if (instance == null) {
            instance = new SilentInstallUtil();
        }
        return instance;
    }

    /**
     * Install APK.
     */
    public void installApk(Context context, String path, InstallApkObserver listener) {
        silentInstall.installApk(context, path, listener);
    }

    /**
     * Uninstall APK.
     */
    public void uninstallApk(Context context, String packageName, DeleteApkObserver listener) {
        silentInstall.uninstallApk(context, packageName, listener);
    }
}
