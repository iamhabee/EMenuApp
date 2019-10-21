package com.arke.sdk.vas;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.arke.vas.data.ResponseBodyData;

import java.lang.ref.WeakReference;

/**
 * Value added service aidl service.
 */

public class VASService extends Service {

    private static VASHandler vasHandler;

    private VASBinder vasBinder;

    @Override
    public void onCreate() {
        super.onCreate();

        vasHandler = new VASHandler(this);
        vasBinder = new VASBinder(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return vasBinder;
    }

    private void onReceive(Message message) {
        switch (VASAction.getAction(message.what)) {
            case VASAction.SALE:
                vasBinder.complete((ResponseBodyData) message.obj);
                break;
        }
    }

    public static void sendResultToVAS(String action, Object object) {
        int what = VASAction.getIndex(action);

        if (vasHandler != null && what >= 0) {
            Message message = new Message();
            message.what = what;
            message.obj = object;
            vasHandler.sendMessage(message);
        }
    }

    private static class VASHandler extends Handler {
        private WeakReference<VASService> weakReference;

        VASHandler(VASService service) {
            weakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            VASService service = weakReference.get();
            service.onReceive(msg);
        }
    }
}
