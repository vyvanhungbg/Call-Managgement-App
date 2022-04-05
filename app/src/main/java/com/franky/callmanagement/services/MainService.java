package com.franky.callmanagement.services;



import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.franky.callmanagement.R;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.receivers.ManagerPhoneStateReceiver;
import com.franky.callmanagement.utils.AppUtil;

// class chính luôn lắng nghe khi app đươc bật
public class MainService extends Service {
    private static final String TAG = MainService.class.getSimpleName();
    private static final int FOREGROUND_NOTIFICATION_ID =1;

    public static boolean isServiceRunning = false;

    private final ManagerPhoneStateReceiver managerPhoneStateReceiver = new ManagerPhoneStateReceiver();
    
    private NotificationManager notificationManager = null;
    private NotificationChannel notificationChannel = null;
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private SharedPreferences sharedPreferences = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Service - on start command ");
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Service create");
        isServiceRunning = true;

        try {
            notificationManager = (NotificationManager) getSystemService (NOTIFICATION_SERVICE);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (notificationManager != null) {
            CharSequence contentText = getString(R.string.running)+"...", contentTitle  = getString (R.string.app_name) + " "+getString(R.string.active);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    notificationChannel = new NotificationChannel (getString (R.string.service) + "-" + FOREGROUND_NOTIFICATION_ID, getString (R.string.service), NotificationManager.IMPORTANCE_NONE);
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
                if (notificationChannel != null) {
                    try {
                        notificationManager.createNotificationChannel (notificationChannel);
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                    Icon logoIcon = Icon.createWithResource (this, R.drawable.ic_stat_name);
                    Icon largeIcon = Icon.createWithResource (this, R.drawable.ic_application);

                    try {
                        startForeground (FOREGROUND_NOTIFICATION_ID, new Notification.Builder (this, getString (R.string.service) + "-" + FOREGROUND_NOTIFICATION_ID)
                                .setSmallIcon (logoIcon)
                                .setLargeIcon (largeIcon)
                                .setContentTitle (contentTitle)
                                .setContentText (contentText)
                                .build ());
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                }
            } else {
                Notification.Builder builder = new Notification.Builder (this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Icon logoIcon = Icon.createWithResource (this, R.drawable.ic_stat_name);
                    Icon largeIcon = Icon.createWithResource (this, R.drawable.ic_application);
                    builder.setSmallIcon (logoIcon);
                    builder.setLargeIcon (largeIcon);
                } else {
                    builder.setSmallIcon (R.drawable.ic_stat_name);
                }
                builder.setContentTitle (contentTitle);
                builder.setContentText (contentText);
                builder.setOngoing (true);
                try {
                    notificationManager.notify (FOREGROUND_NOTIFICATION_ID, builder.build ());
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
        }
        try {
            powerManager = (PowerManager) getSystemService (POWER_SERVICE);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (powerManager != null) {
            try {
                wakeLock = powerManager.newWakeLock (PowerManager.PARTIAL_WAKE_LOCK, getString (R.string.app_name));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            if (wakeLock != null) {
                AppUtil.acquireWakeLock (wakeLock);
            }
        }
        try {
            sharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (sharedPreferences != null) {
            try {
                sharedPreferences.registerOnSharedPreferenceChangeListener (onSharedPreferenceChangeListener);
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
        try {
            registerReceiver (managerPhoneStateReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if(sharedPreferences  == null || s==null) return;
            LogE(TAG, "Shared preference change !");
            if(s.equals(AppConstants.KEY_RECORD_INCOMING_CALLS)){
                if(! sharedPreferences.contains(AppConstants.KEY_RECORD_INCOMING_CALLS)){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(AppConstants.KEY_RECORD_INCOMING_CALLS,true).apply();
                }
            }
            if(s.equals(AppConstants.KEY_RECORD_OUTGOING_CALLS)){
                if(! sharedPreferences.contains(AppConstants.KEY_RECORD_OUTGOING_CALLS)){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(AppConstants.KEY_RECORD_OUTGOING_CALLS,true).apply();
                }
            }
            boolean recordIncomingCalls = sharedPreferences.getBoolean(AppConstants.KEY_RECORD_INCOMING_CALLS,true);
            boolean recordOutgoingCalls = sharedPreferences.getBoolean(AppConstants.KEY_RECORD_OUTGOING_CALLS,true);
            LogE(TAG, recordIncomingCalls+"Shared preference change !"+recordIncomingCalls);
            if (!recordIncomingCalls && !recordOutgoingCalls) {
                try {
                    stopSelf ();
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    e.printStackTrace ();
                }
            }
        };
    };


    @Override
    public void onDestroy () {
        super.onDestroy ();
        LogD (TAG, "Service destroy");
        try {
            unregisterReceiver (managerPhoneStateReceiver);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (sharedPreferences != null) {
            try {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener (onSharedPreferenceChangeListener);
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            sharedPreferences = null;
        }
        if (powerManager != null) {
            if (wakeLock != null) {
                AppUtil.releaseWakeLock (wakeLock);
                wakeLock = null;
            }
            powerManager = null;
        }
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationChannel != null) {
                    try {
                        stopForeground (true);
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                    try {
                        notificationManager.deleteNotificationChannel (notificationChannel.getId ());
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                    notificationChannel = null;
                }
            } else {
                try {
                    notificationManager.cancel (FOREGROUND_NOTIFICATION_ID);
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            notificationManager = null;
        }
        isServiceRunning = false;
    }
}
