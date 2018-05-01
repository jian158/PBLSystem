package com.example.pblsystem.Interface;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.MyUser;

/**
 * Created by 郭聪 on 2017/3/11.
 */
public interface LoginCallBackDB {
    void loginDoneSuccessful(AVUser obj);
    void loginDoneFailed(String exceptionMsg, int errorCode);
}
