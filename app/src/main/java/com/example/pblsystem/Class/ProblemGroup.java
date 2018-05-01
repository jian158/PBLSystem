package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪 on 2017/3/16.
 */
@AVClassName("ProblemGroup")
public class ProblemGroup extends AVObject {
    public static final String CLASS_NAME = "ProblemGroup";

    public static final String S_CLASS = "ofClass";
    public static final String S_PROBLEM = "problem";
    public static final String S_GROUP = "group";
    public static final String S_SPEAKER = "speaker";
    public static final String S_FILE = "file";
    public static final String S_MEMBER_WORK = "memberWork";
    public static final String S_SCHEDULE = "schedule";

    public void setClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setProblem(AVObject obj) {
        put(S_PROBLEM, obj);
    }

    public void setGroup(AVObject obj) {
        put(S_GROUP, obj);
    }

    public void setSpeaker(AVObject obj) {
        put(S_SPEAKER, obj);
    }

    public void setFile(AVObject obj) {
        put(S_FILE, obj);
    }

    public void setSchedule(int obj) {
        put(S_SCHEDULE, obj);
    }

    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }

    public AVObject getFile() {
        return getAVObject(S_FILE);
    }

    public AVObject getGroup() {
        return getAVObject(S_GROUP);
    }

    public AVObject getProblem() {
        return getAVObject(S_PROBLEM);
    }

    public int getSchedule() {
        return getInt(S_SCHEDULE);
    }

    public AVObject getsSpeaker() {
        return getAVObject(S_SPEAKER);
    }
}
