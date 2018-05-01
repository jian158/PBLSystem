package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/3/28.
 */
@AVClassName("EvaluationStandard")
public class EvaluationStandard extends AVObject {
    public static final String CLASS_NAME = "EvaluationStandard";

    public static final String S_CLASS = "ofClass"; //所属班级
    public static final String S_DESCRIPTION = "description";    //评价细则
    public static final String S_TOTAL_SCORE = "score";   //分数

    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }

    public int getScore() {
        return getInt(S_TOTAL_SCORE);
    }

    public void setOfClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setScore(int score) {
        put(S_TOTAL_SCORE, score);
    }
}
