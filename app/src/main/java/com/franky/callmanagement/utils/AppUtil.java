package com.franky.callmanagement.utils;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.franky.callmanagement.services.MainService;

public class AppUtil {
    private static final String TAG = AppUtil.class.getSimpleName ();

    /**
     * Start main service.
     *
     * @param context the context
     */
    public static void startMainService (@NonNull final Context context) {
        if (MainService.isServiceRunning) {
            LogD (TAG, "Will not start \"MainService\", it is running");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startForegroundService (new Intent(context, MainService.class));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            try {
                context.startService (new Intent (context, MainService.class));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
    }

    /**
     * Stop main service.
     *
     * @param context the context
     */
    public static void stopMainService (@NonNull final Context context) {
        if (!MainService.isServiceRunning) {
            LogD (TAG, "Will not stop \"MainService\", it is not running");
            return;
        }
        try {
            context.stopService (new Intent (context, MainService.class));
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
    }

    /**
     * Acquire wake lock.
     *
     * @param wakeLock the wake lock
     * @param timeout  the timeout
     */
    @RequiresPermission(Manifest.permission.WAKE_LOCK)
    public static void acquireWakeLock (@NonNull final PowerManager.WakeLock wakeLock, final long timeout) {
        LogD (TAG, "Trying to acquire wake lock with timeout...");
        try {
            wakeLock.acquire (timeout);
        } catch (Exception e) {
            LogE (TAG, "Exception while trying to acquire wake lock with timeout");
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        try {
            if (wakeLock.isHeld ()) {
                LogD (TAG, "Wake lock acquired");
                return;
            } else {
                LogD (TAG, "Wake lock not acquired");
                return;
            }
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        LogD (TAG, "Wake lock not acquired");
    }

    /**
     * Acquire wake lock.
     *
     * @param wakeLock the wake lock
     */
    @SuppressLint("WakelockTimeout")
    @RequiresPermission (Manifest.permission.WAKE_LOCK)
    public static void acquireWakeLock (@NonNull final PowerManager.WakeLock wakeLock) {
        LogD (TAG, "Trying to acquire wake lock without timeout...");
        try {
            wakeLock.acquire ();
        } catch (Exception e) {
            LogE (TAG, "Exception while trying to acquire wake lock without timeout");
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        try {
            if (wakeLock.isHeld ()) {
                LogD (TAG, "Wake lock acquired");
                return;
            } else {
                LogD (TAG, "Wake lock not acquired");
                return;
            }
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        LogD (TAG, "Wake lock not acquired");
    }

    /**
     * Release wake lock.
     *
     * @param wakeLock the wake lock
     */
    @RequiresPermission (Manifest.permission.WAKE_LOCK)
    public static void releaseWakeLock (@NonNull final PowerManager.WakeLock wakeLock) {
        LogD (TAG, "Trying to release wake lock...");
        try {
            wakeLock.release ();
        } catch (Exception e) {
            LogE (TAG, "Exception while trying to release wake lock");
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        try {
            if (wakeLock.isHeld ()) {
                LogD (TAG, "Wake lock not released");
                return;
            } else {
                LogD (TAG, "Wake lock released");
                return;
            }
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        LogD (TAG, "Wake lock not released");
    }

    /**
     * Open package in market.
     *
     * @param context the context
     */
    public static void openPackageInMarket (@NonNull final Context context) {
        try {
            context.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("market://details?id=" + context.getPackageName ())));
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
            try {
                context.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("https://play.google.com/store/apps/details?id=" + context.getPackageName ())));
            } catch (Exception ex) {
                LogE (TAG, ex.getMessage ());
                LogE (TAG, ex.toString ());
                ex.printStackTrace ();
            }
        }
    }
}

