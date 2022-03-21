package com.franky.callmanagement.fragments;

import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.FragmentCallStatisticsBinding;
import com.franky.callmanagement.interfaces.ICallStatisticsListener;
import com.franky.callmanagement.presenters.AllCallPresenter;
import com.franky.callmanagement.presenters.CallStatisticsPresenter;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CallStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallStatisticsFragment extends Fragment implements View.OnClickListener , ICallStatisticsListener {

    private static final String TAG = CallStatisticsFragment.class.getSimpleName();
    //FragmentCallStatisticsBinding binding;
    private CallStatisticsPresenter presenter;
    ColorStateList def;
    TextView item1;
    TextView item2;
    TextView item3;
    TextView select;
    CombinedChart combinedChart;
    HorizontalBarChart horizontalBarChart;

    TextView tvFromDayToDay ;
    ImageButton btnNextTimeLine,btnPreviousTimeLine;

    private String [] timeline ;
    private int[] dayOfWeeks;

    private Realm mRealm = null;

    public static CallStatisticsFragment newInstance() {
        CallStatisticsFragment fragment = new CallStatisticsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_call_statistics, container, false);
        init();
        item1 = view.findViewById(R.id.item1);
        item2 = view.findViewById(R.id.item2);
        item3 = view.findViewById(R.id.item3);
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        item3.setOnClickListener(this);
        combinedChart = view.findViewById(R.id.combinedChart);
        //horizontalBarChart = view.findViewById(R.id.horizontalBarChart);
        select = view.findViewById(R.id.select);
        def = item2.getTextColors();
        tvFromDayToDay = view.findViewById(R.id.tv_from_day_to_day_in_call_statistic);
        btnNextTimeLine = view.findViewById(R.id.btn_next_time_line_in_call_statistic);
        btnPreviousTimeLine = view.findViewById(R.id.btn_previous_time_line_in_call_statistic);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // createHorizontalBarChart();
        presenter.getTimeLine(CallStatisticsPresenter.NOW_TIME_LINE);

        setOnClickButtonNextOrPrevious();
    }

    public void init(){
        presenter = new CallStatisticsPresenter(this);
        try {
            mRealm = Realm.getDefaultInstance ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
    }

    public void createHorizontalBarChart(){
//        horizontalBarChart.setOnChartValueSelectedListener(new );
        // chart.setHighlightEnabled(false);

        horizontalBarChart.setDrawBarShadow(false);

        horizontalBarChart.setDrawValueAboveBar(true);

        horizontalBarChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        horizontalBarChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        horizontalBarChart.setPinchZoom(false);

        horizontalBarChart.setDrawGridBackground(false);

        XAxis xl = horizontalBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xl.setTypeface(tfLight);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setGranularity(10f);

        YAxis yl = horizontalBarChart.getAxisLeft();
        //yl.setTypeface(tfLight);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yl.setInverted(true);

        YAxis yr = horizontalBarChart.getAxisRight();
       // yr.setTypeface(tfLight);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yr.setInverted(true);



        horizontalBarChart.setFitBars(true);
        horizontalBarChart.animateY(2500);
        setData(3, 25);
        horizontalBarChart.invalidate();
    }

    private void setData(int count, float range) {

        float barWidth = 9f;
        float spaceForBar = 10f;
        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range);
            values.add(new BarEntry(i * spaceForBar, val,
                    getResources().getDrawable(R.drawable.ic_favourit_stroke)));
        }

        BarDataSet set1;

        if (horizontalBarChart.getData() != null &&
                horizontalBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) horizontalBarChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            horizontalBarChart.getData().notifyDataChanged();
            horizontalBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values, "DataSet 1");

            set1.setDrawIcons(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(barWidth);
            horizontalBarChart.setData(data);
        }
    }



    // thanh tablayout
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1){
            select.animate().x(0).setDuration(200);
            item1.setTextColor(Color.WHITE);
            item2.setTextColor(def);
            item3.setTextColor(def);
        } else if (view.getId() == R.id.item2){
            item1.setTextColor(def);
            item2.setTextColor(Color.WHITE);
            item3.setTextColor(def);
            int size = item2.getWidth();
            select.animate().x(size).setDuration(200);
        } else if (view.getId() == R.id.item3){
            item1.setTextColor(def);
            item3.setTextColor(Color.WHITE);
            item2.setTextColor(def);
            int size = item2.getWidth() * 2;
            select.animate().x(size).setDuration(200);
        }
    }

    @Override
    public void displayCombinedChart(LineData numberOfCallByDay, List<String> labelDayOfWeek) {
        combinedChart.animateXY(1500,2000);
        combinedChart.setPinchZoom(true);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setBackgroundColor(Color.WHITE);
        combinedChart.setDrawGridBackground(false);
        combinedChart.setDrawBarShadow(false);
        combinedChart.setHighlightFullBarEnabled(false);
        combinedChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
//                Toast.makeText(getContext(), "Value: "
//                        + e.getY()
//                        + ", index: "
//                        + h.getX()
//                        + ", DataSet index: "
//                        + h.getDataSetIndex(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(),getString(R.string.toast_text_view_combined_chart_on_touch,(int)e.getY(),
                        presenter.getNameOfDay(getContext(),labelDayOfWeek.get((int) h.getX())))
                ,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);


        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelDayOfWeek));

        CombinedData data = new CombinedData();


        data.setData(numberOfCallByDay);

        xAxis.setAxisMaximum(data.getXMax() + 0.25f);

        combinedChart.setData(data);
        combinedChart.invalidate();
    }

    @Override
    public void getTimeLine(String[] days, int[] dayOfWeeks) {
        timeline = days;
        this.dayOfWeeks = dayOfWeeks;

        tvFromDayToDay.setText(days[0]+ " --> "+days[6]);
        int [] numberOfCallPerDay = new int[dayOfWeeks.length];
        for(int i=0;i<dayOfWeeks.length;i++){
            System.out.println("Ngày : "+days[i]+" có : "+ presenter.getCallObjectRealmObject(mRealm,dayOfWeeks[i]) +" cuộc gọi ");
            numberOfCallPerDay[i] = presenter.getCallObjectRealmObject(mRealm,dayOfWeeks[i]);
        }

        presenter.getDataForCombinedChart(requireContext(),numberOfCallPerDay);
    }

    public void setOnClickButtonNextOrPrevious(){
       btnPreviousTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(CallStatisticsPresenter.PREVIOUS_TIME_LINE);
               // setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), presenter.convertRealmResultsToList(mRealm,mCallObjectRealmResults),presenter.getDayOfYearSelected(timeline,isDaySelected)));
            }
        });

        btnNextTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(CallStatisticsPresenter.NEXT_TIME_LINE);
                //setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), presenter.convertRealmResultsToList(mRealm,mCallObjectRealmResults),presenter.getDayOfYearSelected(timeline,isDaySelected)));
            }
        });


    }
}