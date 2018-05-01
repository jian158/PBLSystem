package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.PopWindow.PopWindow;
import com.example.pblsystem.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class SetMySpeechProgress extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "SetMySpeehProgress";
    public static final String EXTRA_TAG = "problem_group";

    private ProgressDialog mProgressBarDialog;


    private TextView mCurrentStepTV, mNextStepTV, mPromptTV;
    private ImageView mGoNextButton;

    private ProblemGroup mProblemGroup;

    private DataBaseManager manager = DataBaseManager.getInstance();

    public static final String[] STEP_DESCRIPTION = new String[]{
        "前期准备", "课题研究", "成果展示", "完成"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_my_speech_progress);

        try {
            bindView();
            initilizeProgressDialog();
            getIntentData();
            getDataFromNet();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 绑定xml布局组件
     */
    private void bindView() {
        mCurrentStepTV = (TextView) findViewById(R.id.current_step_tv);
        mNextStepTV = (TextView) findViewById(R.id.next_step_tv);
        mPromptTV = (TextView) findViewById(R.id.prompt_text);
        mGoNextButton = (ImageView) findViewById(R.id.doSomething);
        mGoNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopWindow window = new PopWindow(SetMySpeechProgress.this);
                window.showAtLocation(SetMySpeechProgress.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
            }
        });
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(SetMySpeechProgress.this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    public void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    public void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializedProblemGroup = intent.getStringExtra(EXTRA_TAG);
        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(serializedProblemGroup);
    }


    /**
     *网络获取数据
     */
    private void getDataFromNet() {
        //刷新变量
        refreshProblemGroupObj();
    }

    private void refreshProblemGroupObj() {
        showProgressDialog("系统君正在拼命加载数据..");

        manager.fetchIfNeededInBackGround(mProblemGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                mProblemGroup = (ProblemGroup) obj;
                checkIfNeedUpdateSchedule();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }


    /**
     * 根据当前日期切换演讲的进度
     */
    private void checkIfNeedUpdateSchedule() {
        Problem problem = (Problem) mProblemGroup.getProblem();
        manager.fetchIfNeededInBackGround(problem, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                Problem problem = (Problem) obj;
                updateScheduleByDate(problem);

            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });

    }

    private void updateScheduleByDate(Problem problem) {
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

        if (currentYear >= speakYear && currentMonth >= speakMonth && currrentDay >= speakDay && (mProblemGroup.getSchedule()) < 2) {//时间超越
            update();
            Log.d("tag", "更新");
        } else {
            initializeUI(mProblemGroup.getSchedule());
            dismissProgressDialog();
        }

    }

    /**
     * 初始化UI
     */
    private void initializeUI(int schedule) {
        String currentStep = STEP_DESCRIPTION[schedule];
        String nextStep;
        if (schedule == 3) {
            nextStep = "已经到达最后一个阶段";
        } else {
            nextStep = STEP_DESCRIPTION[schedule + 1];
        }

        mCurrentStepTV.setText(currentStep);
        mNextStepTV.setText(nextStep);
    }

    private void update() {
        mProblemGroup.setSchedule(2);
        manager.saveInBackGround(mProblemGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                initializeUI(2);
                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                dismissProgressDialog();
            }
        });
    }


    public ProblemGroup getmProblemGroup() {
        return mProblemGroup;
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
        dismissProgressDialog();    //销毁对话框，防止窗体泄露
    }
}
