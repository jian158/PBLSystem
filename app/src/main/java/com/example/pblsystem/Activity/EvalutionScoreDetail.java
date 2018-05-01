package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLogTags;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.example.pblsystem.Class.DescriptionScore;
import com.example.pblsystem.Class.EvalutionDescription;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.MyDecoration;
import java.util.ArrayList;
import java.util.List;

public class EvalutionScoreDetail extends AppCompatActivity {
    public static final String EXTRA_TAG = "evalution";

    private static Toast toast;
    public static final String TAG = "EvalutionScoreDetail";

    private RecyclerView mApplysListView;
    private List<DescriptionScore> mAllDetails = new ArrayList<>();

    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private SpeechEvaluation mEvalution;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evalution_score_detail);
        try {
            initilizeProgressDialog();
            bindView();
            getIntentData();
            setRecycleView();
            getDataFromNet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String str = intent.getStringExtra(EXTRA_TAG);
        mEvalution = (SpeechEvaluation) AVObject.parseAVObject(str);
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("系统君正在拼命加载数据...");
        DataBaseQuery query = new DataBaseQuery(DescriptionScore.CLASS_NAME);
        query.addWhereEqualTo(DescriptionScore.S_SPEECH_EVALUTION, mEvalution);
        query.includePointer(DescriptionScore.S_DESCRIPTION);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    DescriptionScore apply = (DescriptionScore) obj;
                    mAllDetails.add(apply);
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
        mApplysListView.setLayoutManager(new LinearLayoutManager(this));
        mApplysListView.addItemDecoration(new MyDecoration(this));
        mApplysListView.setAdapter(adapter);
    }

    /**
     * 绑定xml组件
     */
    private void bindView() {
        mApplysListView = (RecyclerView) findViewById(R.id.all_problems_recycle_view);
    }



    /**
     * 自定义RecycleView的ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mScore, mTitle, suggestionTV;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.score);
            mScore = (TextView) itemView.findViewById(R.id.time);
            suggestionTV = (TextView) itemView.findViewById(R.id.suggestion);
            suggestionTV.setVisibility(View.GONE);

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
            View itemView = inflater.inflate(R.layout.evaluation_items_detail, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            DescriptionScore descriptionScore = mAllDetails.get(position);
            int score = descriptionScore.getScore();
            EvalutionDescription description = (EvalutionDescription) descriptionScore.getDescription();
            String title = description.getDescriptionTitle();

            holder.mScore.setText(score + "分");
            holder.mTitle.setText(title);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ShowDetails(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllDetails.size();
        }
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(EvalutionScoreDetail.this);
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
