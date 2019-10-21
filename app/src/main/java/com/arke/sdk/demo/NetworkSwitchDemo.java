package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.RemoteException;
import android.widget.Toast;

import com.arke.sdk.R;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Yun on 2017/9/13.
 */

public class NetworkSwitchDemo extends ApiDemo {

    private ConnectivityManager manager;

    private ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            //set app network
            boolean result=false;
            if (Build.VERSION.SDK_INT >= 23) {
                result=manager.bindProcessToNetwork(network);
            } else {
                result=ConnectivityManager.setProcessDefaultNetwork(network);
            }
            showToast(result+"");
            //success
            if(result)
            {
                //  TODO: your network request
            }

            /**
             * if your want to recovery default network
             */
//            if (Build.VERSION.SDK_INT >= 23) {
//                manager.bindProcessToNetwork(null);
//            }
//            else {
//                ConnectivityManager.setProcessDefaultNetwork(null);
//            }
//            manager.unregisterNetworkCallback(callback);

        }
    };

    /**
     * Constructor.
     */
    private NetworkSwitchDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
        init();
    }

    /**
     * Get network switch demo instance.
     */
    public static NetworkSwitchDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new NetworkSwitchDemo(context, toast, dialog);
    }

    /**
     * Do network switch functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.switch_to_wifi))) {
            switchNetwork(value);
        } else if (value.equals(getContext().getString(R.string.switch_to_gprs))) {
            switchNetwork(value);
        } else if (value.equals(getContext().getString(R.string.switch_to_eth))) {
            switchNetwork(value);
        }
    }

    private void init()
    {
        manager = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
    }


    private void switchNetwork(String value)
    {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        //set network type which your want

        // wifi
        if (value.equals(getContext().getString(R.string.switch_to_wifi))) {
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        }
        // gprs
        else if (value.equals(getContext().getString(R.string.switch_to_gprs))) {
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        }
        // eth
        else if (value.equals(getContext().getString(R.string.switch_to_eth))) {
            builder.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
        }
        NetworkRequest networkRequest = builder.build();
        manager.requestNetwork(networkRequest, callback);
    }


}
