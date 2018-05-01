package com.example.pblsystem.DialogActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Tag;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Fragment.GroupFragmentForStudent;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.List;

public class StuCreateDialog extends Activity {
    //常量，调试的Tag值
    private static final String TAG = "StuCreateDialog";
    //Toast静态常量
    private static Toast toast;
    //取消按钮
    private ImageView mCancelBtn;
    //小组名输入框和密码输入框
    private EditText mGroupNameEditText, mPassWordEditText;
    //单选按钮
    private RadioGroup mRadioGroup;
    //提交按钮
    private Button mSubmitBtn;
    //小组名和密码
    private String mGroupName, mPassword;
    //选择的模式, 默认为0:公开
    private int mMode = Group.MODE_OPEN;
    //保存按钮原来的颜色，用于恢复
    private Drawable mSavedButtonDrawable;
    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_create_group);
        //设置窗口大小
        setWindowDemension();
        binView();
        //初始化圆形对话框
        initilizeProgressDialog();

        //为按钮绑定事件
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyClassRoom();
            }
        });
    }

    private void getMyClassRoom() {
        mProgressBarDialog.setMessage("正在检验数据...");
        mProgressBarDialog.show();

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                checkGroupMode(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                mProgressBarDialog.dismiss();
            }
        });
    }

    private void checkGroupMode(ClassRoom classRoom) {
        DataBaseQuery query = new DataBaseQuery(Tag.CLASS_NAME);
        query.addWhereEqualTo(Tag.S_CLASS, classRoom);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {//可以自由组合
                    checkConditions();
                } else {
                    Tag tag = (Tag) results.get(0);
                    int mode = tag.getGroupMode();
                    if (mode == 1) {    //可以自由组合
                        checkConditions();
                    } else if (mode == 0) { //自由组合模式已经关闭
                        showToast("你所在的课堂已经关闭该功能！");

                        mProgressBarDialog.dismiss();
                    }
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 查询该用户是否已经是某个小组的成员如果是，则不可创建小组
     */
    private void checkConditions() {
        //弹出进度框
        mProgressBarDialog.setMessage("正在检验身份...");
        mProgressBarDialog.show();
        AVUser user = AVUser.getCurrentUser();
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, user);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {//该用户已经是某个小组的成员
                    showToast("客官，已经有组织了，不能创建小组呦！");
                    mProgressBarDialog.dismiss();
                } else {//可以创建小组
                    createNewGroup();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                //隐藏进度框
                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(StuCreateDialog.this);
        mProgressBarDialog.setMessage("正在创建");
    }

    /**
     * 新建小组
     */
    private void createNewGroup() {
        //保存按钮的颜色
        mSavedButtonDrawable = mSubmitBtn.getBackground();
        if (checkData()) {//数据完整，条件合法
            //构建一个小组对象
            final Group newGroup = new Group();
            //获得当前用户对象
            AVUser currentUser = AVUser.getCurrentUser();
            //绑定班级
            newGroup.setClass(currentUser.getAVObject(MyUser.S_CLASS));
            //绑定小组名
            newGroup.setName(mGroupName);
            //设置模式
            newGroup.setFlag(mMode);
            if (mMode == Group.MODE_PSW) {//有密码
                //为新对象设置密码
                newGroup.setPassword(mPassword);
            }
            //设置小组长
            newGroup.setLeader(currentUser);
            //创建一个关系
            AVRelation<AVObject> relation = newGroup.getRelation(Group.S_MEMBER);
            //将创建小组的人加入小组成员列表
            relation.add(currentUser);
            //设置小组人数
            newGroup.setNum(1);
            //查询是否该小组名已经被注册
            DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
            query.addWhereEqualTo(Group.S_NAME, mGroupName);
            query.addWhereEqualTo(Group.S_CLASS, currentUser.get(MyUser.S_CLASS));
            mProgressBarDialog.setMessage("正在核验小组名称...");
            query.countInBackgroundDB(new CountCallBackDB() {
                @Override
                public void CountDoneSuccessful(int number) {
                    if (number > 0) {//已经有该名字的小组
                        showToast("客官，你的小组名已经被强占了，换个试试？");
                        mProgressBarDialog.dismiss();
                    } else {
                        startCreateGroup(newGroup);
                    }
                }

                @Override
                public void CountDoneFailed(String exceptionMsg, int errorCode) {
                    showToast(Constants.NET_ERROR_TOAST);
                    mProgressBarDialog.dismiss();
                }
            });
        } else {
            mProgressBarDialog.dismiss();
        }
    }

    private void startCreateGroup(Group newGroup) {
        //弹出对话框
        mProgressBarDialog.setMessage("正在创建小组...");
        //按钮灰掉
        mSubmitBtn.setClickable(false);
        mSubmitBtn.setBackgroundColor(Color.GRAY);
        DataBaseManager manager = DataBaseManager.getInstance();
        manager.saveInBackGround(newGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("恭喜你，新建成功！");
                //构建返回的数据
                Intent intent = new Intent();
                intent.putExtra(GroupFragmentForStudent.CREATE_GROUP_EXTRA_TAG, mGroupName);
                //将新建小组的objectId返回
                setResult(RESULT_OK, intent);
                //关闭此页面
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg + ":" + errorCode);
                showToast("新建小组失败！" + Constants.NET_ERROR_TOAST);
                reNewProgressDialogAndBtn();
            }
        });

    }

    private void reNewProgressDialogAndBtn() {
        //隐藏对话框
        mProgressBarDialog.dismiss();
        //按钮回复
        mSubmitBtn.setBackground(mSavedButtonDrawable);
        mSubmitBtn.setClickable(true);
    }

    /**
     * 检查数据是否合法，完整
     * @return
     */
    private boolean checkData() {
        //获取数据
        mGroupName = mGroupNameEditText.getText().toString();
        mPassword = mPassWordEditText.getText().toString();


        if ("".equals(mGroupName.trim())) {//小组名为空 (小组名去除前后空格仍为空)
            showToast("客官，要先输入小组名呦！");
            return false;
        } else if (mMode == 2){//要输入密码
            if ("".equals(mPassword)) {
                showToast("客官，不要忘记输入密码呦!");
                mProgressBarDialog.dismiss();
                return false;
            }
        } else if (mGroupName.length() > 14){//小组名过长
            showToast("客官，小组名过长了呦！");
            return false;
        }

        return true;
    }

    /**
     * 自定义RadioButton切换响应事件
     */
    private class MyCheckedChangedListenner implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.dialog_group_mode_open:
                    //模式置0
                    mMode = Group.MODE_OPEN;
                    //密码框不可以输入
                    mPassWordEditText.setEnabled(false);
                    break;
                case R.id.dialog_group_mode_hide:
                    //模式置1
                    mMode = Group.MODE_HIDE;
                    //密码框不可以输入
                    mPassWordEditText.setEnabled(false);
                    break;
                case R.id.dialog_group_mode_password:
                    //模式置2
                    mMode = Group.MODE_PSW;
                    //密码框可以输入
                    mPassWordEditText.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    }
    /*
    绑定xml文件中的组件
     */
    private void binView() {
        mGroupNameEditText = (EditText) findViewById(R.id.dialog_group_name);
        mPassWordEditText = (EditText) findViewById(R.id.dialog_group_password);
        //默认密码不能输入
        mPassWordEditText.setEnabled(false);
        mRadioGroup = (RadioGroup) findViewById(R.id.dialog_group_mode_radio_group);
        //默认选中第一个radiobutton
        mRadioGroup.check(R.id.dialog_group_mode_open);
        //监听radiogroup的按钮切换事件
        mRadioGroup.setOnCheckedChangeListener(new MyCheckedChangedListenner());
        mSubmitBtn = (Button) findViewById(R.id.dialog_create_group);
        mCancelBtn = (ImageView) findViewById(R.id.dialog_group_cancel);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁Activity
                finish();
            }
        });
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
//        p.height = (int) (size.y * 0.5);
//        //宽度设置为屏幕的0.8
//        p.width = (int) (size.x * 0.8);
        //设置本身透明度
        p.alpha = 1.0f;
        //设置黑暗度
        p.dimAmount = 0.8f;
        //应用设置后的属性
        getWindow().setAttributes(p);
    }


}
