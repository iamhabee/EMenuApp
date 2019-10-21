package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.ScannerForBack;
import com.arke.sdk.api.ScannerForFront;
import com.usdk.apiservice.aidl.scanner.OnScanListener;

/**
 * Scanner demo.
 */

public class ScannerDemo extends ApiDemo {

    private static final String TAG = "ScannerDemo";

    /**
     * Constructor.
     */
    private ScannerDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get scanner demo instance.
     */
    public static ScannerDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new ScannerDemo(context, toast, dialog);
    }

    /**
     * Do scanner functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.start_front_scan))) {
            startFrontScan();

        } else if (value.equals(getContext().getString(R.string.start_back_scan))) {
            startBackScan();

        } else if (value.equals(getContext().getString(R.string.stop_front_scan))) {
            stopFrontScan();

        } else if (value.equals(getContext().getString(R.string.stop_back_scan))) {
            stopBackScan();
        }
    }

    /**
     * Stop back scan.
     */
    private void stopBackScan() throws RemoteException {
        // Start scanning
        startBackScan();

        // Delay and cancel
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    ScannerForBack.getInstance().stopScan();
                } catch (RemoteException | InterruptedException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }.start();
    }

    /**
     * Stop front scan.
     */
    private void stopFrontScan() throws RemoteException {
        // Start scanning
        startFrontScan();

        // Delay and cancel
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    ScannerForFront.getInstance().stopScan();
                } catch (RemoteException | InterruptedException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }.start();
    }

    /**
     * Start back scan.
     */
    private void startBackScan() throws RemoteException {
        ScannerForBack.getInstance().startScan(30, new OnScanListener.Stub() {

            @Override
            public void onSuccess(String code) throws RemoteException {
                Log.d(TAG, "--- onSuccess ---");

                showToast(code);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "--- onError ---");

                showToast(ScannerForBack.getErrorId(error));
            }

            @Override
            public void onTimeout() throws RemoteException {
                Log.d(TAG, "--- onTimeout ---");

                showToast(R.string.timeout);
            }

            @Override
            public void onCancel() throws RemoteException {
                Log.d(TAG, "--- onCancel ---");

                showToast(R.string.cancel);
            }
        });
    }

    /**
     * Start front scan.
     */
    private void startFrontScan() throws RemoteException {
        ScannerForFront.getInstance().startScan(30, new OnScanListener.Stub() {

            @Override
            public void onSuccess(String code) throws RemoteException {
                Log.d(TAG, "--- onSuccess ---");

                showToast(code);
            }

            @Override
            public void onError(int error) throws RemoteException {
                Log.d(TAG, "--- onError ---");

                showToast(ScannerForFront.getErrorId(error));
            }

            @Override
            public void onTimeout() throws RemoteException {
                Log.d(TAG, "--- onTimeout ---");

                showToast(R.string.timeout);
            }

            @Override
            public void onCancel() throws RemoteException {
                Log.d(TAG, "--- onCancel ---");

                showToast(R.string.cancel);
            }
        });
    }
}
