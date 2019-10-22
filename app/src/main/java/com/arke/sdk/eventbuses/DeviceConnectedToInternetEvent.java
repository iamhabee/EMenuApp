package com.arke.sdk.eventbuses;

public class DeviceConnectedToInternetEvent {
    private boolean connected;

    public DeviceConnectedToInternetEvent(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

}
