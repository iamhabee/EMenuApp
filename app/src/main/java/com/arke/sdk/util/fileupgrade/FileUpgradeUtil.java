package com.arke.sdk.util.fileupgrade;

import android.content.Context;

import com.arke.sdk.R;
import com.landi.upgrade.jni.JNIInvoker;

import java.util.Hashtable;
import java.util.Map;

/**
 * File upgrade util.
 */

public class FileUpgradeUtil {

    private JNIInvoker jniInvoker;
    private static FileUpgradeUtil instance;

    /**
     * Constructor.
     */
    private FileUpgradeUtil() {
        jniInvoker = new JNIInvoker();
    }

    /**
     * Get instance.
     */
    public static FileUpgradeUtil getInstance() {
        if (instance == null) {
            instance = new FileUpgradeUtil();
        }
        return instance;
    }

    /**
     * Get version.
     */
    public String getVersion() {
        return jniInvoker.getVersion();
    }

    /**
     * Upgrade.
     */
    public String upgrade(Context mContext, String fileList, byte[] RetInfo) {
        return jniInvoker.upgrade(mContext, fileList, RetInfo);
    }

    /**
     * Error code.
     */
    private static Map<Integer, Integer> errorCodes;

    static {
        errorCodes = new Hashtable<>();
        errorCodes.put(0, R.string.succeed);
    }

    /**
     * Get error id.
     */
    public static int getErrorId(int errorCode) {
        if (errorCodes.containsKey(errorCode)) {
            return errorCodes.get(errorCode);
        }

        return R.string.other_error;
    }
}
