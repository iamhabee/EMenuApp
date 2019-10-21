package com.arke.sdk.api;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.usdk.apiservice.aidl.pinpad.DeviceName;

/**
 * Pinpad for FK API.
 */

public class PinpadForFK extends Pinpad {

    /**
     * Creator.
     */
    private static class Creator {
        private static final PinpadForFK INSTANCE = new PinpadForFK();
    }

    /**
     * Get pinpad for FK instance.
     */
    public static PinpadForFK getInstance() {
        return Creator.INSTANCE;
    }

    /**
     * Constructor.
     */
    private PinpadForFK() {
        super(ArkeSdkDemoApplication.getContext(), ArkeSdkDemoApplication.getDeviceService().getPinpadForFK());
    }
}
