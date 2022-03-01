package com.franky.callmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.telecom.Call;
import android.view.MenuItem;
import android.view.View;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.ActivityMainBinding;
import com.franky.callmanagement.fragments.AllCallFragment;
import com.franky.callmanagement.fragments.CallStatisticsFragment;
import com.franky.callmanagement.fragments.HomeFragment;
import com.franky.callmanagement.fragments.SettingsFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;


    private final Fragment homeFragment = HomeFragment.newInstance();
    private final Fragment allCallFragment = AllCallFragment.newInstance();
    private final Fragment callStatisticsFragment = CallStatisticsFragment.newInstance();
    private final Fragment settingsFragment = SettingsFragment.newInstance();
    private Fragment activeFragment = homeFragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        customFragmentManager();
        onClickNavigationBar();
    }

    private void customFragmentManager(){
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container,allCallFragment,allCallFragment.getTag()).hide(allCallFragment)
                .add(R.id.fragment_container,callStatisticsFragment,callStatisticsFragment.getTag()).hide(callStatisticsFragment)
                .add(R.id.fragment_container,settingsFragment,settingsFragment.getTag()).hide(settingsFragment)
                .add(R.id.fragment_container,homeFragment,homeFragment.getTag())
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .commit();
    }

    private void showFragment(Fragment  fragment){
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }

    private void onClickNavigationBar(){
        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_home:
                        showFragment(homeFragment);
                        break;

                    case R.id.action_all_call:
                        showFragment(allCallFragment);
                        break;

                    case R.id.action_statistics:
                        showFragment(callStatisticsFragment);
                        break;

                    case R.id.action_settings:
                        showFragment(settingsFragment);
                        break;
                }
                return true;
            }
        });
    }

}