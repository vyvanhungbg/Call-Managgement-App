package com.franky.callmanagement.interfaces;

import com.franky.callmanagement.models.CallObject;

import io.realm.RealmResults;

public interface IAllCallListener {
    void actionViewTimeLine(String[] days, String fromDay, String toDay);
    void actionViewAllCallObject(RealmResults<CallObject> mCallObjectRealmResults);
}
