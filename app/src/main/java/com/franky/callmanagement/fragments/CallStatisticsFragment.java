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
    final static int FLAG_SECOND = 0;
    final static int FLAG_MIN = 1;
    final static int FLAG_HOURS = 2;

    static int FLAG_FORMAT = FLAG_SECOND;

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

    private static int FLAG_FILTER_CALL = CallStatisticsPresenter.ALL_CALL; // xem filter theo cu???c g???i ?????n / ??i / hay t???t c???

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
       // init();
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
        setOnChangedDataToUpdateView();
        setOnClickButtonNextOrPrevious();

    }

    private void setOnChangedDataToUpdateView() {
        try {
            mRealm.addChangeListener(realm ->  presenter.getTimeLine(mRealm,CallStatisticsPresenter.NOW_TIME_LINE,FLAG_FILTER_CALL));
        }catch (Exception e){
            LogE(TAG,e.getMessage());
        }
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
        horizontalBarChart.setDoubleTapToZoomEnabled(false);

        XAxis xl = horizontalBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setGranularity(1);
        xl.setGranularityEnabled(true);




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




        // set v??? tr?? nh??n ?????i l?????ng
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

    public float formatTime(float time){
        if(FLAG_FORMAT == FLAG_HOURS) return  time/3600;
        if(FLAG_FORMAT == FLAG_MIN) return  time/60;
        return time;
    }

    public String formatLabelOfColumnTime(){
        if(FLAG_FORMAT == FLAG_HOURS) return  "Gi???";
        if(FLAG_FORMAT == FLAG_MIN) return  "Ph??t";
        return "Gi??y";
    }

    private void setData(int[] listTotalCallSeconds) {

        float barWidth = 0.8f;
        ArrayList<BarEntry> values = new ArrayList<>();
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

        float aver = (sum*1.0F/listTotalCallSeconds.length);  // t??nh to??n xem n??n hi???n th??? theo ?????i l?????ng n??o
        if(aver /3600 >1)
            FLAG_FORMAT = FLAG_HOURS;
        if(aver/60 >1)
            FLAG_FORMAT = FLAG_MIN;

        //set nh??n
        final List<String> labelDayOfWeek = Arrays.asList(getResources().getStringArray(R.array.title_name_day_of_week));
        XAxis xl = horizontalBarChart.getXAxis();
        List<String> label= new ArrayList<>();
        label.add("G???i nhi???u nh???t "+labelDayOfWeek.get(nameOfDayMax)+" :");
        label.add("T???ng "+formatLabelOfColumnTime()+"  tu???n n??y :");
        label.add("Trung b??nh m???i ng??y :");
        xl.setValueFormatter(new IndexAxisValueFormatter(label));


        horizontalBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Toast.makeText(getContext(),
                        getString(R.string.toast_text_view_horizontal_bar_chart_on_touch,
                                label.get((int) h.getX()),
                        String.format("%.1f"+formatLabelOfColumnTime(),e.getY()))
                        ,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });


        // set gi?? tr??? cho m???i c???t
        values.add(new BarEntry(0, formatTime(max)));
        values.add(new BarEntry(1, formatTime(sum)));
        values.add(new BarEntry(2,  formatTime((sum*1.0F/listTotalCallSeconds.length))));

        BarDataSet mSet;

        List<Integer> colorsOfColumn = new ArrayList();
        colorsOfColumn.add(Color.BLUE);
        colorsOfColumn.add(Color.RED);
        colorsOfColumn.add(Color.GREEN);

        if (horizontalBarChart.getData() != null &&
                horizontalBarChart.getData().getDataSetCount() > 0) {
            mSet = (BarDataSet) horizontalBarChart.getData().getDataSetByIndex(0);
            mSet.setValues(values);
            horizontalBarChart.getData().notifyDataChanged();
            horizontalBarChart.notifyDataSetChanged();
        } else {
            mSet = new BarDataSet(values, formatLabelOfColumnTime()); // set nh??n c???t

            mSet.setDrawIcons(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(mSet);

            BarData data = new BarData(dataSets);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(12f);
            data.setBarWidth(barWidth);
            horizontalBarChart.setData(data);
        }

       // horizontalBarChart.set

        mSet.setColors(colorsOfColumn);


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
        // l??m m???i bi???u ????? m???i khi b???m tab selected
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

    @Override
    public void onDestroy() {
        if (mRealm != null) {
            mRealm.removeAllChangeListeners ();
            if (!mRealm.isClosed ()) {
                try {
                    mRealm.close ();
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            mRealm = null;
        }
        super.onDestroy();
    }
}