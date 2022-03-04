package com.franky.callmanagement.presenters;
import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.util.Log;

import com.franky.callmanagement.interfaces.IAllCallListener;
import com.franky.callmanagement.models.CallObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AllCallPresenter {
    private static final String TAG = AllCallPresenter.class.getSimpleName();
    private IAllCallListener listener;
    public static final int NEXT_TIME_LINE = 1;
    public static final int PREVIOUS_TIME_LINE = -1;
    public static final int NOW_TIME_LINE = 0;


    private static int delta ;
    public AllCallPresenter(IAllCallListener listener) {
        this.listener = listener;
        Calendar now = Calendar.getInstance();
        now = Calendar.getInstance();
        delta = -now.get(GregorianCalendar.DAY_OF_WEEK) + 2; //add 2 if your week start on monday
    }

    public void getTimeLine(int flag){
        Calendar now = Calendar.getInstance();
        final SimpleDateFormat formatOfDay = new SimpleDateFormat("dd");
        final SimpleDateFormat formatOfDayAndMonth = new SimpleDateFormat("dd/MM/yyyy");
        String[] days = new String[7];

        delta = delta + (NEXT_TIME_LINE == flag ? 7 : PREVIOUS_TIME_LINE == flag ? -7 : 0); // change time line by button

        Log.e("dellta",delta+"");
        now.add(Calendar.DAY_OF_MONTH, delta );
        for (int i = 0; i < 7; i++)
        {
            days[i] = formatOfDayAndMonth.format(now.getTime());
            now.add(Calendar.DAY_OF_MONTH, 1);
        }



        // move Sunday into first arr
//        String [] dayOfWeek = new String[days.length];
//        System.arraycopy(days,0,dayOfWeek,1, days.length-1);
//        dayOfWeek[0] = days[dayOfWeek.length-1];
        System.out.println(Arrays.toString(days));
        listener.actionViewTimeLine(days);

    }

    public List<CallObject> convertRealmResultsToList(Realm mRealm,RealmResults<CallObject> mCallObjectRealmResults){
        if (mRealm != null) {
            return mRealm.copyFromRealm (mCallObjectRealmResults);
        }else {
            return new ArrayList<>(mCallObjectRealmResults);
        }
    }

    public int getDayOfYearSelected(String timeline[] , int isDaySelected){
        Calendar calendar = Calendar.getInstance();
        int yearSelected, monthSelected, daySelected;
        String timeLineSelected = timeline[isDaySelected];
        try {
            String [] str = timeLineSelected.split("/");
            daySelected = Integer.parseInt(str[0]);
            monthSelected = Integer.parseInt(str[1]);
            yearSelected = Integer.parseInt(str[2]);
            calendar.set(yearSelected,monthSelected-1,daySelected);
            LogE(TAG, "get Day of Year selec "+calendar.get(Calendar.DAY_OF_YEAR));
            return calendar.get(Calendar.DAY_OF_YEAR);
        }catch (Exception e){
            LogE(TAG,e.getMessage());
            e.printStackTrace();
            return calendar.get(Calendar.DAY_OF_YEAR);
        }

    }



    public void getCallObjectRealmObject(Realm realm){
        RealmResults<CallObject> mCallObjectRealmResults = null;
        if (realm != null && !realm.isClosed ()) {
            try {
                mCallObjectRealmResults = realm.where (CallObject.class)
                        .greaterThan ("mEndTimestamp", 0L)
                        .sort ("mBeginTimestamp", Sort.DESCENDING)
                        .findAll ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            listener.actionViewAllCallObject(mCallObjectRealmResults);
        }
    }
}
