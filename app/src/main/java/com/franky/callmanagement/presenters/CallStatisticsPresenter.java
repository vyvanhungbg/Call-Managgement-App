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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CallStatisticsPresenter {
    private static final String TAG = CallStatisticsPresenter.class.getSimpleName();
    ICallStatisticsListener listener;

    public CallStatisticsPresenter(ICallStatisticsListener listener) {
        this.listener = listener;
    }

    public void getDataForCombinedChart(Context context) {

        final List<String> labelDayOfWeek = Arrays.asList(context.getResources().getStringArray(R.array.title_name_day_of_week)); // T2->T3->..CN

        LineData numberOfCallByDay = new LineData(); // chứ số lượng cuộc gọi theo từng ngày trong tuần

        int[] data = new int[] {  1, 2, 7, 1, 5, 1, 9};

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

    public void getCallObjectRealmObject(Realm realm){
        Calendar calendar = Calendar.getInstance();
        RealmResults<CallObject> callObjectList = null;
        if (realm != null && !realm.isClosed ()) {
            try {
                callObjectList = realm.where (CallObject.class)
                        .greaterThan ("mEndTimestamp", 0L)
                        .sort ("mBeginTimestamp", Sort.DESCENDING)
                        .findAll ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }

        }

        // fitter theo ngày
        List<CallObject> list = new ArrayList<> ();
        if (!callObjectList.isEmpty ()) {
            for (Iterator<CallObject> iterator = callObjectList.iterator (); iterator.hasNext () ; ) {
                CallObject incomingCallObject = iterator.next ();
                calendar.setTime (new Date(incomingCallObject.getBeginTimestamp ()));
                if (calendar.get (Calendar.DAY_OF_YEAR) == ?) {
                    iterator.remove ();
                    list.add (incomingCallObject);
                }
            }

        }
    }
}
