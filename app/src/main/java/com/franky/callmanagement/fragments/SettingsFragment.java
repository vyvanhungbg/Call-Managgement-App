package com.franky.callmanagement.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.FragmentSettingsBinding;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.utils.BackupDataUtil;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment  {
    private FragmentSettingsBinding binding;

    public static SettingsFragment newInstance() {
        
        Bundle args = new Bundle();
        
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_settings, container, false);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment childFragment = new SettingsPreferencesFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.settings, childFragment).commit();
        binding.imgvBackupInFragmentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuPopupBackup();
            }
        });
    }

    public void MenuPopupBackup(){
        PopupMenu popupMenu = new PopupMenu(getContext(), binding.imgvBackupInFragmentSettings);
        popupMenu.inflate(R.menu.layout_popup_menu_setting);

        Menu menu = popupMenu.getMenu();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               return menuItemClicked(menuItem);
            }
        });

        popupMenu.show();
    }

    private boolean menuItemClicked(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_setting_auto_backup:
                Toast.makeText(getContext(),"Auto backup",Toast.LENGTH_LONG).show();break;
            case R.id.menu_setting_manual_backup:
                Toast.makeText(getContext(),"Chọn vị trí tệp lưu",Toast.LENGTH_LONG).show();break;
            case  R.id.menu_setting_manual_backup_external_memory:
                BackupDataUtil.shareFile(getContext(),AppConstants.sExternalFilesDirPathMemory);break;
            case  R.id.menu_setting_manual_backup_internal_memory:
                BackupDataUtil.shareFile(getContext(),AppConstants.sFilesDirPathMemory);break;
        }
        return true;
    }
}