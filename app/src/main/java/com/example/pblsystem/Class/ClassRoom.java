package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪 on 2017/3/5.
 */
@AVClassName("ClassRoom")
public class ClassRoom extends AVObject {
    public ClassRoom() {};

    public static final String CLASS_NAME = "ClassRoom";
    //班级名
    public static final String S_CLASS_NAME = "myClassName";

    public void setMyClassName(String className) {
        put(S_CLASS_NAME, className);
    }

    public String getMyClassName() {
        String name = getString(S_CLASS_NAME);

        if (name == null) {
            name = "null";
        }

        return name;
    }
}
