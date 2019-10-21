package com.arke.sdk.api;

import com.arke.sdk.ArkeSdkDemoApplication;
import com.usdk.apiservice.aidl.pinpad.DeviceName;

/**
 * Pinpad for MK/SK API.
 */

public class PinpadForMKSK extends Pinpad {

    /**
     * Creator.
     */
    private static class Creator {
        private static final PinpadForMKSK INSTANCE = new PinpadForMKSK();
    }

    /**
     * Get pinpad for MK/SK instance.
     */
    public static PinpadForMKSK getInstance() {
        return Creator.INSTANCE;
    }

    /**
     * Constructor.
     */
    private PinpadForMKSK() {
        super(ArkeSdkDemoApplication.getContext(), ArkeSdkDemoApplication.getDeviceService().getPinpadForMKSK());
    }
}
