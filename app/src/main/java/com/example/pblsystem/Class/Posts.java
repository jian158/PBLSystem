package com.example.pblsystem.Class;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

/**
 * Created by 郭聪聪 on 2017/4/20.
 */
@AVClassName("Posts")
public class Posts extends AVObject {
    public static final String CLASS_NAME = "Posts";

    public static final String S_CLASS = "ofClass";
    public static final String S_TITLE = "title";
    public static final String S_CONTENT = "content";
    public static final String S_LIKES = "likes";   // 点赞数
    public static final String S_OWNER = "owner";   // 发帖人

    public AVObject getOfClass() {
        return getAVObject(S_CLASS);
    }

    public String getTitle() {
        String result;
        result = getString(S_TITLE);
        if (result == null) {
            result = "";
        }
        return result;
    }

    public String getContent() {
        String result;
        result = getString(S_CONTENT);
        if (result == null) {
            result = "";
        }
        return result;
    }

    public int getLikes() {
        return getInt(S_LIKES);
    }

    public AVObject getOwner() {
        return getAVObject(S_OWNER);
    }

    public void setClass(AVObject obj) {
        put(S_CLASS, obj);
    }

    public void setTitle(String title) {
        put(S_TITLE, title);
    }

    public void setContent(String content) {
        put(S_CONTENT, content);
    }

    public void setsLikes(int likes) {
        put(S_LIKES, likes);
    }
    public void setOwner(AVObject obj) {
        put(S_OWNER, obj);
    }
}
