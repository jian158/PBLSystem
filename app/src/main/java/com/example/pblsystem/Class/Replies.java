package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/4/20.
 */
@AVClassName("Replies")
public class Replies extends AVObject {
    public static final String S_CLASS_NAME = "Replies";

    public static final String S_CONTENT = "content";   // 回复内容
    public static final String S_OF_POST = "ofPost";    // 回复帖子
    public static final String S_TARGET = "target";     // 回复评论
    public static final String S_OWNER = "owner";

    public String getContent() {
        String result;
        result = getString(S_CONTENT);
        if (result == null) {
            result = "null";
        }
        return result;
    }

    public AVObject getOfPost() {
        return getAVObject(S_OF_POST);
    }

    public AVObject getTarget() {
        return getAVObject(S_TARGET);
    }

    public AVObject getOwner() {
        return getAVObject(S_OWNER);
    }

    public void setContent(String content) {
        put(S_CONTENT, content);
    }

    public void setOfPost(AVObject obj) {
        put(S_OF_POST, obj);
    }

    public void setTarget(AVObject obj) {
        put(S_TARGET, obj);
    }
    public void setOwner(AVObject obj) {
        put(S_OWNER, obj);
    }
}
