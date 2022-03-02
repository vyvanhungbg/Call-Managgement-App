package com.franky.callmanagement.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.FragmentAllCallBinding;
import com.franky.callmanagement.interfaces.IAllCallListener;
import com.franky.callmanagement.presenters.AllCallPresenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
    Chứa các tất cả danh sách cuộc gọi
 */
public class AllCallFragment extends Fragment implements IAllCallListener {


    private FragmentAllCallBinding binding;
    private AllCallPresenter presenter;
    private Calendar calendar = Calendar.getInstance();
    private int isDaySelected ;
    private List<LinearLayout> layoutTimeLine;



    public static AllCallFragment newInstance() {
        AllCallFragment fragment = new AllCallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_all_call, container, false);
        init();
        presenter.getTimeLine(AllCallPresenter.NOW_TIME_LINE);
        setDaysSelectedListener();
        setOnClickButtonNextOrPrevious();
        return binding.getRoot();
    }


    public void init(){
        presenter = new AllCallPresenter(this);
        layoutTimeLine = getAllChildOfTimeLine();
        // lấy vị trí ngày hôm nay
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(nowDayOfWeek >1 && nowDayOfWeek <8){
            isDaySelected = nowDayOfWeek-2;
        }else if(nowDayOfWeek ==1){
            isDaySelected = 6;
        }
    }


    // binding of all child in timeline
    public List<LinearLayout> getAllChildOfTimeLine(){
        List<LinearLayout> linearLayoutList = new ArrayList<>();
        for(int i=0;i<binding.linLayoutTimeLine.getChildCount();i++){
            LinearLayout layout = (LinearLayout) binding.linLayoutTimeLine.getChildAt(i);
            linearLayoutList.add(layout);
        }
        return linearLayoutList;
    }

    // set day on the Time Line View
    @Override
    public void actionViewTimeLine(String[] days, String fromDay, String toDay) {
        for(int i=0;i<days.length;i++){
            LinearLayout layout = (LinearLayout) layoutTimeLine.get(i);
            TextView textViewDays = (TextView) layout.getChildAt(1); // get TextView day
            textViewDays.setText(days[i]);
        }
        binding.tvFromDayToDay.setText(fromDay+ " --> "+toDay);
    }




    public void setDaysSelectedListener() {
        for(int i=0;i<layoutTimeLine.size();i++){
            int positionSelect = i;
            layoutTimeLine.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        layoutTimeLine.get(isDaySelected).setBackground(null);
                        view.setBackground(ContextCompat.getDrawable(view.getContext(),R.drawable.custom_time_line_selected));
                        isDaySelected = positionSelect;
                    Log.e("Selected day", isDaySelected+"");
                }
            });
        }
    }

    public void setOnClickButtonNextOrPrevious(){
        binding.btnPreviousTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    presenter.getTimeLine(AllCallPresenter.PREVIOUS_TIME_LINE);
            }
        });

        binding.btnNextTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(AllCallPresenter.NEXT_TIME_LINE);
            }
        });
    }
}