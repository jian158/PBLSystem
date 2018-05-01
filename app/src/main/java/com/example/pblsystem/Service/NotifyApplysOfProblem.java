package com.example.pblsystem.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;

import java.util.List;

public class NotifyApplysOfProblem extends Service {
    private boolean isRunning = false;

    public NotifyApplysOfProblem() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("tag", "服务被创建");
        isRunning = true;
        startWatchingThread();  //创建一个线程监听
    }

    private void startWatchingThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {//服务如果运行
                    watchTheApplys();
                    try {
                        Thread.sleep(1000 * 60);   //每10分种检测一次
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void watchTheApplys() {
        if (LoginActivity.sSelectedClass == null) return;

        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(ProblemApplyTable.CLASS_NAME);
        query.addWhereEqualTo(ProblemApplyTable.S_CLASS, classRoom);
        query.addWhereEqualTo(ProblemApplyTable.S_STATE, 0);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {//有申请
                    Intent intent=new Intent();
                    intent.setAction("com.example.pblsystem.HaveApplys");
                    sendBroadcast(intent);

                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", "异常"  + exceptionMsg);
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("tag", "服务被开启");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("tag", "服务被销毁.");

        isRunning = false;  //终止线程
    }
}
