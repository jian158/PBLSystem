package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MySpeech extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "MySpeech";
    public static final int REQUEST_CODE_REVOKE = 1;

    private RecyclerView mMySpeechListView;
    private List<ProblemGroup> mMySpeechListViewData = new ArrayList<>();
    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_speech);

        initilizeProgressDialog();
        bindView();
        setRecycleView();
        getDataFromNet();
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
                    showToast("你尚未没有加入任何一个小组，快去寻找组织吧！");
                    dismissProgressDialog();
                } else {
                    Group group = (Group) results.get(0);
                    fiterMyProblem(group);
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

    private void fiterMyProblem(Group group) {
        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_GROUP, group);
        query.includePointer(ProblemGroup.S_PROBLEM);   //顺带查询problem
        query.includePointer(ProblemGroup.S_SPEAKER);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    ProblemGroup problem = (ProblemGroup) obj;
                    mMySpeechListViewData.add(problem);
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

    private void setRecycleView() {
        adapter = new MyAdapter();
        mMySpeechListView.setLayoutManager(new LinearLayoutManager(this));
        mMySpeechListView.setAdapter(adapter);
    }

    /**
     * 绑定xml组件
     */
    private void bindView() {
        mMySpeechListView = (RecyclerView) findViewById(R.id.my_problem_recycle_view);
    }

    /**
     * 自定义RecycleView的ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mProblemTitleTV, mProblemDifficutyTV, mProblemShowTimeTV;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mProblemDifficutyTV = (TextView) itemView.findViewById(R.id.apply_extra_info_tv);
            mProblemShowTimeTV = (TextView) itemView.findViewById(R.id.apply_time_info_tv);
            mProblemTitleTV = (TextView) itemView.findViewById(R.id.problem_title_tv);

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
            View itemView = inflater.inflate(R.layout.problems_list_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Problem problem = (Problem) mMySpeechListViewData.get(position).getProblem();
            String problemTitle = problem.getTitle();
            int problemDifficuty = problem.getDifficutity();
            Date problemShowTime = problem.getSpeakTime();
            String time = getTimeFromDate(problemShowTime);

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mProblemDifficutyTV.setText(String.valueOf(problemDifficuty));
            holder.mProblemShowTimeTV.setText(time);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProblemInfo(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mMySpeechListViewData.size();
        }
    }

    /**
     * 查看课题详情
     */
    private void showProblemInfo(int position) {
        String serializedProblemGroup = mMySpeechListViewData.get(position).toString();
        Intent intent = new Intent(getApplicationContext(), MyProblemInfo.class);
        intent.putExtra(ProblemInfo.PROBLEM_EXTRA_TAG, serializedProblemGroup);
        startActivityForResult(intent, REQUEST_CODE_REVOKE);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(MySpeech.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    refresh();
                    break;
                default:
                    break;
            }
        }

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
     * 刷新
     */
    private void refresh() {
        mMySpeechListViewData.clear();
        adapter.notifyDataSetChanged();
        getDataFromNet();
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
