package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.PinpadForDUKPT;
import com.arke.sdk.util.data.BytesUtil;
import com.arke.sdk.util.pinpad.KeyId;
import com.arke.sdk.util.pinpad.MockKey;
import com.usdk.apiservice.aidl.pinpad.EncKeyFmt;
import com.usdk.apiservice.aidl.pinpad.KAPId;
import com.usdk.apiservice.aidl.pinpad.KeyAlgorithm;
import com.usdk.apiservice.aidl.pinpad.KeyType;
import com.usdk.apiservice.aidl.pinpad.MacMode;
import com.usdk.apiservice.aidl.pinpad.OfflinePinVerify;
import com.usdk.apiservice.aidl.pinpad.OnPinEntryListener;
import com.usdk.apiservice.aidl.pinpad.PinPublicKey;
import com.usdk.apiservice.aidl.pinpad.PinVerifyResult;

import java.util.List;

/**
 * Pinpad for DUKPT demo.
 */

public class PinpadForDUKPTDemo extends ApiDemo {

    private static final String TAG = "PinpadForDUKPTDemo";

    /**
     * Constructor.
     */
    private PinpadForDUKPTDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get pinpad for DUKPT demo instance.
     */
    public static PinpadForDUKPTDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new PinpadForDUKPTDemo(context, toast, dialog);
    }

    /**
     * Do pinpad for DUKPT functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.init_dukpt_ik_ksn))) {
            initKSN();

        } else if (value.equals(getContext().getString(R.string.get_current_ksn))) {
            getCurrentKSN();

        } else if (value.equals(getContext().getString(R.string.increase_current_ksn))) {
            increaseCurrentKSN();

        } else if (value.equals(getContext().getString(R.string.load_plain_text_key))) {
            loadPlainTextKey();

        } else if (value.equals(getContext().getString(R.string.is_key_exist))) {
            isKeyExist();

        } else if (value.equals(getContext().getString(R.string.calculate_mac))) {
            calculateMAC();

        } else if (value.equals(getContext().getString(R.string.calculate_des))) {
            calculateDES();

        } else if (value.equals(getContext().getString(R.string.start_pin_entry))) {
            startPinEntry();

        } else if (value.equals(getContext().getString(R.string.cancel_pin_entry))) {
            cancelPinEntry();

        } else if (value.equals(getContext().getString(R.string.format_pinpad))) {
            format();

        } else if (value.equals(getContext().getString(R.string.switch_to_work_mode))) {
            switchToWorkMode();

        } else if (value.equals(getContext().getString(R.string.encrypt_mag_track))) {
            encryptMagTrack();

        } else if (value.equals(getContext().getString(R.string.get_random))) {
            getRandom();

        } else if (value.equals(getContext().getString(R.string.set_serial_number))) {
            setSerialNumber();

        } else if (value.equals(getContext().getString(R.string.delete_key))) {
            deleteKey();

        } else if (value.equals(getContext().getString(R.string.calculate_kcv))) {
            calculateKCV();

        } else if (value.equals(getContext().getString(R.string.start_offline_pin_entry))) {
            startOfflinePinEntry();

        } else if (value.equals(getContext().getString(R.string.verify_offline_pin))) {
            verifyOfflinePin();

        } else if (value.equals(getContext().getString(R.string.open))) {
            open();

        } else if (value.equals(getContext().getString(R.string.close))) {
            close();

        } else if (value.equals(getContext().getString(R.string.get_existent_kap_ids))) {
            getExistentKapIds();
        }
    }

    /**
     * Get existent KAP IDs.
     */
    private void getExistentKapIds() throws RemoteException {
        // Get KAP IDs
        List<KAPId> list = PinpadForDUKPT.getInstance().getExistentKapIds();

        // Show message
        String message = list.size() + "\r\n";
        for (KAPId kapId : list) {
            message += kapId.getKapNum() + "\r\n";
        }
        showToast(message);
    }

    /**
     * Close device.
     */
    private void close() throws RemoteException {
        // Close
        PinpadForDUKPT.getInstance().close();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Open device.
     */
    private void open() throws RemoteException {
        // Open
        PinpadForDUKPT.getInstance().open();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Verify offline pin.
     */
    private void verifyOfflinePin() throws RemoteException {
        // Verify offline pin
        byte fmtOfPin = OfflinePinVerify.FOPTBV_PLAIN_TEXT;
        int icCardToken = 0;
        byte verifyCmdFmt = 0;
        OfflinePinVerify pinVerify = new OfflinePinVerify(fmtOfPin, icCardToken, verifyCmdFmt, null);
        PinPublicKey publicKey = new PinPublicKey();
        PinVerifyResult verifyResult = new PinVerifyResult();
        PinpadForDUKPT.getInstance().verifyOfflinePin(pinVerify, publicKey, verifyResult);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Start offline pin entry.
     */
    private void startOfflinePinEntry() throws RemoteException {
        Bundle param = new Bundle();
        param.putByteArray("pinLimit", new byte[]{6});
        param.putInt("timeout", 300);
        PinpadForDUKPT.getInstance().startOfflinePinEntry(param, onPinEntryListener);
    }

    /**
     * Calculate KCV.
     */
    private void calculateKCV() throws RemoteException {
        // Calculate KCV
        byte[] data = PinpadForDUKPT.getInstance().calcKCV(KeyId.mainKey);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
    }

    /**
     * Delete key.
     */
    private void deleteKey() throws RemoteException {
        // Delete key
        PinpadForDUKPT.getInstance().deleteKey(KeyId.mainKey);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Set serial number.
     */
    private void setSerialNumber() throws RemoteException {
        // Set pinpad serial number
        byte[] serialNum = BytesUtil.hexString2Bytes("1111111111111111111111111111111111111111111111111111111111111111");
        PinpadForDUKPT.getInstance().setPinpadSerialNum(serialNum);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Get random.
     */
    private void getRandom() throws RemoteException {
        // Get random
        byte[] data = PinpadForDUKPT.getInstance().getRandom(100);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
    }

    /**
     * Encrypt mag track.
     */
    private void encryptMagTrack() throws RemoteException {
        // Encrypt mag track
        byte[] trkData = "\r\n6222600720007543094=4912120351680689\r\n996222600720007543094=1561560500050002123013000000010000000000===0680689351".getBytes();
        trkData = PinpadForDUKPT.getInstance().encryptMagTrack(PinpadForDUKPT.ENCRYPT_MODE_ECB, KeyId.mainKey, trkData);

        // Show message
        showToast(BytesUtil.bytes2HexString(trkData));
    }

    /**
     * Switch to work mode.
     */
    private void switchToWorkMode() throws RemoteException {
        // Switch to work mode
        PinpadForDUKPT.getInstance().switchToWorkMode();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Format.
     */
    private void format() throws RemoteException {
        // Set key algorithm
        PinpadForDUKPT.getInstance().setKeyAlgorithm(KeyAlgorithm.KA_TDEA);

        // Set encrypted key format
        PinpadForDUKPT.getInstance().setEncKeyFormat(EncKeyFmt.ENC_KEY_FMT_NORMAL);

        // Format
        PinpadForDUKPT.getInstance().format();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Cancel pin entry.
     */
    private void cancelPinEntry() throws RemoteException {
        // Start pin entry
        startPinEntry();

        // Delay and cancel pin entry
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    PinpadForDUKPT.getInstance().cancelPinEntry(0);
                    showToast(R.string.succeed);
                } catch (RemoteException | InterruptedException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }.start();
    }

    /**
     * Start pin entry.
     */
    private void startPinEntry() throws RemoteException {
        Bundle param = new Bundle();
        param.putByteArray("pinLimit", new byte[]{6});
        param.putInt("timeout", 300);
        param.putBoolean("isOnline", true);
        param.putByteArray("panBlock", BytesUtil.hexString2Bytes("1111111111111111"));
        PinpadForDUKPT.getInstance().startPinEntry(KeyId.mainKey, param, onPinEntryListener);
    }

    /**
     * Calculate DES.
     */
    private void calculateDES() {
        showToast(R.string.not_support);
    }

    /**
     * Calculate MAC.
     */
    private void calculateMAC() throws RemoteException {
        // Calculate MAC
        int padding = MacMode.MAC_PADDING_MODE_1;
        int alg = MacMode.MAC_ALG_ISO9797;
        byte[] icvData = null;
        byte[] data = BytesUtil.hexString2Bytes("12345678");
        data = PinpadForDUKPT.getInstance().calcMAC(KeyId.mainKey, padding, alg, icvData, data);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
    }

    /**
     * Is key exist.
     */
    private void isKeyExist() throws RemoteException {
        // Get whether key exist
        boolean isExist = PinpadForDUKPT.getInstance().isKeyExist(KeyId.mainKey);

        // Show message
        showToast(String.valueOf(isExist));
    }

    /**
     * Load plain text key.
     */
    private void loadPlainTextKey() throws RemoteException {
        // Load plain text key
        byte[] key = BytesUtil.hexString2Bytes(MockKey.dupktMainKey);
        PinpadForDUKPT.getInstance().loadPlainTextKey(KeyType.MAIN_KEY, KeyId.mainKey, key);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Increase current KSN.
     */
    private void increaseCurrentKSN() throws RemoteException {
        // Increase current KSN
        PinpadForDUKPT.getInstance().increaseCurrentKSN(KeyId.mainKey);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Get current KSN.
     */
    private void getCurrentKSN() throws RemoteException {
        // Get current KSN
        byte[] ksn = PinpadForDUKPT.getInstance().getCurrentKSN(KeyId.mainKey);

        // Show message
        showToast(BytesUtil.bytes2HexString(ksn));
    }

    /**
     * Init KSN.
     */
    private void initKSN() throws RemoteException {
        // Init DUKPT IK KSN
        byte[] ksnData = BytesUtil.hexString2Bytes(MockKey.ksnData);
        PinpadForDUKPT.getInstance().initDUKPTIkKSN(KeyId.mainKey, ksnData);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Callback for Pinpad.
     */
    private OnPinEntryListener onPinEntryListener = new OnPinEntryListener.Stub() {
        @Override
        public void onInput(int len, int key) throws RemoteException {
            Log.d(TAG, "----- onInput -----");
            Log.d(TAG, "len:" + len + ", key:" + key);
        }

        @Override
        public void onConfirm(byte[] data, boolean isNonePin2) throws RemoteException {
            Log.d(TAG, "----- onConfirm -----");

            showToast("data:" + BytesUtil.bytes2HexString(data) + ", isNonePin:" + isNonePin2);
        }

        @Override
        public void onCancel() throws RemoteException {
            Log.d(TAG, "----- onCancel -----");

            showToast(R.string.cancel);
        }

        @Override
        public void onError(int error) throws RemoteException {
            Log.d(TAG, "----- onError -----");

            showToast(PinpadForDUKPT.getErrorId(error));
        }
    };
}
