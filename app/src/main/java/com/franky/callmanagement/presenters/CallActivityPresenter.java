package com.franky.callmanagement.presenters;

import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.content.Intent;
import android.os.Build;

import com.franky.callmanagement.interfaces.ICallActivityListener;
import com.franky.callmanagement.models.CallObject;

import java.time.Duration;
import java.util.Date;
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


    public String getTimeDurationCall(CallObject callObject){
        String durationString = null;
        Date beginDate = new Date (callObject.getBeginTimestamp ());
        Date endDate = new Date (callObject.getEndTimestamp ());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Duration duration = Duration.between (beginDate.toInstant (), endDate.toInstant ());
                long minutes = TimeUnit.SECONDS.toMinutes (duration.getSeconds ());
                durationString = String.format (Locale.getDefault (), "%d min, %d sec",
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
                durationString = String.format (Locale.getDefault (), "%d min, %d sec",
                        minutes,
                        TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (minutes));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                e.printStackTrace ();
            }
        }

        return durationString;
    }
}
