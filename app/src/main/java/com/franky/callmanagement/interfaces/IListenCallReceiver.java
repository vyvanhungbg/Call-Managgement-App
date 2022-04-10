package com.franky.callmanagement.interfaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public interface IListenCallReceiver {
    void onIncomingCallReceived(String mess );
    void onIncomingCallAnswered(SharedPreferences sharedPreferences, Context context, Intent intent);
    void onIncomingCallEnded(SharedPreferences sharedPreferences,Context context,  Intent intent);
    void onOutgoingCallStarted(SharedPreferences sharedPreferences,Context context, Intent intent);
    void onOutgoingCallEnded(SharedPreferences sharedPreferences,Context context, Intent intent);
    void onMissedCall(String mess);
}
