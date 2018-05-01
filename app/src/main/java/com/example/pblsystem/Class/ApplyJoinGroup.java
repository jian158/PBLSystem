package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

/**
 * Created by 郭聪 on 2017/3/9.
 */
@AVClassName("ApplyJoinGroup")
public class ApplyJoinGroup extends AVObject {
    public static final String CLASS_NAME = "ApplyJoinGroup";
    //申请目标小组
    public static final String S_TARGET_GROUP = "targetGroup";
    //申请人
    public static final String S_APPLY_USER = "applyUser";
    //状态
    public static final String S_STATE = "state";
    //附加信息
    public static final String S_EXTRA_INFO = "extraInfo";

    public void setTargetGroup(AVObject obj) {
        put(S_TARGET_GROUP, obj);
    }

    public void setApplyUser(AVObject obj) {
        put(S_APPLY_USER, obj);
    }

    public void setState(int state) {
        put(S_STATE, state);
    }

    public void setsExtraInfo(String info) {
        put(S_EXTRA_INFO, info);
    }

    public AVUser getApplyUser() {
        return getAVUser(S_APPLY_USER);
    }

    public String getExtraInfo() {
        String result;
        result = getString(S_EXTRA_INFO);
        if (result == null) {
            result = "null";
        }
        return result;
    }

    public int getState() {
        return getInt(S_STATE);
    }

    public Group getTargetGroup() {
        return (Group) getAVObject(S_TARGET_GROUP);
    }
}
