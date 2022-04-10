package com.franky.callmanagement.receivers;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;
import static com.franky.callmanagement.utils.LogUtil.LogI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.franky.callmanagement.R;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.services.CallRecorderService;

// Nơi xác nhận kiểu cuộc gọi -> ghi câm CallRecorderService
public class tmp extends BroadcastReceiver {

    private static final String TAG = ManagerPhoneStateReceiver.class.getSimpleName();
    private static boolean isIncoming = false;
    private static boolean isOutgoing = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(context ==  null || intent == null){
            if (context == null) {
                LogE(TAG, "Receiver receive: Context null");
            }
            if (intent == null) {
                LogE(TAG, "Receiver receive: Intent null");
            }
            return;
        }
        if(!intent.getAction().toString().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            LogE(TAG, "Receiver receive: intent action mismatch");
            return;
        }
        ReceiveSuccessful (context, intent);
    }

    public void ReceiveSuccessful(Context context, Intent intent){

        // Trạng thái hệ thống bình thường
        TelephonyManager telephonyManager = null;
        try {
            telephonyManager = (TelephonyManager) context.getSystemService (Context.TELEPHONY_SERVICE);
        } catch (Exception e) {
            LogE(TAG, e.getMessage ());
            e.printStackTrace ();
        }

        // Trạng thái intent gửi vè
        String phoneStateExtraState = null;
        try {
            phoneStateExtraState = intent.getStringExtra (TelephonyManager.EXTRA_STATE);
        } catch (Exception e) {
            LogE(TAG, e.getMessage ());
            e.printStackTrace ();
        }

        if(phoneStateExtraState != null){

            // Kiểm tra trạng thái từ EXTRA_STATE_IDLE - > EXTRA_STATE_RING
            if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_IDLE)) {
                LogD (TAG, "Phone state: Idle");
                if (telephonyManager != null) {
                    if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_IDLE) {
                        onCallStateChange (context, intent, TelephonyManager.CALL_STATE_IDLE);
                    }
                } else {
                    onCallStateChange (context, intent, TelephonyManager.CALL_STATE_IDLE);
                }
            }

            // NẾU LÀ RINGING - > Có người gọi đến
            if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_RINGING)) {
                LogD (TAG, "Phone state: Ringing");
                if (telephonyManager != null) {
                    if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_RINGING) {
                        onCallStateChange (context, intent, TelephonyManager.CALL_STATE_RINGING);
                    }
                } else {
                    onCallStateChange (context, intent, TelephonyManager.CALL_STATE_RINGING);
                }
            }

            // OFFHOOK - > thực hiện cuộc gọi đi
            if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                LogD (TAG, "Phone state: Offhook");
                if (telephonyManager != null) {
                    if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_OFFHOOK) {
                        onCallStateChange (context, intent, TelephonyManager.CALL_STATE_OFFHOOK);
                    }
                } else {
                    onCallStateChange (context, intent, TelephonyManager.CALL_STATE_OFFHOOK);
                }
            }
        }
    }

    // Xác định được chiều gọi -> tiến hành ghi âm
    private void onCallStateChange(Context context, Intent intent, int callState){
        SharedPreferences sharedPreferences = null;
        try {
            sharedPreferences = context.getSharedPreferences (context.getString (R.string.app_name), Context.MODE_PRIVATE);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            e.printStackTrace ();
        }

        if(sharedPreferences != null){
            switch (callState) {
                case TelephonyManager.CALL_STATE_IDLE: // trở về trạng thái bình thường -> dừng ghi
                    if (CallRecorderService.isServiceRunning) {
                        stopRecorder (context, intent);
                    }
                    if (isIncoming) {
                        isIncoming = false;
                    }
                    if (isOutgoing) {
                        isOutgoing = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (!isOutgoing) {
                        isIncoming = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (!isIncoming) {
                        isOutgoing = true;
                    }
                    if (!CallRecorderService.isServiceRunning) {
                        if (isIncoming) {
                            LogI (TAG, "Call type: Incoming");
                            if (sharedPreferences.getBoolean (AppConstants.KEY_RECORD_INCOMING_CALLS, true)) {
                                startRecorder (context, intent);
                            }
                            isIncoming = false;
                        }
                        if (isOutgoing) {
                            LogI (TAG, "Call type: Outgoing");
                            if (sharedPreferences.getBoolean (AppConstants.KEY_RECORD_OUTGOING_CALLS, true)) {
                                startRecorder (context, intent);
                            }
                            isOutgoing = false;
                        }
                    }
                    break;
            }
        }
    }

    private void startRecorder (@NonNull Context context, @NonNull Intent intent) {
        if (CallRecorderService.isServiceRunning) {
            return;
        }
        intent.setClass (context, CallRecorderService.class);
        intent.putExtra (AppConstants.INTENT_ACTION_INCOMING_CALL, isIncoming);
        intent.putExtra (AppConstants.INTENT_ACTION_OUTGOING_CALL, isOutgoing);
        LogE(TAG, "Detect type call "+(isIncoming?"incoming":"outgoing"));
        try {
            context.startService (intent);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
    }

    private void stopRecorder (@NonNull Context context, @NonNull Intent intent) {
        if (!CallRecorderService.isServiceRunning) {
            return;
        }
        intent.setClass (context, CallRecorderService.class);
        try {
            context.stopService (intent);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
    }
}
