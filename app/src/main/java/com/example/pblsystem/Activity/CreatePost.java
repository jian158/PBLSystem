package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
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
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Posts;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

public class CreatePost extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "CreatePost";
    //Toast静态常量
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    private EditText titleET, contentET;
    private Button submitBtn;

    private String title, content;

    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initilizeProgressDialog();
        bindView();
    }

    private void bindView() {
        titleET = (EditText) findViewById(R.id.title);
        contentET = (EditText) findViewById(R.id.content);
        submitBtn = (Button) findViewById(R.id.submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    private void submit() {
        if (checkData()) {
            getMyOfClass();
        }
    }

    private void getMyOfClass() {
        showProgressDialog("正在发布...");

        //如果是教师用户
        int authority = AVUser.getCurrentUser().getInt(MyUser.S_AUTHORITY);
        if (authority != 0) {
            createNewPost((ClassRoom) LoginActivity.sSelectedClass.getTargetClass());
            return;
        }

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                createNewPost(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void createNewPost(ClassRoom myClass) {
        Posts post = new Posts();
        post.setClass(myClass);
        post.setTitle(title);
        post.setContent(content);
        post.setOwner(AVUser.getCurrentUser());

        manager.saveInBackGround(post, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("发布成功!");
                setResult(RESULT_OK);

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                dismissProgressDialog();
            }
        });
    }


    private boolean checkData() {
        title = titleET.getText().toString();
        content = contentET.getText().toString();

        if (TextUtils.isEmpty(title)) {
            showToast("请先输入标题");
            return false;
        }

        return true;
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
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
