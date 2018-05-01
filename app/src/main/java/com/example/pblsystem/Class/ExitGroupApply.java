package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/4/2.
 */
@AVClassName("ExitGroupApply")
public class ExitGroupApply extends AVObject {
    public static final String CLASS_NAME = "ExitGroupApply";

    public static final String S_CLASS = "ofClass";
    public static final String S_APPLYER = "applyer";   //申请者
    public static final String S_STATE = "state";   //状态
    public static final String S_INFO = "extraInfo";    //原因说明

    public void setOfClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setApplyer(AVObject obj) {
        put(S_APPLYER, obj);
    }

    public void setState(int state) {
        put(S_STATE, state);
    }

    public void setInfo(String info) {
        put(S_INFO, info);
    }

    public int getState() {
        return getInt(S_STATE);
    }

    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }

    public AVObject getApplyer() {
        return getAVObject(S_APPLYER);
    }

    public String getInfo() {
        String result = null;
        result = getString(S_INFO);
        if (result == null) {
            return "null";
        }

        return result;
    }
}
