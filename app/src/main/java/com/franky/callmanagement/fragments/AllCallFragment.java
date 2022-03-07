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
import com.franky.callmanagement.env.AppConfig;
import com.franky.callmanagement.interfaces.IAllCallListener;
import com.franky.callmanagement.models.CallObject;
import com.franky.callmanagement.presenters.AllCallPresenter;

import java.util.ArrayList;
import java.util.Arrays;
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
    private String [] timeline ;

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

    private AllCallRecyclerAdapter populateAdapter (@NonNull Context context, @NonNull List<CallObject> callObjectList, int dayOfYearSelected) {
        Calendar calendar = Calendar.getInstance ();

        // filter theo ngày trên time line để hiệnt hị lên màn hình
        List<CallObject> list = new ArrayList<> ();
        if (!callObjectList.isEmpty ()) {
            list.add (new CallObject (true, "Tất cả"));
            for (Iterator<CallObject> iterator = callObjectList.iterator (); iterator.hasNext () ; ) {
                CallObject incomingCallObject = iterator.next ();
                calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
                if (calendar.get (Calendar.DAY_OF_YEAR) == dayOfYearSelected) {
                    iterator.remove ();
                    list.add (incomingCallObject);
                }
            }
            list.get (list.size () - 1).setIsLastInCategory (true);

        }
//        if (!callObjectList.isEmpty ()) {
//            calendar.setTime (new Date (callObjectList.get (0).getBeginTimestamp ()));
//            if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
//                hasYesterday = true;
//            }
//            if (hasYesterday) {
//                list.add (new CallObject (true, context.getString (R.string.yesterday)));
//                for (Iterator<CallObject> iterator = callObjectList.iterator () ; iterator.hasNext () ; ) {
//                    CallObject incomingCallObject = iterator.next ();
//                    calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
//                    if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
//                        iterator.remove ();
//                        list.add (incomingCallObject);
//                    } else {
//                        break;
//                    }
//                }
//                list.get (list.size () - 1).setIsLastInCategory (true);
//            }
//        }
//        if (!callObjectList.isEmpty ()) {
//            list.add (new CallObject (true, context.getString (R.string.older)));
//            list.addAll (callObjectList);
//        }

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



    private void updateLayouts (int numberOfItem) {
        if (numberOfItem > 1) { // kiểm tra xem có phần tử nào không thì hiện thông báo >1 vì luôn có 1 item header
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

        LogE(TAG,"Selected day "+isDaySelected);

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
    public void actionViewTimeLine(String[] days) {
        timeline = days;
        for(int i=0;i<days.length;i++){
            LinearLayout layout = (LinearLayout) layoutTimeLine.get(i);
            TextView textViewDays = (TextView) layout.getChildAt(1); // get TextView day
            String day = days[i];
            try {
                String [] str = day.split("/");
                textViewDays.setText(str[0]);
            }catch (Exception e){
                LogE(TAG,e.getMessage());
                if(day.length()>2){
                    textViewDays.setText(day.substring(0,2));
                }else {
                    textViewDays.setText("__");
                }
            }

        }
        binding.tvFromDayToDay.setText(days[0]+ " --> "+days[6]);

        LogE(TAG,"Selected day in time line "+days[isDaySelected]);

        LogE(TAG," time line "+Arrays.toString(days));

    }

    @Override
    public void actionViewAllCallObject(RealmResults<CallObject> callObjectRealmResults) {
        mCallObjectRealmResults = callObjectRealmResults;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager (getActivity(),LinearLayoutManager.VERTICAL,false);
        binding.fragmentAllCallRecyclerView.setLayoutManager(linearLayoutManager);
        binding.fragmentAllCallRecyclerView.setHasFixedSize(true);
        if (mCallObjectRealmResults != null) {
            
            //Update dữ liệu mỗi khi có thay đổi
            mCallObjectRealmResults.addChangeListener (incomingCallObjectRealmResults -> {
                if (binding.fragmentAllCallRecyclerView != null) {
                    setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), presenter.convertRealmResultsToList(mRealm,incomingCallObjectRealmResults), presenter.getDayOfYearSelected(timeline,isDaySelected)));
                    LogE(TAG,"List Realm Resuls change listener");
                }

            });
            
            // Set dữ liệu lần đầu tiên
            setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), presenter.convertRealmResultsToList(mRealm,mCallObjectRealmResults),presenter.getDayOfYearSelected(timeline,isDaySelected)));
        }
        // set view hiển thị
    }


    private void setAdapter ( AllCallRecyclerAdapter incomingCallRecyclerViewAdapter) {
        updateLayouts(incomingCallRecyclerViewAdapter.getItemCount());
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
                    presenter.getCallObjectRealmObject(mRealm);

                }
            });
        }
    }




    public void setOnClickButtonNextOrPrevious(){
        binding.btnPreviousTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    presenter.getTimeLine(AllCallPresenter.PREVIOUS_TIME_LINE);
                setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), presenter.convertRealmResultsToList(mRealm,mCallObjectRealmResults),presenter.getDayOfYearSelected(timeline,isDaySelected)));
            }
        });

        binding.btnNextTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getTimeLine(AllCallPresenter.NEXT_TIME_LINE);
                setAdapter (populateAdapter (binding.fragmentAllCallRecyclerView.getContext (), presenter.convertRealmResultsToList(mRealm,mCallObjectRealmResults),presenter.getDayOfYearSelected(timeline,isDaySelected)));
            }
        });
        binding.imgvFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}