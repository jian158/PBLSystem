package com.example.pblsystem.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.R;

public class InputNewPassword extends AppCompatActivity {
    public static final String EXTRA_USER = "user";

    private EditText passwordEt;
    private Button save;

    //Toast静态常量
    private static Toast toast;

    private AVUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);

        bindView();
        try {
            getIntentData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String str = intent.getStringExtra(EXTRA_USER);
        currentUser = (AVUser) AVObject.parseAVObject(str);
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
            currentUser.setPassword(passwordEt.getText().toString());
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        showToast("密码修改成功！");

                        finish();
                    } else {
                        Log.d("tag", e.getMessage());
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
