package com.franky.callmanagement.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.FragmentSettingsBinding;
import com.franky.callmanagement.env.AppConstants;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragmentCompat  {

    public static SettingsFragment newInstance() {
        
        Bundle args = new Bundle();
        
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource (R.xml.preferences, rootKey);
        Preference changeConsentInformationPreference = findPreference (AppConstants.FM_SP_CHANGE_CONSENT_INFORMATION);
    }
}