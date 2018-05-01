package com.example.pblsystem.Activity;
/**
 * 注册界面
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.LoginCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "RegisterActivity";
    //Toast静态常量
    private static Toast toast;

    //选择班级下拉框组件
    private Spinner mSelectClassSpinner;
    //已经创建的班级
    private List<ClassTeacher> mAllClassList = new ArrayList<>();
    //下拉框数据源
    private List<String> mSpinnerData;
    //用户名输入框
    private EditText mUsernameEditText;
    //密码第一次输入框
    private EditText mPassWordFirstEditText;
    //密码第二次输入框
    private EditText mPassWordSecondEditText;
    // 邮箱
    private EditText mEmailText;
    //姓名输入框
    private EditText mNameEditText;
    //提交注册的按钮
    private Button mSubmitRegisterButton;
    //选择的下拉框的下标
    private int mChooseSpinnerIndex = 0;
    //保存EditText中输入的值
    private String mUserName, mPasswordFirst, mPasswordSecond, mName,mEmail;
    /*注册教师用户*/
    private TextView mRegisterTeacher;
    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bindView();
        initilizeProgressDialog();
        getClassFromNet();
        initlizeSubmitButton();

    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(RegisterActivity.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }


    /**
     * 从云端获取所有的班级
     */
    private void getClassFromNet() {
        //加载数据之前，弹出进度对话框
        mProgressBarDialog.show();
        /*查询ClassTeacher表，标志位为0的课堂*/
        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.addWhereEqualTo(ClassTeacher.S_AUTHROITY, 0);
        query.includePointer(ClassTeacher.S_TEACHER);
        query.includePointer(ClassTeacher.S_CLASS);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                //获取读到的数据
                for (Object obj: results) {
                    ClassTeacher classTeacher = (ClassTeacher) obj;
                    mAllClassList.add(classTeacher);
                }
                //更新Spinner
                initlizeClassSpinner();
                //设置ProgressDialog不可见
                mProgressBarDialog.dismiss();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, "出错了" + exceptionMsg);
                //设置ProgressDialog不可见
                mProgressBarDialog.dismiss();
            }
        });


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
        mEmail = mEmailText.getText().toString();

        if (checkValidity()) {//用户正确输入数据
            //构建AVObject
            MyUser newUser = new MyUser();
            newUser.setUsername(mUserName);
            newUser.setPassword(mPasswordFirst);
            newUser.setMyName(mName);
            //设置用户权限为学生 0代表学生
            newUser.setAuthority(0);
            //关联用户选择的班级
            ClassTeacher choosedClass = mAllClassList.get(mChooseSpinnerIndex-1);
            newUser.setMyClass(choosedClass.getTargetClass());
            //设置邮箱
            //String email = mEmail.getText().toString();
            if(!mEmail.equals(""))
            {
                newUser.setEmail(mEmail);
            }
            //数据提交前，弹出进度框
            mProgressBarDialog.show();
            //保存按钮的原来背景，用于恢复
            final Drawable savedSubmitButtonBackground = mSubmitRegisterButton.getBackground();
            //按钮暂时灰掉，不可用
            mSubmitRegisterButton.setClickable(false);
            mSubmitRegisterButton.setBackgroundColor(Color.GRAY);
            //设置进度框信息
            mProgressBarDialog.setMessage("注册进行中...");

            DataBaseManager manager = DataBaseManager.getInstance();

            manager.saveInBackGround(newUser, new SaveCallBackDB() {

                @Override
                public void saveDoneSuccessful() {
                    showToast("注册成功！");
                    //数据加载完毕，关闭进度框
                    mProgressBarDialog.dismiss();

                    mSubmitRegisterButton.setClickable(true);
                    mSubmitRegisterButton.setBackground(savedSubmitButtonBackground);

                    PopDialog.popMessageDialog(RegisterActivity.this, "是否要直接进入系统？", "不了", "是",
                            new ConfirmMessage() {
                                @Override
                                public void confirm() {
                                    login();
                                }
                            }, null);
                }

                @Override
                public void saveDoneFailed(String exceptionMsg, int errorCode) {
                    switch (errorCode) {
                        case 202:
                            Log.d(TAG, "用户名重复");
                            showToast("注册失败！该用户名已经被注册！");
                            break;
                        case 125:
                            Log.d(TAG, "邮箱不合法");
                            showToast("注册失败！你输入的邮箱不合法！");
                            break;
                        case 203:
                            showToast("注册失败！你输入的邮箱已经被占用！");
                            break;
                        default:
                            Log.d(TAG, "其他故障" + exceptionMsg);
                            showToast(Constants.NET_ERROR_TOAST);
                            break;
                    }
                    //进度条取消
                    mProgressBarDialog.dismiss();

                    mSubmitRegisterButton.setClickable(true);
                    mSubmitRegisterButton.setBackground(savedSubmitButtonBackground);
                }
            });
        }



    }

    private void login() {
        mProgressBarDialog.setMessage("登录中...");
        mProgressBarDialog.show();

        MyUser.loginSystemInBackground(mUserName, mPasswordFirst, new LoginCallBackDB() {
            @Override
            public void loginDoneSuccessful(AVUser avUser) {
                Intent intent = new Intent(getApplicationContext(), MainActivityForStudent.class);
                // 清空所有的activity栈
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //构建Bundle用于传递数据
                Bundle bundle = new Bundle();
                //将用户对象序列化后，传入
                bundle.putString(MainActivityForStudent.TAG_USER, avUser.toString());
                intent.putExtras(bundle);
                //打开系统界面
                startActivity(intent);
                //保存登陆信息
                saveLoginInfo(mUserName, mPasswordFirst);
                //销毁登录界面
                finish();
            }

            @Override
            public void loginDoneFailed(String exceptionMsg, int errorCode) {
                switch (errorCode) {
                    case 211:
                        showToast("用户名错误！");
                        break;
                    case 210:
                        showToast("密码错误！");
                        break;
                    default:
                        showToast(Constants.NET_ERROR_TOAST);
                        break;
                }
                //隐藏进度框
                mProgressBarDialog.dismiss();
            }
        });
    }

    private void saveLoginInfo(String username, String password) {
        SharedPreferences sp = getSharedPreferences(LoginActivity.SAVED_USER_INFO, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString("username", username);
        edit.putString("password", password);

        edit.commit();
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

        if (mChooseSpinnerIndex == 0) {//未选择班级
            showToast("请先选择一个班级");
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

        Pattern pattern = Pattern.compile("[A-Za-z0-9_]+");
        Matcher matUsername = pattern.matcher(mUserName);
        if (!matUsername.matches()) {
            showToast("用户名只能包含字母、数字、下划线，请勿输入非法字符！");
            return false;
        }

        Matcher matPassword = pattern.matcher(mPasswordFirst);
        if (!matPassword.matches()) {
            showToast("密码只能包含字母、数字、下划线，请勿输入非法字符！");
            return false;
        }

        return true;
    }

    /**
     * 初始化选择课堂下拉框
     */
    private void initlizeClassSpinner() {
        //初始化Spinner数据源
        mSpinnerData = new ArrayList<>();
        mSpinnerData.add("课堂");
        for (ClassTeacher classObj: mAllClassList) {
            ClassRoom classRoom = (ClassRoom) classObj.getTargetClass();
            AVUser teacher = (AVUser) classObj.getTargetTeacher();
            mSpinnerData.add(classRoom.getMyClassName() + "-----" + teacher.getString(MyUser.S_NAME));
        }
        //构建适配器，绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mSpinnerData);
        //设置Spinner样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectClassSpinner.setAdapter(adapter);
        mSelectClassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //设置选择的下拉框的下标
                mChooseSpinnerIndex = position;
                Log.d(TAG, "：" + mChooseSpinnerIndex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 成员变量绑定xml文件中的组件
     */
    private void bindView() {
        mSelectClassSpinner = (Spinner) findViewById(R.id.register_select_class_spinner);
        mUsernameEditText = (EditText) findViewById(R.id.register_username);
        mPassWordFirstEditText = (EditText) findViewById(R.id.register_password_first);
        mPassWordSecondEditText = (EditText) findViewById(R.id.register_password_second);
        mSubmitRegisterButton = (Button) findViewById(R.id.register_submit_btn);
        mNameEditText = (EditText) findViewById(R.id.register_name);
        mRegisterTeacher = (TextView) findViewById(R.id.register_teacher);
        mRegisterTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOneTeacher();
            }
        });
        mEmailText = (EditText) findViewById(R.id.email);
    }

    private void addOneTeacher() {
        Intent intent = new Intent(this, NewTeacher.class);
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
