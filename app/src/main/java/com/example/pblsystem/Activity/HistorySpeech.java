package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Fragment.TodaySpeechFragmentEmpty;
import com.example.pblsystem.Fragment.TodaySpeechFragmentForStudent;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 该acitivity与showAllProblem一致
 */

public class HistorySpeech extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "HistorySpeech";

    private RecyclerView mAllProblemsListView;
    private List<ProblemGroup> mAllProblemGroupDataList = new ArrayList<>();
    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_speech);

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
     * 绑定xml组件
     */
    private void bindView() {
        mAllProblemsListView = (RecyclerView) findViewById(R.id.all_problems_recycle_view);
    }

    private void setRecycleView() {
        adapter = new MyAdapter();
        mAllProblemsListView.setLayoutManager(new LinearLayoutManager(this));
        mAllProblemsListView.setAdapter(adapter);
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("系统君正在拼命加载数据...");

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                fiterHistoryProblemOfMyClass(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void fiterHistoryProblemOfMyClass(ClassRoom myClass) {
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
                        //将新建的Fragment加入ViewPager数据源
                        mAllProblemGroupDataList.add(problemGroup);
                    }
                }
                adapter.notifyDataSetChanged();
                dismissProgressDialog();

            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
            }
        });
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
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            Problem problem = (Problem) mAllProblemGroupDataList.get(position).getProblem();
            Group group = (Group) mAllProblemGroupDataList.get(position).getGroup();
            String groupName;

            if (group == null) {
                groupName = "无效的小组";
            } else {
                groupName = group.getName();
            }

            String problemTitle = problem.getTitle();
            Date problemShowTime = problem.getSpeakTime();
            //String time = getTimeFromDate(problemShowTime);
            holder.mProblemShowTimeTV.setText("检测中");
            // 后台查询是否评价过
            DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
            query.addWhereEqualTo(SpeechEvaluation.S_OWNER, AVUser.getCurrentUser());   // 我的评价
            query.addWhereEqualTo(SpeechEvaluation.S_PROBLEM_GROUP, mAllProblemGroupDataList.get(position));
            query.countInBackgroundDB(new CountCallBackDB() {
                @Override
                public void CountDoneSuccessful(int number) {
                    if (number <= 0) {
                        holder.mProblemShowTimeTV.setText("未评价");
                        holder.mProblemShowTimeTV.setTextColor(Color.GRAY);
                    } else {
                        holder.mProblemShowTimeTV.setText("已评价");
                        holder.mProblemShowTimeTV.setTextColor(Color.GREEN);
                    }
                }

                @Override
                public void CountDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mGroupNameTV.setText(groupName);
            //holder.mProblemShowTimeTV.setText(time);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击事件
                    enterEvalutionPage(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllProblemGroupDataList.size();
        }


    }

    /**
     * 进入评价页面
     */
    private void enterEvalutionPage(int position) {
        Intent intent = new Intent(this, Evalution.class);
        ProblemGroup problemGroup = mAllProblemGroupDataList.get(position);
        String str = problemGroup.toString();
        intent.putExtra(Evalution.EXTRA_TAG, str);
        startActivity(intent);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(HistorySpeech.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
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
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
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

        if (currentYear > speakYear) {
            return true;
        } else if (currentYear == speakYear) {
            if (currentMonth > speakMonth) {
                return true;
            } else if (currentMonth == speakMonth) {
                if (currrentDay >= speakDay) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
