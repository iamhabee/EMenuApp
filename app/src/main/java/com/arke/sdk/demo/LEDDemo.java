package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.LED;
import com.usdk.apiservice.aidl.led.Light;

/**
 * LED demo.
 */

public class LEDDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private LEDDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get LED demo instance.
     */
    public static LEDDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new LEDDemo(context, toast, dialog);
    }

    /**
     * Do led functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.led_on_all))) {
            turnOnAll();

        } else if (value.equals(getContext().getString(R.string.led_off_all))) {
            turnOffAll();

        } else if (value.equals(getContext().getString(R.string.operate_red_light))) {
            operateRedLight();

        } else if (value.equals(getContext().getString(R.string.operate_green_light))) {
            operateGreenLight();

        } else if (value.equals(getContext().getString(R.string.operate_yellow_light))) {
            operateYellowLight();

        } else if (value.equals(getContext().getString(R.string.operate_blue_light))) {
            operateBlueLight();

        } else if (value.equals(getContext().getString(R.string.turn_on_blue_and_red_lights))) {
            turnOnBlueAndRedLights();
        }
    }

    /**
     * Turn on blue and red lights.
     */
    private void turnOnBlueAndRedLights() throws RemoteException {
        LED.getInstance().turnOn(Light.BLUE, Light.RED);
    }

    /**
     * Operate blue light.
     */
    private void operateBlueLight() throws RemoteException {
        LED.getInstance().operateBlueLight();
    }

    /**
     * Operate yellow light.
     */
    private void operateYellowLight() throws RemoteException {
        LED.getInstance().operateYellowLight();
    }

    /**
     * Operate green light.
     */
    private void operateGreenLight() throws RemoteException {
        LED.getInstance().operateGreenLight();
    }

    /**
     * Operate red light.
     */
    private void operateRedLight() throws RemoteException {
        LED.getInstance().operateRedLight();
    }

    /**
     * Turn off all.
     */
    private void turnOffAll() throws RemoteException {
        LED.getInstance().turnOffAll();
    }

    /**
     * Turn on all.
     */
    private void turnOnAll() throws RemoteException {
        LED.getInstance().turnOnAll();
    }
}
