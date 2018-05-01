package com.example.pblsystem.Interface;

import java.util.List;

/**
 * Created by 郭聪 on 2017/3/12.
 */
public interface CountCallBackDB {
    void CountDoneSuccessful(int number);
    void CountDoneFailed(String exceptionMsg, int errorCode);
}
