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

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ProblemAndGroup extends AppCompatActivity {

    private static Toast toast;
    public static final String TAG = "ShowAllProblems";

    private RecyclerView mAllProblemsListView;
    private List<ProblemGroup> mAllProblemGroupDataList = new ArrayList<>();
    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_and_group);

        initilizeProgressDialog();
        try {
            bindView();
            setRecycleView();
            getDataFromNet();
        } catch (NullPointerException e) {
            e.getMessage();
        }
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("系统君正在拼命加载数据...");

        if (LoginActivity.sSelectedClass == null) {
            showToast("你还没有创建课堂！");
            return;
        }

        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        fiterAllProblemOfMyClass(classRoom);
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void fiterAllProblemOfMyClass(ClassRoom myClass) {
        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_CLASS, myClass);
        query.includePointer(ProblemGroup.S_PROBLEM);
        query.includePointer(ProblemGroup.S_GROUP);
        query.includePointer(ProblemGroup.S_SPEAKER);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    ProblemGroup problemGroup = (ProblemGroup) obj;
                    mAllProblemGroupDataList.add(problemGroup);
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
        mAllProblemsListView.setLayoutManager(new LinearLayoutManager(this));
        mAllProblemsListView.setAdapter(adapter);
    }

    /**
     * 绑定xml组件
     */
    private void bindView() {
        mAllProblemsListView = (RecyclerView) findViewById(R.id.all_problems_recycle_view);
    }

    /**
     * 自定义RecycleView的ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mProblemTitleTV, mGroupNameTV, mProblemShowTimeTV;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mGroupNameTV = (TextView) itemView.findViewById(R.id.group_name);
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
            View itemView = inflater.inflate(R.layout.problem_group_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Problem problem = (Problem) mAllProblemGroupDataList.get(position).getProblem();
            Group group = (Group) mAllProblemGroupDataList.get(position).getGroup();

            String problemTitle;
            String groupName;

            if (group == null) {
                groupName = "无效的小组";
            } else {
                groupName = group.getName();
            }

            if (problem == null) {
                problemTitle = "无效的课题";
            } else {
                problemTitle = problem.getTitle();
            }

            Date problemShowTime = problem.getSpeakTime();
            String time = getTimeFromDate(problemShowTime);

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mGroupNameTV.setText(groupName);
            holder.mProblemShowTimeTV.setText(time);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTheDetail(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllProblemGroupDataList.size();
        }


    }

    private void showTheDetail(int position) {
        Intent intent = new Intent(this, ProblemAndGroupDetail.class);
        ProblemGroup problemGroup = mAllProblemGroupDataList.get(position);
        String serializeStr = problemGroup.toString();
        intent.putExtra(ProblemAndGroupDetail.PROBLEM_GROUP_EXTRA_TAG, serializeStr);
        startActivity(intent);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ProblemAndGroup.this);
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

    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }
}
