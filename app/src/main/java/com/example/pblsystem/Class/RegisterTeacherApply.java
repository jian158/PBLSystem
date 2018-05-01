package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/4/7.
 */
@AVClassName("RegisterTeacherApply")
public class RegisterTeacherApply extends AVObject {
    public static final String CLASS_NAME = "RegisterTeacherApply";

    public static final String S_USERNAME = "username";
    public static final String S_PASSWORD = "password";
    public static final String S_NAME = "name";
    public static final String S_EXTRA = "extra";
    public static final String S_STATE = "state";

    public void setUsername(String username) {
        put(S_USERNAME, username);
    }

    public void setPassword(String password) {
        put(S_PASSWORD, password);
    }

    public void setName(String name) {
        put(S_NAME, name);
    }

    public void setExtra(String extra) {
        put(S_EXTRA, extra);
    }

    public void setState(int state) {
        put(S_STATE, state);
    }

    public String getUsername() {
        String result;
        result = getString(S_USERNAME);
        if (result == null) {
            result = "null";
        }

        return result;
    }

    public String getPassword() {
        String result;
        result = getString(S_PASSWORD);
        if (result == null) {
            result = "null";
        }

        return result;
    }

    public String getName() {
        String result;
        result = getString(S_NAME);
        if (result == null) {
            result = "null";
        }

        return result;
    }

    public String getExtra() {
        String result;
        result = getString(S_EXTRA);
        if (result == null) {
            result = "null";
        }

        return result;
    }

    public int getState() {
        return getInt(S_STATE);
    }
}
