package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemRevokeTable;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.MyDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MyApplyProblemRecord extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "MySpeech";

    private RecyclerView mMySpeechApplyRecordListView;
    private List<ProblemApplyTable> mMySpeechApplyRecordListViewData = new ArrayList<>();
    private MyAdapter adapter;

    private RecyclerView mMyRevokeRecordListView;
    private List<ProblemRevokeTable> mMyRevokeListViewData = new ArrayList<>();
    private RevokeAdapter revokeAdapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    LayoutInflater mInflater;
    List<View> mViewList;
    View mApplyProblemView, mRevokedView;
    ViewPager mViewPager;
    MyPageAdapter mViewPagerAdapter;
    TextView firstPageTitleTV, secondPageTitleTV;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_apply_problem);

        initilizeProgressDialog();
        addViewsForViewPager();
        bindView();
        setViewPager();
        setRecycleView();
        getDataFromNet();
        getRevokeRcords();
    }



    private void addViewsForViewPager() {
        mViewList = new ArrayList<>();
        mInflater = getLayoutInflater();
        mApplyProblemView = mInflater.inflate(R.layout.activity_my_apply_problem_record, null, false);
        mRevokedView = mInflater.inflate(R.layout.activity_my_revoke_problem_record, null, false);
        mViewList.add(mApplyProblemView);
        mViewList.add(mRevokedView);
    }


    private void setViewPager() {
        mViewPagerAdapter = new MyPageAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        firstPageTitleTV.setBackgroundColor(Color.parseColor("#7f2aad"));
                        firstPageTitleTV.setTextColor(Color.parseColor("#e3d42b"));
                        secondPageTitleTV.setBackgroundColor(Color.parseColor("#f2f1f1"));
                        secondPageTitleTV.setTextColor(Color.parseColor("#7f7c7c"));
                        break;
                    case 1:
                        secondPageTitleTV.setBackgroundColor(Color.parseColor("#7f2aad"));
                        secondPageTitleTV.setTextColor(Color.parseColor("#e3d42b"));
                        firstPageTitleTV.setBackgroundColor(Color.parseColor("#f2f1f1"));
                        firstPageTitleTV.setTextColor(Color.parseColor("#7f7c7c"));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 自定义PagerAdapter
     */
    private class MyPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewList.get(position);
            if (view == null) {
                Log.d(TAG, "空" + position);
            }
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("系统君正在拼命加载数据...");

        //首先获取我所在的小组
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    showToast("你还没有加入任何一个小组，快去寻找组织吧！");
                    dismissProgressDialog();
                } else {
                    Group group = (Group) results.get(0);
                    fiterMyApplyProblemRecord(group);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void fiterMyApplyProblemRecord(Group group) {
        DataBaseQuery query = new DataBaseQuery(ProblemApplyTable.CLASS_NAME);
        query.addWhereEqualTo(ProblemApplyTable.S_GROUP, group);
        query.includePointer(ProblemApplyTable.S_PROBLEM);   //顺带查询problem
        query.orderByDescendingDB(ProblemApplyTable.CREATED_AT);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    ProblemApplyTable problemApplyTable = (ProblemApplyTable) obj;
                    mMySpeechApplyRecordListViewData.add(problemApplyTable);
                }
                //通知列表刷新
                adapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }


    private void getRevokeRcords() {
        showProgressDialog("系统君正在拼命加载数据...");

        //首先获取我所在的小组
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    showToast("你还没有加入任何一个小组，快去寻找组织吧！");
                    dismissProgressDialog();
                } else {
                    Group group = (Group) results.get(0);
                    fiterMyRevokeRecord(group);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });

    }

    private void fiterMyRevokeRecord(Group group) {
        DataBaseQuery query = new DataBaseQuery(ProblemRevokeTable.CLASS_NAME);
        query.orderByDescendingDB(ProblemApplyTable.CREATED_AT);
        query.addWhereEqualTo(ProblemRevokeTable.S_GROUP, group);
        query.includePointer(ProblemRevokeTable.S_PROBLEM);   //顺带查询problem
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    ProblemRevokeTable problemApplyTable = (ProblemRevokeTable) obj;
                    mMyRevokeListViewData.add(problemApplyTable);
                }
                //通知列表刷新
                revokeAdapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void setRecycleView() {
        adapter = new MyAdapter();
        mMySpeechApplyRecordListView.setLayoutManager(new LinearLayoutManager(this));
        mMySpeechApplyRecordListView.setAdapter(adapter);
        mMySpeechApplyRecordListView.addItemDecoration(new MyDecoration(this));

        revokeAdapter = new RevokeAdapter();
        mMyRevokeRecordListView.setLayoutManager(new LinearLayoutManager(this));
        mMyRevokeRecordListView.setAdapter(revokeAdapter);
        mMyRevokeRecordListView.addItemDecoration(new MyDecoration(this));
    }

    /**
     * 绑定xml组件
     */
    private void bindView() {
        mMySpeechApplyRecordListView = (RecyclerView) mApplyProblemView.findViewById(R.id.my_problem_apply_record_recycle_view);
        mMyRevokeRecordListView = (RecyclerView) mRevokedView.findViewById(R.id.my_problem_apply_record_recycle_view);

        mViewPager = (ViewPager) findViewById(R.id.my_apply_view_pager);
        firstPageTitleTV = (TextView) findViewById(R.id.apply_title);
        secondPageTitleTV = (TextView) findViewById(R.id.revoke_title);

        firstPageTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });

        secondPageTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });
    }

    /**
     * 自定义RecycleView的ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mProblemTitleTV, mProblemApplyTimeTV, mStateTV;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mProblemApplyTimeTV = (TextView) itemView.findViewById(R.id.apply_time_tv);
            mProblemTitleTV = (TextView) itemView.findViewById(R.id.problem_title_tv);
            mStateTV = (TextView) itemView.findViewById(R.id.state);

            mSavedView = itemView;
        }
    }

    /**
     * 自定义RecycleView适配器
     */
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View itemView = inflater.inflate(R.layout.my_apply_problem_record_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ProblemApplyTable applyTable = mMySpeechApplyRecordListViewData.get(position);
            Problem problem = (Problem) applyTable.getProblem();
            String problemTitle = problem.getTitle();
            Date applyTime = applyTable.getCreatedAt();
            int state = applyTable.getState();
            String stateStr = null;
            int stateStrColor = 0;

            switch (state) {
                case 0:
                    stateStr = "待处理";
                    stateStrColor = Color.parseColor("#c9ad45");
                    break;
                case 1:
                    stateStr = "已通过";
                    stateStrColor = Color.parseColor("#336dbd");
                    break;
                case 2:
                    stateStr = "未通过";
                    stateStrColor = Color.parseColor("#c41f0f");
                    break;
            }

            String time = getTimeFromDate(applyTime);

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mStateTV.setText(stateStr);
            holder.mStateTV.setTextColor(stateStrColor);
            holder.mProblemApplyTimeTV.setText(time);

        }

        @Override
        public int getItemCount() {
            return mMySpeechApplyRecordListViewData.size();
        }
    }


    /**
     * 自定义RecycleView适配器
     */
    private class RevokeAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View itemView = inflater.inflate(R.layout.my_apply_problem_record_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ProblemRevokeTable applyTable = mMyRevokeListViewData.get(position);
            Problem problem = (Problem) applyTable.getProblem();
            String problemTitle = problem.getTitle();
            Date applyTime = applyTable.getCreatedAt();
            int state = applyTable.getState();
            String stateStr = null;
            int stateStrColor = 0;

            switch (state) {
                case 0:
                    stateStr = "待处理";
                    stateStrColor = Color.parseColor("#c9ad45");
                    break;
                case 1:
                    stateStr = "已通过";
                    stateStrColor = Color.parseColor("#336dbd");
                    break;
                case 2:
                    stateStr = "未通过";
                    stateStrColor = Color.parseColor("#c41f0f");
                    break;
            }

           String time = getTimeFromDate(applyTime);

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mStateTV.setText(stateStr);
            holder.mStateTV.setTextColor(stateStrColor);
            holder.mProblemApplyTimeTV.setText(time);
        }

        @Override
        public int getItemCount() {
            return mMyRevokeListViewData.size();
        }
    }

    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }

    /**
     * 查看课题详情
     */
    private void showProblemInfo(int position) {
        String serializedProblem = mMySpeechApplyRecordListViewData.get(position).toString();
        Intent intent = new Intent(getApplicationContext(), ProblemInfo.class);
        intent.putExtra(ProblemInfo.PROBLEM_EXTRA_TAG, serializedProblem);
        startActivity(intent);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(MyApplyProblemRecord.this);
        mProgressBarDialog.setMessage("数据加载中...");
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


    //防止窗体句柄泄露
    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissProgressDialog();
    }
}

