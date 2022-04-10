package com.franky.callmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telecom.Call;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.franky.callmanagement.CallManagementApp;
import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.ActivityMainBinding;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.fragments.AllCallFragment;
import com.franky.callmanagement.fragments.CallStatisticsFragment;
import com.franky.callmanagement.fragments.HomeFragment;
import com.franky.callmanagement.fragments.SettingsFragment;
import com.franky.callmanagement.services.MainService;
import com.franky.callmanagement.utils.AppUtil;
import com.franky.callmanagement.utils.BackupDataUtil;
import com.franky.callmanagement.utils.RequestIgnoreBatteryOptimizationsUtil;
import com.google.android.material.navigation.NavigationBarView;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;
import static com.franky.callmanagement.utils.LogUtil.LogI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
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
       // BackupDataUtil.shareFile(getApplicationContext(),AppConstants.sFilesDirPathMemory);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
       /* if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0) {
            // it is enabled
            LogE(TAG, "ENABLE _ BACKUP");
        } else {
            // it is disabled
            LogE(TAG, "DISABLE _ BACKUP");
        }*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG},101);
           // String tmp = intent.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);
            LogE (TAG, "-------phoneStateIncomingNumber "  );
        }else {
            LogE (TAG, "-------No Permistion READ CALL LOG 1" );

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG},101);
            // String tmp = intent.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);
            LogE (TAG, "Reqest permiss "  );
        }else {
            LogE (TAG, "-------No Reqest Permistion READ CALL LOG " );

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG},101);
            // String tmp = intent.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);
            LogE (TAG, "-------Suucess per mis phoneStateIncomingNumber "  );
        }else {
            LogE (TAG, "-------No Permistion READ CALL LOG " );

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 102);

        }
    }




    // Thêm các fragment vào fragmentManager
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
    public void createNotification(CharSequence title, CharSequence mess){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_app);
        Notification notification = new NotificationCompat.Builder(this, CallManagementApp.CHANNEL_ID_LOCAL_NOTIFICATIONS)
                .setContentTitle(title)
                .setContentText(mess)
                .setSmallIcon(R.drawable.ic_application)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(notificationId,notification);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        int notificationId = (int) new Date().getTime();
        notificationManagerCompat.notify(notificationId,notification);
    }



}