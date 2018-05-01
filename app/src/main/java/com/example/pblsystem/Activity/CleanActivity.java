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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CleanActivity extends AppCompatActivity {
    public static final String EXTRA_TAG = "problem_group";

    private static Toast toast;
    public static final String TAG = "ShowAllEvalutions";

    private RecyclerView mApplysListView;
    private List<SpeechEvaluation> mAllEvalutions = new ArrayList<>();

    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

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
            setRecycleView();
            getDataFromNet(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        query.setLimit(500); // 只获取500项
        query.includePointer("problemGroup.problem");
        query.includePointer(SpeechEvaluation.S_OWNER);
        //query.orderByAscendingDB(SpeechEvaluation.CREATED_AT);
        query.orderByAscendingDB(SpeechEvaluation.S_OWNER);
        query.addWhereEqualTo(SpeechEvaluation.S_FLAG, 0); // 只获取学生评价
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
        if (totalShowItems % 500 == 0 && !isLoading) { // 说明还有列表项
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
        private TextView mScore, mTime, suggestionTV;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mScore = (TextView) itemView.findViewById(R.id.score);
            mTime = (TextView) itemView.findViewById(R.id.time);
            suggestionTV = (TextView) itemView.findViewById(R.id.suggestion);
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
            SpeechEvaluation evaluation = mAllEvalutions.get(position);
            AVUser user = (AVUser) evaluation.getsOwner();
            ProblemGroup problemGroup = (ProblemGroup) evaluation.getProblemGroup();
            Problem problem = (Problem) problemGroup.getProblem();
            Date speakTime = problem.getSpeakTime();
            String speakTimeStr = getTimeFromDate(speakTime);

            int score = evaluation.getsScore();
            Date time = evaluation.getCreatedAt();
            String timeStr = getTimeFromDate(time);
            String suggestion = evaluation.getsCommentText();

            boolean right = compareDate(time, speakTime);
            String showResult;
            if (right) {
                showResult = "合法";
            } else {
                showResult = "不合法";
            }

            boolean repeat = false;
            if (position  > 0) {
                SpeechEvaluation last_evaluation = mAllEvalutions.get(position-1);
                repeat = isRepeat(evaluation, last_evaluation);
            }

            holder.mScore.setText(score + "分");
            //holder.mTime.setText(speakTimeStr + "|" + timeStr);
            if (repeat) {
                holder.mTime.setText("重复" + timeStr);
                holder.mTime.setTextColor(Color.RED);
            } else {
                holder.mTime.setText(speakTimeStr + "|" + timeStr);
                holder.mTime.setTextColor(Color.BLUE);
            }
            //holder.suggestionTV.setText("建议: " + suggestion);
            if (user == null) {
                holder.suggestionTV.setText("孔");
                holder.suggestionTV.setTextColor(Color.YELLOW);
                holder.mSavedView.setBackgroundColor(Color.YELLOW);
            } else {
                holder.suggestionTV.setText(user.getObjectId());
                holder.suggestionTV.setTextColor(Color.BLACK);
                holder.mSavedView.setBackgroundColor(Color.GRAY);
            }
//            if (right) {
//                holder.suggestionTV.setTextColor(Color.BLUE);
//            } else {
//                holder.suggestionTV.setTextColor(Color.RED);
//            }

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowDetails(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllEvalutions.size();
        }
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
        mProgressBarDialog = new ProgressDialog(CleanActivity.this);
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
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }

    private boolean compareDate(Date date1, Date date2) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        c.setTime(date1);

        int year1 = c.get(Calendar.YEAR);
        int month1 = c.get(Calendar.MONTH) + 1;
        int day1 = c.get(Calendar.DAY_OF_MONTH);

        c.setTime(date2);

        int year2 = c.get(Calendar.YEAR);
        int month2 = c.get(Calendar.MONTH) + 1;
        int day2 = c.get(Calendar.DAY_OF_MONTH);

//        if (year1 >= year2) {
//            return true;
//        } else if (year1 == year2) {
//            if (month1 > month2) {
//                return true;
//            } else if (month1 == month2) {
//                if (day1 >= day2) {
//                    return true;
//                } else {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//
//        } else {
//            return false;
//        }
        if (month1 > month2) {
            return true;
        } else if (month1 == month2) {
            if (day1 >= day2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    private boolean isRepeat(SpeechEvaluation evaluation1, SpeechEvaluation evaluaiton2) {
        String id1 = evaluation1.getProblemGroup().getObjectId();
        String id2 = evaluaiton2.getProblemGroup().getObjectId();

        AVUser owner1 = (AVUser) evaluation1.getsOwner();
        AVUser owner2 = (AVUser) evaluaiton2.getsOwner();

        if (owner1 == null || owner2 == null) {
            return true;
        }

        String owner1Id = owner1.getObjectId();
        String owner2Id = owner2.getObjectId();

        if (id1.equals(id2) && owner1Id.equals(owner2Id)) {
            return true;
        } else {
            return false;
        }
    }

}
