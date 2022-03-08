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
import com.franky.callmanagement.models.CallObject;
import com.franky.callmanagement.utils.ResourceUtil;

/**
 * The type Call activity.
 */
public class CallActivity extends AppCompatActivity {
    private static final String TAG = CallActivity.class.getSimpleName ();
    private ActivityCallBinding binding;
    private boolean mIsIncoming = false;
    private boolean mIsOutgoing = false;
    private Realm mRealm = null;
    private CallObject mIncomingCallObject = null;
    private CallObject mOutgoingCallObject = null;
    private MediaPlayer mMediaPlayer = null;
    private ImageView playImageButton;
    private boolean adShowed = false;
    private CircleImageView btnBack;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        LogD (TAG, "Activity create");
        //setStatusBarGradiant (this);
        binding = DataBindingUtil.setContentView (this, R.layout.activity_call);

        playImageButton = findViewById (R.id.content_call_play_image_button);
        btnBack = findViewById(R.id.crimv_action_bar_back);
        btnBack.setOnClickListener(view -> {
            finish();
        });
        Intent intent = getIntent ();
        long beginTimestamp = 0L, endTimestamp = 0L;
        if (intent != null) {
            if (intent.hasExtra ("mType") && Objects.equals (intent.getStringExtra ("mType"), "incoming")) {
                mIsIncoming = true;
            }
            if (intent.hasExtra ("mType") && Objects.equals (intent.getStringExtra ("mType"), "outgoing")) {
                mIsOutgoing = true;
            }
            if (mIsIncoming || mIsOutgoing) {
                if (intent.hasExtra ("mBeginTimestamp")) {
                    beginTimestamp = intent.getLongExtra ("mBeginTimestamp", 0L);
                }
                if (intent.hasExtra ("mEndTimestamp")) {
                    endTimestamp = intent.getLongExtra ("mEndTimestamp", 0L);
                }
            }
        }
        if (beginTimestamp == 0L || endTimestamp == 0L) {
            getMissingDataDialog ().show ();
            return;
        }
        try {
            mRealm = Realm.getDefaultInstance ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (mRealm != null && !mRealm.isClosed ()) {
            if (mIsIncoming) {
                mIncomingCallObject = mRealm.where (CallObject.class)
                        .equalTo ("mBeginTimestamp", beginTimestamp)
                        .equalTo ("mEndTimestamp", endTimestamp)
                        .sort ("mBeginTimestamp", Sort.DESCENDING)
                        .beginGroup ()
                        .equalTo ("type", "incoming")
                        .endGroup ()
                        .findFirst ();
            } else if (mIsOutgoing) {
                mOutgoingCallObject = mRealm.where (CallObject.class)
                        .equalTo ("mBeginTimestamp", beginTimestamp)
                        .equalTo ("mEndTimestamp", endTimestamp)
                        .sort ("mBeginTimestamp", Sort.DESCENDING)
                        .beginGroup ()
                        .equalTo ("type", "outgoing")
                        .endGroup ()
                        .findFirst ();
            }
        }
        mIsIncoming = mIsIncoming && mIncomingCallObject != null;
        mIsOutgoing = mIsOutgoing && mOutgoingCallObject != null;
        if (!mIsIncoming && !mIsOutgoing) {
            getMissingDataDialog ().show ();
            return;
        }
        if (intent.hasExtra ("mCorrespondentName")) {
            String correspondentName = intent.getStringExtra ("mCorrespondentName");
            binding.tvNameUser.setText(correspondentName);
            if (mIsIncoming) {
                mIncomingCallObject.setCorrespondentName (correspondentName);
            }
            if (mIsOutgoing) {
                mOutgoingCallObject.setCorrespondentName (correspondentName);
            }
        }
        if (mIsIncoming) {
            String phoneNumber = mIncomingCallObject.getPhoneNumber ();
            if (mIncomingCallObject.getCorrespondentName () == null) {
                if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
                    //title.setText (phoneNumber);
                } else {
                  //  title.setText (getString (R.string.unknown_number));
                }
            }
        } else if (mIsOutgoing) {
            String phoneNumber = mOutgoingCallObject.getPhoneNumber ();
            if (mOutgoingCallObject.getCorrespondentName () == null) {
                if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
                   // title.setText (phoneNumber);
                } else {
                   // title.setText (getString (R.string.unknown_number));
                }
            }
        }
       // TextView typeTextView = findViewById (R.id.content_call_type_text_view);
       // ImageView typeImageView = findViewById (R.id.content_call_type_image_view);
        String beginTimeDate = null, endTimeDate = null;
        if (mIsIncoming) {
            String phoneNumber = mIncomingCallObject.getPhoneNumber ();
            Bitmap imageBitmap = null;
            if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
                ((TextView) findViewById (R.id.content_call_number_text_view)).setText (phoneNumber);
                try {
                    if (ActivityCompat.checkSelfPermission (this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (phoneNumber));
                        Cursor cursor = getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup._ID}, null, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst ()) {
                                @SuppressLint("Range") String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
                                if (id != null && !id.trim ().isEmpty ()) {
                                    InputStream inputStream = null;
                                    try {
                                        inputStream = ContactsContract.Contacts.openContactPhotoInputStream (getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
                                    } catch (Exception e) {
                                        LogE (TAG, e.getMessage ());
                                        LogE (TAG, e.toString ());
                                        e.printStackTrace ();
                                    }
                                    if (inputStream != null) {
                                        Bitmap bitmap = null;
                                        try {
                                            bitmap = BitmapFactory.decodeStream (inputStream);
                                        } catch (Exception e) {
                                            LogE (TAG, e.getMessage ());
                                            LogE (TAG, e.toString ());
                                            e.printStackTrace ();
                                        }
                                        if (bitmap != null) {
                                            imageBitmap = ResourceUtil.getBitmapClippedCircle (bitmap);
                                        }
                                    }
                                }
                            }
                            cursor.close ();
                        }
                    }
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            } else {
                ((TextView) findViewById (R.id.content_call_number_text_view)).setText (getString (R.string.unknown_number));
            }
          //  typeTextView.setText (getString (R.string.incoming_call_record));
            if (imageBitmap != null) {
             binding.crimvImageOfUser.setImageBitmap (imageBitmap);
            } else {
              //  typeImageView.setImageDrawable (ResourceUtil.getDrawable (this, R.drawable.ic_incoming));
              //  typeImageView.setColorFilter (ContextCompat.getColor (getApplicationContext (), R.color.cp_6), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            if (!DateFormat.is24HourFormat (this)) {
                try {
                    beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mIncomingCallObject.getBeginTimestamp ()));
                    endTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mIncomingCallObject.getEndTimestamp ()));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            } else {
                try {
                    beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mIncomingCallObject.getBeginTimestamp ()));
                    endTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mIncomingCallObject.getEndTimestamp ()));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            String durationString = null;
            Date beginDate = new Date (mIncomingCallObject.getBeginTimestamp ());
            Date endDate = new Date (mIncomingCallObject.getEndTimestamp ());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Duration duration = Duration.between (beginDate.toInstant (), endDate.toInstant ());
                    long minutes = TimeUnit.SECONDS.toMinutes (duration.getSeconds ());
                    durationString = String.format (Locale.getDefault (), "%d min, %d sec",
                            minutes,
                            duration.getSeconds () - TimeUnit.MINUTES.toSeconds (minutes));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            } else {
                long durationMs = endDate.getTime () - beginDate.getTime ();
                try {
                    long minutes = TimeUnit.MILLISECONDS.toMinutes (durationMs);
                    durationString = String.format (Locale.getDefault (), "%d min, %d sec",
                            minutes,
                            TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (minutes));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            //durationString = durationString != null && !durationString.isEmpty () ? durationString : "N/A";
            //((TextView) findViewById (R.id.content_call_duration_text_view)).setText (durationString);

        } else if (mIsOutgoing) {
            String phoneNumber = mOutgoingCallObject.getPhoneNumber ();
            Bitmap imageBitmap = null;
            if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
                ((TextView) findViewById (R.id.content_call_number_text_view)).setText (phoneNumber);
                try {
                    if (ActivityCompat.checkSelfPermission (this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (phoneNumber));
                        Cursor cursor = getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup._ID}, null, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst ()) {
                                @SuppressLint("Range") String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
                                if (id != null && !id.trim ().isEmpty ()) {
                                    InputStream inputStream = null;
                                    try {
                                        inputStream = ContactsContract.Contacts.openContactPhotoInputStream (getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
                                    } catch (Exception e) {
                                        LogE (TAG, e.getMessage ());
                                        LogE (TAG, e.toString ());
                                        e.printStackTrace ();
                                    }
                                    if (inputStream != null) {
                                        Bitmap bitmap = null;
                                        try {
                                            bitmap = BitmapFactory.decodeStream (inputStream);
                                        } catch (Exception e) {
                                            LogE (TAG, e.getMessage ());
                                            LogE (TAG, e.toString ());
                                            e.printStackTrace ();
                                        }
                                        if (bitmap != null) {
                                            imageBitmap = ResourceUtil.getBitmapClippedCircle (bitmap);
                                        }
                                    }
                                }
                            }
                            cursor.close ();
                        }
                    }
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            } else {
                ((TextView) findViewById (R.id.content_call_number_text_view)).setText (getString (R.string.unknown_number));
            }
           // typeTextView.setText (getString (R.string.outgoing_call_record));
            if (imageBitmap != null) {
               binding.crimvImageOfUser.setImageBitmap (imageBitmap);
            } else {
              //  typeImageView.setImageDrawable (ResourceUtil.getDrawable (this, R.drawable.ic_outgoing));
              //  typeImageView.setColorFilter (ContextCompat.getColor (getApplicationContext (), R.color.cp_5), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            if (!DateFormat.is24HourFormat (this)) {
                try {
                    beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getBeginTimestamp ()));
                    endTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getEndTimestamp ()));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            } else {
                try {
                    beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getBeginTimestamp ()));
                    endTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getEndTimestamp ()));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
            String durationString = null;
            Date beginDate = new Date (mOutgoingCallObject.getBeginTimestamp ());
            Date endDate = new Date (mOutgoingCallObject.getEndTimestamp ());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Duration duration = Duration.between (beginDate.toInstant (), endDate.toInstant ());
                    long minutes = TimeUnit.SECONDS.toMinutes (duration.getSeconds ());
                    durationString = String.format (Locale.getDefault (), "%d min, %d sec",
                            minutes,
                            duration.getSeconds () - TimeUnit.MINUTES.toSeconds (minutes));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            } else {
                long durationMs = endDate.getTime () - beginDate.getTime ();
                try {
                    long minutes = TimeUnit.MILLISECONDS.toMinutes (durationMs);
                    durationString = String.format (Locale.getDefault (), "%d min, %d sec",
                            minutes,
                            TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (minutes));
                } catch (Exception e) {
                    LogE (TAG, e.getMessage ());
                    LogE (TAG, e.toString ());
                    e.printStackTrace ();
                }
            }
           // durationString = durationString != null && !durationString.isEmpty () ? durationString : "N/A";
          //  ((TextView) findViewById (R.id.content_call_duration_text_view)).setText (durationString);

        }
        TextView beginTimeDateTextView = findViewById (R.id.content_call_begin_time_date_text_view);
        beginTimeDateTextView.setText (beginTimeDate != null && !beginTimeDate.trim ().isEmpty () ? beginTimeDate : "N/A");
        TextView endTimeDateTextView = findViewById (R.id.content_call_end_time_date_text_view);
        endTimeDateTextView.setText (endTimeDate != null && !endTimeDate.trim ().isEmpty () ? endTimeDate : "N/A");
        float mainMargin = getResources ().getDimension (R.dimen._16sdp);
        File file = null;
        try {
            if (mIsIncoming) {
                file = new File (mIncomingCallObject.getOutputFile ());
            } else if (mIsOutgoing) {
                file = new File (mOutgoingCallObject.getOutputFile ());
            }
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        String path = file != null ? file.getPath () : null;
        boolean exists = false, isFile = false;
        if (file != null) {
            exists = file.exists ();
            isFile = file.isFile ();
        }
        if (path != null && !path.trim ().isEmpty ()) {
            if (exists && isFile) {
                SeekBar playSeekBar = findViewById (R.id.content_call_play_seek_bar);
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
                TextView playTimeElapsedTextView = findViewById (R.id.content_call_play_time_elapsed);
                TextView playTimeRemainingTextView = findViewById (R.id.content_call_play_time_remaining);
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
                    mMediaPlayer.setVolume (0.5f, 0.5f);
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
        if (mIncomingCallObject != null) {
            mIncomingCallObject = null;
        }
        if (mOutgoingCallObject != null) {
            mOutgoingCallObject = null;
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
        if (mIsIncoming) {
            mIsIncoming = false;
        }
        if (mIsOutgoing) {
            mIsOutgoing = false;
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

    private void makePhoneCall () {
        LogI (TAG, "Make phone call");
        String phoneNumber = null;
        if (mIsIncoming && mIncomingCallObject != null) {
            phoneNumber = mIncomingCallObject.getPhoneNumber ();
        } else if (mIsOutgoing && mOutgoingCallObject != null) {
            phoneNumber = mOutgoingCallObject.getPhoneNumber ();
        }
        if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()
                && ActivityCompat.checkSelfPermission (this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                startActivity (new Intent (Intent.ACTION_DIAL, Uri.fromParts ("tel", phoneNumber, null)));
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
        } else {
            new AlertDialog.Builder (this)
                    .setTitle ("Cannot make phone call")
                    .setMessage ("Making phone call to this correspondent is not possible.")
                    .setNeutralButton (android.R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss ();
                    })
                    .create ().show ();
        }
    }

    private void delete () {
        LogI (TAG, "Delete");
        new AlertDialog.Builder (this)
                .setTitle ("Delete call recording")
                .setMessage ("Are you sure you want to delete this call recording (and its audio file)? Data cannot be recovered.")
                .setPositiveButton (R.string.yes, (dialogInterface, i) -> {
                    dialogInterface.dismiss ();
                    Realm realm = null;
                    try {
                        realm = Realm.getDefaultInstance ();
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }
                    if (realm != null && !realm.isClosed ()) {
                        try {
                            realm.beginTransaction ();
                            if (mIsIncoming && mIncomingCallObject != null) {
                                CallObject incomingCallObject1 = realm.where (CallObject.class)
                                        .equalTo ("mBeginTimestamp", mIncomingCallObject.getBeginTimestamp ())
                                        .equalTo ("mEndTimestamp", mIncomingCallObject.getEndTimestamp ())
                                        .beginGroup ()
                                        .equalTo ("type", "incoming")
                                        .endGroup ()
                                        .findFirst ();
                                if (incomingCallObject1 != null) {
                                    File outputFile = null;
                                    try {
                                        outputFile = new File (incomingCallObject1.getOutputFile ());
                                    } catch (Exception e) {
                                        LogE (TAG, e.getMessage ());
                                        LogE (TAG, e.toString ());
                                        e.printStackTrace ();
                                    }
                                    if (outputFile != null) {
                                        if (outputFile.exists () && outputFile.isFile ()) {
                                            try {
                                                outputFile.delete ();
                                            } catch (Exception e) {
                                                LogE (TAG, e.getMessage ());
                                                LogE (TAG, e.toString ());
                                                e.printStackTrace ();
                                            }
                                        }
                                    }
                                    incomingCallObject1.deleteFromRealm ();
                                    realm.commitTransaction ();
                                    Toast.makeText (this, "Call recording is deleted", Toast.LENGTH_SHORT).show ();
                                    finish ();
                                } else {
                                    realm.cancelTransaction ();
                                    Toast.makeText (this, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                                }
                            } else if (mIsOutgoing && mOutgoingCallObject != null) {
                                CallObject outgoingCallObject1 = realm.where (CallObject.class)
                                        .equalTo ("mBeginTimestamp", mOutgoingCallObject.getBeginTimestamp ())
                                        .equalTo ("mEndTimestamp", mOutgoingCallObject.getEndTimestamp ())
                                        .beginGroup ()
                                        .equalTo ("type", "outgoing")
                                        .endGroup ()
                                        .findFirst ();
                                if (outgoingCallObject1 != null) {
                                    File outputFile = null;
                                    try {
                                        outputFile = new File (outgoingCallObject1.getOutputFile ());
                                    } catch (Exception e) {
                                        LogE (TAG, e.getMessage ());
                                        LogE (TAG, e.toString ());
                                        e.printStackTrace ();
                                    }
                                    if (outputFile != null) {
                                        if (outputFile.exists () && outputFile.isFile ()) {
                                            try {
                                                outputFile.delete ();
                                            } catch (Exception e) {
                                                LogE (TAG, e.getMessage ());
                                                LogE (TAG, e.toString ());
                                                e.printStackTrace ();
                                            }
                                        }
                                    }
                                    outgoingCallObject1.deleteFromRealm ();
                                    realm.commitTransaction ();
                                    Toast.makeText (this, "Call recording is deleted", Toast.LENGTH_SHORT).show ();
                                    finish ();
                                } else {
                                    realm.cancelTransaction ();
                                    Toast.makeText (this, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                                }
                            } else {
                                realm.cancelTransaction ();
                                Toast.makeText (this, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                            }
                            realm.close ();
                        } catch (Exception e) {
                            LogE (TAG, e.getMessage ());
                            LogE (TAG, e.toString ());
                            e.printStackTrace ();
                        }
                    } else {
                        Toast.makeText (this, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                    }
                })
                .setNegativeButton (R.string.no, (dialogInterface, i) -> dialogInterface.dismiss ())
                .create ().show ();
    }
}

