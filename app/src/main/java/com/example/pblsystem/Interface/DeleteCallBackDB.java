package com.example.pblsystem.Interface;

/**
 * Created by 郭聪 on 2017/3/12.
 */
public interface DeleteCallBackDB {
    void deleteDoneSuccessful();
    void deleteDoneFailed(String exceptionMsg, int errorCode);
}
