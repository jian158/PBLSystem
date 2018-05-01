package com.example.pblsystem.DialogActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.ApplyJoinGroup;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

public class DialogApplyGroup extends Activity {
    //Intent传值 flag
    public static final String GROUP_EXTRA_FLAG = "group";
    //调试 TAG
    public final String TAG = "DialogApplyGroup";
    //Toast静态常量
    private static Toast toast;
    //取消按钮
    private ImageView mCancelBtn;
    //确认按钮
    private Button mSubmitBtn;
    //附加信息输入框
    private EditText mExtraInfoEditText;
    //小组名
    private TextView mGroupNameTextView;
    //小组长姓名
    private TextView mGroupLeaderNameTextView;
    //传来的小组对象
    private Group mGroup;
    //附加信息
    private String mExtraString;
    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //保存按钮原来的颜色，用于恢复
    private Drawable mSavedDrawable;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_apply_group);

        setWindowDemension();
        bindView();
        initilizeProgressDialog();
        getIntentData();
        //初始化界面
        initilizeUi();
    }

    /**
     * 初始化界面
     */
    private void initilizeUi() {
        //获取小组名
        final String groupName = mGroup.getName();
        //获取小组长对象 本地
        AVUser groupLeader = (AVUser) mGroup.getLeader();
        //进度条开启
        mProgressBarDialog.show();

        manager.fetchInBackGround(groupLeader, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                AVUser groupLeader;
                groupLeader = (AVUser) obj;
                //获取leader名
                String leaderName = groupLeader.getString(MyUser.S_NAME);
                //初始化TextView
                mGroupLeaderNameTextView.setText(leaderName);
                mGroupNameTextView.setText(groupName);

                //进度条隐藏
                mProgressBarDialog.dismiss();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);
                //进度条隐藏
                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(DialogApplyGroup.this);
        mProgressBarDialog.setMessage("正在加载数据...");
    }

    /**
     * 获取传来的数据
     */
    private void getIntentData() {
        //获取序列化后的字符串
        String serializedString = getIntent().getStringExtra(DialogApplyGroup.GROUP_EXTRA_FLAG);
        //反序列化
        try {
            mGroup = (Group) AVObject.parseAVObject(serializedString);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "反序列化失败");
        }
    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mCancelBtn = (ImageView) findViewById(R.id.dialog_apply_group_cancel);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        mGroupNameTextView = (TextView) findViewById(R.id.dialog_apply_group_name);
        mGroupLeaderNameTextView = (TextView) findViewById(R.id.dialog_apply_group_leader_name);
        mExtraInfoEditText = (EditText) findViewById(R.id.dialog_apply_group_extra_info);
        mSubmitBtn = (Button) findViewById(R.id.dialog_join_group_btn);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitApply();
            }
        });
    }

    /**
     * 销毁窗口
     */
    private void cancel() {
        finish();
    }

    /**
     * 提交申请
     */
    private void submitApply() {
        //获取附加信息
        mExtraString = mExtraInfoEditText.getText().toString();
        //新建加组申请对象
        ApplyJoinGroup applyJoinGroupObj = new ApplyJoinGroup();
        applyJoinGroupObj.setApplyUser(AVUser.getCurrentUser());
        applyJoinGroupObj.setTargetGroup(mGroup);
        //默认初始状态
        applyJoinGroupObj.setState(0);
        if (TextUtils.isEmpty(mExtraString)) {//附加信息为空
            applyJoinGroupObj.setsExtraInfo("无");
        } else {
            applyJoinGroupObj.setsExtraInfo(mExtraString);
        }
        mProgressBarDialog.setMessage("系统君正在拼命提交申请...");
        mProgressBarDialog.show();
        //保存按钮原来的颜色
        mSavedDrawable = mSubmitBtn.getBackground();
        //按钮灰掉
        mSubmitBtn.setBackgroundColor(Color.GRAY);
        mSubmitBtn.setClickable(false);
        //保存对象
        manager.saveInBackGround(applyJoinGroupObj, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("申请成功,请等待审核!");
                //关掉当前对话框
                finish();
                //隐藏进度框
                mProgressBarDialog.dismiss();
                //恢复按钮
                mSubmitBtn.setClickable(true);
                mSubmitBtn.setBackground(mSavedDrawable);
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast("申请失败!" + Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);
                //隐藏进度框
                mProgressBarDialog.dismiss();
                //恢复按钮
                mSubmitBtn.setClickable(true);
                mSubmitBtn.setBackground(mSavedDrawable);
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
}
