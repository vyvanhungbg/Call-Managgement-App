package com.franky.callmanagement.presenters;

import static com.franky.callmanagement.utils.LogUtil.LogE;
import static com.franky.callmanagement.utils.LogUtil.LogI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.franky.callmanagement.R;
import com.franky.callmanagement.interfaces.ICallActivityListener;
import com.franky.callmanagement.models.CallObject;

import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.Sort;

public class CallActivityPresenter {
    private static final String TAG = CallActivityPresenter.class.getSimpleName();
    ICallActivityListener listener;

    public CallActivityPresenter(ICallActivityListener listener) {
        this.listener = listener;
    }

    public void getDataFromAllCallRecyclerAdapter(Realm mRealm, Intent intent){
        long beginTimestamp = 0L, endTimestamp = 0L;
        boolean mIsIncoming = false,mIsOutgoing = false;
        String correspondentName = "Người dùng";

        // lấy dữ liệu từ Adapter truyền sang
        if (intent != null) {
            if (intent.hasExtra ("mType") && Objects.equals (intent.getStringExtra ("mType"), "incoming")) {
                mIsIncoming = true;
            }
            if (intent.hasExtra ("mType") && Objects.equals (intent.getStringExtra ("mType"), "outgoing")) {
                mIsOutgoing = true;
            }
            if (mIsIncoming || mIsOutgoing) {
                if (intent.hasExtra ("mBeginTimestamp")) {
                    beginTimestamp = intent.getLongExtra ("mBeginTimestamp", 0L);
                }
                if (intent.hasExtra ("mEndTimestamp")) {
                    endTimestamp = intent.getLongExtra ("mEndTimestamp", 0L);
                }
            }
            if (intent.hasExtra ("mCorrespondentName")) {
                 correspondentName = intent.getStringExtra ("mCorrespondentName");
            }
        }

        // nếu thời gian cuộc gọi có vấn đề -> báo lỗi
        if (beginTimestamp == 0L || endTimestamp == 0L) {
            listener.actionGetIntentFailed("Có lỗi xảy ra khi nhận dữ liệu intent");
        }else {
            CallObject mIncomingCallObject = null, mOutgoingCallObject= null;
            // truy vấn realm lấy đối tượng
            if (mRealm != null && !mRealm.isClosed ()) {
                if (mIsIncoming) {
                    mIncomingCallObject = mRealm.where (CallObject.class)
                            .equalTo ("mBeginTimestamp", beginTimestamp)
                            .equalTo ("mEndTimestamp", endTimestamp)
                            .sort ("mBeginTimestamp", Sort.DESCENDING)
                            .beginGroup ()
                            .equalTo ("type", "incoming")
                            .endGroup ()
                            .findFirst ();
                } else if (mIsOutgoing) {
                    mOutgoingCallObject = mRealm.where (CallObject.class)
                            .equalTo ("mBeginTimestamp", beginTimestamp)
                            .equalTo ("mEndTimestamp", endTimestamp)
                            .sort ("mBeginTimestamp", Sort.DESCENDING)
                            .beginGroup ()
                            .equalTo ("type", "outgoing")
                            .endGroup ()
                            .findFirst ();
                }
            }
            // kiểm tra lại mội lần nữa xem là cuộc gọi đến là gì và đối tượng lấy ra có phù hợp hay không
            mIsIncoming = mIsIncoming && mIncomingCallObject != null;
            mIsOutgoing = mIsOutgoing && mOutgoingCallObject != null;
            if (mIsIncoming) {
                mIncomingCallObject.setCorrespondentName(correspondentName);
                listener.actionGetInComingObjectSuccess(mIsIncoming,mIncomingCallObject);
            }else if(mIsOutgoing){
                mOutgoingCallObject.setCorrespondentName(correspondentName);
                listener.actionGetOutgoingCallObjectSuccess(mIsOutgoing,mOutgoingCallObject);
            }else {
                listener.actionGetIntentFailed("Đối tượng truy vấn không phù hợp");
            }

        }
    }


    public String getTimeDurationCall(CallObject callObject, Context context){
        String durationString = null;
        Date beginDate = new Date (callObject.getBeginTimestamp ());
        Date endDate = new Date (callObject.getEndTimestamp ());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Duration duration = Duration.between (beginDate.toInstant (), endDate.toInstant ());
                long minutes = TimeUnit.SECONDS.toMinutes (duration.getSeconds ());
                durationString = String.format (Locale.getDefault (), context.getString(R.string.format_duration) ,
                        minutes,
                        duration.getSeconds () - TimeUnit.MINUTES.toSeconds (minutes));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                e.printStackTrace ();
            }
        } else {
            long durationMs = endDate.getTime () - beginDate.getTime ();
            try {
                long minutes = TimeUnit.MILLISECONDS.toMinutes (durationMs);
                durationString = String.format (Locale.getDefault (), context.getString(R.string.format_duration),
                        minutes,
                        TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (minutes));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                e.printStackTrace ();
            }
        }

        return durationString;
    }

    public void makeAPhoneCall (CallObject callObject, Context context) {
        LogI (TAG, "Make phone call");
        String phoneNumber = null;
        if (callObject != null) {
            phoneNumber = callObject.getPhoneNumber();
        }
        if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()
                && ActivityCompat.checkSelfPermission (context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                context.startActivity (new Intent (Intent.ACTION_DIAL, Uri.fromParts ("tel", phoneNumber, null)));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            new AlertDialog.Builder (context)
                    .setTitle (context.getString(R.string.title_header_action_make_phone_call_failed))
                    .setMessage (context.getString(R.string.title_message_action_make_phone_call_failed))
                    .setNeutralButton (android.R.string.ok, (dialogInterface1, i) -> {
                        dialogInterface1.dismiss ();
                    })
                    .create ().show ();
        }
    }


    public void makeAMessage (CallObject callObject, Context context) {
        LogI (TAG, "Make a message");
        String phoneNumber = null;
        if (callObject != null) {
            phoneNumber = callObject.getPhoneNumber();
        }
        if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()
//                && ActivityCompat.checkSelfPermission (context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                ) {
            try {
                context.startActivity (new Intent (Intent.ACTION_SENDTO, Uri.fromParts ("sms", phoneNumber, null)));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            new AlertDialog.Builder (context)
                    .setTitle (context.getString(R.string.title_header_action_make_a_message_failed))
                    .setMessage (context.getString(R.string.title_message_action_make_a_message_failed))
                    .setNeutralButton (android.R.string.ok, (dialogInterface1, i) -> {
                        dialogInterface1.dismiss ();
                    })
                    .create ().show ();
        }
    }


    public void actionDeleteThisItem(Realm realm,@NonNull CallObject incomingCallObject,@NonNull Context context) {
        new AlertDialog.Builder (context)
                .setTitle (context.getString(R.string.title_header_dialog_delete))
                .setMessage (context.getString(R.string.title_message_dialog_delete))
                .setPositiveButton (R.string.yes, (dialogInterface1, i) -> {
                    dialogInterface1.dismiss ();
                    if (realm != null && !realm.isClosed ()) {
                        try {
                            realm.beginTransaction ();
                            CallObject incomingCallObject1 = realm.where (CallObject.class)
                                    .equalTo ("mBeginTimestamp", incomingCallObject.getBeginTimestamp ())
                                    .equalTo ("mEndTimestamp", incomingCallObject.getEndTimestamp ())
                                    .beginGroup ()
                                    .equalTo ("type", incomingCallObject.getType ())
                                    .endGroup ()
                                    .findFirst ();
                            if (incomingCallObject1 != null) {
                                File outputFile = null;
                                try {
                                    outputFile = new File (incomingCallObject1.getOutputFile ());
                                } catch (Exception e) {
                                    LogE(TAG,e.getMessage());
                                    e.printStackTrace ();
                                }
                                if (outputFile != null) {
                                    if (outputFile.exists () && outputFile.isFile ()) {
                                        try {
                                            outputFile.delete ();
                                        } catch (Exception e) {
                                            LogE(TAG,e.getMessage());
                                            e.printStackTrace ();
                                        }
                                    }
                                }
                                incomingCallObject1.deleteFromRealm ();
                                realm.commitTransaction ();
                                Toast.makeText (context, context.getText(R.string.title_message_action_delete_success), Toast.LENGTH_SHORT).show ();
                                listener.actionDeleteCallObjectSuccess(context.getText(R.string.title_message_action_delete_success).toString());
                            } else {
                                realm.cancelTransaction ();
                                Toast.makeText (context,  context.getText(R.string.title_message_action_delete_failed), Toast.LENGTH_SHORT).show ();
                            }
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    } else {
                        Toast.makeText (context, context.getText(R.string.title_message_action_delete_failed), Toast.LENGTH_SHORT).show ();
                    }
                })
                .setNegativeButton (R.string.no, (dialogInterface1, i) -> dialogInterface1.dismiss ())
                .create ().show ();
    }
}
