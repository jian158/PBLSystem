package com.example.pblsystem.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.LoginCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.example.pblsystem.R.id.username;

public class AutoLogin extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "AutoLogin";
    private static Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_auto_login);

        getSavedData();
    }

    private void getSavedData() {
        SharedPreferences sp = getSharedPreferences(LoginActivity.SAVED_USER_INFO, MODE_PRIVATE);
        String username = sp.getString("username", "null");
        String password = sp.getString("password", "null");
        String classRoomStr = sp.getString("classTeacher", null);

        login(username, password, classRoomStr);
    }

    /**
     * 处理登录事件
     */
    private void login(final String username,final String password, final String classTeacherStr) {
        MyUser.loginSystemInBackground(username, password, new LoginCallBackDB() {
            @Override
            public void loginDoneSuccessful(AVUser avUser) {
                if (avUser.getInt(MyUser.S_AUTHORITY) == 0) {//学生用户
                    //进入系统主界面
                    Intent intent = new Intent(getApplicationContext(), MainActivityForStudent.class);
                    //构建Bundle用于传递数据
                    Bundle bundle = new Bundle();
                    //将用户对象序列化后，传入
                    bundle.putString(MainActivityForStudent.TAG_USER, avUser.toString());
                    intent.putExtras(bundle);
                    //打开系统界面
                    startActivity(intent);
                    //销毁登录界面
                    finish();
                } else if (avUser.getInt(MyUser.S_AUTHORITY) == 1) {
                    //教师用户
                    try {
                        if (classTeacherStr != null) {//上次存时保存班级
                            ClassTeacher classTeacher = (ClassTeacher) AVObject.parseAVObject(classTeacherStr);
                            LoginActivity.sSelectedClass = classTeacher;
                        }
                        //进入系统主界面
                        Intent intent = new Intent(getApplicationContext(), MainActivityForTeacher.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        //销毁记录
                        cleanSavedInfo();
                        enterLoginPage();
                        finish();
                    }
                } else {//管理员用户

                }
            }

            @Override
            public void loginDoneFailed(String exceptionMsg, int errorCode) {
                cleanSavedInfo();
                enterLoginPage();
                finish();

            }
        });


    }

    private void cleanSavedInfo() {
        //销毁记录
        SharedPreferences sp = getSharedPreferences(LoginActivity.SAVED_USER_INFO,
                MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
    }

    private void enterLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }



    /**
     * 弹出Toast
     */
    public void showToast(String msg) {
        if (toast == null) {//第一次初始化toast变量
            toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }
}
