package com.franky.callmanagement.services;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;
import static com.franky.callmanagement.utils.LogUtil.LogI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import com.franky.callmanagement.R;
import com.franky.callmanagement.config.CallRecorderConfig;
import com.franky.callmanagement.env.AppConfig;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.models.CallObject;

import java.io.File;
import java.util.Date;

import io.realm.Realm;

// Nơi xử lí thao tác ghi âm
public class CallRecorderService extends Service {
    private static final String TAG = CallRecorderService.class.getSimpleName();
    private static final int FOREGROUND_NOTIFICATION_ID = 2;

    public static boolean isServiceRunning = false;
    private Realm realm = null;
    private NotificationChannel notificationChannel = null;
    TelephonyManager telephonyManager = null;

    private Vibrator vibrator = null;

    private AudioManager audioManager = null;
    private SharedPreferences sharedPreferences = null;
    private boolean isIncoming = false;
    private boolean isOutgoing = false;

    NotificationManager notificationManager;
    private String phoneStateIncomingNumber = null;
    private MediaRecorder mediaRecorder = null;
    private boolean vibrate = true, turnOnSpeaker = false, maxUpVolume = true;
    private int voiceCallStreamVolume = -1;
    private CallObject callObject = null;
   // private CallObject incomingCallObject = null;
    //private CallObject outgoingCallObject = null;
    private boolean favorite = false;

    @Override
    public void onCreate() {
        super.onCreate();
        LogD (TAG, "Service create");
        isServiceRunning = true;
        try {
            realm = Realm.getDefaultInstance ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        try {
            notificationManager = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (notificationManager != null) {
            CharSequence contentTitle = getString(R.string.title_notification), contentText = getString(R.string.title_detail_notification);
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
                    Icon largeIcon = Icon.createWithResource (this, R.mipmap.ic_launcher);
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
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            try {
                telephonyManager = (TelephonyManager) getSystemService (Context.TELEPHONY_SERVICE);
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            try {
                vibrator = (Vibrator) getSystemService (Context.VIBRATOR_SERVICE);
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            try {
                audioManager = (AudioManager) getSystemService (Context.AUDIO_SERVICE);
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
        LogE (TAG, "Service start command");
        if(intent != null){
            // Lấy cài đặt mặc định
            try {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences (this);
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                e.printStackTrace ();
            }

            if (intent.hasExtra (AppConstants.INTENT_ACTION_INCOMING_CALL)) {
                isIncoming = intent.getBooleanExtra (AppConstants.INTENT_ACTION_INCOMING_CALL, false);
                LogE (TAG, "mIsIncoming " + isIncoming);
            }

            if (intent.hasExtra (AppConstants.INTENT_ACTION_OUTGOING_CALL)) {
                isOutgoing = intent.getBooleanExtra (AppConstants.INTENT_ACTION_OUTGOING_CALL, false);
                LogE (TAG, "mIsOutgoing " + isOutgoing);
            }



            // Kiểm tra số điện thoại gọi đến phải thuộc danh sách yêu thích không
            if (intent.hasExtra (TelephonyManager.EXTRA_INCOMING_NUMBER)) {
                phoneStateIncomingNumber = intent.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);
                LogE (TAG, "-------phoneStateIncomingNumber " + phoneStateIncomingNumber);

                Realm realmf = null;
                try {
                    realmf = Realm.getDefaultInstance ();
                } catch (Exception e) {
                    e.printStackTrace ();
                }

                if (realmf != null && !realmf.isClosed ()) {
                    try {
                        realmf.beginTransaction ();
                        CallObject object = realmf.where (CallObject.class)
                                .equalTo ("mPhoneNumber", phoneStateIncomingNumber)
                                .findFirst ();
                        if (object != null) {
                            favorite = object.isFavourite ();
                            realmf.commitTransaction ();
                        } else {
                            realmf.cancelTransaction ();
                        }
                        realmf.close ();
                    } catch (Exception e) {
                        e.printStackTrace ();
                    }
                }

                if (!phoneStateIncomingNumber.trim ().isEmpty ()) {
                    LogE (TAG, "Phone state incoming number: " + phoneStateIncomingNumber);
                }
            }


            if (isIncoming || isOutgoing) {
                if (ContextCompat.checkSelfPermission (this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    int audioSource = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_SOURCE;
                    int outputFormat = CallRecorderConfig.CALL_RECORDER_DEFAULT_OUTPUT_FORMAT;
                    int audioEncoder = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_ENCODER;
                    LogE (TAG, "beginRecorder");
                    beginRecorder (audioSource, outputFormat, audioEncoder);
                } else {
                    try {
                        LogE (TAG, "stopSelf() 1");
                        stopSelf ();
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                }
            } else {
                // dừng tiến trình nếu ko có c gọi
                try {
                    LogE (TAG, "stopSelf() 2");
                    stopSelf ();
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
        }
        return START_STICKY_COMPATIBILITY;
    }


    @SuppressLint("HardwareIds")
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void beginRecorder (@Nullable Integer audioSource, @Nullable Integer outputFormat, @Nullable Integer audioEncoder) {
       /* if (mediaRecorder != null) {
            return;
        }*/
        long beginTimestamp = new Date().getTime ();
        if (sharedPreferences != null) {
            if (audioSource == null) {
                audioSource = Integer.valueOf (sharedPreferences.getString (AppConstants.FM_SP_AUDIO_SOURCE,
                        String.valueOf (CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_SOURCE)));
            }
            if (outputFormat == null) {
                outputFormat = Integer.valueOf (sharedPreferences.getString (AppConstants.FM_SP_OUTPUT_FORMAT,
                        String.valueOf (CallRecorderConfig.CALL_RECORDER_DEFAULT_OUTPUT_FORMAT)));
            }
            if (audioEncoder == null) {
                audioEncoder = Integer.valueOf (sharedPreferences.getString (AppConstants.FM_SP_AUDIO_ENCODER,
                        String.valueOf (CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_ENCODER)));
            }
            vibrate = sharedPreferences.getBoolean (AppConstants.FM_SP_VIBRATE, true);
            turnOnSpeaker = sharedPreferences.getBoolean (AppConstants.FM_SP_TURN_ON_SPEAKER, false);
            maxUpVolume = sharedPreferences.getBoolean (AppConstants.FM_SP_MAX_UP_VOLUME, true);
        }
        if (maxUpVolume) {
            if (audioManager != null) {
                try {
                    voiceCallStreamVolume = audioManager.getStreamVolume (AudioManager.STREAM_VOICE_CALL);
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
        }

        if (audioSource == null) {
            audioSource = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_SOURCE;
        }
        if (outputFormat == null) {
            outputFormat = CallRecorderConfig.CALL_RECORDER_DEFAULT_OUTPUT_FORMAT;
        }
        if (audioEncoder == null) {
            audioEncoder = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_ENCODER;
        }
        String type = "-";
        if (isIncoming) {
            type = "-I--";
        }
        if (isOutgoing) {
            type = "--0-";
        }
        // xác định vị trí lưu
        String valueExternal = getResources ().getStringArray (R.array.records_output_location_entry_values)[ 0 ];
        String valueInternal = getResources ().getStringArray (R.array.records_output_location_entry_values)[ 1 ];

        String recordsOutputDirectoryPath = null;
        if (sharedPreferences != null) {
            if (sharedPreferences.contains (AppConstants.FM_KEY_RECORDS_OUTPUT_LOCATION)) {
                String recordsOutputLocation = sharedPreferences.getString (AppConstants.FM_KEY_RECORDS_OUTPUT_LOCATION, valueExternal);
                if (recordsOutputLocation.equals (valueExternal)) {
                    recordsOutputDirectoryPath = AppConstants.sExternalFilesDirPathMemory;
                }
                if (recordsOutputLocation.equals (valueInternal)) {
                    recordsOutputDirectoryPath = AppConstants.sFilesDirPathMemory;
                }
            }
        }
        if (recordsOutputDirectoryPath == null) {
            recordsOutputDirectoryPath = AppConstants.sExternalFilesDirPathMemory;
        }
        String outputFilePath = recordsOutputDirectoryPath + File.separator + phoneStateIncomingNumber + type + beginTimestamp;
        LogE (TAG, "outputFilePath :" + outputFilePath);

        try {
            mediaRecorder = new MediaRecorder ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (mediaRecorder == null) {
            try {
                LogE (TAG, "stopSelf() 3");
                stopSelf ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            return;
        }
        mediaRecorder.setOnInfoListener (onInfoListener);
        mediaRecorder.setOnErrorListener (onErrorListener);
        mediaRecorder.setAudioSource (audioSource);
        mediaRecorder.setOutputFormat (outputFormat);
        mediaRecorder.setAudioEncoder (audioEncoder);
        mediaRecorder.setOutputFile (outputFilePath);
        boolean prepare = prepare ();
        boolean start = start ();
        boolean succeed = prepare && start;
        if (!succeed) {
            if (!prepare) {
                LogI (TAG, "Media recorder (telephony) has prepare exception");
            }
            if (!start) {
                LogI (TAG, "Media recorder (telephony) has start exception");
            }

            // Nếu audiosource không thỏa mãn thì tự đồng tìm audiosouces thỏa mãn ghi âm được
            for (int otherAudioSource : CallRecorderConfig.getAudioSources ()) {
                if (otherAudioSource == audioSource) {
                    continue;
                }
                audioSource = otherAudioSource;
                reset ();
                mediaRecorder.setOnInfoListener (onInfoListener);
                mediaRecorder.setOnErrorListener (onErrorListener);
                mediaRecorder.setAudioSource (audioSource);
                mediaRecorder.setOutputFormat (outputFormat);
                mediaRecorder.setAudioEncoder (audioEncoder);
                mediaRecorder.setOutputFile (outputFilePath);
                boolean otherPrepare = prepare();
                boolean otherStart = start();
                if (otherPrepare && otherStart) {
                    succeed = true;
                    break;
                } else {
                    if (!otherPrepare) {
                        LogI (TAG, "Media recorder (telephony) has other prepare exception");
                    }
                    if (!otherStart) {
                        LogI (TAG, "Media recorder (telephony) has other start exception");
                    }
                }
            }
        }
        if (!succeed) {
            try {
                LogE (TAG, "stopSelf() 4");
                stopSelf ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            return;
        }
        if (vibrate) {
            if (ContextCompat.checkSelfPermission (this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            vibrator.vibrate (VibrationEffect.createOneShot (AppConfig.BEGIN_RECORDER_VIBE_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    } else {
                        try {
                            vibrator.vibrate (AppConfig.BEGIN_RECORDER_VIBE_TIME);
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    }
                }
            }
        }
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            if (audioManager != null) {
                if (turnOnSpeaker) {
                    try {
                        if (!audioManager.isSpeakerphoneOn ()) {
                            audioManager.setSpeakerphoneOn (true);
                        }
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                }
                if (maxUpVolume) {
                    if (voiceCallStreamVolume != -1) {
                        try {
                            audioManager.setStreamVolume (AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume (AudioManager.STREAM_VOICE_CALL), 0);
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    }
                }
            }
        }
        String simSerialNumber = null;
        String simOperator = null;
        String simOperatorName = null;
        String simCountryIso = null;
        String networkOperator = null;
        String networkOperatorName = null;
        String networkCountryIso = null;
        TelephonyManager telephonyManager = null;
        try {
            telephonyManager = (TelephonyManager) getSystemService (TELEPHONY_SERVICE);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (telephonyManager != null) {
            try {
//                simSerialNumber = Settings.Secure.getString(getApplicationContext().getContentResolver(),
//                        Settings.Secure.ANDROID_ID);
                simSerialNumber = telephonyManager.getSimSerialNumber();
                LogE (TAG, "SIM Serial Number: " + simSerialNumber);
                simOperator = telephonyManager.getSimOperator ();
                simOperatorName = telephonyManager.getSimOperatorName ();
                simCountryIso = telephonyManager.getSimCountryIso ();
                LogE (TAG, "SIM Operator: " + simOperator);
                LogE (TAG, "SIM Operator Name: " + simOperatorName);
                LogE (TAG, "SIM Country ISO: " + simCountryIso);
                networkOperator = telephonyManager.getNetworkOperator ();
                networkOperatorName = telephonyManager.getNetworkOperatorName ();
                networkCountryIso = telephonyManager.getNetworkCountryIso ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();

                /// Android 11 bị lỗi chức năng này

                simSerialNumber = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                simOperator = "-";
                simOperatorName = "-";
                simCountryIso = "-";
                networkOperator = "-";
                networkOperatorName = "-";
                networkCountryIso = "-";
            }
        }

        // lưu cuộc gọi
        if (realm != null && !realm.isClosed ()) {
            try {
                if (isIncoming) {
                    SaveCalObjectToDatabase(AppConstants.TYPE_INCOMING_CALL,beginTimestamp, simSerialNumber, simOperator, simOperatorName, simCountryIso, networkOperator, networkOperatorName, networkCountryIso, audioSource, outputFormat, audioEncoder, outputFilePath);
                }
                if (isOutgoing) {
                    SaveCalObjectToDatabase(AppConstants.TYPE_OUTGOING_CALL,beginTimestamp, simSerialNumber, simOperator, simOperatorName, simCountryIso, networkOperator, networkOperatorName, networkCountryIso, audioSource, outputFormat, audioEncoder, outputFilePath);
                }
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
    }

    private void SaveCalObjectToDatabase(String typeCall,long beginTimestamp, String finalSimSerialNumber, String finalSimOperator, String finalSimOperatorName, String finalSimCountryIso, String finalNetworkOperator, String finalNetworkOperatorName, String finalNetworkCountryIso, int finalAudioSource, int finalOutputFormat, int finalAudioEncoder, String finalOutputFilePath) {
        realm.executeTransaction (realm -> {
            callObject = realm.createObject (CallObject.class);
            LogE (TAG, "info phoneStateNumber " + phoneStateIncomingNumber);
            LogE (TAG, "info beginTimestamp" + beginTimestamp);
            if (callObject != null) {
                callObject.setPhoneNumber (phoneStateIncomingNumber);
                callObject.setBeginTimestamp (beginTimestamp);
                callObject.setSimOperator (finalSimOperator);
                callObject.setSimSerialNumber (finalSimSerialNumber);
                callObject.setSimOperatorName (finalSimOperatorName);
                callObject.setSimCountryIso (finalSimCountryIso);
                callObject.setNetworkOperator (finalNetworkOperator);
                callObject.setNetworkOperatorName (finalNetworkOperatorName);
                callObject.setNetworkCountryIso (finalNetworkCountryIso);
                callObject.setAudioSource (finalAudioSource);
                callObject.setOutputFormat (finalOutputFormat);
                callObject.setAudioEncoder (finalAudioEncoder);
                callObject.setOutputFile (finalOutputFilePath);
                callObject.setType (typeCall);
                callObject.setFavourite (favorite);
            }
        });
    }


    // Lắng nghe quá trình ghi âm
    private final MediaRecorder.OnInfoListener onInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mediaRecorder, int code, int extra) {
            LogD(TAG,"Media recorder info");
            switch (code){
                case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                    LogI (TAG, "Media recorder info: Unknown");
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    LogI (TAG, "Media recorder info: Max duration reached");
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    LogI (TAG, "Media recorder info: Max file size reached");
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING:
                    LogI (TAG, "Media recorder info: Max file size approaching");
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED:
                    LogI (TAG, "Media recorder info: Next output file started");
                    break;
            }
            LogD (TAG, "Media recorder info extra: " + extra);
        }

    };

    // lắng nghe lỗi trong quá trình ghi âm
    private  final MediaRecorder.OnErrorListener onErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mediaRecorder, int code, int extra) {
            LogD (TAG, "Media recorder error");
            switch (code) {
                case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
                    LogE (TAG, "Media recorder error: Unknown");
                    break;
                case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
                    LogE (TAG, "Media error: Server died");
                    break;
            }
            LogE (TAG, "Media recorder error extra: " + extra);
        }
    };
    @RequiresPermission (Manifest.permission.RECORD_AUDIO)
    public void endRecorder () {
        if (mediaRecorder == null) {
            return;
        }
        long endTimestamp = new Date ().getTime ();
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            if (audioManager != null) {
                if (turnOnSpeaker) {
                    try {
                        if (audioManager.isSpeakerphoneOn ()) {
                            audioManager.setSpeakerphoneOn (false);
                        }
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                }
                if (maxUpVolume) {
                    if (voiceCallStreamVolume != -1) {
                        try {
                            audioManager.setStreamVolume (AudioManager.STREAM_VOICE_CALL, voiceCallStreamVolume, 0);
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    }
                }
            }
        }
        // set rung khi bắt đầu và kết thúc cuộc ghi thoại
        if (vibrate) {
            if (ContextCompat.checkSelfPermission (this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            vibrator.vibrate (VibrationEffect.createOneShot (AppConfig.END_RECORDER_VIBE_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    } else {
                        try {
                            vibrator.vibrate (AppConfig.END_RECORDER_VIBE_TIME);
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    }
                }
            }
        }
        boolean stop = stop (), reset = reset (), release = release ();
        if (!stop || !reset || !release) {
            if (!stop) {
                LogI (TAG, "Media recorder (telephony) has stop exception");
            }
            if (!reset) {
                LogI (TAG, "Media recorder (telephony) has reset exception");
            }
            if (!release) {
                LogI (TAG, "Media recorder (telephony) has release exception");
            }
        }
        mediaRecorder = null;
        if (voiceCallStreamVolume != -1) {
            voiceCallStreamVolume = -1;
        }
        if (!maxUpVolume) {
            maxUpVolume = true;
        }
        if (turnOnSpeaker) {
            turnOnSpeaker = false;
        }
        if (!vibrate) {
            vibrate = true;
        }
        if (realm != null && !realm.isClosed ()) {
            try {
                // set thời gian kết thúc
                if (callObject != null) {
                    LogE (TAG, "inc endTimestamp" + endTimestamp);
                    realm.executeTransaction (realm -> callObject.setEndTimestamp (endTimestamp));
                    callObject = null;
                }

            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        }
    }

    private boolean prepare () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to prepare media recorder...");
            try {
                mediaRecorder.prepare ();
                LogI (TAG, "Prepare OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to prepare media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot prepare media recorder when it is null");
        }
        return false;
    }

    private boolean start () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to start media recorder...");
            try {
                mediaRecorder.start ();
                LogI (TAG, "Start OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to prepare media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot start media recorder when it is null");
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean resume () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to resume media recorder...");
            try {
                mediaRecorder.resume ();
                LogI (TAG, "Resume OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to resume media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot resume media recorder when it is null");
        }
        return false;
    }

    @RequiresApi (api = Build.VERSION_CODES.N)
    private boolean pause () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to pause media recorder...");
            try {
                mediaRecorder.pause ();
                LogI (TAG, "Pause OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to pause media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot pause media recorder when it is null");
        }
        return false;
    }

    private boolean stop () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to stop media recorder...");
            try {
                mediaRecorder.stop ();
                LogI (TAG, "Stop OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to stop media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot stop media recorder when it is null");
        }
        return false;
    }

    private boolean reset () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to reset media recorder...");
            try {
                mediaRecorder.reset ();
                LogI (TAG, "Reset OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to reset media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot reset media recorder when it is null");
        }
        return false;
    }

    private boolean release () {
        if (mediaRecorder != null) {
            LogD (TAG, "Trying to release media recorder...");
            try {
                mediaRecorder.release ();
                LogI (TAG, "Release OK");
                return true;
            } catch (Exception e) {
                LogE (TAG, "Exception while trying to release media recorder");
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            LogD (TAG, "Cannot release media recorder when it is null");
        }
        return false;
    }

    @Override
    public void onDestroy () {
        if (ContextCompat.checkSelfPermission (this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            endRecorder ();
        }
        if (phoneStateIncomingNumber != null) {
            phoneStateIncomingNumber = null;
        }
        if (isOutgoing) {
            isOutgoing = false;
        }
        if (isIncoming) {
            isIncoming = false;
        }
        if (sharedPreferences != null) {
            sharedPreferences = null;
        }
        super.onDestroy ();
        LogD (TAG, "Service destroy");
        if (audioManager != null) {
            audioManager = null;
        }
        if (vibrator != null) {
            vibrator = null;
        }
        if (telephonyManager != null) {
            telephonyManager = null;
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
        if (realm != null) {
            if (!realm.isClosed ()) {
                try {
                    realm.close ();
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            realm = null;
        }
        isServiceRunning = false;
    }
}
