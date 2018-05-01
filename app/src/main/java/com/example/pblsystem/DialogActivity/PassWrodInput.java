package com.example.pblsystem.DialogActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pblsystem.Fragment.GroupFragmentForStudent;
import com.example.pblsystem.R;

public class PassWrodInput extends Activity {
    //Toast静态常量
    private static Toast toast;
    //销毁按钮
    private ImageView mCancelImageView;
    //提交按钮
    private Button mSubmitBtn;
    //输入框
    private EditText mPasswordEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input_password);

        setWindowDemension();
        bindView();
    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mCancelImageView = (ImageView) findViewById(R.id.dialog_input_password_cancel);
        mCancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁视图
                finish();
            }
        });
        mSubmitBtn = (Button) findViewById(R.id.dialog_input_password_submit_btn);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnPassword();
            }
        });
        mPasswordEditText = (EditText) findViewById(R.id.dialog_input_password_edit_text);
    }

    /**
     * 返回输入的密码
     */
    private void returnPassword() {
        //获得输入的密码
        String password = mPasswordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            showToast("客官，不要忘记输入密码.");
        } else {
            Intent intent = new Intent();
            //将输入的数据放入intent返回
            intent.putExtra(GroupFragmentForStudent.INPUT_PASSWORD_EXTRA_TAG, password);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 设置Dialog的参数
     */
    private void setWindowDemension() {
        //保存屏幕宽和高
        Point size = new Point();
        WindowManager m = getWindowManager();
        //为获取屏幕宽、高
        Display d = m.getDefaultDisplay();
        d.getSize(size);
        //获取对话框当前的参数值
        WindowManager.LayoutParams p = getWindow().getAttributes();
//        //高度设置为屏幕的0.8
//        p.height = (int) (size.y * 0.40);
//        //宽度设置为屏幕的0.8
//        p.width = (int) (size.x * 0.8);
        //设置本身透明度
        p.alpha = 1.0f;
        //设置黑暗度
        p.dimAmount = 0.8f;
        //应用设置后的属性
        getWindow().setAttributes(p);
    }

    /**
     * 弹出toast
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
