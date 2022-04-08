package com.franky.callmanagement.utils;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FullBackupDataOutput;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class CustomBackupAgent extends BackupAgent {
    @Override
    public void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor1) throws IOException {

    }

    @Override
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {

    }

    @Override
    public void onFullBackup(FullBackupDataOutput data) throws IOException {
        super.onFullBackup(data);
    }
}
