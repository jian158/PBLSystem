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

import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemRevokeTable;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.MyDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ApplyProblem extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "ApplyProblem";
    //Toast静态常量
    private static Toast toast;

    public static final int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_REVOKE = 2;

    private RecyclerView mAllApplysListView;
    private MyAdapter adapter;
    private List<ProblemApplyTable> mProblemApplyTableList = new ArrayList<>();

    private RecyclerView mAllRevokeRecords;
    private RevokeAdapter mRevokeAdapter;
    private List<ProblemRevokeTable> mProblemRevokeTableList = new ArrayList<>();

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();

    LayoutInflater mInflater;
    List<View> mViewList;
    View mApplyProblemView, mRevokedView;
    ViewPager mViewPager;
    MyPageAdapter mViewPagerAdapter;

    private TextView firstPageTitleTV, secondPageTitleTV;

    private int currentClickIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_problem_to_deal);

        initilizeProgressDialog();
        addViewsForViewPager();
        try {
            bindView();
            setViewPager();
            setRecycleView();
            getApplysDataFromNet();
            getRevokesRecordsFromNet();
        } catch (NullPointerException e) {
            e.getMessage();
        }
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

    private void addViewsForViewPager() {
        mViewList = new ArrayList<>();
        mInflater = getLayoutInflater();
        mApplyProblemView = mInflater.inflate(R.layout.apply_problem_pager1, null, false);
        mRevokedView = mInflater.inflate(R.layout.apply_problem_pager2, null, false);
        mViewList.add(mApplyProblemView);
        mViewList.add(mRevokedView);
    }


    private void setRecycleView() {
        adapter = new MyAdapter();
        mAllApplysListView.setLayoutManager(new LinearLayoutManager(this));
        mAllApplysListView.setAdapter(adapter);
        mAllApplysListView.addItemDecoration(new MyDecoration(this));

        mRevokeAdapter = new RevokeAdapter();
        mAllRevokeRecords.setLayoutManager(new LinearLayoutManager(this));
        mAllRevokeRecords.setAdapter(mRevokeAdapter);
        mAllRevokeRecords.addItemDecoration(new MyDecoration(this));

    }

    /**
     * 获取课题申请列表数据
     */
    private void getApplysDataFromNet() {
        showProgressDialog("正在加载数据...");

        DataBaseQuery query = new DataBaseQuery(ProblemApplyTable.CLASS_NAME);
        query.addWhereEqualTo(ProblemApplyTable.S_CLASS, LoginActivity.sSelectedClass.getTargetClass());
        query.addWhereEqualTo(ProblemApplyTable.S_STATE, 0);    //只处理未处理的申请
        query.includePointer(ProblemApplyTable.S_GROUP);
        query.includePointer(ProblemApplyTable.S_PROBLEM);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    dismissProgressDialog();

                    return;
                }

                for (Object obj: results) {
                    ProblemApplyTable applyTable = (ProblemApplyTable) obj;
                    mProblemApplyTableList.add(applyTable);
                }
                //通知列表适配器
                adapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 获取课题撤销申请数据
     */
    private void getRevokesRecordsFromNet() {
        showProgressDialog("正在加载数据...");

        DataBaseQuery query = new DataBaseQuery(ProblemRevokeTable.CLASS_NAME);
        query.addWhereEqualTo(ProblemRevokeTable.S_CLASS, LoginActivity.sSelectedClass.getTargetClass());
        query.addWhereEqualTo(ProblemRevokeTable.S_STATE, 0);    //只处理未处理的申请
        query.includePointer(ProblemRevokeTable.S_GROUP);
        query.includePointer(ProblemRevokeTable.S_PROBLEM);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    dismissProgressDialog();

                    return;
                }

                for (Object obj: results) {
                    ProblemRevokeTable applyTable = (ProblemRevokeTable) obj;
                    mProblemRevokeTableList.add(applyTable);
                }
                //通知列表适配器
                mRevokeAdapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mAllApplysListView = (RecyclerView)  mApplyProblemView.findViewById(R.id.deal_apply_problem_recycle_view);
        mAllRevokeRecords = (RecyclerView)  mRevokedView.findViewById(R.id.deal_revoke_problem_recycle_view);

        mViewPager = (ViewPager) findViewById(R.id.apply_problem_view_pager);
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
     * 自定义ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mProblemTitleTV, mGroupNameTV, mApplyTimeTV;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mProblemTitleTV = (TextView) itemView.findViewById(R.id.problem_title_tv);
            mGroupNameTV = (TextView) itemView.findViewById(R.id.group_name_tv);
            mApplyTimeTV = (TextView) itemView.findViewById(R.id.apply_time_tv);

            mSavedView = itemView;
        }
    }


    /**
     * 自定义Adapter
     */
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.activity_apply_problem_deal_list_view_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ProblemApplyTable applyTable = mProblemApplyTableList.get(position);
            Problem problem = (Problem) applyTable.getProblem();
            Group group = (Group) applyTable.getGroup();
            String groupName;
            if (group == null) {
                groupName = "无效的小组";
            } else {
                groupName = group.getName();
            }

            String problemTitle = problem.getTitle();
            String applyTime = getTimeFromDate(applyTable.getCreatedAt());

            holder.mApplyTimeTV.setText(applyTime);
            holder.mGroupNameTV.setText(groupName);
            holder.mProblemTitleTV.setText(problemTitle);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProblemApplyTable applyTable = mProblemApplyTableList.get(position);
                    showApplyInfoDetail(applyTable);
                    currentClickIndex = position;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mProblemApplyTableList.size();
        }
    }

    /**
     * 自定义ViewHolder
     */
    private class RevokeViewHolder extends RecyclerView.ViewHolder {
        private TextView mProblemTitleTV, mGroupNameTV, mApplyTimeTV;
        private View mSavedView;

        public RevokeViewHolder(View itemView) {
            super(itemView);

            mProblemTitleTV = (TextView) itemView.findViewById(R.id.problem_title_tv);
            mGroupNameTV = (TextView) itemView.findViewById(R.id.group_name_tv);
            mApplyTimeTV = (TextView) itemView.findViewById(R.id.apply_time_tv);

            mSavedView = itemView;
        }
    }


    /**
     * 自定义Adapter
     */
    private class RevokeAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.activity_apply_problem_deal_list_view_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ProblemRevokeTable applyTable = mProblemRevokeTableList.get(position);
            Problem problem = (Problem) applyTable.getProblem();
            Group group = (Group) applyTable.getGroup();
            String problemTitle = problem.getTitle();
            String groupName;
            if (group == null) {
                groupName = "小组已经解散";
            } else {
                groupName = group.getName();
            }
            String applyTime = getTimeFromDate(applyTable.getCreatedAt());

            holder.mApplyTimeTV.setText(applyTime);
            holder.mGroupNameTV.setText(groupName);
            holder.mProblemTitleTV.setText(problemTitle);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProblemRevokeTable applyTable = mProblemRevokeTableList.get(position);
                    showRevokesInfoDetail(applyTable);
                    currentClickIndex = position;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mProblemRevokeTableList.size();
        }
    }

    private void showRevokesInfoDetail(ProblemRevokeTable applyTable) {
        String serializeObj = applyTable.toString();

        Intent intent = new Intent(getApplicationContext(), ShowApplyInfoDetail.class);
        intent.putExtra(ShowApplyInfoDetail.EXTRA_TAG, serializeObj);
        intent.putExtra(ShowApplyInfoDetail.EXTRA_TAG_MODE, 1); //模式为1，代表撤销申请处理
        startActivityForResult(intent, REQUEST_CODE_REVOKE);
    }

    private void showApplyInfoDetail(ProblemApplyTable applyTable) {
        String serializeObj = applyTable.toString();

        Intent intent = new Intent(getApplicationContext(), ShowApplyInfoDetail.class);
        intent.putExtra(ShowApplyInfoDetail.EXTRA_TAG, serializeObj);
        intent.putExtra(ShowApplyInfoDetail.EXTRA_TAG_MODE, 0); //模式为0，代表申请处理
        startActivityForResult(intent, REQUEST_CODE);
    }

    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ApplyProblem.this);
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

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    refreshApply();
                    break;

                case REQUEST_CODE_REVOKE:
                    refreshRevoke();
                    break;

                default:
                    break;
            }
        }
    }

    private void refreshApply() {
        if (currentClickIndex == -1) return;
        mProblemApplyTableList.remove(currentClickIndex);
        adapter.notifyDataSetChanged();
    }

    private void refreshRevoke() {
        if (currentClickIndex == -1) return;
        mProblemRevokeTableList.remove(currentClickIndex);
        mRevokeAdapter.notifyDataSetChanged();
    }
}
