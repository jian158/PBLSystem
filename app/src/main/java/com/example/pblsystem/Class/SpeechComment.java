package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/4/30.
 */
@AVClassName("SpeechComment")
public class SpeechComment extends AVObject {
    public static final String CLASS_NAME = "SpeechComment";

    public static final String S_CONTENT = "content";   // 评论内容
    public static final String S_OF_PROBLEM_GROUP = "problemGroup";    // 评论的目标 课题-小组
    public static final String S_TARGET = "target";     // 回复评论
    public static final String S_OWNER = "owner";   // 评论者
    public static final String S_LIKES = "likes";   // 点赞数

    public void setContent(String content) {
        put(S_CONTENT, content);
    }

    public void setOfProblemGroup(AVObject obj) {
        put(S_OF_PROBLEM_GROUP, obj);
    }

    public void setTarget(AVObject obj) {
        put(S_TARGET, obj);
    }

    public void setOwner(AVObject obj) {
        put(S_OWNER, obj);
    }

    public void setLikes(int likes) {
        put(S_LIKES, likes);
    }

    public String getContent() {
        String result = getString(S_CONTENT);
        if (result == null) {
            result = "";
        }

        return result;
    }

    public AVObject getOfProblemGroup() {
        return getAVObject(S_OF_PROBLEM_GROUP);
    }

    public AVObject getTarget() {
        return getAVObject(S_TARGET);
    }

    public AVObject getOwner() {
        return getAVObject(S_OWNER);
    }

    public int getLikes() {
        return getInt(S_LIKES);
    }


}
