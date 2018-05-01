package com.example.pblsystem.Interface;

import com.avos.avoscloud.AVObject;

import java.util.List;

/**
 * Created by 郭聪 on 2017/3/11.
 */
public interface FindCallBackDB<Object> {
    void findDoneSuccessful(List<Object> results);
    void findDoneFailed(String exceptionMsg, int errorCode);
}
