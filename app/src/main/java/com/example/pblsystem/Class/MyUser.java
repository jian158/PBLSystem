package com.example.pblsystem.Class;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.example.pblsystem.Interface.LoginCallBackDB;

/**
 * Created by 郭聪 on 2017/3/5.
 */
public class MyUser extends AVUser {
    public static final String CLASS_NAME = "MyUser";
    //姓名
    public static final String S_NAME = "name";
    //班级
    public static final String S_CLASS = "class";
    //性别
    public static final String S_SEX = "sex";
    //头像
    public static final String S_HEAD_IMAGE = "headImage";
    //密保问题
    public static final String S_SECRET_QUESTION = "secretQuestion";
    //密保答案
    public static final String S_SECTET_ANSWER = "secretAnswer";
    //权限
    public static final String S_AUTHORITY = "authority";

    public void setAuthority(int authority) {
        put(S_AUTHORITY, authority);
    }

    public void setMyClass(AVObject myClass) {
        put(S_CLASS, myClass);
    }

    public void setHeadImage(AVFile headImage) {
        put(S_HEAD_IMAGE, headImage);
    }

    public void setSecretAnswer(String secretAnswer) {
        put(S_SECTET_ANSWER, secretAnswer);
    }

    public void setSecretQuestion(String secretQuestion) {
        put(S_SECRET_QUESTION, secretQuestion);
    }

    public void setMyName(String name) {
        put(S_NAME, name);
    }

    public void setSex(int sex) {
        put(S_SEX, sex);
    }

    /**
     * 登录系统
     */
    public static void loginSystemInBackground(String useranme, String password, final LoginCallBackDB callback) {
        logInInBackground(useranme, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) { 
                if (e == null) {
                    callback.loginDoneSuccessful(avUser);
                } else {
                    callback.loginDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }


    public  AVObject getMyClass() {
        return getAVObject(S_CLASS);
    }

    public int getAuthority() {
        return getInt(S_AUTHORITY);
    }

    public  AVObject getHeadImage() {
        return getAVObject(S_HEAD_IMAGE);
    }

    public  String getName() {
        String result = getString(S_NAME);
        if (result == null) {
            return "null";
        }

        return result;
    }

    public  String getSecretQuestion() {
        String result = getString(S_SECRET_QUESTION);
        if (result == null) {
            return "null";
        }

        return result;
    }

    public  String getSectetAnswer() {
        String result = getString(S_SECTET_ANSWER);
        if (result == null) {
            return "null";
        }

        return result;
    }

    public  int getSex() {
        return getInt(S_SEX);
    }
}
