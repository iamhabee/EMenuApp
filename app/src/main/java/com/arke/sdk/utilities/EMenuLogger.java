package com.arke.sdk.utilities;

import android.util.Log;

import com.arke.sdk.BuildConfig;

//import com.elitepath.android.emenu.BuildConfig;

public class EMenuLogger {

    public static void d(String tag, String message) {
        Log.d(tag, message);
    }

    public static void w(String tag,String message){
        if (BuildConfig.DEBUG){
            Log.w(tag,message);
        }
    }
}
