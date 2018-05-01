package com.example.pblsystem.Interface;

/**
 * Created by 郭聪 on 2017/3/11.
 */
public interface SaveCallBackDB {
    void saveDoneSuccessful();
    void saveDoneFailed(String exceptionMsg, int errorCode);

}
