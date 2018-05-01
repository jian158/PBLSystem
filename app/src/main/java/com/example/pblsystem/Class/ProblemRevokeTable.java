package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/3/26.
 */
@AVClassName("ProblemRevokeTable")
public class ProblemRevokeTable extends AVObject {
    public static final String CLASS_NAME = "ProblemRevokeTable";

    public static final String S_CLASS = "ofClass";
    public static final String S_PROBLEM = "problem";
    public static final String S_GROUP = "group";
    public static final String S_STATE = "state";
    public static final String S_EXTRA = "extraInfo";


    public void setOfClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setProblem(AVObject obj) {
        put(S_PROBLEM, obj);
    }

    public void setGroup(AVObject obj) {
        put(S_GROUP, obj);
    }

    public void setState(int state) {
        put(S_STATE, state);
    }

    public void setExtraInfo(String info) {
        put(S_EXTRA, info);
    }

    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }

    public AVObject getProblem() {
        return getAVObject(S_PROBLEM);
    }

    public AVObject getGroup() {
        return getAVObject(S_GROUP);
    }

    public int getState() {
        return getInt(S_STATE);
    }

    public String getExtraInfo() {
        String extraInfo;
        extraInfo = getString(S_EXTRA);
        if (extraInfo == null) {
            extraInfo = "null";
        }

        return extraInfo;
    }
}
