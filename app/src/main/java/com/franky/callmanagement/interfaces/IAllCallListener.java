package com.franky.callmanagement.interfaces;

import com.franky.callmanagement.models.CallObject;

import io.realm.RealmResults;

public interface IAllCallListener {
    void actionViewTimeLine(String[] days);
    void actionViewAllCallObject(RealmResults<CallObject> mCallObjectRealmResults);
}
