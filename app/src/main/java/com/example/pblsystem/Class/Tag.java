package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/3/28.
 */
@AVClassName("Tag")
public class Tag extends AVObject {
    public static final String CLASS_NAME = "Tag";

    public static final String S_CLASS = "ofClass";    //所属课题-小组
    public static final String S_MAX_GROUP_NUM = "max"; //小组人数上限
    public static final String S_GROUP_MODE = "group_mode";     //小组自由组合模式

    public void setOfClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setMaxGroupNum(int max) {
        put(S_MAX_GROUP_NUM, max);
    }

    public int getMaxGroupNum() {
        return getInt(S_MAX_GROUP_NUM);
    }

    public void setGroupMode(int mode) {
        put(S_GROUP_MODE, mode);
    }

    public int getGroupMode() {
        return getInt(S_GROUP_MODE);
    }

    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }
}
