package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.api.Ethernet;

/**
 * Ethernet demo.
 */

public class EthernetDemo extends ApiDemo {

    /**
     * Constructor.
     */
    private EthernetDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get ethernet demo instance.
     */
    public static EthernetDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new EthernetDemo(context, toast, dialog);
    }

    /**
     * Do ethernet functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.open))) {
            open();

        } else if (value.equals(getContext().getString(R.string.close))) {
            close();

        } else if (value.equals(getContext().getString(R.string.get_ethernet_info))) {
            getInfo();

        } else if (value.equals(getContext().getString(R.string.get_ethernet_config_info))) {
            getConfigInfo();

        } else if (value.equals(getContext().getString(R.string.get_ethernet_state))) {
            getEthernetState();

        } else if (value.equals(getContext().getString(R.string.get_ethernet_enabled_state))) {
            getEthernetEnabledState();

        } else if (value.equals(getContext().getString(R.string.config_ethernet_dhcp_to_false))) {
            config(false);

        } else if (value.equals(getContext().getString(R.string.config_ethernet_dhcp_to_true))) {
            config(true);

        } else if (value.equals(getContext().getString(R.string.is_ethernet_exist))) {
            isEthernetExist();

        } else if (value.equals(getContext().getString(R.string.is_ethernet_enabled))) {
            isEthernetEnabled();
        }
    }

    /**
     * Open.
     */
    private void open() throws RemoteException {
        boolean isSuccessful = Ethernet.getInstance().open();

        // Show message
        showToast(String.valueOf(isSuccessful));
    }

    /**
     * Close.
     */
    private void close() throws RemoteException {
        boolean isSuccessful = Ethernet.getInstance().close();

        // Show message
        showToast(String.valueOf(isSuccessful));
    }

    /**
     * Get real info.
     */
    private void getInfo() throws RemoteException {
        String message;
        Bundle bundle = Ethernet.getInstance().getInfo();
        if (bundle != null) {
            message = "MAC:" + bundle.getString(Ethernet.CONFIG_MAC) + "\r\n";
            message += "LOCAL_IP:" + bundle.getString(Ethernet.CONFIG_LOCAL_IP) + "\r\n";
            message += "MASK:" + bundle.getString(Ethernet.CONFIG_MASK) + "\r\n";
            message += "GATEWAY:" + bundle.getString(Ethernet.CONFIG_GATEWAY) + "\r\n";
            message += "DNS1:" + bundle.getString(Ethernet.CONFIG_DNS1) + "\r\n";
            message += "DNS2:" + bundle.getString(Ethernet.CONFIG_DNS1) + "\r\n";
            message += "IS_DHCP:" + bundle.getBoolean(Ethernet.CONFIG_IS_DHCP);
        } else {
            message = getContext().getString(R.string.failed);
        }

        // Show message
        showToast(message);
    }

    /**
     * Get config info.
     */
    private void getConfigInfo() throws RemoteException {
        String message;
        Bundle bundle = Ethernet.getInstance().getConfigInfo();
        if (bundle != null) {
            message = "LOCAL_IP:" + bundle.getString(Ethernet.CONFIG_LOCAL_IP) + "\r\n";
            message += "MASK:" + bundle.getString(Ethernet.CONFIG_MASK) + "\r\n";
            message += "GATEWAY:" + bundle.getString(Ethernet.CONFIG_GATEWAY) + "\r\n";
            message += "DNS1:" + bundle.getString(Ethernet.CONFIG_DNS1) + "\r\n";
            message += "DNS2:" + bundle.getString(Ethernet.CONFIG_DNS1) + "\r\n";
            message += "IS_DHCP:" + bundle.getBoolean(Ethernet.CONFIG_IS_DHCP);
        } else {
            message = getContext().getString(R.string.failed);
        }

        // Show message
        showToast(message);
    }

    /**
     * Set config info.
     */
    private void config(boolean isDhcp) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putString(Ethernet.CONFIG_LOCAL_IP, "192.168.1.1");
        bundle.putString(Ethernet.CONFIG_MASK, "255.255.255.0");
        bundle.putString(Ethernet.CONFIG_GATEWAY, "192.168.1.1");
        bundle.putString(Ethernet.CONFIG_DNS1, "0.0.0.0");
        bundle.putString(Ethernet.CONFIG_DNS2, "0.0.0.0");
        bundle.putBoolean(Ethernet.CONFIG_IS_DHCP, isDhcp);
        boolean isSuccessful = Ethernet.getInstance().config(bundle);

        // Show message
        showToast(String.valueOf(isSuccessful));
    }

    /**
     * Get ethernet state.
     */
    private void getEthernetState() throws RemoteException {
        int state = Ethernet.getInstance().getEthernetState();

        // Show message
        switch (state) {
            case Ethernet.STATE_DISABLED:
                showToast(R.string.ethernet_disabled);
                break;
            case Ethernet.STATE_DISABLING:
                showToast(R.string.ethernet_disabling);
                break;
            case Ethernet.STATE_ENABLED:
                showToast(R.string.ethernet_enabled);
                break;
            case Ethernet.STATE_ENABLING:
                showToast(R.string.ethernet_enabling);
                break;
            case Ethernet.STATE_UNKNOWN:
                showToast(R.string.ethernet_state_unknown);
                break;
        }
    }

    /**
     * Is ethernet enabled.
     */
    private void isEthernetEnabled() throws RemoteException {
        boolean isEnabled = Ethernet.getInstance().isEthernetEnabled();

        // Show message
        showToast(String.valueOf(isEnabled));
    }

    /**
     * Get ethernet enabled state.
     */
    private void getEthernetEnabledState() throws RemoteException {
        boolean enabledState = Ethernet.getInstance().getEthernetEnabledState();

        // Show message
        showToast(String.valueOf(enabledState));
    }

    /**
     * Is ethernet exist.
     */
    private void isEthernetExist() throws RemoteException {
        boolean isExist = Ethernet.getInstance().isEthernetExist("eth0");

        // Show message
        showToast(String.valueOf(isExist));
    }
}
