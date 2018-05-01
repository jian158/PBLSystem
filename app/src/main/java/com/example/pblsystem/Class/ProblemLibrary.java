package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 郭聪 on 2017/3/6.
 */
@AVClassName("ProblemLibrary")
public class ProblemLibrary extends AVObject {
    public ProblemLibrary() {};

    public static final String CLASS_NAME = "ProblemLibrary";

    //题目
    public static final String S_TITLE = "title";
    //简介
    public static final String S_INTRODUCTION = "introduction";
    //演讲时间
    public static final String S_SPEAK_TIME = "speakTime";
    //课题难度值
    public static final String S_DIFFICUTITY = "difficuty";
    //课题可供申请的次数
    public static final String S_TIMES = "times";


    public void setTitle(String title) {
        put(S_TITLE, title);
    }

    public void setIntroduction(String introduction) {
        put(S_INTRODUCTION, introduction);
    }


    public void setSpeakTime(Date time) {
        put(S_SPEAK_TIME, time);
    }

    public void setTimes(int times) {
        put(S_TIMES, times);
    }

    public void setDifficutity(int difficutity) {
        put(S_DIFFICUTITY, difficutity);
    }


    public int getDifficutity() {
        return getInt(S_DIFFICUTITY);
    }


    public String getIntroduction() {
        String result;
        result = getString(S_INTRODUCTION);
        if (result == null) {
            result = "null";
        }

        return result;
    }

    public Date getSpeakTime() {
        return getDate(S_SPEAK_TIME);
    }

    public int getTimes() {
        return getInt(S_TIMES);
    }

    public String getTitle() {
        String result;
        result = getString(S_TITLE);
        if (result == null) {
            result = "null";
        }

        return result;
    }
}
