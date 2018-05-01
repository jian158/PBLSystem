package com.example.pblsystem.Class;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Activity.MainActivityForStudent;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Created by 郭聪聪 on 2017/5/10.
 */

public class MyCustomReceiver extends BroadcastReceiver {
    private static final String TAG = "MyCustomReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Get Broadcat");

        String action = intent.getAction();
        String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
        //获取消息内容
        JSONObject json = null;
        try {
            json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            String tag = json.getString("tag");
            if (tag.equals("exit")) {
                Toast.makeText(context, "你的账号在另一台设备登录，你已被强制下线", Toast.LENGTH_LONG).show();
                clearLoginInfo(context);
                // 重新登录
                reLogin(context);
                // 清除
                clearOnlineInfo();
            }
        } catch (org.json.JSONException e) {
            Log.d("tag", e.getMessage());
        }

    }

    /**
     * 清除登录系统信息
     */
    private void clearOnlineInfo() {
        DataBaseQuery query = new DataBaseQuery(OnLine.CLASS_NAME);
        query.addWhereEqualTo(OnLine.S_USERNAME, AVUser.getCurrentUser().getUsername());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    DataBaseManager manager = DataBaseManager.getInstance();
                    manager.deleteInBackGround((AVObject) results.get(0));
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {

            }
        });
    }

    private void clearLoginInfo(Context context) {
        Activity activity = (Activity) context;
        SharedPreferences sp = activity.getSharedPreferences(LoginActivity.SAVED_USER_INFO,
                activity.MODE_PRIVATE);
        LoginActivity.sSelectedClass = null;
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
    }

    private void reLogin(Context context) {
        Activity activity = (Activity) context;
        Intent returnLogin = new Intent(context, LoginActivity.class);
        context.startActivity(returnLogin);
        activity.finish();
    }
}
