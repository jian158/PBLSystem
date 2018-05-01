package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVRelation;

import java.lang.reflect.Array;

/**
 * Created by 郭聪 on 2017/3/7.
 */

/**
 * 小组类
 */
@AVClassName("Group")
public class Group extends AVObject {
    public static final String CLASS_NAME = "Group";
    //小组的对外模式变量
    public static final int MODE_OPEN = 0;
    public static final int MODE_HIDE = 1;
    public static final int MODE_PSW = 2;
    //所属班级
    public static final String S_CLASS = "ofClass";
    //小组长
    public static final String S_LEADER = "leader";
    //小组名
    public static final String S_NAME = "name";
    //小组对外权限
    public static final String S_FLAG = "flag";
    //小组成员
    public static final String S_MEMBER = "member";
    //小组人数
    public static final String S_NUM = "number";
    //密码
    public static final String S_PASSWORD = "password";

    public void setClass (AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setName (String name) {
        put(S_NAME, name);
    }

    public void setFlag (int flag) {
        put(S_FLAG, flag);
    }

    public void setMember (Array objs) {
        put(S_MEMBER, objs);
    }

    public void setLeader (AVObject obj) {
        put(S_LEADER, obj);
    }

    public void setPassword (String password) {
        put(S_PASSWORD, password);
    }

    public void setNum(int num) {
        put(S_NUM, num);
    }


    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }

    public int getFlag() {
        return getInt(S_FLAG);
    }

    public AVObject getLeader() {
        return getAVObject(S_LEADER);
    }


    public String getName() {
        String result;
        result = getString(S_NAME);
        if (result == null) {
            result = "null";
        }
        return result;
    }

    public int getNum() {
        return getInt(S_NUM);
    }

    public String getPassword() {
        String result;
        result = getString(S_PASSWORD);
        if (result == null) {
            result = "null";
        }
        return result;
    }
}
