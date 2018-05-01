package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪 on 2017/3/14.
 */
@AVClassName("ClassTeacher")
public class ClassTeacher extends AVObject {
    public static final String CLASS_NAME = "ClassTeacher";

    public static final String S_TEACHER = "targetTeacher"; //所属老师
    public static final String S_CLASS = "targetClass";    //所属班级
    public static final String S_AUTHROITY = "authority";   //flag, 0代表拥有，2代表代理

    public void setTargetTeacher(AVObject obj) {
        put(S_TEACHER, obj);
    }

    public void setTargetClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setAuthroity(int authroity) {
        put(S_AUTHROITY, authroity);
    }

    public AVObject getTargetClass() {
        return getAVObject(S_CLASS);
    }

    public int getsAuthroity() {
        return getInt(S_AUTHROITY);
    }

    public AVObject getTargetTeacher() {
        return getAVObject(S_TEACHER);
    }
}
