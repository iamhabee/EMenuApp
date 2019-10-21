package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.DeviceManager;
import com.arke.sdk.api.ParamFile;
import com.arke.sdk.util.data.DateUtil;

/**
 * Other demo.
 */

public class OtherDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private OtherDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get other demo instance.
     */
    public static OtherDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new OtherDemo(context, toast, dialog);
    }

    /**
     * Do other functions.
     */
    public void execute(String value) throws RemoteException {
        String message = "";

        if (value.equals(getContext().getString(R.string.get_terminal_info))) {
            message = getTerminalInfo();

        } else if (value.equals(getContext().getString(R.string.update_system_time))) {
            message = updateSystemTime();

        } else if (value.equals(getContext().getString(R.string.is_first_run))) {
            message = isFirstRun();

        } else if (value.equals(getContext().getString(R.string.is_exist))) {
            message = isParamFileExist();

        } else if (value.equals(getContext().getString(R.string.get_boolean_param))) {
            message = getBooleanParam();

        } else if (value.equals(getContext().getString(R.string.get_string_param))) {
            message = getStringParam();
        }

        // Show message
        showToast(message);
    }

    /**
     * Get string param.
     */
    private String getStringParam() throws RemoteException {
        return ParamFile.getInstance().getString("01000001", "111111111111111");
    }

    /**
     * Get boolean param.
     */
    private String getBooleanParam() throws RemoteException {
        boolean isSuccessful = ParamFile.getInstance().getBoolean("01012108", true);
        return String.valueOf(isSuccessful);
    }

    /**
     * Is param file exist.
     */
    private String isParamFileExist() throws RemoteException {
        boolean isSuccessful = ParamFile.getInstance().isParamFileExists();
        return String.valueOf(isSuccessful);
    }

    /**
     * Is first rum.
     */
    private String isFirstRun() throws RemoteException {
        boolean isSuccessful = ParamFile.getInstance().isFirstRun();
        return String.valueOf(isSuccessful);
    }

    /**
     * Update system time.
     */
    private String updateSystemTime() throws RemoteException {
        // Get current time
        String curDate = DateUtil.getCurrentTime("yyyy.MM.dd HH:mm:ss");

        // Update system time
        DeviceManager.getInstance().updateSystemTime("20170101000000");

        // Check
        String newDate = DateUtil.getCurrentTime("yyyy.MM.dd HH:mm:ss");

        return "curDate: " + curDate + "\r\n" + "newDate: " + newDate;
    }

    /**
     * Get terminal info.
     */
    private String getTerminalInfo() throws RemoteException {
        return "PSamID: " + DeviceManager.getInstance().getPSamId() + "\r\n" +
                "SerialNo: " + DeviceManager.getInstance().getSerialNo() + "\r\n" +
                "IMSI: " + DeviceManager.getInstance().getIMSI() + "\r\n" +
                "IMEI: " + DeviceManager.getInstance().getIMEI() + "\r\n" +
                "ICCID: " + DeviceManager.getInstance().getICCID() + "\r\n" +
                "Manufacture: " + DeviceManager.getInstance().getManufacture() + "\r\n" +
                "Model: " + DeviceManager.getInstance().getModel() + "\r\n" +
                "AndroidOSVersion: " + DeviceManager.getInstance().getAndroidOSVersion() + "\r\n" +
                "AndroidKernelVersion: " + DeviceManager.getInstance().getAndroidKernelVersion() + "\r\n" +
                "ROMVersion: " + DeviceManager.getInstance().getRomVersion() + "\r\n" +
                "getFirmwareVersion: " + DeviceManager.getInstance().getFirmwareVersion() + "\r\n" +
                "getHardwareVersion: " + DeviceManager.getInstance().getHardwareVersion() + "\r\n";
    }
}
