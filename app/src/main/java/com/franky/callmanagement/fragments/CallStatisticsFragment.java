package com.franky.callmanagement.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.franky.callmanagement.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CallStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallStatisticsFragment extends Fragment implements View.OnClickListener {

    ColorStateList def;
    TextView item1;
    TextView item2;
    TextView item3;
    TextView select;

    public static CallStatisticsFragment newInstance() {
        CallStatisticsFragment fragment = new CallStatisticsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_call_statistics, container, false);
        item1 = view.findViewById(R.id.item1);
        item2 = view.findViewById(R.id.item2);
        item3 = view.findViewById(R.id.item3);
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        item3.setOnClickListener(this);
        select = view.findViewById(R.id.select);
        def = item2.getTextColors();

        return view;
    }

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

}