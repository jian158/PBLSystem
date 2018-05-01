package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class CreateClass extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "CreateClass";
    //Toast静态常量
    private static Toast toast;

    private EditText mClassNameET;
    private Button mConfirmBtn;
    private TextView mChooseOtherClassesTV;

    private String mClassName;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private Drawable mSavedBtnDrawable;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        initilizeProgressDialog();
        bindView();
        setClickListennerForBtn();
    }

    private void setClickListennerForBtn() {
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewClass();
            }
        });

        mChooseOtherClassesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseOtherClass();
            }
        });
    }

    private void chooseOtherClass() {
        Intent intent = new Intent(getApplicationContext(), ChooseClassOfOthers.class);
        startActivity(intent);
    }

    private void createNewClass() {
        getInputData();
        if (checkInputData()) {
            checkTheClassNameRepeatity();
        }
    }

    /**
     * 开始检测班级名是否重复问题
     */
    private void checkTheClassNameRepeatity() {
        disableBtn();
        showProgressDialog("正在检验班级名是否合法...");

        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.addWhereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser());
        query.includePointer(ClassTeacher.S_CLASS); //将其所有的班级查出
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    ClassTeacher classTeacher = (ClassTeacher) obj;
                    ClassRoom targetClass = (ClassRoom) classTeacher.getTargetClass();
                    if (targetClass.getMyClassName().equals(mClassName)) {
                        showToast("该名字已被占用，换个名字试试吧！");

                        dismissProgressDialog();
                        enableBtn();
                        return;
                    }
                }

                createNewClssObjAndSave();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                if (errorCode == 101) { //尚未有Class
                    createNewClssObjAndSave();
                } else {
                    showToast(Constants.NET_ERROR_TOAST);
                    dismissProgressDialog();
                    enableBtn();
                }
            }
        });
    }

    private void enableBtn() {
        mConfirmBtn.setBackground(mSavedBtnDrawable);
        mConfirmBtn.setClickable(true);
    }

    private void disableBtn() {
        mSavedBtnDrawable = mConfirmBtn.getBackground();
        mConfirmBtn.setClickable(false);
        mConfirmBtn.setBackgroundColor(Color.GRAY);
    }

    private void createNewClssObjAndSave() {
        showProgressDialog("正在创建新课堂...");

        final ClassTeacher newClassTeacher = new ClassTeacher();
        newClassTeacher.setTargetTeacher(AVUser.getCurrentUser());  //班级所有者为我
        newClassTeacher.setAuthroity(0);    //拥有权限

        ClassRoom newClass = new ClassRoom();
        newClass.setMyClassName(mClassName);

        newClassTeacher.setTargetClass(newClass);

        manager.saveInBackGround(newClassTeacher, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("新建课堂成功！");
                setResult(RESULT_OK);

                updateCurrentClassIfNeed(newClassTeacher);

                dismissProgressDialog();
                enableBtn();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);
                Log.d(TAG, "最后创建除了问题.");
                dismissProgressDialog();
                enableBtn();
            }
        });
    }

    /**
     * 更新当前教师用户的class
     */
    private void updateCurrentClassIfNeed(ClassTeacher newClassTeacher) {
        if (LoginActivity.sSelectedClass == null) {
            LoginActivity.sSelectedClass = newClassTeacher;
            //更新登陆信息
            SharedPreferences sp = getSharedPreferences(LoginActivity.SAVED_USER_INFO, MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("classTeacher", LoginActivity.sSelectedClass.toString());
            edit.commit();
        }
    }

    private void getInputData() {
        mClassName = mClassNameET.getText().toString();
    }


    private boolean checkInputData() {
        if (TextUtils.isEmpty(mClassName)) {
            showToast("你还没有填写班级名!");
            return false;
        }

        return true;
    }

    private void bindView() {
        mChooseOtherClassesTV = (TextView) findViewById(R.id.chooseOtherClass);
        mConfirmBtn = (Button) findViewById(R.id.create_new_class_btn);
        mClassNameET = (EditText) findViewById(R.id.class_name_et);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(CreateClass.this);
        mProgressBarDialog.setMessage("数据加载中...");
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

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }


}
