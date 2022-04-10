package com.franky.callmanagement.utils;

import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FullBackupDataOutput;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.franky.callmanagement.CallManagementApp;
import com.franky.callmanagement.R;
import com.franky.callmanagement.activities.MainActivity;

import java.io.IOException;
import java.util.Date;

public class CustomBackupAgent extends BackupAgent {
    @Override
    public void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor1) throws IOException {
        createNotification("Call Management","Chuẩn bị tiến hành sao lưu");
        LogE("Call Management","Chuẩn bị tiến hành sao lưu");
    }

    @Override
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        LogE("Call Management","Đang khôi phục dữ liệu");
        createNotification("Call Management","Đang khôi phục dữ liệu ");
    }

    @Override
    public void onQuotaExceeded(long backupDataBytes, long quotaBytes) {
        super.onQuotaExceeded(backupDataBytes, quotaBytes);
        createNotification("Call Management","Đã đạt tới giới hạn sao lưu");
        LogE("Call Management","Đã đạt tới giới hạn sao lưu");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        createNotification("Call Management","Sao lưu hoàn tất");
        LogE("Call Management","Sao lưu hoàn tất");
    }

    @Override
    public void onRestoreFinished() {
        super.onRestoreFinished();
        LogE("Call Management","Hoàn thành sao lưu dữ liệu cũ");
        createNotification("Call Management","Hoàn thành sao lưu dữ liệu cũ");
    }





    public void createNotification(CharSequence title, CharSequence mess){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_app);

        Notification notification = new NotificationCompat.Builder(this, CallManagementApp.CHANNEL_ID_LOCAL_NOTIFICATIONS)
                .setContentTitle(title)
                .setContentText(mess)
                .setSmallIcon(R.drawable.ic_app)
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
