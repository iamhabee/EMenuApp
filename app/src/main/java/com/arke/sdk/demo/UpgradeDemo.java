package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.util.data.StringUtil;
import com.arke.sdk.util.fileupgrade.FileUpgradeUtil;
import com.arke.sdk.util.silentinstall.SilentInstallUtil;
import com.landicorp.silentinstallation.DeleteApkObserver;
import com.landicorp.silentinstallation.InstallApkObserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Upgrade demo.
 */

public class UpgradeDemo extends ApiDemo {

    private static final String TAG = "UpgradeDemo";
    private static final String SDCARD_PATH = "/sdcard/";
    private static final String UNS_FILE_NAME = "TestApplication-v2.0.uns";
    private static final String PKG_FILE_NAME = "APOSOVS_ShellApk-1.0.0.pkg";
    private static final String APK_FILE_NAME = "TestApplication-v1.0.apk";
    private static final String APK_PACKAGE_NAME = "com.example.test.testapplication";

    /**
     * Constructor.
     */
    private UpgradeDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get upgrade demo instance.
     */
    public static UpgradeDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new UpgradeDemo(context, toast, dialog);
    }

    /**
     * Do upgrade functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.get_version))) {
            getVersion();

        } else if (value.equals(getContext().getString(R.string.mock_download))) {
            mockDownload();

        } else if (value.equals(getContext().getString(R.string.offline_dll_upgrade))) {
            upgradeDll();

        } else if (value.equals(getContext().getString(R.string.install_apk))) {
            installApk();

        } else if (value.equals(getContext().getString(R.string.uninstall_apk))) {
            uninstallApk();
        }
    }

    /**
     * Uninstall APK.
     */
    private void uninstallApk() {
        showDialog(R.string.uninstalling, false);
        SilentInstallUtil.getInstance().uninstallApk(getContext(), APK_PACKAGE_NAME, new DeleteApkObserver() {

            @Override
            public void onUnInstallFinished(String packageName) {
                Log.d(TAG, "onUnInstallFinished: " + packageName);
                hideDialog();
                showToast(R.string.succeed);
            }

            @Override
            public void onUnInstallError(int code, String packageName) {
                Log.d(TAG, "onUnInstallError: [" + code + "]" + packageName);
                hideDialog();
                showToast(R.string.failed);
            }
        });
    }

    /**
     * Install APK.
     */
    private void installApk() {
        String path = SDCARD_PATH + APK_FILE_NAME;
        showDialog(R.string.installing, false);
        SilentInstallUtil.getInstance().installApk(getContext(), path, new InstallApkObserver() {

            @Override
            public void onInstallFinished(String packageName) {
                Log.d(TAG, "onInstallFinished: " + packageName);
                hideDialog();
                showToast(R.string.succeed);
            }

            @Override
            public void onInstallError(int code, String packageName) {
                Log.d(TAG, "onInstallError: [" + code + "]" + packageName);
                hideDialog();
                showToast(R.string.failed);
            }
        });
    }

    /**
     * Mock download.
     */
    private void mockDownload() throws RemoteException {
        copyAssetsFileToSdcard(UNS_FILE_NAME);
        copyAssetsFileToSdcard(PKG_FILE_NAME);
        copyAssetsFileToSdcard(APK_FILE_NAME);
        showToast(R.string.succeed);
    }

    /**
     * Upgrade DLL.
     */
    private void upgradeDll() {
        String list = SDCARD_PATH + UNS_FILE_NAME + ";"
                + SDCARD_PATH + PKG_FILE_NAME;
        byte[] retInfo = new byte[1024];
        showDialog(R.string.upgrading, false);
        new Thread() {
            @Override
            public void run() {
                String ret = FileUpgradeUtil.getInstance().upgrade(getContext(), list, retInfo);
                hideDialog();
                if (ret != null && StringUtil.isNumber(ret)) {
                    showToast(FileUpgradeUtil.getErrorId(Integer.parseInt(ret)));
                } else {
                    showToast(R.string.failed);
                }
            }
        }.start();
    }

    /**
     * Get version.
     */
    private void getVersion() {
        String version = FileUpgradeUtil.getInstance().getVersion();
        showToast(version);
    }

    /**
     * Copy assets file.
     */
    private void copyAssetsFileToSdcard(String fileName) throws RemoteException {
        int byteRead;
        InputStream input = null;
        try {
            input = getContext().getAssets().open(fileName);
            FileOutputStream fs = new FileOutputStream(SDCARD_PATH + fileName);
            byte[] buffer = new byte[input.available()];
            while ((byteRead = input.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            input.close();
        } catch (IOException e) {
            throw new RemoteException(e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }
    }
}
