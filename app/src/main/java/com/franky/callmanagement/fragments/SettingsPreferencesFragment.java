package com.franky.callmanagement.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.franky.callmanagement.R;
import com.franky.callmanagement.env.AppConstants;

public class SettingsPreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource (R.xml.preferences, rootKey);
        Preference changeConsentInformationPreference = findPreference (AppConstants.FM_SP_CHANGE_CONSENT_INFORMATION);
    }
}
