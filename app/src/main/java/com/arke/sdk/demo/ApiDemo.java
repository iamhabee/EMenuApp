package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.arke.sdk.R;

/**
 * API demo common interface.
 */

abstract class ApiDemo {

    private static final String TAG = "ApiDemo";

    /**
     * Context.
     */
    protected Context context;

    /**
     * Toast.
     */
    private Toast toast;

    /**
     * Alert dialog.
     */
    private AlertDialog dialog;

    /**
     * Constructor.
     */
    ApiDemo(Context context, Toast toast, AlertDialog dialog) {
        this.context = context;
        this.toast = toast;
        this.dialog = dialog;
    }

    /**
     * Get context.
     */
    Context getContext() {
        return context;
    }

    /**
     * Show toast message.
     */
    void showToast(String message) {
        message = message != null ? message : context.getString(R.string.unknown_error);
        Log.d(TAG, "showToast: " + message);
        toast.setText(message);
        toast.show();
    }

    /**
     * Show toast message by resource id.
     */
    void showToast(int resId) {
        showToast(context.getString(resId));
    }

    /**
     * Show dialog by resource id.
     */
    void showDialog(int resId, boolean cancelable) {
        Log.d(TAG, "showDialog: " + context.getString(resId));
        dialog.setMessage(context.getString(resId));
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(cancelable);
        if (dialog.getWindow() != null) {
            // // TODO: 2017/9/8 屏蔽的Home的方法在 4.0 以后已经不再支持，在 C10  Android 7.0 上会报错。
            /** 为了更好地实现应用屏蔽HOME键的功能而又不引起异常，现已在P990及W280PV2的版本上新增了一个接口，
            该接口可以让应用的window屏蔽HOME键及APP_SWITCH键（就是调出近期应用的键），调用方法如下：

            @Override
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
                Window win = getWindow();
                try {
                    Class<?> cls = win.getClass();
                    final Class<?>[] PARAM_TYPES = new Class[] {int.class};
                    Method method = cls.getMethod("addCustomFlags", PARAM_TYPES);
                    method.setAccessible(true);
                    method.invoke(win, new Object[] {0x00000001});
                } catch (Exception e) {
                    // handle the error here.
                }
            }

            该方法在2015.03.17号的烧片版本才有效。以后的应用屏蔽HOME键，请尽量使用此方法，不要再使用TYPE_KEYGUARD_DIALOG方式。
            **/
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
    }

    void showDialog(String message, boolean cancelable) {
        dialog.setMessage(message);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(cancelable);
        if (dialog.getWindow() != null) {
            // // TODO: 2017/9/8 屏蔽的Home的方法在 4.0 以后已经不再支持，在 C10  Android 7.0 上会报错。
            /** 为了更好地实现应用屏蔽HOME键的功能而又不引起异常，现已在P990及W280PV2的版本上新增了一个接口，
             该接口可以让应用的window屏蔽HOME键及APP_SWITCH键（就是调出近期应用的键），调用方法如下：

             @Override
             public void onAttachedToWindow() {
             super.onAttachedToWindow();
             Window win = getWindow();
             try {
             Class<?> cls = win.getClass();
             final Class<?>[] PARAM_TYPES = new Class[] {int.class};
             Method method = cls.getMethod("addCustomFlags", PARAM_TYPES);
             method.setAccessible(true);
             method.invoke(win, new Object[] {0x00000001});
             } catch (Exception e) {
             // handle the error here.
             }
             }

             该方法在2015.03.17号的烧片版本才有效。以后的应用屏蔽HOME键，请尽量使用此方法，不要再使用TYPE_KEYGUARD_DIALOG方式。
             **/
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
    }

    /**
     * Show dialog by resource id.
     */
    void showDialog(DialogInterface.OnDismissListener dismissListener, int resId) {
        dialog.setOnDismissListener(dismissListener);
        showDialog(resId, true);
    }

    void showDialog(DialogInterface.OnDismissListener dismissListener, String message) {
        dialog.setOnDismissListener(dismissListener);
        showDialog(message, true);
    }

    /**
     * Hide dialog.
     */
    void hideDialog() {
        Log.d(TAG, "hideDialog: ");
        dialog.cancel();
    }
}
