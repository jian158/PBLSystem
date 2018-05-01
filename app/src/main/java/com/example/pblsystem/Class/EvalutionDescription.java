package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/3/28.
 */
@AVClassName("EvalutionDescription")
public class EvalutionDescription extends AVObject {
    public static final String CLASS_NAME = "EvalutionDescription";

    public static final String S_EVALUTION = "ofEvalution";
    public static final String S_TITLE = "title"; //标题
    public static final String S_DESCRIPTION = "details";    //简介
    public static final String S_SCORE = "score";   // 单项分值

    public String getDescriptionTitle() {
        String result;
        result = getString(S_TITLE);
        if (result == null) {
            result = "无";
        }
        return result;
    }

    public String getDescriptionDetails() {
        String result;
        result = getString(S_DESCRIPTION);
        if (result == null) {
            result = "无";
        }
        return result;
    }

    public AVObject getOfEvalution() {
        return getAVObject(S_EVALUTION);
    }

    public void setDescriptionTitle(String title) {
        put(S_TITLE, title);
    }

    public void setDescriptionDetails(String details) {
        put(S_DESCRIPTION, details);
    }

    public void setOfEvalution(AVObject obj) {
        put(S_EVALUTION, obj);
    }

    public void setScore(int score) {
        put(S_SCORE, score);
    }

    public int getScore() {
        return getInt(S_SCORE);
    }

}
