package com.arke.sdk.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.arke.sdk.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * Base activity.
 */

abstract class BaseActivity extends Activity {

    private static final String TAG = "BaseActivity";

    /**
     * Toast.
     */
    private Toast toast;

    /**
     * Handler.
     */
    private MessageHandler handler;

    /**
     * Called when the mActivity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new MessageHandler(this);
    }

    protected boolean allowDisableSystemButton() {
        return true;
    }

    /**
     * Catch the HOME key event.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (allowDisableSystemButton()) {
            Window win = getWindow();
            try {
                Class<?> cls = win.getClass();
                final Class<?>[] PARAM_TYPES = new Class[]{int.class};
                Method method = cls.getMethod("addCustomFlags", PARAM_TYPES);
                method.setAccessible(true);
                method.invoke(win, new Object[]{0x00000001});
            } catch (Exception e) {
                // handle the error here.
                Timber.e(e.getCause());
            }
        }
    }

    /**
     * Deal the HOME key events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_HOME || super.onKeyDown(keyCode, event);
    }

    /**
     * Deal the BACK key events.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Find view by id.
     */
    public View findViewById(int id) {
        return getWindow().findViewById(id);
    }

    /**
     * Show toast message.
     */
    protected void showToast(String message) {
        message = message != null ? message : getString(R.string.unknown_error);
        Log.d(TAG, "showToast: " + message);
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    /**
     * Send message.
     */
    protected void sendMessage(int tag, Object object) {
        Message message = new Message();
        message.what = tag;
        message.obj = object;
        handler.sendMessage(message);
    }

    /**
     * Send message.
     */
    protected void sendMessage(int tag, int arg1, Object object) {
        Message message = new Message();
        message.what = tag;
        message.arg1 = arg1;
        message.obj = object;
        handler.sendMessage(message);
    }

    /**
     * Receive message.
     */
    protected void receiveMessage(Message message) {

    }

    /**
     * Message handler.
     */
    private static class MessageHandler extends Handler {

        private WeakReference<BaseActivity> weakReference;

        private MessageHandler(BaseActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity baseActivity = weakReference.get();
            baseActivity.receiveMessage(msg);
        }
    }
}
