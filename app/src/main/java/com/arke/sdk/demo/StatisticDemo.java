package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.view.SystemStatisticActivity;

/**
 * Statistic demo.
 */

public class StatisticDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private StatisticDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get statistic demo instance.
     */
    public static StatisticDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new StatisticDemo(context, toast, dialog);
    }

    /**
     * Do system statistic functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.system_statistic))) {
            Intent intent = new Intent(getContext(), SystemStatisticActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }
}
