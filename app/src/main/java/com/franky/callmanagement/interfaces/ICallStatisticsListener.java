package com.franky.callmanagement.interfaces;

import com.github.mikephil.charting.data.LineData;

import java.util.List;

public interface ICallStatisticsListener {
    void displayCombinedChart(LineData numberOfCallByDay, List<String> labelDayOfWeek);
    void getTimeLine(String[] days, int[] dayOfWeeks);
}
