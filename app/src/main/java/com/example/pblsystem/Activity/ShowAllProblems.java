package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

public class ShowAllProblems extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "ShowAllProblems";
    public static final int REQUEST_CODE_TEACHER = 2;
    public static final int REQUST_CODE_CREATE_PROBLEM = 3;

    private RecyclerView mAllProblemsListView;
    private List<Problem> mAllProblemsDataList = new ArrayList<>();
    private MyAdapter adapter;

    /*题库选择课题*/
    private TextView selectProblem;

    // 下拉刷新
    private SwipeRefreshLayout refreshLayout;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    private FloatingActionButton createBtn;

    private int authority=0;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_problems);

        initilizeProgressDialog();
        bindView();
        setRecycleView();
        getDataFromNet();
        fiterAllProblemOfMyClass();
    }

    private void fiterAllProblemOfMyClass() {
        AVQuery<AVObject> query=new AVQuery<>(Problem.CLASS_NAME);
//        DataBaseQuery query = new DataBaseQuery(Problem.CLASS_NAME);
//        query.addWhereEqualTo(Problem.S_CLASS, myClass);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list.size() < 1) {
                    return;
                }
                Log.i("Size",String.valueOf(list.size()));
                for (Object obj: list) {
                    Problem problem = (Problem) obj;
                    Log.i("Name",problem.getTitle());
                }
            }
        });
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("系统君正在拼命加载数据...");

        //如果是教师用户
        authority = AVUser.getCurrentUser().getInt(MyUser.S_AUTHORITY);
        if (authority != 0) {
            //开启按钮
            createBtn.setVisibility(View.VISIBLE);
            selectProblem.setVisibility(View.VISIBLE);
            fiterAllProblemOfMyClass((ClassRoom) LoginActivity.sSelectedClass.getTargetClass());
            return;
        }

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                fiterAllProblemOfMyClass(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
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

    private void fiterAllProblemOfMyClass(ClassRoom myClass) {
        DataBaseQuery query = new DataBaseQuery(Problem.CLASS_NAME);
        query.addWhereEqualTo(Problem.S_CLASS, myClass);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    Problem problem = (Problem) obj;
                    mAllProblemsDataList.add(problem);
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
        createBtn = (FloatingActionButton) findViewById(R.id.create_problem);
        createBtn.setVisibility(View.GONE);     //默认隐藏按钮



        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowAllProblems.this, CreateNewProblem.class);
                startActivityForResult(intent, REQUST_CODE_CREATE_PROBLEM);
            }
        });

        selectProblem = (TextView) findViewById(R.id.select_problem);
        selectProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProblemsOfLibrary();
            }
        });
        selectProblem.setVisibility(View.GONE); // 默认隐藏


        // 设置下拉刷新
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light

        );
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();

            }
        });
    }

    private void showProblemsOfLibrary() {
        Intent intent = new Intent(this, ShowLibrarayProblems.class);
        startActivity(intent);
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
            final Problem problem = mAllProblemsDataList.get(position);

            String problemTitle = problem.getTitle();
            int problemDifficuty = problem.getDifficutity();
            Date problemShowTime = problem.getSpeakTime();
            String time = getTimeFromDate(problemShowTime);

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mProblemDifficutyTV.setText(String.valueOf(problemDifficuty) + "星");
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
            return mAllProblemsDataList.size();
        }
    }

//    private void deleteProblem(Problem problem){
//        ProblemLibrary library = new ProblemLibrary();
//
//        library.setTitle(problem.getTitle());
//        library.setIntroduction(problem.getIntroduction());
//        library.setDifficutity(problem.getDifficutity());
//        library.setTimes(problem.getTimes());
//        library.setSpeakTime(problem.getSpeakTime());
//        /*后台删除数据*/
//        manager.deleteInBackGround(library);
//    }

    /**
     * 查看课题详情
     */
    private void showProblemInfo(int position) {
        if (isTheStudent()) {
            String serializedProblem = mAllProblemsDataList.get(position).toString();
            Intent intent = new Intent(getApplicationContext(), ProblemInfo.class);
            intent.putExtra(ProblemInfo.PROBLEM_EXTRA_TAG, serializedProblem);
            startActivity(intent);
        } else {
            String serializedProblem = mAllProblemsDataList.get(position).toString();
            Intent intent = new Intent(getApplicationContext(), SetProblemInfo.class);
            intent.putExtra(ProblemInfo.PROBLEM_EXTRA_TAG, serializedProblem);
            startActivityForResult(intent, REQUEST_CODE_TEACHER);
        }

    }

    private boolean isTheStudent() {
        AVUser user = AVUser.getCurrentUser();
        int authority = user.getInt(MyUser.S_AUTHORITY);
        if (authority == 0) {
            return true;
        }

        return false;
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowAllProblems.this);
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
        Log.d("AllProblemtag", result);
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TEACHER) {
                refresh();
            }

            if (requestCode == REQUST_CODE_CREATE_PROBLEM) {
                refresh();
            }
        }
    }

    private void refresh() {
        mAllProblemsDataList.clear();
        adapter.notifyDataSetChanged();
        getDataFromNet();

        refreshLayout.setRefreshing(false);
    }
}
