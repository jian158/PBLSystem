package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/4/7.
 */
@AVClassName("DescriptionScore")
public class DescriptionScore extends  AVObject {
    public static final String CLASS_NAME = "DescriptionScore";

    public static final String S_SPEECH_EVALUTION = "speechEvalution";  // 属于哪个评价
    public static final String S_SCORE = "score";
    public static final String S_DESCRIPTION = "description";  // 属于哪个细则

    public void setSpeechEvalution(AVObject object) {
        put(S_SPEECH_EVALUTION, object);
    }

    public void setScore(int score) {
        put(S_SCORE, score);
    }

    public void setDescription(AVObject object) {
        put(S_DESCRIPTION, object);
    }

    public AVObject getSpeechEvalution() {
        return getAVObject(S_SPEECH_EVALUTION);
    }

    public AVObject getDescription() {
        return getAVObject(S_DESCRIPTION);
    }

    public int getScore() {
        return getInt(S_SCORE);
    }
}
