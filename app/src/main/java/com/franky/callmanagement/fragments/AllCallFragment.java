package com.franky.callmanagement.fragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.FragmentAllCallBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
    Chứa các tất cả danh sách cuộc gọi
 */
public class AllCallFragment extends Fragment {


    private FragmentAllCallBinding binding;






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



        return binding.getRoot();
    }


}