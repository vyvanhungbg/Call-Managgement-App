package com.franky.callmanagement.activities;




import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;


import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.Sort;

import static com.franky.callmanagement.utils.LogUtil.LogD;
import static com.franky.callmanagement.utils.LogUtil.LogE;
import static com.franky.callmanagement.utils.LogUtil.LogI;

import com.franky.callmanagement.R;
import com.franky.callmanagement.databinding.ActivityCallBinding;
import com.franky.callmanagement.interfaces.ICallActivityListener;
import com.franky.callmanagement.models.CallObject;
import com.franky.callmanagement.presenters.CallActivityPresenter;
import com.franky.callmanagement.utils.ResourceUtil;

/**
 * The type Call activity.
 */
public class CallActivity extends AppCompatActivity implements ICallActivityListener {
    private static final String TAG = CallActivity.class.getSimpleName ();
    private ActivityCallBinding binding;
    private Realm mRealm = null;
    private MediaPlayer mMediaPlayer = null;
    private ImageView playImageButton;
    private SeekBar playSeekBar;
    private CircleImageView btnBack;


    private CallActivityPresenter presenter;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        LogD (TAG, "Activity call create");
        init();
        binding = DataBindingUtil.setContentView (this, R.layout.activity_call);


        playImageButton = findViewById (R.id.imgv_content_call_play_image_button);
        playSeekBar = findViewById (R.id.sb_call_play_seek_bar);
        btnBack = findViewById(R.id.crimv_action_bar_back);


        btnBack.setOnClickListener(view -> {
            finish();
        });

        try {
            mRealm = Realm.getDefaultInstance ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            e.printStackTrace ();
        }
        Intent intent = getIntent ();
        presenter.getDataFromAllCallRecyclerAdapter(mRealm, intent);

    }

    public void init(){
        presenter = new CallActivityPresenter(this);
    }

    private void getFileRecord(File file) {
        String path = file != null ? file.getPath () : null;
        boolean exists = false, isFile = false;
        if (file != null) {
            exists = file.exists ();
            isFile = file.isFile ();
        }
        if (path != null && !path.trim ().isEmpty ()) {
            if (exists && isFile) {
                playSeekBar.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
                    @Override
                    public void onProgressChanged (SeekBar seekBar, int i, boolean b) {
                        if (b) {
                            if (mMediaPlayer != null) {
                                mMediaPlayer.seekTo (i);
                            }
                            playSeekBar.setProgress (i);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch (SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch (SeekBar seekBar) {
                    }
                });

                // set sự kiện onclick pause, stop
                TextView playTimeElapsedTextView = findViewById (R.id.tv_content_call_play_time_elapsed);
                TextView playTimeRemainingTextView = findViewById (R.id.tv_content_call_play_time_remaining);
                playImageButton.setOnClickListener (view -> {
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying ()) {
                            mMediaPlayer.pause ();
                            playImageButton.setImageResource (R.drawable.ic_play);
                        } else {
                            mMediaPlayer.start ();
                            playImageButton.setImageResource (R.drawable.ic_pause);
                        }
                    } else {
                        playImageButton.setImageResource (R.drawable.ic_play);
                    }
                });
                SeekBar volumeSeekBar = findViewById (R.id.content_call_play_volume_seek_bar);
                volumeSeekBar.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
                    @Override
                    public void onProgressChanged (SeekBar seekBar, int i, boolean b) {
                        if (b) {
                            if (mMediaPlayer != null) {
                                mMediaPlayer.setVolume (i / 100f, i / 100f);
                                LogE(TAG,"volume "+(i / 100f));
                            }
                            volumeSeekBar.setProgress (i);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch (SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch (SeekBar seekBar) {
                    }
                });
                try {
                    mMediaPlayer = MediaPlayer.create (this, Uri.parse (path));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.setOnCompletionListener (mediaPlayer -> {
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying ()) {
                                mediaPlayer.pause ();
                                playImageButton.setImageResource (R.drawable.ic_play);
                            } else {
                                mediaPlayer.start ();
                                playImageButton.setImageResource (R.drawable.ic_pause);
                            }
                        } else {
                            playImageButton.setImageResource (R.drawable.ic_play);
                        }
                    });
                    mMediaPlayer.setOnInfoListener ((mp, what, extra) -> false);
                    mMediaPlayer.setOnErrorListener ((mp, what, extra) -> false);
                    mMediaPlayer.seekTo (0);
                   // mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                    mMediaPlayer.setVolume (1.0f, 1.0f);
                    playSeekBar.setMax (mMediaPlayer.getDuration ());
                    Handler handler = new Handler ();
                    runOnUiThread (new Runnable () {
                        @Override
                        public void run () {
                            if (mMediaPlayer != null) {
                                int currentPosition = mMediaPlayer.getCurrentPosition ();
                                playSeekBar.setProgress (currentPosition);
                                String elapsedTime;
                                int minElapsed = currentPosition / 1000 / 60;
                                int secElapsed = currentPosition / 1000 % 60;
                                elapsedTime = minElapsed + ":";
                                if (secElapsed < 10) {
                                    elapsedTime += "0";
                                }
                                elapsedTime += secElapsed;
                                playTimeElapsedTextView.setText (elapsedTime);
                                String remainingTime;
                                int minRemaining = (playSeekBar.getMax () - currentPosition) / 1000 / 60;
                                int secRemaining = (playSeekBar.getMax () - currentPosition) / 1000 % 60;
                                remainingTime = minRemaining + ":";
                                if (secRemaining < 10) {
                                    remainingTime += "0";
                                }
                                remainingTime += secRemaining;
                                playTimeRemainingTextView.setText (remainingTime);
                            }
                            handler.postDelayed (this, 1000);
                        }
                    });
                }
            }
        }
    }

    @Nullable
    private Bitmap getImageInfoFromContacts(CallObject callObject) {
        String phoneNumber = callObject.getPhoneNumber();
        Bitmap imageBitmap = null;
        if (phoneNumber == null ){
            return null;

        }
        if(phoneNumber.trim().isEmpty()){  // check trim() bị lỗi khi phone == null
            return null;
        }
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                        if (id != null && !id.trim().isEmpty()) {
                            InputStream inputStream = null;
                            try {
                                inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(id)));
                            } catch (Exception e) {
                                LogE(TAG, e.getMessage());
                                e.printStackTrace();
                            }
                            if (inputStream != null) {
                                Bitmap bitmap = null;
                                try {
                                    bitmap = BitmapFactory.decodeStream(inputStream);
                                } catch (Exception e) {
                                    LogE(TAG, e.getMessage());
                                    e.printStackTrace();
                                }
                                if (bitmap != null) {
                                    imageBitmap = ResourceUtil.getBitmapClippedCircle(bitmap);
                                }
                            }
                        }
                    }
                    cursor.close();
                }
            }
        } catch (Exception e) {
            LogE(TAG, e.getMessage());
            LogE(TAG, e.toString());
            e.printStackTrace();
        }

        return imageBitmap;
    }



    public void display(CallObject callObject){

        // set têm
        String nameOfUser = callObject.getCorrespondentName();
        binding.tvNameUser.setText(nameOfUser !=null ? nameOfUser : getString(R.string.unknown_number));

        // set số điện thoại
        String phoneNumber = callObject.getPhoneNumber();
        binding.tvPhoneNumberInActivityCall.setText(phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber : getString(R.string.unknown_number));
        // set ảnh đại diện
        Bitmap bitmap = getImageInfoFromContacts(callObject);
        if(bitmap != null){
            binding.crimvImageOfUser.setImageBitmap(bitmap);
        }

        String beginTimeDate = null, endTimeDate = null;

        if (!DateFormat.is24HourFormat (this)) {
            try {
                beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (callObject.getBeginTimestamp ()));
                endTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (callObject.getEndTimestamp ()));

            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                e.printStackTrace ();
            }
        } else {
            try {
                beginTimeDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date(callObject.getBeginTimestamp()));
                endTimeDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date(callObject.getEndTimestamp()));
            } catch (Exception e) {
                LogE(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        binding.tvBeginTimeDateInActivityCall.setText(beginTimeDate!=null? beginTimeDate : "N/A");
        binding.tvEndTimeDateInActivityCall.setText(endTimeDate!=null? endTimeDate : "N/A");
            // thời gian cuộc gọi
            String durationTime = presenter.getTimeDurationCall(callObject,this);
            if(durationTime != null ){
                binding.tvTimeDurationInActivityCall.setText(durationTime);
            }
        // set file record
        File file = null;
        try {
            file = new File (callObject.getOutputFile ());
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        getFileRecord(file);


        // click button

        binding.linLayoutMakeAMessageInActivityCall.setOnClickListener(view -> presenter.makeAMessage(callObject,this));
        binding.linLayoutMakeAPhoneInActivityCall.setOnClickListener(view -> presenter.makeAPhoneCall(callObject,this));
        binding.linLayoutButtonFavoriteInActivityCall.setOnClickListener(view -> presenter.actionDeleteThisItem(mRealm,callObject,this));
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        LogD (TAG, "Activity destroy");
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            try {
                mMediaPlayer.reset ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            try {
                mMediaPlayer.release ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            mMediaPlayer = null;
        }

        if (mRealm != null) {
            if (!mRealm.isClosed ()) {
                try {
                    mRealm.close ();
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            mRealm = null;
        }

    }

    private Dialog getMissingDataDialog () {
        return new AlertDialog.Builder (this)
                .setTitle ("Cannot get call recording data")
                .setMessage ("Getting call recording data is not possible. Some data is missing at all.")
                .setNeutralButton (android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss ();
                    finish ();
                })
                .setCancelable (false)
                .create ();
    }



    @Override
    public void actionGetIntentFailed(String mess) {
        LogE(TAG,mess);
        getMissingDataDialog().show ();
    }

    @Override
    public void actionGetInComingObjectSuccess(boolean mIsIncoming, CallObject mIncomingCallObject) {
        display(mIncomingCallObject);
    }

    @Override
    public void actionGetOutgoingCallObjectSuccess(boolean mIsOutgoing, CallObject mOutgoingCallObject) {
        display(mOutgoingCallObject);
    }

    @Override
    public void actionDeleteCallObjectSuccess(String mess) {
        finish();
    }


}

