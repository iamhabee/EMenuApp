package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.util.signpanel.SignPanelUtil;
import com.smartpos.signpanel.SignPanel;

/**
 * Sign panel demo.
 */

public class SignPanelDemo extends ApiDemo {

    private static final String TAG = "SignPanelDemo";

    /**
     * Constructor.
     */
    private SignPanelDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get sign panel demo instance.
     */
    public static SignPanelDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new SignPanelDemo(context, toast, dialog);
    }

    /**
     * Do sign panel functions.
     */
    public void execute(String value) {
        if (value.equals(getContext().getString(R.string.show_sign_panel))) {
            showSignPanel();

        } else if (value.equals(getContext().getString(R.string.hide_sign_panel))) {
            hideSignPanel();
        }
    }

    /**
     * Show sign panel.
     */
    private void showSignPanel() {
        SignPanelUtil.getInstance(getContext()).showSignPanel(50, "0123ABCD", SignPanel.ROTATION_AUTO, 3, true, new SignPanel.SignPanelCallback() {

            @Override
            public void onResult(int resultCode, String compressedData, String picData) {
                Log.d(TAG, "ResultCode : " + resultCode + ", " + "CompressedData : " + compressedData + "," + "PicData : " + picData);
                showToast(SignPanelUtil.getErrorId(resultCode));
            }
        });
    }

    /**
     * Hide sign panel.
     */
    private void hideSignPanel() {
        // Show sign panel
        showSignPanel();

        // Delay and hide
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    SignPanelUtil.getInstance(getContext()).cancelSignPanelByForce();
                } catch (InterruptedException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }.start();
    }
}
