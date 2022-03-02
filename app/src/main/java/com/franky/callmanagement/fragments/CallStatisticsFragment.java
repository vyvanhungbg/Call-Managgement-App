package com.franky.callmanagement.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franky.callmanagement.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CallStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallStatisticsFragment extends Fragment  {


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



        return inflater.inflate(R.layout.fragment_call_statistics, container, false);
    }



}