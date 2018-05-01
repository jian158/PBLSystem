package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/5/9.
 */
@AVClassName("OnLine")
public class OnLine extends AVObject {
    public static final String CLASS_NAME = "OnLine";

    public static final String S_USERNAME = "username";

    public void setUsername(String username) {
        put(S_USERNAME, username);
    }

    public String getUsername() {
        String result;
        result = getString(S_USERNAME);
        if (result == null) {
            result = "null";
        }

        return result;
    }
}
