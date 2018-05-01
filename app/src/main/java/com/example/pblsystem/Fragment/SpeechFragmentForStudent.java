package com.example.pblsystem.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Activity.MyApplyProblemRecord;
import com.example.pblsystem.Activity.MySpeech;
import com.example.pblsystem.Activity.ShowAllProblems;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.MyPagerTransformer;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by 郭聪 on 2017/3/6.
 */
public class SpeechFragmentForStudent extends Fragment {
    //常量，调试的Tag值
    private static final String TAG = "SpeechFragment";
    //Toast静态常量
    private static Toast toast;
    //显示今日所有演讲的ViewPager
    private ViewPager mTodaySpeechViewPager;
    //今日演讲Fragment的列表
    private List<Fragment> mTodaySpeechesList = new ArrayList<>();
    //Fragment管理器
    private FragmentManager mFragmentManager;
    //ViewPager适配器
    private MyFragmentPagerAdapter mMyAdapter;
    //“所有演讲”和“我的演讲”TextView
    private TextView mAllSpeechesTv, mMySpeechesTv, mMyApplysTv;
    //保存视图，防止视图被销毁后，重新创建视图造成的消耗
    private View mSavedView;

    // 刷新按钮
    private ImageView refreshIV;

    private DataBaseManager manager = DataBaseManager.getInstance();

    private ProgressDialog mProgressBarDialog;

    private LinearLayout slideIndicator;
    private List<ImageView> indicatorOvals = new ArrayList<>(); // 指示器的小圆点
    private int indicatorIndex = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//如果该Fragment还没有绑定过View
            View view = inflater.inflate(R.layout.student_speech_fragment, container, false);
            //保留view
            mSavedView = view;
            bindView(view);
            initilizeProgressDialog();
            setClickListenerForBtn();
            setViewPager();
            getFragmentList();
            setViewPager();
        }
        return mSavedView;
    }

    private void setClickListenerForBtn() {
        mMySpeechesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MySpeech.class);
                startActivity(intent);
            }
        });

        mAllSpeechesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowAllProblems.class);
                startActivity(intent);
            }
        });
        mMyApplysTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyApplyProblemRecord.class);
                startActivity(intent);
            }
        });
    }


    /**
     * 获取Fragment集合
     */
    private void getFragmentList() {
        //从云端拉取数据   只获取我的班级里面的
        getMyClass();
    }

    private void getMyClass() {
        showProgressDialog("正在加载数据...");

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                fiterTodayProblemOfMyClass(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                //showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    private void fiterTodayProblemOfMyClass(ClassRoom myClass) {
        /*注意查询条件的限定*/
        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.includePointer(ProblemGroup.S_GROUP);
        query.includePointer(ProblemGroup.S_PROBLEM);
        query.includePointer(ProblemGroup.S_SPEAKER);
        query.addWhereEqualTo(ProblemGroup.S_CLASS, myClass);   //只查询我的班级
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                //遍历查询到的数据
                for (Object obj : results) {
                    //新建一个Fragment实例
                    TodaySpeechFragmentForStudent newSpeechFragment = new TodaySpeechFragmentForStudent();
                    //绑定一个课题实例
                    ProblemGroup problemGroup = (ProblemGroup) obj;
                    if (checkIfTheToday((Problem) problemGroup.getProblem())) { //当日演讲
                        newSpeechFragment.setProblemObj(problemGroup);
                        //将新建的Fragment加入ViewPager数据源
                        mTodaySpeechesList.add(newSpeechFragment);
                    }
                }

                if (mTodaySpeechesList.size() == 0) {//没有对应的数据
                    //展示一个空Fragment用于提示用户
                    mTodaySpeechesList.add(new TodaySpeechFragmentEmpty());
                } else {
                    // 显示滑动指示器
                    showSlideIndicator(mTodaySpeechesList.size());
                }

                // 更新数据展示
                mMyAdapter.notifyDataSetChanged();


                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                //showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    private void showSlideIndicator(int number) {
        for (int i = 0; i < number; i++) {
            createOneOval();
        }
    }

    // 为指示器显示一个小圆点
    private void createOneOval() {
        ImageView oval = new ImageView(getActivity());
        oval.setImageResource(R.drawable.gray_oval);

        // 宽和高均为10dp
        LinearLayout.LayoutParams ovalParams = new LinearLayout.LayoutParams(Util.dp2Px(10, getActivity()),
                Util.dp2Px(10, getActivity()));
        // 左侧margin 5dp
        ovalParams.setMargins(Util.dp2Px(5, getActivity()), 0, 0, 0);
        oval.setLayoutParams(ovalParams);

        slideIndicator.addView(oval);
        indicatorOvals.add(oval);

        if (indicatorIndex == -1) {
            indicatorIndex = 0; // 初始化为0
            // 初始化第一个小圆点为实心
            oval.setImageResource(R.drawable.gray_fill_oval);
        }
    }

    private boolean checkIfTheToday(Problem problem) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        Date date = problem.getSpeakTime();
        c.setTime(problem.getSpeakTime());

        int speakYear = c.get(Calendar.YEAR);
        int speakMonth = c.get(Calendar.MONTH) + 1;
        int speakDay = c.get(Calendar.DAY_OF_MONTH);

        Date currentTime = new Date();
        c.setTime(currentTime);

        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        int currrentDay = c.get(Calendar.DAY_OF_MONTH);

        if (currentYear == speakYear && currentMonth == speakMonth && currrentDay == speakDay) {//时间完全相同
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置ViewPager适配器
     */
    private void setViewPager() {
        //获得Fragment管理器
        mFragmentManager = getChildFragmentManager();
        //创建适配器
        mMyAdapter = new MyFragmentPagerAdapter(mFragmentManager);
        //为viewPager添加适配器
        mTodaySpeechViewPager.setAdapter(mMyAdapter);
        //为ViewPager绑定动画效果
        mTodaySpeechViewPager.setPageTransformer(true, new MyPagerTransformer());

        // 页面切换事件监听
        mTodaySpeechViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //将原来的实心变成空心
                indicatorOvals.get(indicatorIndex).setImageResource(R.drawable.gray_oval);

                // 将小圆点变成实心
                indicatorOvals.get(position).setImageResource(R.drawable.gray_fill_oval);
                // 更新下标
                indicatorIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 与xml文件中的组件绑定
     */
    private void bindView(View view) {
        mTodaySpeechViewPager = (ViewPager) view.findViewById(R.id.fragment_speech_view_pager);
        mAllSpeechesTv = (TextView) view.findViewById(R.id.fragment_all_speech_text_view);
        mMySpeechesTv = (TextView) view.findViewById(R.id.fragment_my_speech_text_view);
        mMyApplysTv = (TextView) view.findViewById(R.id.fragment_speech_apply_text_view);
        refreshIV = (ImageView) view.findViewById(R.id.refresh);
        refreshIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        slideIndicator = (LinearLayout) view.findViewById(R.id.slide_layout);

    }

    private void refresh() {
        mTodaySpeechesList.clear();
        mMyAdapter.notifyDataSetChanged();

        // 清空小圆点
        slideIndicator.removeAllViews();
        indicatorOvals.clear();
        indicatorIndex = -1;

        //重新获取数据
        getFragmentList();
    }

    /**
     * 自定义ViewPager的适配器
     */
    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "获得第几个" + position);
            //获取数据源中对应的Fragment
            return mTodaySpeechesList.get(position);
        }

        @Override
        public int getCount() {
            //返回今日所有演讲的数目
            return mTodaySpeechesList.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 弹出Toast
     */
    public void showToast(String msg) {
        if (toast == null) {//第一次初始化toast变量
            toast = Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(getActivity());
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }
}
