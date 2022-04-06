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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.franky.callmanagement.R;
import com.franky.callmanagement.interfaces.ICallStatisticsListener;
import com.franky.callmanagement.presenters.CallStatisticsPresenter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
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
    ImageView btnNextTimeLine,btnPreviousTimeLine;

    private String [] timeline ;
    private int[] dayOfWeeks;

    private Realm mRealm = null;
    private PieChart pieChart;

    private static int FLAG_FILTER_CALL = CallStatisticsPresenter.ALL_CALL; // xem filter theo cuộc gọi đến / đi / hay tất cả

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
        horizontalBarChart = view.findViewById(R.id.horizontalBarChart);
        select = view.findViewById(R.id.select);
        def = item2.getTextColors();
        tvFromDayToDay = view.findViewById(R.id.tv_date_in_layout_next_previous_time_line);
        btnNextTimeLine = view.findViewById(R.id.imgv_right_in_layout_next_previous_time_line);
        btnPreviousTimeLine = view.findViewById(R.id.imgv_left_in_layout_next_previous_time_line);


        //pie chart

        horizontalBarChart = view.findViewById(R.id.horizontalBarChart);
//        setupPieChart();
//        loadPieChartData();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        presenter.getTimeLine(mRealm,CallStatisticsPresenter.NOW_TIME_LINE,FLAG_FILTER_CALL);

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

    public void createHorizontalBarChart(int[] listTotalCallSeconds){
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
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setGranularity(1);
        xl.setGranularityEnabled(true);



        List<String> label= new ArrayList<>();
        label.add("Label 1 :");
        label.add("Label 2 :");
        label.add("Label 3 :");
        xl.setValueFormatter(new IndexAxisValueFormatter(label));

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

        Legend l = horizontalBarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);

        horizontalBarChart.setFitBars(true);
        horizontalBarChart.animateY(2500);
        setData(listTotalCallSeconds);
        horizontalBarChart.invalidate();


    }

    private void setData(int[] listTotalCallSeconds) {

        float barWidth = 1f;
        float spaceForBar = 3f;
        ArrayList<BarEntry> values = new ArrayList<>();

       /* for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range);
            values.add(new BarEntry(i * spaceForBar, val,
                    getResources().getDrawable(R.drawable.ic_favourit_stroke)));
        }*/
        int nameOfDayMax = 0;
        int max = listTotalCallSeconds[nameOfDayMax];
        int sum = 0;
        for(int i=0;i<listTotalCallSeconds.length;i++){
            if(max < listTotalCallSeconds[i]){
                max = listTotalCallSeconds[i];
                nameOfDayMax = i;
            }
            sum+= listTotalCallSeconds[i];
        }

        values.add(new BarEntry(0, max));
        values.add(new BarEntry(1, sum));
        values.add(new BarEntry(2,  (sum*1.0F/listTotalCallSeconds.length)));

        BarDataSet set1;


        if (horizontalBarChart.getData() != null &&
                horizontalBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) horizontalBarChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            horizontalBarChart.getData().notifyDataChanged();
            horizontalBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values, "Seconds");

            set1.setDrawIcons(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(10f);
            data.setBarWidth(barWidth);
            horizontalBarChart.setData(data);
        }

       // horizontalBarChart.set

        set1.setColors(Color.RED);

    }




    // thanh tablayout
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1){
            select.animate().x(0).setDuration(200);
            item1.setTextColor(Color.WHITE);
            item2.setTextColor(def);
            item3.setTextColor(def);
            FLAG_FILTER_CALL = CallStatisticsPresenter.ALL_CALL;
        } else if (view.getId() == R.id.item2){
            item1.setTextColor(def);
            item2.setTextColor(Color.WHITE);
            item3.setTextColor(def);
            int size = item2.getWidth();
            select.animate().x(size).setDuration(200);
            FLAG_FILTER_CALL = CallStatisticsPresenter.INCOMING_CALL;
        } else if (view.getId() == R.id.item3){
            item1.setTextColor(def);
            item3.setTextColor(Color.WHITE);
            item2.setTextColor(def);
            int size = item2.getWidth() * 2;
            select.animate().x(size).setDuration(200);
            FLAG_FILTER_CALL = CallStatisticsPresenter.OUTGOING_CALL;
        }
        // làm mới biểu đồ mỗi khi bấm tab selected
        presenter.getTimeLine(mRealm, CallStatisticsPresenter.NOW_TIME_LINE, FLAG_FILTER_CALL);
    }

    @Override
    public void displayCombinedChart(LineData numberOfCallByDay, List<String> labelDayOfWeek) {
        combinedChart.animateXY(1000,1500);
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
    public void getTimeLine(String[] listDaysOfAWeek, int[] dayOfWeeks, int[] numberOfCallPerDay, int[] listTotalCallSeconds) {
        timeline = listDaysOfAWeek;
        this.dayOfWeeks = dayOfWeeks;

        tvFromDayToDay.setText(listDaysOfAWeek[0].substring(0,listDaysOfAWeek[0].length()-5)+ " - "+listDaysOfAWeek[6].substring(0,listDaysOfAWeek[6].length()-5));


        presenter.getDataForCombinedChart(requireContext(),numberOfCallPerDay);
        createHorizontalBarChart(listTotalCallSeconds);
    }

    public void setOnClickButtonNextOrPrevious(){
       btnPreviousTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(mRealm, CallStatisticsPresenter.PREVIOUS_TIME_LINE, FLAG_FILTER_CALL);
            }
        });

        btnNextTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(mRealm, CallStatisticsPresenter.NEXT_TIME_LINE, FLAG_FILTER_CALL);
            }
        });


    }

    // Pie Chart

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
       // pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Spending by Category");
        //pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void loadPieChartData() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(70, "Food & Dining"));
        entries.add(new PieEntry(30, ""));
       // entries.add(new PieEntry(0.10f, "Entertainment"));
        //entries.add(new PieEntry(0.25f, "Electricity and Gas"));
        //entries.add(new PieEntry(0.3f, "Housing"));

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Category");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
       // data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.setUsePercentValues(false);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

}