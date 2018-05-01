package com.example.pblsystem.Activity;
/**
 * 学生用户系统主界面
 */

import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentController;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.DescriptionScore;
import com.example.pblsystem.Class.EvaluationStandard;
import com.example.pblsystem.Class.EvalutionDescription;
import com.example.pblsystem.Class.MyCustomReceiver;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.OnLine;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Fragment.FindFragmentForStudent;
import com.example.pblsystem.Fragment.GroupFragmentForStudent;
import com.example.pblsystem.Fragment.MeFragmentForStudent;
import com.example.pblsystem.Fragment.SpeechFragmentForStudent;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivityForStudent extends AppCompatActivity {
    long firstTime = 0; //退出程序计时器
    //常量Intent附加数据的TAG值
    public static final String TAG_USER = "user";
    //常量，调试的Tag值
    private static final String TAG = "MainStudentActivity";
    //底部TextView暗色颜色值
    private static final int GRAY_COLOR_TEXT = Color.parseColor("#767676");
    //底部TextView亮色颜色值
    private static final int LIGHT_COLOR_TEXT = Color.parseColor("#27bdef");
    //Toast静态常量
    private static Toast toast;
    //登录人
    public static AVUser mUser;
    //ViewPager引用
    private ViewPager mViewPager;
    //定义Fragment管理器
    private FragmentManager mFragmentManager;
    //定义Fragment适配器
    private MyFragmentPagerAdapter mViewPagerAdapter;
    //屏幕下方指示器View引用
    private TextView mSpeechTextView;
    private TextView mFindTextView;
    private TextView mGroupTextView;
    private TextView mMeTextView;
    //保存4个Fragment页面
    private List<Fragment> mFragmentList;
    //当前页面指示器
    private int mCurrentPageIndex = 0;
    private MyCustomReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);
        getInentData();
        bindView();
        getFragments();
        setViewPager();
        registerBroadCast();


        //test
//        getMyClass();
    }
    /////////////////// test
    private DataBaseManager manager = DataBaseManager.getInstance();
    private void getMyClass() {

        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                Log.i("Tag","Find success!");
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                fiterEvalutionDescription(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    /**
     * 根据我所在的班级搜索评价细则
     * @param myClass
     */
    private EvaluationStandard mEvalutionStandard;
    private void fiterEvalutionDescription(ClassRoom myClass) {

        DataBaseQuery query = new DataBaseQuery(EvaluationStandard.CLASS_NAME);
        query.addWhereEqualTo(EvaluationStandard.S_CLASS, myClass);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {

                if (results.size() == 1) {
                    mEvalutionStandard = (EvaluationStandard) results.get(0);
                    getEvalutionDetails();
                } else if (results.size() == 0 ) {
                    Log.i("Tag","无法评分！");
//                    showToast("你所在的课堂尚未配置评价表，暂时无法评分！");
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private List<EvalutionDescription> details = new ArrayList<>();
    private void getEvalutionDetails() {

        AVRelation relation = mEvalutionStandard.getRelation(EvaluationStandard.S_DESCRIPTION);
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    //获取评价标准
                    Log.i("评价标准数目:",String.valueOf(list.size()));
                    for (AVObject obj: list) {
                        details.add((EvalutionDescription) obj);
                        //用于生成评价的单项打分
                        DescriptionScore descriptionScore = new DescriptionScore();
                        Log.i("评价标题:",((EvalutionDescription) obj).getDescriptionTitle());
                        descriptionScore.setDescription((EvalutionDescription) obj);
//                        evalutionResultDetail.add(descriptionScore);
                    }

//                    createTable();
//                    checkIfHaveScored();
                } else {
                    Log.i("Tag", "获取评价标准错误");
//                    dismissProgressDialog();
                }
            }
        });
    }





    ////////////////////////////////

    private void registerBroadCast() {
        receiver = new MyCustomReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.avos.UPDATE_STATUS");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 获取Intent中附属的数据
     */
    private void getInentData() {
        //获取bundle
        Bundle bundle = getIntent().getExtras();
        //得到序列化后的对象
        String serializedUser = (String) bundle.get(TAG_USER);
        //反序列化
        try {
            mUser = (AVUser) AVObject.parseAVObject(serializedUser);
            Log.d(TAG, mUser.getUsername());
        } catch (Exception e) {
            Log.d(TAG, "反序列化失败");
        }
    }

    /**
     * 设置Viewpager
     */
    private void setViewPager() {
        //获取Fragment管理器
        mFragmentManager = getSupportFragmentManager();
        //构建适配器
        mViewPagerAdapter = new MyFragmentPagerAdapter(mFragmentManager);
        //为Viewpager添加适配器
        mViewPager.setAdapter(mViewPagerAdapter);
        //设置默认页面为第一个
        mViewPager.setCurrentItem(mCurrentPageIndex);
        //为viewpager添加页面切换监听器
        mViewPager.addOnPageChangeListener(new MyViewpagerChangeListenner());
        //设置ViewPager的第一页为初始页
        mViewPager.setCurrentItem(0);

    }

    /**
     * 创建4个Framgnet
     */
    private void getFragments() {
        //初始化Fragment数组
        mFragmentList = new ArrayList<Fragment>();
        //添加Fragment
        mFragmentList.add(new SpeechFragmentForStudent());
        mFragmentList.add(new GroupFragmentForStudent());
        mFragmentList.add(new FindFragmentForStudent());
        mFragmentList.add(new MeFragmentForStudent());
    }

    /**
     * 绑定xml文件中组件
     */
    private void bindView() {
        mViewPager = (ViewPager) findViewById(R.id.student_ui_view_pager);
        mFindTextView = (TextView) findViewById(R.id.student_ui_find_text_view);
        mSpeechTextView = (TextView) findViewById(R.id.student_ui_speech_text_view);
        mGroupTextView = (TextView) findViewById(R.id.student_ui_group_text_view);
        mMeTextView = (TextView) findViewById(R.id.student_ui_me_text_view);
        //为textview设置监听器
        mSpeechTextView.setOnClickListener(new MyTextViewOnClickListenner());
        mGroupTextView.setOnClickListener(new MyTextViewOnClickListenner());
        mFindTextView.setOnClickListener(new MyTextViewOnClickListenner());
        mMeTextView.setOnClickListener(new MyTextViewOnClickListenner());
    }



    /**
     * 定义一个viewPager的适配器
     */
    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        /**
         * 构造函数，调用父类构造函数
         * @param fm
         */
        public MyFragmentPagerAdapter (FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            //根据postion选择展示的Fragment页面
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            //返回4，这里只有4个页面
            return 4;
        }
    }

    /**
     * 定义一个viewpager的滑动监听器
     */
    private class MyViewpagerChangeListenner implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "当前页面" + position);
            //将原来的底部亮的图标设置为暗色
            reNewBottomIndicator();
            //重置当前页面下标值
            mCurrentPageIndex = position;
            //更新底部按钮指示器
            updateBottomIndicator();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * 定义TextView的点击监听器
     */
    private class MyTextViewOnClickListenner implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //底部指示器首先复原
            reNewBottomIndicator();
            switch (v.getId()) {
                case R.id.student_ui_speech_text_view:
                    mCurrentPageIndex = 0;
                    break;
                case R.id.student_ui_group_text_view:
                    mCurrentPageIndex = 1;
                    break;
                case R.id.student_ui_find_text_view:
                    mCurrentPageIndex = 2;
                    break;
                case R.id.student_ui_me_text_view:
                    mCurrentPageIndex = 3;
                    break;
                default:
                    break;
            }
            //设置ViewPager的当前页面
            mViewPager.setCurrentItem(mCurrentPageIndex);
            //更新底部按钮指示
            updateBottomIndicator();
        }
    }

    /**
     * 将底部指示器复原
     */
    private void reNewBottomIndicator() {
        switch (mCurrentPageIndex) {
            case 0:
                //恢复暗色字体颜色
                mSpeechTextView.setTextColor(GRAY_COLOR_TEXT);
                //获得暗色“演讲”图标资源
                Drawable speechDrawable = getResources().getDrawable(R.drawable.speech_gray);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                speechDrawable.setBounds(0, 0, speechDrawable.getMinimumWidth(), speechDrawable.getMinimumHeight());
                //设置文本上方图标
                mSpeechTextView.setCompoundDrawables(null, speechDrawable, null, null);
                break;
            case 1:
                //恢复暗色字体颜色
                mGroupTextView.setTextColor(GRAY_COLOR_TEXT);
                //获得暗色“小组”图标资源
                Drawable groupDrawable = getResources().getDrawable(R.drawable.group_gray);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                groupDrawable.setBounds(0, 0, groupDrawable.getMinimumWidth(), groupDrawable.getMinimumHeight());
                //设置文本上方图标
                mGroupTextView.setCompoundDrawables(null, groupDrawable, null, null);
                break;
            case 2:
                //恢复暗色字体颜色
                mFindTextView.setTextColor(GRAY_COLOR_TEXT);
                //获得暗色“发现”图标资源
                Drawable findDrawable = getResources().getDrawable(R.drawable.find_gray);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                findDrawable.setBounds(0, 0, findDrawable.getMinimumWidth(), findDrawable.getMinimumHeight());
                //设置文本上方图标
                mFindTextView.setCompoundDrawables(null, findDrawable, null, null);
                break;
            case 3:
                //恢复暗色字体颜色
                mMeTextView.setTextColor(GRAY_COLOR_TEXT);
                //获得暗色“我”图标资源
                Drawable meDrawable = getResources().getDrawable(R.drawable.my_gray);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                meDrawable.setBounds(0, 0, meDrawable.getMinimumWidth(), meDrawable.getMinimumHeight());
                //设置文本上方图标
                mMeTextView.setCompoundDrawables(null, meDrawable, null, null);
                break;
            default:
                break;
        }
    }

    /**
     * 更新底部按钮指示
     */
    private void updateBottomIndicator() {
        switch (mCurrentPageIndex) {
            case 0:
                //设置亮色字体颜色
                mSpeechTextView.setTextColor(LIGHT_COLOR_TEXT);
                //获得亮色“演讲”图标资源
                Drawable speechDrawable = getResources().getDrawable(R.drawable.speech_bright);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                speechDrawable.setBounds(0, 0, speechDrawable.getMinimumWidth(), speechDrawable.getMinimumHeight());
                //设置文本上方图标
                mSpeechTextView.setCompoundDrawables(null, speechDrawable, null, null);
                break;
            case 1:
                //设置亮色字体颜色
                mGroupTextView.setTextColor(LIGHT_COLOR_TEXT);
                //获得亮色“小组”图标资源
                Drawable groupDrawable = getResources().getDrawable(R.drawable.group_bright);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                groupDrawable.setBounds(0, 0, groupDrawable.getMinimumWidth(), groupDrawable.getMinimumHeight());
                //设置文本上方图标
                mGroupTextView.setCompoundDrawables(null, groupDrawable, null, null);
                break;
            case 2:
                //设置亮色字体颜色
                mFindTextView.setTextColor(LIGHT_COLOR_TEXT);
                //获得亮色“发现”图标资源
                Drawable findDrawable = getResources().getDrawable(R.drawable.find_bright);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                findDrawable.setBounds(0, 0, findDrawable.getMinimumWidth(), findDrawable.getMinimumHeight());
                //设置文本上方图标
                mFindTextView.setCompoundDrawables(null, findDrawable, null, null);
                break;
            case 3:
                //设置亮色字体颜色
                mMeTextView.setTextColor(LIGHT_COLOR_TEXT);
                //获得亮色“我”图标资源
                Drawable meDrawable = getResources().getDrawable(R.drawable.my_bright);
                //设置图标位置信息，为图标腾出空间，否则将无法显示
                meDrawable.setBounds(0, 0, meDrawable.getMinimumWidth(), meDrawable.getMinimumHeight());
                //设置文本上方图标
                mMeTextView.setCompoundDrawables(null, meDrawable, null, null);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - firstTime) > 2000) {
                showToast("再按一次退出程序!");
                firstTime = System.currentTimeMillis();
            } else {
                // 退出系统
                finish();
                System.exit(0);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 弹出Toast
     */
    public void showToast(String msg) {
        if (toast == null) {//第一次初始化toast变量
            toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }
}
