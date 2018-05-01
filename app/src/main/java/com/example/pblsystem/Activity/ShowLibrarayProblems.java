package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ShowLibrarayProblems extends AppCompatActivity {

    private static Toast toast;
    public static final String TAG = "ShowLibrarayProblems";

    private RecyclerView mAllProblemsListView;
    private List<ProblemLibrary> mAllProblemsDataList = new ArrayList<>();
    private List<ProblemLibrary> mSavedData = mAllProblemsDataList; //保存原始数据

    private MyAdapter adapter;

    private EditText search;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_libraray_problems);

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
        DataBaseQuery query = new DataBaseQuery(ProblemLibrary.CLASS_NAME);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    ProblemLibrary library = (ProblemLibrary) obj;
                    mAllProblemsDataList.add(library);
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

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
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
        search = (EditText) findViewById(R.id.label);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("tag", "搜索");
                String searchStr = editable.toString();
                if (TextUtils.isEmpty(searchStr)) {
                    mAllProblemsDataList = mSavedData;
                } else {
                    mAllProblemsDataList = searchSpecificProblem(searchStr);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private List<ProblemLibrary> searchSpecificProblem(String searchStr) {
        List<ProblemLibrary> specificProblems = new ArrayList<>();
        for (ProblemLibrary obj: mSavedData) {
            String title = obj.getTitle();
            if (title.indexOf(searchStr) != -1) {//找到子串
                specificProblems.add(obj);
            }
        }

        return specificProblems;
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
            ProblemLibrary problem = mAllProblemsDataList.get(position);
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

    private void showProblemInfo(int position) {
        String str = mAllProblemsDataList.get(position).toString();
        Intent intent = new Intent(this, ShowLibraryProblemInfo.class);
        intent.putExtra(ShowLibraryProblemInfo.KEY_INTENT, str);
        startActivity(intent);
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowLibrarayProblems.this);
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
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("Tag", result);
        return result;
    }

}
