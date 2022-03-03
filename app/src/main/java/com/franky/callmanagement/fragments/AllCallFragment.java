package com.franky.callmanagement.fragments;

import static com.franky.callmanagement.utils.LogUtil.LogE;

import android.Manifest;
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


    public RecyclerView mRecyclerView = null;
    private Realm mRealm = null;
    private RealmResults<CallObject> mCallObjectRealmResults = null;

    private ScrollView mScrollView = null;
    private LinearLayout mMainLinearLayout = null;

    public static AllCallFragment newInstance() {
        AllCallFragment fragment = new AllCallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Realm.init(getContext());
            mRealm = Realm.getDefaultInstance ();
        } catch (Exception e) {
            LogE (TAG, e.getMessage ());
            LogE (TAG, e.toString ());
            e.printStackTrace ();
        }
        if (mRealm != null && !mRealm.isClosed ()) {
            try {
                mCallObjectRealmResults = mRealm.where (CallObject.class)
                        .greaterThan ("mEndTimestamp", 0L)
                        .sort ("mBeginTimestamp", Sort.DESCENDING)
                        .findAll ();
            } catch (Exception e) {
                LogE (TAG, e.getMessage ());
                LogE (TAG, e.toString ());
                e.printStackTrace ();
            }
            if (mCallObjectRealmResults != null) {
                mCallObjectRealmResults.addChangeListener (incomingCallObjectRealmResults -> {
                    if (mRecyclerView != null) {
                        List<CallObject> incomingCallObjectList = null;
                        if (mRealm != null) {
                            incomingCallObjectList = mRealm.copyFromRealm (incomingCallObjectRealmResults);
                        }
                        if (incomingCallObjectList == null) {
                            incomingCallObjectList = new ArrayList<> (incomingCallObjectRealmResults);
                        }
                        setAdapter (populateAdapter (mRecyclerView.getContext (), incomingCallObjectList));
                    }
                    updateLayouts ();
                });
            }
        }
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



    private void updateLayouts () {
        if (mRecyclerView != null && mRecyclerView.getAdapter () != null && mRecyclerView.getAdapter ().getItemCount () > 0) {
            if (mScrollView != null && mScrollView.getVisibility () != View.GONE) {
                mScrollView.setVisibility (View.GONE);
            }
            if (mMainLinearLayout != null && mMainLinearLayout.getVisibility () != View.VISIBLE) {
                mMainLinearLayout.setVisibility (View.VISIBLE);
            }
        } else {
            if (mMainLinearLayout != null && mMainLinearLayout.getVisibility () != View.GONE) {
                mMainLinearLayout.setVisibility (View.GONE);
            }
            if (mScrollView != null && mScrollView.getVisibility () != View.VISIBLE) {
                mScrollView.setVisibility (View.VISIBLE);
            }
        }
    }

    private void setAdapter (@NonNull AllCallRecyclerAdapter incomingCallRecyclerViewAdapter) {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter (incomingCallRecyclerViewAdapter);
            mRecyclerView.setItemViewCacheSize (incomingCallRecyclerViewAdapter.getItemCount ());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_all_call, container, false);
        init();

        presenter.getTimeLine(AllCallPresenter.NOW_TIME_LINE);
        setDaysSelectedListener();
        setOnClickButtonNextOrPrevious();


        mScrollView = binding.fragmentAllcallTabScrollView;
        mMainLinearLayout = binding.fragmentAllcallTabMainLinearLayout;
        mRecyclerView = binding.fragmentAllcallTabRecyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager (mRecyclerView.getContext ());
        linearLayoutManager.setOrientation (RecyclerView.VERTICAL);
        mRecyclerView.setHasFixedSize (true);
        mRecyclerView.setLayoutManager (linearLayoutManager);
        mRecyclerView.setItemAnimator (new DefaultItemAnimator());
        List<CallObject> incomingCallObjectList = null;
        if (mRealm != null) {
            incomingCallObjectList = mRealm.copyFromRealm (mCallObjectRealmResults);
        }
        if (incomingCallObjectList == null) {
            incomingCallObjectList = new ArrayList<> (mCallObjectRealmResults);
        }
        setAdapter (populateAdapter (mRecyclerView.getContext (), incomingCallObjectList));

        return binding.getRoot();
    }


    public void init(){
        presenter = new AllCallPresenter(this);
        layoutTimeLine = getAllChildOfTimeLine();
        // lấy vị trí ngày hôm nay
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(nowDayOfWeek >1 && nowDayOfWeek <8){
            isDaySelected = nowDayOfWeek-2;
        }else if(nowDayOfWeek ==1){
            isDaySelected = 6;
        }
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
    }
}