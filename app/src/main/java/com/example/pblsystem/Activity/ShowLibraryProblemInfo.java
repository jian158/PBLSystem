package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ShowLibraryProblemInfo extends AppCompatActivity {
    private static Toast toast;
    /*传递Intent使用的key值*/
    public static final String KEY_INTENT = "problem_library";
    /*标题*/
    private TextView problemTitleTV;
    /*简介*/
    private TextView problemIntroductionTV;
    /*难度*/
    private TextView problemDifficutyTV;
    /*演讲时间*/
    private TextView problemSpeakTimeTV;
    /*添加至我的课堂*/
    private Button addToMyClass;
    /*Intent传递的ProblemLibrary对象*/
    private ProblemLibrary mProblem;

    private ProgressDialog mProgressBarDialog;
    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_library_problem_info);
        try {
            initilizeProgressDialog();
            bindView();
            getIntentData();
            initializeUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindView() {
        problemTitleTV = (TextView) findViewById(R.id.problem_title_tv);
        problemIntroductionTV = (TextView) findViewById(R.id.problem_introduction);
        problemDifficutyTV = (TextView) findViewById(R.id.problem_difficuty);
        problemSpeakTimeTV = (TextView) findViewById(R.id.problem_speaker_time);
        addToMyClass = (Button) findViewById(R.id.add_to_my_class);
        addToMyClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopDialog.popMessageDialog(ShowLibraryProblemInfo.this, "你确定要将该课题添加至我的课堂？", "点错了", "确定",
                        new ConfirmMessage() {
                            @Override
                            public void confirm() {
                                addProblemToMyClass();
                            }
                        }, null);

            }
        });
    }

    private void addProblemToMyClass() {
        if (LoginActivity.sSelectedClass == null) return;
        if (mProblem == null) return;

        checkTheProblemTitle();
    }

    /**
     * 检测是否存在同名课题
     */
    private void checkTheProblemTitle() {
        showProgressDialog("正在检测数据合法性...");
        ClassRoom myClass = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(Problem.CLASS_NAME);
        query.addWhereEqualTo(Problem.S_CLASS, myClass);
        query.addWhereEqualTo(Problem.S_TITLE, mProblem.getTitle());
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    Log.d("tag", "存在同名课题");
                    showToast("添加失败！你所在的课堂已经存在同名课题！");
                    dismissProgressDialog();
                } else {
                    createOneProblemObjAndSave();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                if (errorCode != 101) {
                    Log.d("tag", exceptionMsg);
                    dismissProgressDialog();
                } else {
                    createOneProblemObjAndSave();
                }
            }
        });
    }

    private void createOneProblemObjAndSave() {
        showProgressDialog("正在添加课题...");
        Problem problem = createProblem(mProblem);
        manager.saveInBackGround(problem, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("课题添加成功！");
                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private Problem createProblem(ProblemLibrary mProblem) {
        ClassRoom myClass = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        Problem problem = new Problem();
        problem.setOfClass(myClass);
        problem.setTitle(mProblem.getTitle());
        problem.setIntroduction(mProblem.getIntroduction());
        problem.setSpeakTime(mProblem.getSpeakTime());
        problem.setDifficutity(mProblem.getDifficutity());
        problem.setTimes(mProblem.getTimes());  // bug 已修复
        return problem;
    }

    private void getIntentData() throws Exception {
        String str = getIntent().getStringExtra(KEY_INTENT);
        mProblem = (ProblemLibrary) AVObject.parseAVObject(str);
    }

    private void initializeUI() {
        if (mProblem == null) return;

        problemTitleTV.setText(mProblem.getTitle());
        problemIntroductionTV.setText(mProblem.getIntroduction());
        problemDifficutyTV.setText(mProblem.getDifficutity() + "星");
        problemSpeakTimeTV.setText(getTimeFromDate(mProblem.getSpeakTime()));
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
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowLibraryProblemInfo.this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

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
        dismissProgressDialog();    //销毁对话框，防止窗体泄露
    }
}
