package com.franky.callmanagement.fragments;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;
import static com.franky.callmanagement.utils.LogUtil.LogI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.franky.callmanagement.R;
import com.franky.callmanagement.activities.MainActivity;
import com.franky.callmanagement.custom_ui.CircleAngleAnimation;
import com.franky.callmanagement.databinding.ActivityMainBinding;
import com.franky.callmanagement.databinding.FragmentHomeBinding;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.services.MainService;
import com.franky.callmanagement.utils.AppUtil;
import com.franky.callmanagement.utils.RequestIgnoreBatteryOptimizationsUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding binding;

    private SharedPreferences sharedPreferences ;


    private  boolean isEnable = true;
    private boolean mRecordIncomingCalls = isEnable;
    private boolean mRecordOutgoingCalls = isEnable;


    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSharedPrefers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false);
        binding.btnOnOffRecord.setChecked(isEnable);
        binding.btnOnOffRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isEnable = b;
                mRecordIncomingCalls = isEnable;
                mRecordOutgoingCalls = isEnable;
                if (sharedPreferences != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit ();
                    editor.putBoolean (AppConstants.KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
                    editor.putBoolean (AppConstants.KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
                    editor.apply ();
                }
                if (mRecordIncomingCalls && !MainService.isServiceRunning) {
                    AppUtil.startMainService (getContext());
                }
                if (mRecordOutgoingCalls && !MainService.isServiceRunning) {
                    AppUtil.startMainService (getContext());
                }
            }
        });


        return binding.getRoot();
    }

    public void getSharedPrefers(){
        try {
            sharedPreferences = getContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        }catch (Exception e){
            LogE(TAG,e.getMessage());
            e.printStackTrace();
        }
        if(sharedPreferences != null){
            // khởi tạo
            if(! sharedPreferences.contains(AppConstants.KEY_RECORD_INCOMING_CALLS)){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(AppConstants.KEY_RECORD_INCOMING_CALLS,true).apply();
            }
            if(! sharedPreferences.contains(AppConstants.KEY_RECORD_OUTGOING_CALLS)){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(AppConstants.KEY_RECORD_OUTGOING_CALLS,true).apply();
            }
        }

        boolean recordIncomingCalls = sharedPreferences.getBoolean(AppConstants.KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls) ;
        boolean recordOutgoingCalls = sharedPreferences.getBoolean(AppConstants.KEY_RECORD_OUTGOING_CALLS,mRecordOutgoingCalls );
        if(recordIncomingCalls || recordOutgoingCalls){
            if (!MainService.isServiceRunning) {
                AppUtil.startMainService (getContext());
                isEnable = true;
            }else {
                isEnable = false;
            }
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences != null) {
            mRecordIncomingCalls = sharedPreferences.getBoolean (AppConstants.KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
            mRecordOutgoingCalls = sharedPreferences.getBoolean (AppConstants.KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
        } else {
//
            mRecordIncomingCalls = sharedPreferences.getBoolean (AppConstants.KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
            mRecordOutgoingCalls = sharedPreferences.getBoolean (AppConstants.KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
        }
        isEnable = mRecordIncomingCalls;
        binding.btnOnOffRecord.setChecked(isEnable);
    }


    @Override
    public void onStart () {
        super.onStart ();
        LogD (TAG, "Activity start");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> runtimePermissionsArrayList = new ArrayList<> ();
            runtimePermissionsArrayList.add (Manifest.permission.INTERNET);
            runtimePermissionsArrayList.add (Manifest.permission.READ_PHONE_STATE);
            runtimePermissionsArrayList.add (Manifest.permission.CALL_PHONE);
            runtimePermissionsArrayList.add (Manifest.permission.RECORD_AUDIO);
            runtimePermissionsArrayList.add (Manifest.permission.VIBRATE);
            runtimePermissionsArrayList.add (Manifest.permission.RECEIVE_BOOT_COMPLETED);
            runtimePermissionsArrayList.add (Manifest.permission.READ_CONTACTS);
            runtimePermissionsArrayList.add (Manifest.permission.MODIFY_AUDIO_SETTINGS);
            runtimePermissionsArrayList.add (Manifest.permission.WAKE_LOCK);
            runtimePermissionsArrayList.add (Manifest.permission.READ_EXTERNAL_STORAGE);
            runtimePermissionsArrayList.add (Manifest.permission.WRITE_EXTERNAL_STORAGE);
            runtimePermissionsArrayList.add (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                runtimePermissionsArrayList.add (Manifest.permission.FOREGROUND_SERVICE);
            }
            if (!runtimePermissionsArrayList.isEmpty ()) {
                ArrayList<String> requestRuntimePermissionsArrayList = new ArrayList<> ();
                for (String requestRuntimePermission : runtimePermissionsArrayList) {
                    if (getContext().checkSelfPermission (requestRuntimePermission) != PackageManager.PERMISSION_GRANTED) {
                        requestRuntimePermissionsArrayList.add (requestRuntimePermission);
                    }
                }
                if (!requestRuntimePermissionsArrayList.isEmpty ()) {
                    requestPermissions (requestRuntimePermissionsArrayList.toArray (new String[ 0 ]), 1);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
                PowerManager powerManager = null;
                try {
                    powerManager = (PowerManager) getContext().getSystemService (Context.POWER_SERVICE);
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
                if (powerManager != null) {
                    if (powerManager.isIgnoringBatteryOptimizations (getContext().getPackageName ())) {
                        LogD (TAG, "2. Request ignore battery optimizations (\"1.\" alternative; with package URI) - Entire application: Enabled");
                    } else {
                        LogD (TAG, "2. Request ignore battery optimizations (\"1.\" alternative; with package URI) - Entire application: Not enabled");
                        Intent intent = RequestIgnoreBatteryOptimizationsUtil.getRequestIgnoreBatteryOptimizationsIntent (getContext());
                        if (intent != null) {
                            startActivityForResult (intent, 2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy () {
        super.onDestroy ();
        LogD (TAG, "Activity destroy");
        if (sharedPreferences != null) {
            sharedPreferences = null;
        }

    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults.length == permissions.length) {
                    boolean allGranted = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            allGranted = false;
                        }
                    }
                    if (allGranted) {
                        LogI (TAG, "All requested permissions are granted");
                    } else {
                        LogD (TAG, "Not all requested permissions are granted");
                        AlertDialog.Builder builder = new AlertDialog.Builder (getContext());
                        builder.setTitle (getString (R.string.runtime_permissions_not_granted_title));
                        builder.setMessage (getString (R.string.runtime_permissions_not_granted_message));
                        builder.setNeutralButton (android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss ());
                        AlertDialog alertDialog = builder.create ();
                        alertDialog.show ();
                    }
                }
                break;
        }
    }

}