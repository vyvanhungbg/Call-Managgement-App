package com.franky.callmanagement.fragments;

import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.franky.callmanagement.R;
import com.franky.callmanagement.adapters.AllCallRecyclerAdapter;
import com.franky.callmanagement.databinding.FragmentAllCallBinding;
import com.franky.callmanagement.interfaces.IAllCallListener;
import com.franky.callmanagement.models.CallObject;
import com.franky.callmanagement.presenters.AllCallPresenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
    Chứa các tất cả danh sách cuộc gọi
 */
public class AllCallFragment extends Fragment implements IAllCallListener {

    private static final String TAG = AllCallFragment.class.getSimpleName ();

    private FragmentAllCallBinding binding;
    private AllCallPresenter presenter;
    private Calendar calendar = Calendar.getInstance();
    private int isDaySelected ;
    private List<LinearLayout> layoutTimeLine;
    private String isMonthSelected ;

    private Realm mRealm = null;
    private RealmResults<CallObject> mCallObjectRealmResults = null;


    public static AllCallFragment newInstance() {
        AllCallFragment fragment = new AllCallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private Context getContextNonNull () {
        return Objects.requireNonNull (getContext ());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

    }

    private AllCallRecyclerAdapter populateAdapter (@NonNull Context context, @NonNull List<CallObject> callObjectList) {
        Calendar calendar = Calendar.getInstance ();
        int todayDayOfYear = calendar.get (Calendar.DAY_OF_YEAR), yesterdayDayOfYear = todayDayOfYear - 1;
        boolean hasToday = false, hasYesterday = false;
        List<CallObject> list = new ArrayList<> ();
        if (!callObjectList.isEmpty ()) {
            calendar.setTime (new Date(callObjectList.get (0).getBeginTimestamp ()));
            if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                hasToday = true;
            }
            if (hasToday) {
                list.add (new CallObject (true, context.getString (R.string.today)));
                for (Iterator<CallObject> iterator = callObjectList.iterator (); iterator.hasNext () ; ) {
                    CallObject incomingCallObject = iterator.next ();
                    calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
                    if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                        iterator.remove ();
                        list.add (incomingCallObject);
                    } else {
                        break;
                    }
                }
                list.get (list.size () - 1).setIsLastInCategory (true);
            }


        }
        if (!callObjectList.isEmpty ()) {
            calendar.setTime (new Date (callObjectList.get (0).getBeginTimestamp ()));
            if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
                hasYesterday = true;
            }
            if (hasYesterday) {
                list.add (new CallObject (true, context.getString (R.string.yesterday)));
                for (Iterator<CallObject> iterator = callObjectList.iterator () ; iterator.hasNext () ; ) {
                    CallObject incomingCallObject = iterator.next ();
                    calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
                    if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
                        iterator.remove ();
                        list.add (incomingCallObject);
                    } else {
                        break;
                    }
                }
                list.get (list.size () - 1).setIsLastInCategory (true);
            }
        }
        if (!callObjectList.isEmpty ()) {
            list.add (new CallObject (true, context.getString (R.string.older)));
            list.addAll (callObjectList);
        }

        try {
            if (ActivityCompat.checkSelfPermission (getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                return new AllCallRecyclerAdapter ( list, true);
            }
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        return new AllCallRecyclerAdapter ( list,false);
    }



    private void updateLayouts (RealmResults<CallObject> mCallObjectRealmResults) {
        if(mCallObjectRealmResults == null){
            if ( binding.fragmentAllCallMainLinearLayout.getVisibility () != View.GONE) {
                binding.fragmentAllCallMainLinearLayout.setVisibility (View.GONE);
            }
            if ( binding.fragmentAllScrollView.getVisibility () != View.VISIBLE) {
                binding.fragmentAllScrollView.setVisibility (View.VISIBLE);
            }
        }else if (mCallObjectRealmResults.size() > 0) {
            if (binding.fragmentAllScrollView.getVisibility () != View.GONE) {
                binding.fragmentAllScrollView.setVisibility (View.GONE);
            }
            if ( binding.fragmentAllCallMainLinearLayout.getVisibility () != View.VISIBLE) {
                binding.fragmentAllCallMainLinearLayout.setVisibility (View.VISIBLE);
            }
        } else {
            if ( binding.fragmentAllCallMainLinearLayout.getVisibility () != View.GONE) {
                binding.fragmentAllCallMainLinearLayout.setVisibility (View.GONE);
            }
            if ( binding.fragmentAllScrollView.getVisibility () != View.VISIBLE) {
                binding.fragmentAllScrollView.setVisibility (View.VISIBLE);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_all_call, container, false);
        layoutTimeLine = getAllChildOfTimeLine(); // lấy binding của 7 item để set value


        presenter.getTimeLine(AllCallPresenter.NOW_TIME_LINE);
        setDaysSelectedListener();
        setOnClickButtonNextOrPrevious();
        

        presenter.getCallObjectRealmObject(mRealm);
        return binding.getRoot();
    }


    public void init(){
        presenter = new AllCallPresenter(this);
        /// Khởi tạo Realm
        try {
            mRealm = Realm.getDefaultInstance ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        // lấy vị trí ngày hôm nay
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(nowDayOfWeek >1 && nowDayOfWeek <8){
            isDaySelected = nowDayOfWeek-2;
        }else if(nowDayOfWeek ==1){
            isDaySelected = 6;
        }
        isMonthSelected = String.valueOf(calendar.get(Calendar.MONTH));
    }


    @Override
    public void onDestroy() {
        if (mCallObjectRealmResults != null) {
            mCallObjectRealmResults.removeAllChangeListeners ();
            mCallObjectRealmResults = null;
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
        super.onDestroy ();
    }

    // binding of all child in timeline
    public List<LinearLayout> getAllChildOfTimeLine(){
        List<LinearLayout> linearLayoutList = new ArrayList<>();
        for(int i=0;i<binding.linLayoutTimeLine.getChildCount();i++){
            LinearLayout layout = (LinearLayout) binding.linLayoutTimeLine.getChildAt(i);
            linearLayoutList.add(layout);
        }
        return linearLayoutList;
    }

    // set day on the Time Line View
    @Override
    public void actionViewTimeLine(String[] days, String fromDay, String toDay) {
        for(int i=0;i<days.length;i++){
            LinearLayout layout = (LinearLayout) layoutTimeLine.get(i);
            TextView textViewDays = (TextView) layout.getChildAt(1); // get TextView day
            textViewDays.setText(days[i]);
        }
        binding.tvFromDayToDay.setText(fromDay+ " --> "+toDay);
        try {
            String [] str = fromDay.split("/");
            isMonthSelected = str[1];
        }catch (Exception e){
            LogE(TAG,e.getMessage());
            isMonthSelected = fromDay.substring(fromDay.length()-2,fromDay.length());
        }
    }

    @Override
    public void actionViewAllCallObject(RealmResults<CallObject> mCallObjectRealmResults) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager (getActivity(),LinearLayoutManager.VERTICAL,false);
        binding.fragmentAllCallRecyclerView.setLayoutManager(linearLayoutManager);
        binding.fragmentAllCallRecyclerView.setHasFixedSize(true);
        if (mCallObjectRealmResults != null) {
            
            //Update dữ liệu mỗi khi có thay đổi
            mCallObjectRealmResults.addChangeListener (incomingCallObjectRealmResults -> {
                if (binding.fragmentAllCallRecyclerView != null) {
                    setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), convertRealmResultsToList(incomingCallObjectRealmResults)));
                }
                updateLayouts(incomingCallObjectRealmResults);
            });
            
            // Set dữ liệu lần đầu tiên
            setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), convertRealmResultsToList(mCallObjectRealmResults)));
        }
        // set view hiển thị 
        updateLayouts(mCallObjectRealmResults);
    }

    public List<CallObject> convertRealmResultsToList(RealmResults<CallObject> mCallObjectRealmResults){
        if (mRealm != null) {
            return mRealm.copyFromRealm (mCallObjectRealmResults);
        }else {
            return new ArrayList<>(mCallObjectRealmResults);
        }
    }
    private void setAdapter ( AllCallRecyclerAdapter incomingCallRecyclerViewAdapter) {
        if (binding.fragmentAllCallRecyclerView != null) {
            binding.fragmentAllCallRecyclerView.setAdapter (incomingCallRecyclerViewAdapter);
            binding.fragmentAllCallRecyclerView.setItemViewCacheSize (incomingCallRecyclerViewAdapter.getItemCount ());
        }
    }

    public void setDaysSelectedListener() {
        for(int i=0;i<layoutTimeLine.size();i++){
            int positionSelect = i;
            layoutTimeLine.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        layoutTimeLine.get(isDaySelected).setBackground(null);
                        view.setBackground(ContextCompat.getDrawable(view.getContext(),R.drawable.custom_time_line_selected));
                        isDaySelected = positionSelect;
                    Log.e("Selected day", isDaySelected+"");

                }
            });
        }
    }

    public void filterCallByDay(){
        LinearLayout layout = (LinearLayout) layoutTimeLine.get(isDaySelected);
        TextView textViewDays = (TextView) layout.getChildAt(1); // get TextView day
        String dayOfMonth = textViewDays.getText().toString();
        Log.e(TAG,"dayofmonthSelected "+dayOfMonth);
        LogE(TAG,"monthSelected "+isMonthSelected);

    }

    public void setOnClickButtonNextOrPrevious(){
        binding.btnPreviousTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    presenter.getTimeLine(AllCallPresenter.PREVIOUS_TIME_LINE);
            }
        });

        binding.btnNextTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(AllCallPresenter.NEXT_TIME_LINE);
            }
        });
        binding.imgvFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterCallByDay();
            }
        });

    }
}