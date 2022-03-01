package com.franky.callmanagement.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franky.callmanagement.R;
import com.franky.callmanagement.activities.MainActivity;
import com.franky.callmanagement.custom_ui.CircleAngleAnimation;
import com.franky.callmanagement.databinding.ActivityMainBinding;
import com.franky.callmanagement.databinding.FragmentHomeBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private  boolean isEnable = false;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
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
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false);

        binding.btnOnOffRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEnable = !isEnable;
                if(isEnable){
                    binding.imgvMicro.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_baseline_mic_24));
                    binding.btnOnOffRecord.setText("Đang ghi âm");
                }else {
                    binding.imgvMicro.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_baseline_mic_off_24));
                    binding.btnOnOffRecord.setText("Bật ghi âm");
                }
            }
        });


        return binding.getRoot();
    }
}