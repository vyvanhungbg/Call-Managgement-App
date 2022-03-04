package com.franky.callmanagement.adapters;



import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.franky.callmanagement.R;
import com.franky.callmanagement.env.AppConstants;
import com.franky.callmanagement.models.CallObject;
import com.franky.callmanagement.utils.ResourceUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class AllCallRecyclerAdapter extends RecyclerView.Adapter<AllCallRecyclerAdapter.ViewHolder> {
    private static final String TAG = AllCallRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_HEADER = 0 , VIEW_ITEM = 1;
    private List<CallObject> objectList, objectFilteredList;
    private boolean readContacts = false;

    public AllCallRecyclerAdapter(List<CallObject> mObjectList, boolean readContacts) {
        this.objectList = mObjectList;
        this.objectFilteredList = mObjectList;
        this.readContacts = readContacts;

        LogE(TAG,"View adapter"+objectList.size()+" "+objectFilteredList.size());
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ViewHolder viewHolder;
        if (viewType == VIEW_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_view_header, parent, false);
            viewHolder = new HeaderViewHolder (view);
            LogE(TAG,"View header");
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call, parent, false);
            viewHolder = new ItemViewHolder (view);
            LogE(TAG,"View type");
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return objectFilteredList.get (position).getIsHeader() ? VIEW_HEADER : VIEW_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CallObject callObject = objectFilteredList.get(position);
            if(callObject != null){
                if(holder instanceof HeaderViewHolder){
                    HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                    headerViewHolder.titleTextView.setText(callObject.getHeaderTitle());
                }else {
                    ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                    String phoneNumber = callObject.getPhoneNumber();
                    if(!phoneNumber.trim().isEmpty() && phoneNumber!=null){
                        // lấy thông tin từ đanh bạ
                        if (readContacts) {
                            try {
                                Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (phoneNumber));
                                Cursor cursor = holder.itemView.getContext ().getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
                                if (cursor != null) {
                                    if (cursor.moveToFirst ()) {
                                        @SuppressLint("Range") String tempDisplayName = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup.DISPLAY_NAME));
                                        if (tempDisplayName != null && !tempDisplayName.trim ().isEmpty ()) {
                                            callObject.setCorrespondentName (phoneNumber = tempDisplayName);
                                        }
                                        @SuppressLint("Range") String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
                                        if (id != null && !id.trim ().isEmpty ()) {
                                            InputStream inputStream = null;
                                            try {
                                                inputStream = ContactsContract.Contacts.openContactPhotoInputStream (holder.itemView.getContext ().getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
                                            } catch (Exception e) {
                                                e.printStackTrace ();
                                            }
                                            if (inputStream != null) {
                                                Bitmap bitmap = null;
                                                try {
                                                    bitmap = BitmapFactory.decodeStream (inputStream);
                                                } catch (Exception e) {
                                                    e.printStackTrace ();
                                                }
                                                if (bitmap != null) {
                                                }
                                            }
                                        }
                                    }
                                    cursor.close ();
                                }
                            } catch (Exception e) {
                                e.printStackTrace ();
                            }
                        }
                        if(phoneNumber.equals(callObject.getPhoneNumber())){
                            itemViewHolder.numberTextView.setText (holder.itemView.getContext ().getString (R.string.unknown_number));
                        }else {
                            itemViewHolder.numberTextView.setText (phoneNumber);
                        }
                    }else {
                        itemViewHolder.numberTextView.setText (phoneNumber = holder.itemView.getContext ().getString (R.string.unknown_number));
                    }

                    // lấy time gọi
                    String beginDateTime = null;
                    if (!DateFormat.is24HourFormat (holder.itemView.getContext ())) {
                        try {
                            beginDateTime = new SimpleDateFormat("hh:mm a", Locale.getDefault ()).format (new Date(callObject.getBeginTimestamp ()));
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    } else {
                        try {
                            beginDateTime = new SimpleDateFormat ("HH:mm", Locale.getDefault ()).format (new Date (callObject.getBeginTimestamp ()));
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }

                    // time gọi
                    String durationString = null;
                    Date beginDate = new Date (callObject.getBeginTimestamp ());
                    Date endDate = new Date (callObject.getEndTimestamp ());
                    long durationMs = endDate.getTime () - beginDate.getTime ();
                    try {
                        durationString = String.format (Locale.getDefault (), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours (durationMs),
                                TimeUnit.MILLISECONDS.toMinutes (durationMs) - TimeUnit.HOURS.toMinutes (TimeUnit.MILLISECONDS.toHours (durationMs)),
                                TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (TimeUnit.MILLISECONDS.toMinutes (durationMs)));
                    } catch (Exception e) {
                        LogE (TAG, e.getMessage ());
                        LogE (TAG, e.toString ());
                        e.printStackTrace ();
                    }

                    //

                    itemViewHolder.beginDateTimeTextView.setText (beginDateTime != null ? beginDateTime + "\n(" + durationString + ")" : "N/A");
                    String finalCorrespondent = phoneNumber;
                   // itemViewHolder.menuImageButton.setOnClickListener (view -> showItemMenuDialog (holder.itemView.getContext (), callObject, finalCorrespondent));
                    if (callObject.getType ().equals ("incoming")) {
                        itemViewHolder.imageView.setImageResource (R.drawable.ic_incoming);
                        itemViewHolder.imageView.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.cp_6), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                    if (callObject.getType ().equals ("outgoing")) {
                        itemViewHolder.imageView.setImageResource (R.drawable.ic_outgoing);
                        itemViewHolder.imageView.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.cp_5), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                    itemViewHolder.tvPhoneNumer.setText (String.valueOf (callObject.getPhoneNumber ()));
                    if (callObject.isFavourit ()) {
                        itemViewHolder.ivFavourit.setImageResource (R.drawable.ic_favorit);
                    } else {
                        itemViewHolder.ivFavourit.setImageResource (R.drawable.ic_favourit_stroke);
                    }
                    itemViewHolder.ivFavourit.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.cp_4), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }


    }


    private boolean showItemMenuDialog (@NonNull Context context, @NonNull CallObject incomingCallObject, @NonNull String correspondent) {
        CharSequence[] menuItems = {incomingCallObject.isFavourit () ? "UnFavourite" : "Favourite", "Open recording", "Make phone call", "Delete"};
        Drawable drawable;
        String stitle = "";
        if (incomingCallObject.getType ().equals ("incoming")) {
            drawable = ResourceUtil.getDrawable (context, R.drawable.ic_incoming);
            stitle = "Incoming call";
        } else {
            drawable = ResourceUtil.getDrawable (context, R.drawable.ic_outgoing);
            stitle = "Outgoing call";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint (ResourceUtil.getColor (context, R.color.main_color));
        } else {
            DrawableCompat.setTint (drawable, ResourceUtil.getColor (context, R.color.main_color));
        }
        Dialog dialog = new AlertDialog.Builder (context)
                .setIcon (drawable)
                .setTitle (stitle + " - " + correspondent)
                .setItems (menuItems, (dialogInterface, which) -> {
                    dialogInterface.dismiss ();
                    switch (which) {
                        case 0:
                            Realm realmf = null;
                            try {
                                realmf = Realm.getDefaultInstance ();
                            } catch (Exception e) {
                                e.printStackTrace ();
                            }
                            if (realmf != null && !realmf.isClosed ()) {
                                try {
                                    realmf.beginTransaction ();
                                    List<CallObject> incomingCallObjects = realmf.where (CallObject.class)
                                            .equalTo ("mPhoneNumber", incomingCallObject.getPhoneNumber ())
                                            .findAll ();
                                    if (incomingCallObjects != null) {
                                        for (CallObject callObject : incomingCallObjects) {
                                            callObject.setFavourit (!incomingCallObject.isFavourit ());
                                        }
                                        realmf.commitTransaction ();
                                    } else {
                                        realmf.cancelTransaction ();
                                        Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                                    }
                                    realmf.close ();
                                } catch (Exception e) {
                                    e.printStackTrace ();
                                }
                            } else {
                                Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                            }
                            break;
                        case 1:
                            openIncomingCall (context, incomingCallObject);
                            break;
                        case 2:
                            String phoneNumber = incomingCallObject.getPhoneNumber ();
                            if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()
                                    && ActivityCompat.checkSelfPermission (context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                context.startActivity (new Intent(Intent.ACTION_DIAL, Uri.fromParts ("tel", phoneNumber, null)));
                            } else {
                                new AlertDialog.Builder (context)
                                        .setTitle ("Cannot make phone call")
                                        .setMessage ("Making phone call to this correspondent is not possible.")
                                        .setNeutralButton (android.R.string.ok, (dialogInterface1, i) -> {
                                            dialogInterface1.dismiss ();
                                        })
                                        .create ().show ();
                            }
                            break;
                        case 3:
                            new AlertDialog.Builder (context)
                                    .setTitle ("Delete call recording")
                                    .setMessage ("Are you sure you want to delete this call recording (and its audio file)? Data cannot be recovered.")
                                    .setPositiveButton (R.string.yes, (dialogInterface1, i) -> {
                                        dialogInterface1.dismiss ();
                                        Realm realm = null;
                                        try {
                                            realm = Realm.getDefaultInstance ();
                                        } catch (Exception e) {
                                            e.printStackTrace ();
                                        }
                                        if (realm != null && !realm.isClosed ()) {
                                            try {
                                                realm.beginTransaction ();
                                                CallObject incomingCallObject1 = realm.where (CallObject.class)
                                                        .equalTo ("mBeginTimestamp", incomingCallObject.getBeginTimestamp ())
                                                        .equalTo ("mEndTimestamp", incomingCallObject.getEndTimestamp ())
                                                        .beginGroup ()
                                                        .equalTo ("type", incomingCallObject.getType ())
                                                        .endGroup ()
                                                        .findFirst ();
                                                if (incomingCallObject1 != null) {
                                                    File outputFile = null;
                                                    try {
                                                        outputFile = new File (incomingCallObject1.getOutputFile ());
                                                    } catch (Exception e) {
                                                        e.printStackTrace ();
                                                    }
                                                    if (outputFile != null) {
                                                        if (outputFile.exists () && outputFile.isFile ()) {
                                                            try {
                                                                outputFile.delete ();
                                                            } catch (Exception e) {
                                                                e.printStackTrace ();
                                                            }
                                                        }
                                                    }
                                                    incomingCallObject1.deleteFromRealm ();
                                                    realm.commitTransaction ();
                                                    Toast.makeText (context, "Call recording is deleted", Toast.LENGTH_SHORT).show ();
                                                } else {
                                                    realm.cancelTransaction ();
                                                    Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                                                }
                                                realm.close ();
                                            } catch (Exception e) {
                                                e.printStackTrace ();
                                            }
                                        } else {
                                            Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
                                        }
                                    })
                                    .setNegativeButton (R.string.no, (dialogInterface1, i) -> dialogInterface1.dismiss ())
                                    .create ().show ();
                            break;
                    }
                }).create ();
        dialog.show ();
        return dialog.isShowing ();
    }


    private void openIncomingCall (@NonNull Context context, @NonNull CallObject incomingCallObject) {
//        Intent intent = new Intent (context, CallActivity.class);
//        intent.putExtra (AppConstants.INTENT_ACTION_INCOMING_CALL, true);
//        intent.putExtra ("mBeginTimestamp", incomingCallObject.getBeginTimestamp ());
//        intent.putExtra ("mEndTimestamp", incomingCallObject.getEndTimestamp ());
//        intent.putExtra ("mType", incomingCallObject.getType ());
//        if (incomingCallObject.getCorrespondentName () != null && !incomingCallObject.getCorrespondentName ().trim ().isEmpty ()) {
//            intent.putExtra ("mCorrespondentName", incomingCallObject.getCorrespondentName ());
//        }
//        try {
//            context.startActivity (intent);
//        } catch (Exception e) {
//            e.printStackTrace ();
//        }
    }


    @Override
    public int getItemCount() {
        return objectFilteredList != null ? objectFilteredList.size () : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class HeaderViewHolder extends ViewHolder {
        /**
         * The Title text view.
         */
        TextView titleTextView;

        /**
         * Instantiates a new Header view holder.
         *
         * @param itemView the item view
         */
        public HeaderViewHolder (@NonNull View itemView) {
            super (itemView);
            titleTextView = itemView.findViewById (R.id.adapter_view_header_title);
        }
    }

    class ItemViewHolder extends ViewHolder {
        /**
         * The Image view.
         */
        ImageView imageView;
        /**
         * The Number text view.
         */
        TextView numberTextView, /**
         * The Begin date time text view.
         */
        beginDateTimeTextView;
        /**
         * The Menu image button.
         */
        ImageButton menuImageButton;
        /**
         * The Tv phone numer.
         */
        TextView tvPhoneNumer;
        /**
         * The Iv favourit.
         */
        ImageView ivFavourit;

        /**
         * Instantiates a new Item view holder.
         *
         * @param itemView the item view
         */
        public ItemViewHolder (@NonNull View itemView) {
            super (itemView);
            imageView = itemView.findViewById (R.id.iv_phonenumber);
            numberTextView = itemView.findViewById (R.id.adapter_item_number);
            beginDateTimeTextView = itemView.findViewById (R.id.adapter_item_begin_date_time);
            menuImageButton = itemView.findViewById (R.id.adapter_item_menu);
            tvPhoneNumer = itemView.findViewById (R.id.tv_phonenumber);
            ivFavourit = itemView.findViewById (R.id.tv_favourit);
        }
    }
}
