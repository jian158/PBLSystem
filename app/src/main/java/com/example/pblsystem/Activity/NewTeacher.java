package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SignUpCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.RegisterTeacherApply;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.List;

public class NewTeacher extends AppCompatActivity {

    //常量，调试的Tag值
    private static final String TAG = "RegisterActivity";
    //Toast静态常量
    private static Toast toast;


    //用户名输入框
    private EditText mUsernameEditText;
    //密码第一次输入框
    private EditText mPassWordFirstEditText;
    //密码第二次输入框
    private EditText mPassWordSecondEditText;
    //姓名输入框
    private EditText mNameEditText;
    //提交注册的按钮
    private Button mSubmitRegisterButton;
    // 申请说明填写
    private EditText mExtraEditText;

    //保存EditText中输入的值
    private String mUserName, mPasswordFirst, mPasswordSecond, mName, mExtra;
    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_teacher);
        bindView();
        initilizeProgressDialog();
        initlizeSubmitButton();

    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("数据加载中...");
    }




    /**
     * 初始化提交按钮控件，并设置响应监听
     */
    private void initlizeSubmitButton() {
        mSubmitRegisterButton = (Button) findViewById(R.id.register_submit_btn);
        mSubmitRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateAndSubmit();
            }
        });
    }

    /**
     * 获取输入框中的数据，并提交注册信息
     */
    private void getDateAndSubmit() {
        //获取输入数据
        mUserName = mUsernameEditText.getText().toString();
        mPasswordFirst = mPassWordFirstEditText.getText().toString();
        mPasswordSecond = mPassWordSecondEditText.getText().toString();
        mName = mNameEditText.getText().toString();

        if (checkValidity()) {//用户正确输入数据
            RegisterTeacherApply apply = new RegisterTeacherApply();
            apply.setUsername(mUserName);
            apply.setPassword(mPasswordFirst);
            apply.setState(0);
            apply.setName(mName);
            String extra = mExtraEditText.getText().toString();
            if (TextUtils.isEmpty(extra)) {
                extra = "我是" + mName + ", 我要申请一个教师用户";
            }
            apply.setExtra(extra);

            mProgressBarDialog.setMessage("申请提交中...");
            mProgressBarDialog.show();

            DataBaseManager manager = DataBaseManager.getInstance();
            manager.saveInBackGround(apply, new SaveCallBackDB() {
                @Override
                public void saveDoneSuccessful() {
                    PopDialog.popConfirmDialog(NewTeacher.this, "申请提交成功！管理员将会及时处理，请稍后再登录系统！");
                    mProgressBarDialog.dismiss();
                }

                @Override
                public void saveDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                    showToast("申请提交失败！请检查一下网络吧！");

                    mProgressBarDialog.dismiss();
                }
            });
//            //构建AVObject
//            MyUser newUser = new MyUser();
//            newUser.setUsername(mUserName);
//            newUser.setPassword(mPasswordFirst);
//            newUser.setMyName(mName);
//            //设置用户权限为教师 1代表教师
//            newUser.setAuthority(1);
//
//            //数据提交前，弹出进度框
//            mProgressBarDialog.show();
//            //保存按钮的原来背景，用于恢复
//            final Drawable savedSubmitButtonBackground = mSubmitRegisterButton.getBackground();
//            //按钮暂时灰掉，不可用
//            mSubmitRegisterButton.setClickable(false);
//            mSubmitRegisterButton.setBackgroundColor(Color.GRAY);
//            //设置进度框信息
//            mProgressBarDialog.setMessage("正在提交信息...");
//
//            newUser.signUpInBackground(new SignUpCallback() {
//                @Override
//                public void done(AVException e) {
//                    if (e == null) {
//                        showToast("注册成功！");
//                        //数据加载完毕，关闭进度框
//                        mProgressBarDialog.dismiss();
//
//                        mSubmitRegisterButton.setClickable(true);
//                        mSubmitRegisterButton.setBackground(savedSubmitButtonBackground);
//                    } else {
//                        int errorCode = e.getCode();
//                        switch (errorCode) {
//                            case 202:
//                                Log.d(TAG, "用户名重复");
//                                showToast("注册失败！该用户名已经被注册！");
//                                break;
//                            default:
//                                showToast(Constants.NET_ERROR_TOAST);
//                                break;
//                        }
//                        //进度条取消
//                        mProgressBarDialog.dismiss();
//
//                        mSubmitRegisterButton.setClickable(true);
//                        mSubmitRegisterButton.setBackground(savedSubmitButtonBackground);
//                    }
//                }
//            });
        }



    }

    private boolean checkValidity() {
        if (!mPasswordFirst.equals(mPasswordSecond)) {//两次密码输入不一致
            showToast("两次输入密码不一致");
            return false;
        }

        if (mPasswordFirst.length() < 6 || mPasswordFirst.length() > 20) {//密码长度不合适
            showToast("密码长度必须在6-20个字符");
            return false;
        }


        if ("".equals(mUserName)) {//用户名为空
            showToast("用户名不可为空");
            return false;
        }

        if (mUserName.length() < 6 || mUserName.length() > 20) {
            showToast("用户名长度必须在6-20个字符");
            return false;
        }

        if ("".equals(mName)) {
            showToast("不要忘记输入姓名哦！");
            return false;
        }

        return true;
    }


    /**
     * 成员变量绑定xml文件中的组件
     */
    private void bindView() {
        mUsernameEditText = (EditText) findViewById(R.id.register_username);
        mPassWordFirstEditText = (EditText) findViewById(R.id.register_password_first);
        mPassWordSecondEditText = (EditText) findViewById(R.id.register_password_second);
        mSubmitRegisterButton = (Button) findViewById(R.id.register_submit_btn);
        mNameEditText = (EditText) findViewById(R.id.register_name);
        mExtraEditText = (EditText) findViewById(R.id.extra);
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
