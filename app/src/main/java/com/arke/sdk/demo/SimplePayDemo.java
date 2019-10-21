package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.view.SaleActivity;
import com.arke.sdk.R;

/**
 * Simple pay demo.
 */

public class SimplePayDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private SimplePayDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get simple pay demo instance.
     */
    public static SimplePayDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new SimplePayDemo(context, toast, dialog);
    }

    /**
     * Do simple pay functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.sale))) {
            Intent intent = new Intent(getContext(), SaleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }
}
