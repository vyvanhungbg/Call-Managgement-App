package com.franky.callmanagement.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.franky.callmanagement.env.AppConfig;

import java.io.File;

import io.realm.Realm;

public class BackupDataUtil {

    public void exportDatabase(Context context) {

        // init realm
        Realm realm = Realm.getDefaultInstance();

        File exportRealmFile = null;
        // get or create an "export.realm" file
        exportRealmFile = new File(context.getExternalCacheDir(), "export.realm");

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
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        context.startActivity(Intent.createChooser(intent, "Export realm data"));
    }

}
