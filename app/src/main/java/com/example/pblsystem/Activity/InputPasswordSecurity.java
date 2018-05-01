package com.example.pblsystem.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.R;

import java.util.regex.Pattern;

public class InputPasswordSecurity extends AppCompatActivity {

    private EditText questionTV, answerTV;
    private Button save;
    private TextView prompt;

    //Toast静态常量
    private static Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password_security);

        bindView();
        initializeUI();
    }


    private void bindView() {
        questionTV = (EditText) findViewById(R.id.question);
        answerTV = (EditText) findViewById(R.id.answer);
        save = (Button) findViewById(R.id.save);
        prompt = (TextView) findViewById(R.id.prompt);

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
            user.put(MyUser.S_SECRET_QUESTION, questionTV.getText().toString());
            user.put(MyUser.S_SECTET_ANSWER, answerTV.getText().toString());

            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        showToast("密保设置成功！");

                        finish();
                    }
                }
            });
        }
    }


    private boolean checkData() {
        String question = questionTV.getText().toString();
        String answer = answerTV.getText().toString();
        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
            showToast("请先输入问题和答案！");
            return false;
        }

        return true;
    }


    private void initializeUI() {
        AVUser user = AVUser.getCurrentUser();
        String question = user.getString(MyUser.S_SECRET_QUESTION);
        String answer = user.getString(MyUser.S_SECTET_ANSWER);

        if (!TextUtils.isEmpty(question) && !TextUtils.isEmpty(answer)) {
            prompt.setText("温馨提示：你已经设置过密保，再次设置将会覆盖原有数据");

            questionTV.setText(question);
            answerTV.setText(answer);
        }

        save.setText("覆盖");
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
