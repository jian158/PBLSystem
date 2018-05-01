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
import com.example.pblsystem.R;

import java.util.regex.Pattern;

public class InputPhoneNumber extends AppCompatActivity {
    private EditText phoneNumberET;
    private Button save;

    //Toast静态常量
    private static Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inpuy_phone_number);

        bindView();
    }

    private void bindView() {
        phoneNumberET = (EditText) findViewById(R.id.question);
        save = (Button) findViewById(R.id.save);

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
            user.setMobilePhoneNumber(phoneNumberET.getText().toString());
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        showToast("手机号绑定成功！");
                        Intent intent = new Intent();
                        intent.putExtra("phone_number", phoneNumberET.getText().toString());
                        setResult(RESULT_OK, intent);

                        finish();
                    }
                }
            });
        }
    }

    private boolean checkData() {
        String inputNumber = phoneNumberET.getText().toString();
        if (TextUtils.isEmpty(inputNumber)) {
            showToast("请先输入手机号！");
            return false;
        } else if (!isNumeric(inputNumber) || inputNumber.length() != 11){
            showToast("手机号格式有误！");
            return false;
        }

        return true;
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
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
