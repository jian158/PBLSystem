package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SignUpCallback;
import com.example.pblsystem.Class.EvaluationStandard;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.RegisterTeacherApply;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CancelMessage;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.MyDecoration;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ShowAllEvalutions extends AppCompatActivity {
    public static final String EXTRA_TAG = "problem_group";

    private static Toast toast;
    public static final String TAG = "ShowAllEvalutions";

    private RecyclerView mApplysListView;
    private List<SpeechEvaluation> mAllEvalutions = new ArrayList<>();

    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private ProblemGroup mProblemGroup;

    private SwipeRefreshLayout refreshLayout;
    private int totalShowItems = 0; // 当前已经显示出的列表项
    private boolean isLoading = false;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_evalutions);
        try {
            initilizeProgressDialog();
            bindView();
            getIntentData();
            setRecycleView();
            getDataFromNet(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String str = intent.getStringExtra(EXTRA_TAG);
        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(str);
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet(boolean showDialog) {
        if (showDialog) {
            showProgressDialog("系统君正在拼命加载数据...");
        }
        DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
        query.setSkip(totalShowItems);  // 跳过已经加载出来的
        query.setLimit(20); // 只获取10项
        query.addWhereEqualTo(SpeechEvaluation.S_FLAG, 0); // 只获取学生评价
        query.addWhereEqualTo(SpeechEvaluation.S_PROBLEM_GROUP, mProblemGroup);
//        query.setLimit(500);    // 限制最多刷出500条记录
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                totalShowItems += results.size();
                isLoading = false;
                refreshLayout.setRefreshing(false);

                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    SpeechEvaluation apply = (SpeechEvaluation) obj;
                    mAllEvalutions.add(apply);
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
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        mApplysListView.setLayoutManager(manager);
        mApplysListView.addItemDecoration(new MyDecoration(this));
        mApplysListView.setAdapter(adapter);
        mApplysListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean canScroll = recyclerView.canScrollVertically(1);
                if (!canScroll) {
                    refresh();
                }

            }
        });
    }

    private void refresh() {
        if (totalShowItems % 20 == 0 && !isLoading) { // 说明还有列表项
            isLoading = true;
            getDataFromNet(false);
            refreshLayout.setRefreshing(true);
        }
    }

    /**
     * 绑定xml组件
     */
    private void bindView() {
        mApplysListView = (RecyclerView) findViewById(R.id.all_problems_recycle_view);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swrip_layout);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
            }
        });

    }



    /**
     * 自定义RecycleView的ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mScore, mTime, suggestionTV, mCommentTime;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mScore = (TextView) itemView.findViewById(R.id.score);
            mTime = (TextView) itemView.findViewById(R.id.time);
            suggestionTV = (TextView) itemView.findViewById(R.id.suggestion);
            mCommentTime = (TextView) itemView.findViewById(R.id.comment_time);
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
            View itemView = inflater.inflate(R.layout.evalution_detail_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final SpeechEvaluation evaluation = mAllEvalutions.get(position);
            int score = evaluation.getsScore();
            Date time = evaluation.getCreatedAt();
            String timeStr = getTimeFromDate(time);
            String suggestion = evaluation.getsCommentText();

            holder.mScore.setText(score + "分");
            holder.mTime.setText("举报");
            holder.mTime.setTextColor(Color.parseColor("#343433"));
            holder.mTime.setBackgroundColor(Color.parseColor("#9f54c0"));
            holder.suggestionTV.setText("建议: " + suggestion);
            holder.mCommentTime.setText(timeStr);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowDetails(position);
                }
            });

            holder.mTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopDialog.popMessageDialog(ShowAllEvalutions.this, "确定要举报这个评价么？", "点错了", "确定", new ConfirmMessage() {
                        @Override
                        public void confirm() {
                            report(evaluation);
                        }
                    }, null);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllEvalutions.size();
        }
    }

    private void report(SpeechEvaluation evaluation) {
        if (evaluation == null) return;

        evaluation.put("report", 1);
        manager.saveInBackGround(evaluation, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("举报成功");
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast("举报失败！系统故障！");
            }
        });
    }

    private void ShowDetails(int position) {
        SpeechEvaluation evalution = mAllEvalutions.get(position);
        String str = evalution.toString();
        Intent intent = new Intent(this, EvalutionScoreDetail.class);
        intent.putExtra(EvalutionScoreDetail.EXTRA_TAG, str);
        startActivity(intent);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowAllEvalutions.this);
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
        Log.d("tag", result);
        return result;
    }
}
