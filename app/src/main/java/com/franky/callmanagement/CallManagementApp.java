package com.franky.callmanagement;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.os.Build;

import com.franky.callmanagement.env.AppConstants;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

public class CallManagementApp extends Application {

    private static final String TAG = CallManagementApp.class.getSimpleName();
    private final RealmMigration mRealmMigration = (realm, oldVersion, newVersion) -> {
    };

    @Override
    public void onCreate () {
        AppConstants.sFilesDirMemory = getFilesDir ();
        AppConstants.sFilesDirPathMemory = getFilesDir ().getPath ();
        AppConstants.sCacheDirMemory = getCacheDir ();
        AppConstants.sCacheDirPathMemory = getCacheDir ().getPath ();
        try {
            AppConstants.sExternalFilesDirMemory = getExternalFilesDir (null);
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        try {
            AppConstants.sExternalFilesDirPathMemory = Objects.requireNonNull (getExternalFilesDir (null)).getPath ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        AppConstants.sExternalCacheDirMemory = getExternalCacheDir ();
        AppConstants.sExternalCacheDirPathMemory = Objects.requireNonNull (getExternalCacheDir ()).getPath ();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AppConstants.sProcessName = getProcessName ();
        }
        super.onCreate ();
        LogD (TAG, "Application create");
        Realm.init (this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder ()
                .migration (mRealmMigration)
                .build ();
        LogD (TAG, "Realm configuration schema version: " + realmConfiguration.getSchemaVersion ());
        Realm.setDefaultConfiguration (realmConfiguration);
    }

    @Override
    public void onTrimMemory (int level) {
        super.onTrimMemory (level);
        LogD (TAG, "Application trim memory");
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                LogD (TAG, "Application trim memory: Running moderate");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                LogD (TAG, "Application trim memory: Running low");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                LogD (TAG, "Application trim memory: Running critical");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                LogD (TAG, "Application trim memory: UI hidden");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                LogD (TAG, "Application trim memory: Background");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                LogD (TAG, "Application trim memory: Moderate");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                LogD (TAG, "Application trim memory: Complete");
                if (AppConstants.sFilesDirMemory == null) {
                    AppConstants.sFilesDirMemory = getFilesDir ();
                }
                if (AppConstants.sFilesDirPathMemory == null) {
                    AppConstants.sFilesDirPathMemory = getFilesDir ().getPath ();
                }
                if (AppConstants.sCacheDirMemory == null) {
                    AppConstants.sCacheDirMemory = getCacheDir ();
                }
                if (AppConstants.sCacheDirPathMemory == null) {
                    AppConstants.sCacheDirPathMemory = getCacheDir ().getPath ();
                }
                if (AppConstants.sExternalFilesDirMemory == null) {
                    try {
                        AppConstants.sExternalFilesDirMemory = getExternalFilesDir (null);
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                }
                if (AppConstants.sExternalFilesDirPathMemory == null) {
                    try {
                        AppConstants.sExternalFilesDirPathMemory = Objects.requireNonNull (getExternalFilesDir (null)).getPath ();
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                }
                if (AppConstants.sExternalCacheDirMemory == null) {
                    AppConstants.sExternalCacheDirMemory = getExternalCacheDir ();
                }
                if (AppConstants.sExternalCacheDirPathMemory == null) {
                    AppConstants.sExternalCacheDirPathMemory = Objects.requireNonNull (getExternalCacheDir ()).getPath ();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (AppConstants.sProcessName == null) {
                        AppConstants.sProcessName = getProcessName ();
                    }
                }
                break;
        }
    }
}
