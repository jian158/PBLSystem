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
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.List;

public class InputClassName extends AppCompatActivity {
    public static final String EXTRA_CLASS_ROOM = "class_room";
    private EditText classNameET;
    private Button save;

    //Toast静态常量
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    private ClassRoom classRoom;

    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_class_name);
        try {
            initilizeProgressDialog();
            getIntentData();
            bindView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindView() {
        classNameET = (EditText) findViewById(R.id.question);
        save = (Button) findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateClassName();
            }
        });
    }


    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializeStr = intent.getStringExtra(EXTRA_CLASS_ROOM);
        classRoom = (ClassRoom) AVObject.parseAVObject(serializeStr);
    }


    private void updateClassName() {
        if (classRoom == null) {
            return;
        }

        if (checkData()) {
            checkTheName();
        }
    }

    private void checkTheName() {
        showProgressDialog("检验名称是否合法...");

        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.addWhereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser());
        query.includePointer(ClassTeacher.S_CLASS); //将其所有的班级查出
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    ClassTeacher classTeacher = (ClassTeacher) obj;
                    ClassRoom targetClass = (ClassRoom) classTeacher.getTargetClass();
                    if (targetClass.getMyClassName().equals(classNameET.getText().toString())) {
                        showToast("该名字已被占用，换个名字试试吧！");

                        dismissProgressDialog();
                        return;
                    }
                }

                update();

            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void update() {
        classRoom.setMyClassName(classNameET.getText().toString());
        manager.saveInBackGround(classRoom, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("修改成功！");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    private boolean checkData() {
        String classname = classNameET.getText().toString();
        if (TextUtils.isEmpty(classname)) {
            showToast("请先输入课堂名！");
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

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("数据加载中...");
    }


    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }
}
