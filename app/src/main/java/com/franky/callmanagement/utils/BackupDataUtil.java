package com.franky.callmanagement.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.core.os.ParcelableCompatCreatorCallbacks;

import com.franky.callmanagement.env.AppConfig;
import com.franky.callmanagement.env.AppConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;

public class BackupDataUtil {

    private static final String AUTHORITY = "com.franky.callmanagement.fileprovider";

    public void exportDatabase(Context context) {

        // init realm
        Realm realm = Realm.getDefaultInstance();

        File exportRealmFile = null;
        // get or create an "export.realm" file
        exportRealmFile = new File(context.getExternalCacheDir(), "export.realm");
        Log.e("PATH", AppConstants.FM_KEY_RECORDS_OUTPUT_LOCATION);
        Log.e("PATH internal", AppConstants.sFilesDirPathMemory);
        Log.e("PATH external", AppConstants.sExternalFilesDirPathMemory);
        // if "export.realm" already exists, delete
        exportRealmFile.delete();

        // copy current realm to "export.realm"
        realm.writeCopyTo(exportRealmFile);

        realm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, AppConfig.EMAIL_NAME);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Export realm data");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_ASSIST_PACKAGE,"Hùng");
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        context.startActivity(Intent.createChooser(intent, "Export realm data"));
    }

    public static void exportFileAudio(Context context){
        File file = new File(AppConstants.sExternalFilesDirPathMemory);
        File fileInternal = new File(AppConstants.sFilesDirPathMemory);
        Log.e("Path external",file.getPath());
        Log.e("Path internal",fileInternal.getPath());
       /* List<File> folder = new ArrayList<>();
        for(File filePath : file.listFiles()){
            if(!file.isDirectory()){
                folder.add(file);
            }
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_STREAM, (Parcelable) folder.get(0));
        intent.setType("text/plain");
        context.startActivity(intent);*/
        shareMultiple(Arrays.asList(Objects.requireNonNull(file.listFiles())),context);
    }

    public static void shareMultiple(List<File> files, Context context){

        ArrayList<Uri> uris = new ArrayList<>();
        for(File file: files){
            uris.add(Uri.fromFile(file));
        }
        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(intent, "Share"));
    }

    public static void shareFile(Context context,String path){  // path or
        //File fileExternal = new File(AppConstants.sExternalFilesDirPathMemory);
       // File fileInternal = new File(AppConstants.sFilesDirPathMemory);
        File folder = new File(path);

        ArrayList<Uri> uris = new ArrayList<>();
        for(File file: Objects.requireNonNull(folder.listFiles())){
            uris.add(FileProvider.getUriForFile(context, AUTHORITY, file));
        }

        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(intent, "Share"));
    }
   /* E/Path external: /storage/emulated/0/Android/data/com.franky.callmanagement/files
    E/Path internal: /data/user/0/com.franky.callmanagement/files*/

}
