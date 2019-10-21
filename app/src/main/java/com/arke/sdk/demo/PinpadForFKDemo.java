package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.PinpadForFK;
import com.arke.sdk.util.data.BytesUtil;
import com.arke.sdk.util.pinpad.KeyId;
import com.arke.sdk.util.pinpad.MockKey;
import com.usdk.apiservice.aidl.pinpad.DESMode;
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
 * Pinpad for FK demo.
 */

public class PinpadForFKDemo extends ApiDemo {

    private static final String TAG = "PinpadForFKDemo";

    /**
     * Constructor.
     */
    private PinpadForFKDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get pinpad for FK demo instance.
     */
    public static PinpadForFKDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new PinpadForFKDemo(context, toast, dialog);
    }

    /**
     * Do pinpad for FK functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.load_plain_text_key))) {
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
        List<KAPId> list = PinpadForFK.getInstance().getExistentKapIds();

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
        PinpadForFK.getInstance().close();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Open device.
     */
    private void open() throws RemoteException {
        // Open
        PinpadForFK.getInstance().open();

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
        PinpadForFK.getInstance().verifyOfflinePin(pinVerify, publicKey, verifyResult);

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
        PinpadForFK.getInstance().startOfflinePinEntry(param, onPinEntryListener);
    }

    /**
     * Calculate KCV.
     */
    private void calculateKCV() throws RemoteException {
        // Calculate KCV
        byte[] data = PinpadForFK.getInstance().calcKCV(KeyId.macKey);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
    }

    /**
     * Delete key.
     */
    private void deleteKey() throws RemoteException {
        // Delete key
        PinpadForFK.getInstance().deleteKey(KeyId.macKey);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Set serial number.
     */
    private void setSerialNumber() throws RemoteException {
        // Set pinpad serial number
        byte[] serialNum = BytesUtil.hexString2Bytes("1111111111111111111111111111111111111111111111111111111111111111");
        PinpadForFK.getInstance().setPinpadSerialNum(serialNum);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Get random.
     */
    private void getRandom() throws RemoteException {
        // Get random
        byte[] data = PinpadForFK.getInstance().getRandom(100);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
    }

    /**
     * Encrypt mag track.
     */
    private void encryptMagTrack() throws RemoteException {
        // Encrypt mag track
        byte[] trkData = "\r\n6222600720007543094=4912120351680689\r\n996222600720007543094=1561560500050002123013000000010000000000===0680689351".getBytes();
        trkData = PinpadForFK.getInstance().encryptMagTrack(PinpadForFK.ENCRYPT_MODE_ECB, KeyId.tdkKey, trkData);

        // Show message
        showToast(BytesUtil.bytes2HexString(trkData));
    }

    /**
     * Switch to work mode.
     */
    private void switchToWorkMode() throws RemoteException {
        // Switch to work mode
        PinpadForFK.getInstance().switchToWorkMode();

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Format.
     */
    private void format() throws RemoteException {
        // Set key algorithm
        PinpadForFK.getInstance().setKeyAlgorithm(KeyAlgorithm.KA_TDEA);

        // Set encrypted key format
        PinpadForFK.getInstance().setEncKeyFormat(EncKeyFmt.ENC_KEY_FMT_NORMAL);

        // Format
        PinpadForFK.getInstance().format();

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
                    PinpadForFK.getInstance().cancelPinEntry(0);
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
        PinpadForFK.getInstance().startPinEntry(KeyId.pinKey, param, onPinEntryListener);
    }

    /**
     * Calculate DES.
     */
    private void calculateDES() throws RemoteException {
        // Calculate DES
        byte[] data = BytesUtil.hexString2Bytes(MockKey.macKey);
        byte[] icvData = null;
        DESMode desMode = new DESMode(DESMode.DM_ENC, DESMode.DM_OM_TECB);
        data = PinpadForFK.getInstance().calculateDes(KeyId.dekKey, desMode, icvData, data);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
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
        data = PinpadForFK.getInstance().calcMAC(KeyId.macKey, padding, alg, icvData, data);

        // Show message
        showToast(BytesUtil.bytes2HexString(data));
    }

    /**
     * Load plain text key.
     */
    private void loadPlainTextKey() throws RemoteException {
        // Get all keys
        byte[] macKey = BytesUtil.hexString2Bytes(MockKey.macKey);
        byte[] pinKey = BytesUtil.hexString2Bytes(MockKey.pinKey);
        byte[] tdkKey = BytesUtil.hexString2Bytes(MockKey.tdkKey);
        byte[] dekKey = BytesUtil.hexString2Bytes(MockKey.dekKey);
        byte[] cbcKey = BytesUtil.hexString2Bytes(MockKey.cbcKey);

        // Load MAC key
        PinpadForFK.getInstance().loadPlainTextKey(KeyType.MAC_KEY, KeyId.macKey, macKey);

        // Load PIN key
        PinpadForFK.getInstance().loadPlainTextKey(KeyType.PIN_KEY, KeyId.pinKey, pinKey);

        // Load TDK key
        PinpadForFK.getInstance().loadPlainTextKey(KeyType.TDK_KEY, KeyId.tdkKey, tdkKey);

        // Load DEK key
        PinpadForFK.getInstance().loadPlainTextKey(KeyType.DEK_KEY, KeyId.dekKey, dekKey);

        // Load CBC MAC key
        PinpadForFK.getInstance().loadPlainTextKey(KeyType.CBC_MAC_KEY, KeyId.cbcKey, cbcKey);

        // Show message
        showToast(R.string.succeed);
    }

    /**
     * Is key exist.
     */
    private void isKeyExist() throws RemoteException {
        // Get whether key exist
        boolean isExist = PinpadForFK.getInstance().isKeyExist(KeyId.macKey);

        // Show message
        showToast(String.valueOf(isExist));
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

            showToast(PinpadForFK.getErrorId(error));
        }
    };
}
