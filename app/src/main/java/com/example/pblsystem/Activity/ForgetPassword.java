package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;

import java.util.List;

public class ForgetPassword extends AppCompatActivity {
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    private Button submitBtn;
    // 用户名
    private EditText usernameET;

    private AVUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        bindView();
    }

    private void bindView() {
        usernameET = (EditText) findViewById(R.id.username);
        submitBtn = (Button) findViewById(R.id.save);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmTheQuesiton();
            }
        });
    }

    private void confirmTheQuesiton() {
        String username = usernameET.getText().toString();
        if (TextUtils.isEmpty(username)) {
            showToast("请先输入你的用户名");
            return;
        }

        findEmail(username);

//        AVUser.requestPasswordResetInBackground("myemail@example.com", new RequestPasswordResetCallback() {
//            @Override
//            public void done(AVException e) {
//                if (e == null) {
//
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void findEmail(String username) {
        final DataBaseQuery query = new DataBaseQuery("_User");
        query.addWhereEqualTo("username", username);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() != 1) {
                    showToast("找不到该用户！");
                } else{
                    AVUser user = (AVUser) results.get(0);
                    String email = user.getEmail();
                    sendEmail(email);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });

    }

    private void sendEmail(String email) {
        AVUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    showToast("重置密码邮件发送成功！");
                } else {
                    switch (e.getCode()) {
                        case 204:
                            showToast("你还没有注册邮箱！");
                            break;
                        default:
                            Log.d("tag", e.getMessage());
                            break;
                    }
                    e.printStackTrace();
                    Log.d("tag", e.getMessage());
                }
            }
        });
    }

//    private void confirmTheQuesiton() {
//        if (currentUser == null) {
//            showToast("请先获取密保问题!");
//            return;
//        }
//
//        String question = currentUser.getString(MyUser.S_SECRET_QUESTION);
//        if (question == null) {
//            showToast("抱歉！你没有设置密保问题！");
//        } else {
//            if (checkTheAnswerInput()) {
////                Intent intent = new Intent(this, InputNewPassword.class);
////                String str = currentUser.toString();
////                intent.putExtra(InputNewPassword.EXTRA_USER, str);
////                startActivity(intent);
//                showToast("对不起！该功能正在开发中！");
//            }
//        }
//    }

//    private boolean checkTheAnswerInput() {
//        String answer = answerET.getText().toString();
//        if (TextUtils.isEmpty(answer)) {
//            showToast("请先输入答案！");
//            return false;
//        }
//
//        String realAnswer = currentUser.getString(MyUser.S_SECTET_ANSWER);
//        if (!realAnswer.equals(answer)) {
//            showToast("答案错误！请重新输入！");
//            return false;
//        }
//
//        return true;
//    }
//
//    private void getQuestion() {
//        final String username = usernameET.getText().toString();
//        if (TextUtils.isEmpty(username)) {
//            showToast("请先输入用户名！");
//            return;
//        }
//
//        final DataBaseQuery query = new DataBaseQuery("_User");
//        query.addWhereEqualTo("username", username);
//        query.findInBackGroundDB(new FindCallBackDB() {
//            @Override
//            public void findDoneSuccessful(List results) {
//                if (results.size() == 1) {
//                    currentUser = (AVUser) results.get(0);
//                    initializeUI(results);
//                } else {
//                    showToast("该用户名不存在！请重新输入！");
//                }
//            }
//
//            @Override
//            public void findDoneFailed(String exceptionMsg, int errorCode) {
//                showToast("出错了！请检查网络连接！");
//            }
//        });
//    }
//
//    private void initializeUI(List results) {
//        AVUser user = (AVUser) results.get(0);
//        String question = user.getString(MyUser.S_SECRET_QUESTION);
//        if (question == null) {
//            showToast("抱歉！你没有设置密保问题！");
//        } else {
//            questionTV.setText(question + "?");
//        }
//    }
//
//    private void showProgressDialog(String msg) {
//        mProgressBarDialog.setMessage(msg);
//        mProgressBarDialog.show();
//    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
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

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }
}
