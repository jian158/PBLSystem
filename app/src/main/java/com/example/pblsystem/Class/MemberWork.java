package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/3/27.
 */
@AVClassName("MemberWork")
public class MemberWork extends AVObject {
    public static final String CLASS_NAME = "MemberWork";

    public static final String S_OWNER = "owner";
    public static final String S_PROBLEM = "problemGroup";
    public static final String S_PROPORTION = "proportion";
    public static final String S_DESRIPTION = "description";

    public void setOwner(AVObject obj) {
        put(S_OWNER, obj);
    }

    public void setProblemGroup(AVObject obj) {
        put(S_PROBLEM, obj);
    }

    public void setProportion(int scale) {
        put(S_PROPORTION, scale);
    }

    public void setDescription(String description) {
        put(S_DESRIPTION, description);
    }

    public AVObject getOwner() {
        return getAVObject(S_OWNER);
    }

    public AVObject getProblem() {
        return getAVObject(S_PROBLEM);
    }

    public int getProportion() {
        return getInt(S_PROPORTION);
    }

    public String getDesription() {
        String result;
        result = getString(S_DESRIPTION);
        if (result == null) {
            result = "null";
        }

        return result;
    }
}
