package com.franky.callmanagement.interfaces;

import com.franky.callmanagement.models.CallObject;

public interface ICallActivityListener {
    void actionGetIntentFailed(String mess);

    void actionGetInComingObjectSuccess(boolean mIsIncoming, CallObject mIncomingCallObject);
    void actionGetOutgoingCallObjectSuccess(boolean mIsOutgoing, CallObject mOutgoingCallObject);
}
