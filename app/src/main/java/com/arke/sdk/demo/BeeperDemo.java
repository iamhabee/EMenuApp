package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.Beeper;

/**
 * Beeper demo.
 */

public class BeeperDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private BeeperDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get beeper demo instance.
     */
    public static BeeperDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new BeeperDemo(context, toast, dialog);
    }

    /**
     * Do beeper functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.normal))) {
            beepWhenNormal();

        } else if (value.equals(getContext().getString(R.string.error))) {
            beepWhenError();

        } else if (value.equals(getContext().getString(R.string.interval))) {
            beepWhenInterval();

        } else if (value.equals(getContext().getString(R.string.beep_two_seconds))) {
            beepTwoSeconds();
        }
    }

    /**
     * Beep two seconds.
     */
    private void beepTwoSeconds() throws RemoteException {
        Beeper.getInstance().startBeep(2000);
    }

    /**
     * Beep when interval.
     */
    private void beepWhenInterval() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Beeper.getInstance().startBeep(200);
                    sleep(500);
                    Beeper.getInstance().startBeep(200);
                    sleep(500);
                    Beeper.getInstance().startBeep(200);
                } catch (RemoteException | InterruptedException e) {
                    showToast(e.getLocalizedMessage());
                }
            }
        }.start();
    }

    /**
     * Beep when error.
     */
    private void beepWhenError() throws RemoteException {
        Beeper.getInstance().startBeep(1000);
    }

    /**
     * Beep when normal.
     */
    private void beepWhenNormal() throws RemoteException {
        Beeper.getInstance().startBeep(500);
    }
}
