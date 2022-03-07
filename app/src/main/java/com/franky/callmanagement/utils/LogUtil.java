package com.franky.callmanagement.utils;

import android.util.Log;

import androidx.core.content.ContextCompat;

import com.franky.callmanagement.env.AppConfig;

public class LogUtil {
    private static final String TAG = LogUtil.class.getSimpleName();

    
    // ghi lại luồng chương trình
    public static void LogD(String tag, String message){
        int numberOfLine = Thread.currentThread().getStackTrace()[2].getLineNumber();
        if(tag == null || message ==null){
            LogE(TAG, getLineCurrent(numberOfLine)+"TAG or message of class "+TAG+" is null");
            return;
        }
        if(tag.length()>22){
            tag = tag.substring(0,22);
        }
        if(AppConfig.ENABLE_LOG ){
            Log.d(tag, getLineCurrent(numberOfLine) + message);
        }
    }

    // ghi lại lỗi chương trình
    public static void LogE(String tag, String message){
        int numberOfLine = Thread.currentThread().getStackTrace()[2].getLineNumber();
        if(tag == null || message ==null){
            LogE(TAG, Thread.currentThread().getStackTrace()[2].getLineNumber()+"TAG or message of class "+TAG+" is null");
            return;
        }

        if(tag.length()>22){
            tag = tag.substring(0,22);
        }
        if(AppConfig.ENABLE_LOG){
            Log.e(tag,Thread.currentThread().getStackTrace()[2].getLineNumber() + message);
        }
    }

    
    // ghi lại các quá tình thành công
    public static void LogI ( String tag, String message) {
        int numberOfLine = Thread.currentThread().getStackTrace()[2].getLineNumber();
        if(tag == null || message ==null){
            LogE(TAG, getLineCurrent(numberOfLine)+"TAG or message of class "+TAG+" is null");
            return;
        }

        if(tag.length()>22){
            tag = tag.substring(0,22);
        }
        if (AppConfig.ENABLE_LOG) {
            Log.i (tag,getLineCurrent(numberOfLine) + message);
        }
    }

    // ghi lại những phần xử lí không mong muốn , vd check version
    public static void LogW ( String tag, String message) {
        int numberOfLine = Thread.currentThread().getStackTrace()[2].getLineNumber();
        if(tag == null || message ==null){
            LogE(TAG, getLineCurrent(numberOfLine)+"TAG or message of class "+TAG+" is null");
            return;
        }

        if(tag.length()>22){
            tag = tag.substring(0,22);
        }
        if (AppConfig.ENABLE_LOG) {
            Log.w (tag,getLineCurrent(numberOfLine) + message);
        }
    }



    public static String getLineCurrent(int numberOfLine){
        final String str = "Code in line ";
        return str + numberOfLine +" has an error : ";
    }
}
