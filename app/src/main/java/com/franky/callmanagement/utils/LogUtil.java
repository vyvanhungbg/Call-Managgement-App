package com.franky.callmanagement.utils;

import android.util.Log;

import com.franky.callmanagement.env.AppConfig;

public class LogUtil {
    private static final String TAG = LogUtil.class.getSimpleName();

    public static void LogD(String TAG, String mess){
        if(AppConfig.ENABLE_LOG && Log.isLoggable(TAG, Log.DEBUG)){
            Log.d(TAG, mess);
        }
    }

    public static void LogE(String TAG, String mess){
        if(AppConfig.ENABLE_LOG){
            Log.e(TAG, mess);
        }
    }

    public static void LogI (final String tag, String message) {
        if (AppConfig.ENABLE_LOG) {
            Log.i (tag, message);
        }
    }
}
