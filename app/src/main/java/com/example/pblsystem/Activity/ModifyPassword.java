package com.example.pblsystem.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.R;

import java.util.regex.Pattern;

public class ModifyPassword extends AppCompatActivity {

    private EditText passwordEt;
    private Button save;

    //Toast静态常量
    private static Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);

        bindView();
    }

    private void bindView() {
        passwordEt = (EditText) findViewById(R.id.password);
        save = (Button) findViewById(R.id.modify);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePhoneNumber();
            }
        });
    }

    private void savePhoneNumber() {
        if (checkData()) {
            AVUser user = AVUser.getCurrentUser();

            user.setPassword(passwordEt.getText().toString());
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        showToast("密码修改成功！");

                        finish();
                    }
                }
            });
        }
    }

    private boolean checkData() {
        String inputPassword = passwordEt.getText().toString();
        if (TextUtils.isEmpty(inputPassword)) {
            showToast("请先输入密码！");
            return false;
        } else if (inputPassword.length() < 6){
            showToast("密码至少为6个字符！");
            return false;
        }

        return true;
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
