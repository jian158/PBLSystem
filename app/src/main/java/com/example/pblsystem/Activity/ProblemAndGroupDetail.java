package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ProblemAndGroupDetail extends AppCompatActivity {
    /*常量，调试的Tag值*/
    private static final String TAG = "ProblemAndGroupDetail";
    /*Toast静态常量*/
    private static Toast toast;

    public static final String PROBLEM_GROUP_EXTRA_TAG = "prolem_group_xtra";

    public TextView mProblemTV, mGroupTV, mSpeakerTV, mSpeakerTimeTV, mSchedule, mShowProblem;
    public TextView mShowScoreTV;

    private ProblemGroup mProblemGroup;
    private Problem mProblem;
    private Group mMyGroup;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_and_group_detail);
        try {
            bindView();
            getIntentData();
            initializeUI();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void bindView() {
        mProblemTV = (TextView) findViewById(R.id.problem);
        mGroupTV = (TextView) findViewById(R.id.group);
        mSpeakerTV = (TextView) findViewById(R.id.speaker);
        mSpeakerTimeTV = (TextView) findViewById(R.id.speak_time);
        mSchedule = (TextView) findViewById(R.id.schedule);
        mShowProblem = (TextView) findViewById(R.id.show_group_info);
        mShowProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGroupInfo();
            }
        });
        mShowScoreTV = (TextView) findViewById(R.id.show_score);
        mShowScoreTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showScore();
            }
        });
    }

    /**
     *  查看得分情况
     */
    private void showScore() {
        Intent intent = new Intent(this, MyScore.class);
        if (mProblemGroup == null) {
            return;
        }

        intent.putExtra(MyScore.EXTRA_TAG, mProblemGroup.toString());
        startActivity(intent);
    }

    private void showGroupInfo() {
        Group group = (Group) mProblemGroup.getGroup();
        if (group == null) return;

        Intent intent = new Intent(this, ShowGroupInfo.class);
        intent.putExtra(ShowGroupInfo.EXTRA_TAG, group.toString());
        startActivity(intent);
    }


    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializedProblem = intent.getStringExtra(PROBLEM_GROUP_EXTRA_TAG);

        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(serializedProblem);
    }

    private void initializeUI() {
        Group group = (Group) mProblemGroup.getGroup();
        Problem problem = (Problem) mProblemGroup.getProblem();
        AVUser speaker = (AVUser) mProblemGroup.getsSpeaker();

        String problemName = problem.getTitle();
        String groupName = group.getName();
        String speakerName;
        if (speaker != null) {
            speakerName = speaker.getString(MyUser.S_NAME) + "  (" + speaker.getUsername() + ")";
        } else {
            speakerName = "暂无";
        }

        String speakerTime = getTimeFromDate(problem.getSpeakTime());
        int schedule = mProblemGroup.getSchedule();
        String schedultStr = SetMySpeechProgress.STEP_DESCRIPTION[schedule];

        mProblemTV.setText(problemName);
        mGroupTV.setText(groupName);
        mSpeakerTV.setText(speakerName);
        mSpeakerTimeTV.setText(speakerTime);
        mSchedule.setText(schedultStr);

    }


    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
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
}
