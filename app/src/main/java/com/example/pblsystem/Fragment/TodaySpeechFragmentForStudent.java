package com.example.pblsystem.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Activity.Evalution;
import com.example.pblsystem.Activity.ShowGroupInfo;
import com.example.pblsystem.Activity.SpeechInteraction;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.SpeechComment;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by 郭聪 on 2017/3/6.
 */
public class TodaySpeechFragmentForStudent extends Fragment {
    //每个Fragment都保留一个Problem实例
    private ProblemGroup mProblemGroup;
    private Group mGroup;
    private Problem mProblem;
    private AVUser mSpeaker;

    private TextView mProblemTitle;
    private TextView mSpeakTime;
    private TextView mGroupName;
    private TextView mSpeakerTV;
    private TextView mSeeGroupDetail;
    private TextView mScore;
    private TextView mComment;

    private View mSavedView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_speech, container, false);
        mSavedView = view;
        try {
            bindView();
            setFragmentUI();

        } catch (NullPointerException e) {  //防止出现空指针异常
            e.getMessage();
        } finally {
            return view;
        }
    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mProblemTitle = (TextView) mSavedView.findViewById(R.id.problem_title_tv);
        mSpeakerTV = (TextView) mSavedView.findViewById(R.id.speaker_tv);
        mGroupName = (TextView) mSavedView.findViewById(R.id.group_name);
        mSpeakTime = (TextView) mSavedView.findViewById(R.id.speaker_time);
        mSeeGroupDetail = (TextView) mSavedView.findViewById(R.id.see_group_detail);
        mScore = (TextView) mSavedView.findViewById(R.id.score);

        mSeeGroupDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGroupInfo();
            }
        });

        mScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScore();
            }
        });

        mComment = (TextView) mSavedView.findViewById(R.id.comment);
        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment();
            }
        });
    }

    private void comment() {
        Intent intent = new Intent(getActivity(), SpeechInteraction.class);
        String problemGroupStr = mProblemGroup.toString();
        intent.putExtra(SpeechInteraction.EXTRA, problemGroupStr);
        startActivity(intent);
    }

    private void setScore() {
        Intent intent = new Intent(getActivity(), Evalution.class);
        String str = mProblemGroup.toString();
        intent.putExtra(Evalution.EXTRA_TAG, str);
        startActivity(intent);
    }

    private void showGroupInfo() {
        if (mGroup == null) {
            return;
        }

        String serializeStr = mGroup.toString();
        Intent intent = new Intent(getActivity(), ShowGroupInfo.class);
        intent.putExtra(ShowGroupInfo.EXTRA_TAG, serializeStr);
        startActivity(intent);
    }

    /**
     * 初始化mProblemObj
     */
    public void setProblemObj(ProblemGroup obj) {
        mProblemGroup = obj;
    }

    /**
     *设置Fragment内容
     */
    private void setFragmentUI() {
        mProblem = (Problem) mProblemGroup.getProblem();
        mGroup = (Group) mProblemGroup.getGroup();
        mSpeaker = (AVUser) mProblemGroup.getsSpeaker();
        String group_name;
        if (mGroup != null) {
            group_name = mGroup.getName();
        } else {
            group_name = "无效的小组";
        }

        String title = mProblem.getTitle();

        String speakerUsername, speakerName;
        if (mSpeaker != null) {
            speakerUsername = mSpeaker.getUsername();
            speakerName = (String) mSpeaker.get(MyUser.S_NAME);
        } else {
            speakerUsername = "无";
            speakerName = "";
        }
        String time = getTimeFromDate(mProblem.getSpeakTime());

        mProblemTitle.setText(title);
        mGroupName.setText(group_name);
        mSpeakerTV.setText(speakerName + "  (" + speakerUsername + ")");
        mSpeakTime.setText(time);
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
}
