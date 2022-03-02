package com.franky.callmanagement.utils;

import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.franky.callmanagement.R;

import java.util.Calendar;

public class DayUtil {
    enum DAY {
        T2, T3, T4, T5, T6, T7,CN;
    }

    @BindingAdapter("setIsDaySelected")
    public static void setIsDaySelected(View view ,int day) {
         Calendar calendar = Calendar.getInstance();
         int nowDay = calendar.get(Calendar.DAY_OF_WEEK);
         Log.e("Now day",nowDay+"");
         view.setBackground(nowDay==day ? ContextCompat.getDrawable(view.getContext(), R.drawable.custom_time_line_selected) : null);

    }
}
