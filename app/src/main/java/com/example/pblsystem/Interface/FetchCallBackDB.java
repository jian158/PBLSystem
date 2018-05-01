package com.example.pblsystem.Interface;

import com.avos.avoscloud.AVObject;

import java.util.List;

/**
 * Created by 郭聪 on 2017/3/12.
 */
public interface FetchCallBackDB {
    void fetchDoneSuccessful(AVObject obj);
    void fetchDoneFailed(String exceptionMsg, int errorCode);
}
