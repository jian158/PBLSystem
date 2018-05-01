package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SetProblemInfo extends AppCompatActivity {

    /*常量，调试的Tag值*/
    private static final String TAG = "MyGroupActivity";
    /*Toast静态常量*/
    private static Toast toast;

    public static final String PROBLEM_EXTRA_TAG = "prolem_xtra";
    public static final int REQUEST_CODE = 2;

    public TextView mProblemTitleTV, mProblemIntroductionTV, mProblemDifficutyTV, mProblemTimeTV, mProblemApplyTimes;
    public ImageView config;
    private Problem mProblem;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_problem_info);

        try {
            initilizeProgressDialog();
            bindView();
            getIntentData();
            getDataFromNet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializedProblem = intent.getStringExtra(PROBLEM_EXTRA_TAG);
        mProblem = (Problem) AVObject.parseAVObject(serializedProblem);
    }


    private void initializeUI() {
        String title = mProblem.getTitle();
        String introduction = mProblem.getIntroduction();
        int difficuty = mProblem.getDifficutity();
        Date showTime = mProblem.getSpeakTime();
        String dateStr = getTimeFromDate(showTime);
        int times = mProblem.getTimes();

        mProblemTitleTV.setText(title);
        mProblemIntroductionTV.setText(introduction);
        mProblemDifficutyTV.setText(String.valueOf(difficuty) + "星");
        mProblemTimeTV.setText(String.valueOf(dateStr));
        mProblemApplyTimes.setText(times + "次");
    }




    /**
     * 绑定组件
     */
    private void bindView() {
        mProblemTitleTV = (TextView) findViewById(R.id.problem_title_tv);
        mProblemIntroductionTV = (TextView) findViewById(R.id.group_name_tv);
        mProblemDifficutyTV = (TextView) findViewById(R.id.apply_extra_info_tv);
        mProblemTimeTV = (TextView) findViewById(R.id.apply_time_info_tv);
        mProblemApplyTimes = (TextView) findViewById(R.id.max_times);
        config = (ImageView) findViewById(R.id.config);

        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProblem();
            }
        });

    }

    private void updateProblem() {
        Intent intent = new Intent(this, CreateNewProblem.class);
        String serilizeStr = mProblem.toString();
        intent.putExtra(CreateNewProblem.EXTRA_TAG, serilizeStr);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(SetProblemInfo.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    /**
     * 弹出对话框
     */
    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {//刷新页面
                getDataFromNet();
                setResult(RESULT_OK);
            }
        }
    }

    private void getDataFromNet() {
        showProgressDialog("读取数据中...");

        manager.fetchInBackGround(mProblem, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                mProblem = (Problem) obj;
                initializeUI();

                dismissProgressDialog();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }
}
