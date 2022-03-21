package com.franky.callmanagement.presenters;

import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.franky.callmanagement.R;
import com.franky.callmanagement.interfaces.ICallStatisticsListener;
import com.franky.callmanagement.models.CallObject;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CallStatisticsPresenter {
    private static final String TAG = CallStatisticsPresenter.class.getSimpleName();
    ICallStatisticsListener listener;

    public static final int NEXT_TIME_LINE = 1;
    public static final int PREVIOUS_TIME_LINE = -1;
    public static final int NOW_TIME_LINE = 0;
    private static int delta ;

    public CallStatisticsPresenter(ICallStatisticsListener listener) {
        this.listener = listener;

        Calendar now = Calendar.getInstance();
        boolean isSunday = now.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY;
        delta = -now.get(GregorianCalendar.DAY_OF_WEEK) +2; //add 2 if your week start on monday
        if(isSunday){
            delta = delta -7; // -7 để lịch chủ nhật không bị tính vào tuần sau . Fix cho lỗi nếu vào ngày chủ nhật sẽ bị nhảy 1 tuẩn ( chủ nhật tuần sau ) vì Calender là Chủ nhật là  ngày đầu tuần
        }
    }

    public void getDataForCombinedChart(Context context ,int [] numberOfCallPerDay) {

        final List<String> labelDayOfWeek = Arrays.asList(context.getResources().getStringArray(R.array.title_name_day_of_week)); // T2->T3->..CN

        LineData numberOfCallByDay = new LineData(); // chứ số lượng cuộc gọi theo từng ngày trong tuần

       // int[] data = new int[] {  1, 2, 7, 1, 5, 1, 9};
        int[] data = numberOfCallPerDay;

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < 7; index++) {
            entries.add(new Entry(index, data[index]));
        }

        LineDataSet set = new LineDataSet(entries, "Số cuộc gọi");
        set.setColor(Color.GREEN);
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.GREEN);
        set.setCircleRadius(5f);
        set.setFillColor(Color.GREEN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.GREEN);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        numberOfCallByDay.addDataSet(set);

        listener.displayCombinedChart(numberOfCallByDay, labelDayOfWeek);
    }

    public void getDataForHorizontalBarChart(){

    }

    // lấy tên các ngày trong tuần đẻ hiện trong biểu đồ
    public  String getNameOfDay(Context context,String subName){
        try{
            if(subName.startsWith("T") && (subName.endsWith("2") || subName.endsWith("3")|| subName.endsWith("4")  || subName.endsWith("5") || subName.endsWith("6") || subName.endsWith("7"))){
                return context.getString(R.string.day_of_week, subName.charAt(subName.length()-1)).toString();
            }else if(subName.equals("CN")){
                return context.getString(R.string.sunday);
            }else {
                return "";
            }
        }catch (Exception e){
            LogE(TAG,e.toString());
            return "";
        }
    }

    // convert realmObject về List object
    public List<CallObject> convertRealmResultsToList(Realm mRealm,RealmResults<CallObject> mCallObjectRealmResults){
        if (mRealm != null) {
            return mRealm.copyFromRealm (mCallObjectRealmResults);
        }else {
            return new ArrayList<>(mCallObjectRealmResults);
        }
    }

    // lấy số cuộc gọi theo ngày
    public int getCallObjectRealmObject(Realm realm, int dayOfYear){
        Calendar calendar = Calendar.getInstance();
        RealmResults<CallObject> callObjectRealmResults = null;
        if (realm != null && !realm.isClosed ()) {
            try {
                callObjectRealmResults = realm.where (CallObject.class)
                        .greaterThan ("mEndTimestamp", 0L)
                        .sort ("mBeginTimestamp", Sort.DESCENDING)
                        .findAll ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }

        }

        List<CallObject> callObjectList = convertRealmResultsToList(realm,callObjectRealmResults);

        // fitter theo ngày
        List<CallObject> list = new ArrayList<> ();
        if (!callObjectList.isEmpty ()) {
            for (Iterator<CallObject> iterator = callObjectList.iterator (); iterator.hasNext () ; ) {
                CallObject incomingCallObject = iterator.next ();
                calendar.setTime (new Date(incomingCallObject.getBeginTimestamp ()));
                if (calendar.get (Calendar.DAY_OF_YEAR) == dayOfYear) {
                    iterator.remove ();
                    list.add (incomingCallObject);
                }
            }

        }
        return list.size();
    }

    // lấy cả tuần chưa ngày hiện tại
    public void getTimeLine(int flag){
        Calendar now = Calendar.getInstance();
        final SimpleDateFormat formatOfDay = new SimpleDateFormat("dd");
        final SimpleDateFormat formatOfDayAndMonth = new SimpleDateFormat("dd/MM/yyyy");
        String[] days = new String[7];
        int[] dayOfWeeks = new int[7];

        delta = delta + (NEXT_TIME_LINE == flag ? 7 : PREVIOUS_TIME_LINE == flag ? -7 : 0); // change time line by button

        //Log.e("dellta",delta+" | now : "+now.getTime().toString()); // t2 deleta =0, dayofwwek = 2
       // Log.e("DAy of week", now.get(Calendar.DAY_OF_WEEK)+"");
        now.add(Calendar.DAY_OF_MONTH, delta );
        for (int i = 0; i < 7; i++)
        {
            days[i] = formatOfDayAndMonth.format(now.getTime());
            dayOfWeeks[i] = now.get(Calendar.DAY_OF_YEAR);
            now.add(Calendar.DAY_OF_MONTH, 1);
        }

        listener.getTimeLine(days
                ,dayOfWeeks);
    }
}
