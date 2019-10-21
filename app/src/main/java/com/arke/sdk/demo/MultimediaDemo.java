package com.arke.sdk.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.arke.sdk.R;
import com.arke.sdk.view.VideoListActivity;


/**
 * Multimedia demo.
 */

public class MultimediaDemo extends ApiDemo {

    private static final String TAG = "MultimediaDemo";
    /**
     * Constructor.
     */
    private MultimediaDemo(Context context, Toast toast, AlertDialog dialog) {
        super(context, toast, dialog);
    }

    /**
     * Get Multimedia demo instance.
     */
    public static MultimediaDemo getInstance(Context context, Toast toast, AlertDialog dialog) {
        return new MultimediaDemo(context, toast, dialog);
    }

    /**
     * Do Multimedia functions.
     */
    public void execute(String value) throws RemoteException {
        if (value.equals(getContext().getString(R.string.video_player))) {
            videoPlayer();
        }
    }
    /**
     * Do videoPlayer functions.
     */

    private void videoPlayer() throws RemoteException {
        Log.d(TAG, "VideoListActivity");
        Intent intent = new Intent(getContext(), VideoListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}


