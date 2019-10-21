package com.arke.sdk.vas;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.arke.sdk.view.SaleActivity;
import com.arke.vas.IVASCallSdkInterface;
import com.arke.vas.IVASListener;
import com.arke.vas.data.RequestBodyData;
import com.arke.vas.data.ResponseBodyData;
import com.arke.vas.data.VASPayload;
import com.google.common.base.Strings;
import com.google.gson.Gson;

/**
 * Value added service aidl binder.
 */

public class VASBinder extends IVASCallSdkInterface.Stub {
    private static final String TAG = "VASBinder";

    private VASService context;

    private IVASListener vasListener;

    VASBinder(VASService context) {
        this.context = context;
    }

    void complete(ResponseBodyData responseData) {
        if (vasListener != null) {
            try {
                vasListener.onComplete(new VASPayload(new Gson().toJson(responseData)));
            } catch (RemoteException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void sale(VASPayload requestData, IVASListener listener) throws RemoteException {
        Log.d(TAG,"head:" + requestData.getHead());
        Log.d(TAG,"body:" + requestData.getBody());

        if (listener != null) {
            vasListener = listener;
            vasListener.onStart();
        }

        RequestBodyData requestBodyData = parseRequestDataFormString(requestData.getBody());
        Intent intent = new Intent(context, SaleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("amount", requestBodyData.getAmount());
        context.startActivity(intent);
    }

    private RequestBodyData parseRequestDataFormString(String requestData) {
        if (Strings.isNullOrEmpty(requestData)) {
            return new RequestBodyData();
        }

        return new Gson().fromJson(requestData, RequestBodyData.class);
    }
}
