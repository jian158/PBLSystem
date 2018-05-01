package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/3/28.
 */
@AVClassName("SpeechEvaluation")
public class SpeechEvaluation extends AVObject {
    public static final String CLASS_NAME = "SpeechEvaluation";

    public static final String S_PROBLEM_GROUP = "problemGroup";    //所属课题-小组
    public static final String S_OWNER = "owner";   //评论者
    public static final String S_COMMENT_TEXT = "comment";  //文字评价
    public static final String S_SCORE = "score";   //分数
    public static final String S_FLAG = "flag";  // 0表示学生评价，1表示教师评价


    public void setProblemGroup(AVObject obj) {
        put(S_PROBLEM_GROUP, obj);
    }

    public void setOwner(AVObject obj) {
        put(S_OWNER, obj);
    }

    public void setCommentText(String comment) {
        put(S_COMMENT_TEXT, comment);
    }

    public void setScore(int score) {
        put(S_SCORE, score);
    }

    public AVObject getProblemGroup() {
        return getAVObject(S_PROBLEM_GROUP);
    }

    public String getsCommentText() {
        String result;
        result = getString(S_COMMENT_TEXT);
        if (result == null) {
            result = "无";
        }
        return result;
    }

    public AVObject getsOwner() {
        return getAVObject(S_OWNER);
    }

    public int getsScore() {
        return getInt(S_SCORE);
    }

    public void setFlag(int flag) {
        put(S_FLAG, flag);
    }

    public int getFlag() {
        return getInt(S_FLAG);
    }
}
