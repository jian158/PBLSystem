package com.example.pblsystem.Activity;
/**
 * 登录界面
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SendCallback;
import com.example.pblsystem.Class.ApplyJoinGroup;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.DescriptionScore;
import com.example.pblsystem.Class.EvaluationStandard;
import com.example.pblsystem.Class.EvalutionDescription;
import com.example.pblsystem.Class.ExitGroupApply;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MemberWork;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.OnLine;
import com.example.pblsystem.Class.Posts;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.Class.ProblemRevokeTable;
import com.example.pblsystem.Class.RegisterTeacherApply;
import com.example.pblsystem.Class.Replies;
import com.example.pblsystem.Class.SpeechComment;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.Class.Tag;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.LoginCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    public static final String SAVED_USER_INFO = "savedUser";
    //常量，调试的Tag值
    private static final String TAG = "LoginActivity";
    //Toast静态常量
    private static Toast toast;
    //新用户注册按钮
    private Button mCreateNewUserBtn;
    //用户名、密码
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    //清除按钮
    private ImageButton mClearUserNameBtn;
    private ImageButton mClearPasswordBtn;
    //登录按钮
    private Button mLoginBtn, mForgetPasswordBtn;
    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //保存输入框中的数据
    private String mUsername;
    private String mPassword;

    //保存我选择的课堂
    public static ClassTeacher sSelectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //应用初始化
        AVOSCloud.initialize(this,"VWjx8rs9K4uF1UepyINe5RUn-gzGzoHsz","Jqs9oDYovSAFrmPo6hAybw56");
        if (checkIfAutoLogin()) {
            enterAutoLoginPage();
        }

        setContentView(R.layout.activity_login);
        registerSubclasses();



        //AVOSCloud.initialize(this,"yDSvny8WEVWmORMN4Hma8WMc-gzGzoHsz","ODtnw7oesaeDMd6O6rUEpuEr");    // 学弟的
        //AVOSCloud.initialize(this,"3frd51czCjHViYN8RECQTwRG-gzGzoHsz","v6ojYvihlkFaaL7NxsMKhNx2");    // test
        //AVOSCloud.initialize(this,"xH89Yo2EwCAMyphRp33iTgC7-gzGzoHsz","dMTKVKA5jRWPqYV0wWkvHfuY");

        bindView();
        initilizeProgressDialog();

    }

    private void enterAutoLoginPage() {
        Intent intent = new Intent(this, AutoLogin.class);
        startActivity(intent);
        finish();
    }

    private boolean checkIfAutoLogin() {
        SharedPreferences sp = getSharedPreferences(SAVED_USER_INFO, MODE_PRIVATE);
        String username = sp.getString("username", "null");
        String password = sp.getString("password", "null");
        if ("null".equals(username) || "null".equals(password)) {//没有登陆过
            return false;
        }

        return true;

     }

    /**
     * 注册子类
     */
    private void registerSubclasses() {
        //注册子类
        AVObject.registerSubclass(ClassRoom.class);
        AVObject.registerSubclass(Problem.class);
        AVObject.registerSubclass(Group.class);
        AVObject.registerSubclass(ApplyJoinGroup.class);
        AVObject.registerSubclass(ClassTeacher.class);
        AVObject.registerSubclass(ProblemGroup.class);
        AVObject.registerSubclass(ProblemApplyTable.class);
        AVObject.registerSubclass(ProblemRevokeTable.class);
        AVObject.registerSubclass(MemberWork.class);
        AVObject.registerSubclass(SpeechEvaluation.class);
        AVObject.registerSubclass(EvaluationStandard.class);
        AVObject.registerSubclass(EvalutionDescription.class);
        AVObject.registerSubclass(ExitGroupApply.class);
        AVObject.registerSubclass(ProblemLibrary.class);
        AVObject.registerSubclass(RegisterTeacherApply.class);
        AVObject.registerSubclass(DescriptionScore.class);
        AVObject.registerSubclass(Tag.class);
        AVObject.registerSubclass(Posts.class);
        AVObject.registerSubclass(Replies.class);
        AVObject.registerSubclass(SpeechComment.class);
        AVObject.registerSubclass(OnLine.class);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(LoginActivity.this);
        mProgressBarDialog.setMessage("正在登录");
    }

    /**
     * 绑定布局组件，并设置监听事件
     */
    private void bindView() {
        //新用户注册
        mCreateNewUserBtn = (Button) findViewById(R.id.login_register_btn);
        mCreateNewUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*进入注册页面*/
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
        mUserNameEditText = (EditText) findViewById(R.id.login_username);
        mPasswordEditText = (EditText) findViewById(R.id.login_password);
        //为输入框设置文字变化监听事件
        mUserNameEditText.addTextChangedListener(new MyWatcher(mUserNameEditText));
        mPasswordEditText.addTextChangedListener(new MyWatcher(mPasswordEditText));

        mClearUserNameBtn = (ImageButton) findViewById(R.id.login_clear_username);
        mClearUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用户名输入框置空
                mUserNameEditText.setText("");
            }
        });
        mClearPasswordBtn = (ImageButton) findViewById(R.id.login_clear_password);
        mClearPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //密码输入框置空
                mPasswordEditText.setText("");
            }
        });
        //默认隐藏清理按钮
        mClearUserNameBtn.setVisibility(View.INVISIBLE);
        mClearPasswordBtn.setVisibility(View.INVISIBLE);
        //登录按钮
        mLoginBtn = (Button) findViewById(R.id.login_submit_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录响应事件
                //获取输入框中的数据
                mUsername = mUserNameEditText.getText().toString();
                mPassword = mPasswordEditText.getText().toString();

//                // 推送
//                AVPush push = new AVPush();
//                JSONObject object = new JSONObject();
//                object.put("alert", "push message to android device directly");
//                object.put("name", "LeanCloud");
//                object.put("tag", "exit");
//                object.put("action", "com.avos.UPDATE_STATUS");
//                push.setPushToAndroid(true);
//                push.setData(object);
//                push.sendInBackground(new SendCallback() {
//                    @Override
//                    public void done(AVException e) {
//                        if (e == null) {
//                            // push successfully.
//                            showToast("推送成功！");
//                        } else {
//                            // something wrong.
//                            Log.d("tag", e.getMessage());
//                        }
//                    }
//                });

//                AVQuery pushQuery = AVInstallation.getQuery();
//                pushQuery.whereEqualTo("installationId", "9416e7924702dcc1ea680b8a209f7e7f");
//                AVPush.sendMessageInBackground("go to see other ui",  pushQuery, new SendCallback() {
//                    @Override
//                    public void done(AVException e) {
//                        if (e == null) {
//                            Log.d("tag", "推送成功");
//                        } else {
//                            Log.d("tag", e.getMessage());
//                        }
//                    }
//                });
                login(mUsername, mPassword);
            }
        });

        mForgetPasswordBtn = (Button) findViewById(R.id.forgetpassword);
        mForgetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterForgetPasswordPage();
            }
        });
    }

    private void enterForgetPasswordPage() {
        Intent intent = new Intent(this, ForgetPassword.class);
        startActivity(intent);
    }

    /**
     * 判断当前用户是否登录
     */
    private void checkIfOnLine(final String username, final  String password) {
        mProgressBarDialog.setMessage("身份检验中...");
        mProgressBarDialog.show();

        DataBaseQuery query = new DataBaseQuery(OnLine.CLASS_NAME);
        query.addWhereEqualTo(OnLine.S_USERNAME, username);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    login(username, password);
                } else {
                    OnLine onLine = (OnLine) results.get(0);
                    pushExit(onLine.getString("installId"));
                    Log.d("tag", "push");
                    login(username, password);
                }

                mProgressBarDialog.dismiss();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                mProgressBarDialog.dismiss();
            }
        });

    }

    private void pushExit(String installId) {
        AVQuery pushQuery = AVInstallation.getQuery();
        pushQuery.whereEqualTo("installationId", installId);
        JSONObject object = new JSONObject();
        try {
            object.put("alert", "push message to android device directly");
            object.put("name", "LeanCloud");
            object.put("action", "com.avos.UPDATE_STATUS");
            object.put("tag", "exit");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("tag", e.getMessage());
        }

        AVPush.sendDataInBackground(object, pushQuery, new SendCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Log.d("tag", "推送成功");
                } else {
                    Log.d("tag", e.getMessage());
                }
            }
        });
    }

    /**
     * 处理登录事件
     */
    private void login(final String username,final String password) {

        if (checkData()) {//用户输入数据合法
            //登录之前，弹出进度框
            mProgressBarDialog.show();
            //保存按钮的原来背景，用于恢复
            final Drawable savedLoginButtonBackground = mLoginBtn.getBackground();
            //按钮暂时灰掉，不可用
            mLoginBtn.setClickable(false);
            mLoginBtn.setBackgroundColor(Color.GRAY);
            //验证用户名和密码
            MyUser.loginSystemInBackground(username, password, new LoginCallBackDB() {
                @Override
                public void loginDoneSuccessful(AVUser avUser) {
                    // 登录成功, 设备注册推送功能
                    registerPush(username);
                    if (avUser.getInt(MyUser.S_AUTHORITY) == 0) {//学生用户
                        //进入系统主界面
                        Intent intent = new Intent(getApplicationContext(), MainActivityForStudent.class);
                        //构建Bundle用于传递数据
                        Bundle bundle = new Bundle();
                        //将用户对象序列化后，传入
                        bundle.putString(MainActivityForStudent.TAG_USER, avUser.toString());
                        intent.putExtras(bundle);
                        //打开系统界面
                        startActivity(intent);
                        //保存登陆信息
                        saveLoginInfo(username, password);
                        //销毁登录界面
                        finish();
                    } else if (avUser.getInt(MyUser.S_AUTHORITY) == 1){
                        selectTheClass();
                    } else {    //管理员
                        //
                        //
                        Intent intent = new Intent(getApplicationContext(), MainActivityForAdmin.class);
                        startActivity(intent);
                        finish();
                    }
                    reNewProgressDialogAndButton(savedLoginButtonBackground);

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
                    //恢复按钮
                    mLoginBtn.setClickable(true);
                    mLoginBtn.setBackground(savedLoginButtonBackground);
                }
            });

        }
    }

    /**
     *  注册设备启动推送功能
     */
    private void registerPush(final String username) {
        PushService.setDefaultPushCallback(LoginActivity.this, LoginActivity.class);
        AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) { // 启动成功
                    // 保存InstallId到user表
                    saveInstallId(AVInstallation.getCurrentInstallation().getInstallationId(), username);

                } else {   // 启动失败
                    Log.d("tag", e.getMessage());
                }
            }
        });
    }

    private void saveInstallId(String installId, final String username) {
        final AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.put("installId", installId);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        Log.d("tag", "installId保存成功");
                        // 上传信息到online
                        OnLine onLine = new OnLine();
                        onLine.setUsername(username);
                        onLine.put("installId", currentUser.getString("installId"));
                        onLine.saveInBackground();
                    } else {
                        Log.d("tag", e.getMessage());
                    }
                }
            });
        }
    }


    private void saveLoginInfo(String username, String password) {
        SharedPreferences sp = getSharedPreferences(SAVED_USER_INFO, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString("username", username);
        edit.putString("password", password);

        edit.commit();
    }


    private void enterSystem() {
        saveLoginInfoOfTeacher();
        //进入系统主界面
        Intent intent = new Intent(getApplicationContext(), MainActivityForTeacher.class);
        startActivity(intent);
        finish();
    }

    private void saveLoginInfoOfTeacher() {
        SharedPreferences sp = getSharedPreferences(SAVED_USER_INFO, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString("username", mUsername);
        edit.putString("password", mPassword);
        if (sSelectedClass != null) {
            edit.putString("classTeacher", sSelectedClass.toString());
        }

        edit.commit();
    }

    /**
     * 弹出对话框，选择一个班级
     */
    private void selectTheClass() {
        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.whereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser());
        query.includePointer(ClassTeacher.S_CLASS);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() > 1) {
                    popClassesSelectDialog(results);
                } else if (results.size() == 1) {
                    sSelectedClass = (ClassTeacher) results.get(0);
                    enterSystem();
                } else {
                    enterSystem();
                }

            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                if (errorCode == 101) { //班级尚未创建
                    enterSystem();
                } else {
                    showToast(Constants.NET_ERROR_TOAST);
                }
            }
        });
    }

    /**
     * 弹出对话框
     */
    private void popClassesSelectDialog(final List results) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(this);
        //获取对话框view
        View view = inflater.inflate(R.layout.dialog_select_class, null, false);
        //绑定组件
        ListView classesListView = (ListView) view.findViewById(R.id.dialog_class_list_view);
        ImageView dismissBtn = (ImageView) view.findViewById(R.id.dialog_cancel_imageview);

        List<String> classesData = new ArrayList<>();
        for (Object obj: results) {
            ClassTeacher classTeacher = (ClassTeacher) obj;
            ClassRoom myclass = (ClassRoom) classTeacher.getTargetClass();
            classesData.add(myclass.getMyClassName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, classesData);

        classesListView.setAdapter(adapter);

        classesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sSelectedClass = (ClassTeacher) results.get(position);
                enterSystem();
            }
        });

        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();;
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realDialog.dismiss();
            }
        });

    }

    private void reNewProgressDialogAndButton(Drawable savedLoginButtonBackground) {
        //隐藏进度框
        mProgressBarDialog.dismiss();
        //恢复按钮
        mLoginBtn.setClickable(true);
        mLoginBtn.setBackground(savedLoginButtonBackground);
    }

    /**
     * 检查输入数据是否合法
     * @return
     */
    private boolean checkData() {
        if ("".equals(mUsername)) {
            showToast("请输入用户名");
            return false;
        } else if ("".equals(mPassword)) {
            showToast("请输入密码");
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
     * 监听输入框文字变化
     */
    private class MyWatcher implements TextWatcher {
        //监听的输入框
        private EditText mWatchedEditText;
        /*
        构造函数，判断监听的是哪个输入框
         */
        public MyWatcher(EditText editText) {
            mWatchedEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mWatchedEditText.getText().length() > 0) {//输入框中有文字输入
                switch (mWatchedEditText.getId()) {
                    case R.id.login_username:
                        //显示清除用户框的按钮
                        mClearUserNameBtn.setVisibility(View.VISIBLE);
                        break;
                    case R.id.login_password:
                        //显示清除密码框的按钮
                        mClearPasswordBtn.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                switch (mWatchedEditText.getId()) {
                    case R.id.login_username:
                        //隐藏清除用户框的按钮
                        mClearUserNameBtn.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.login_password:
                        //隐藏清除密码框的按钮
                        mClearPasswordBtn.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        }
    }
}
