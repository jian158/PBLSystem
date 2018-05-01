package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SignUpCallback;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.Class.RegisterTeacherApply;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CancelMessage;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.MyDecoration;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ApplyTeacherRegister extends AppCompatActivity {

    private static Toast toast;
    public static final String TAG = "ShowLibrarayProblems";

    private RecyclerView mApplysListView;
    private List<RegisterTeacherApply> mAllApplysList = new ArrayList<>();

    private MyAdapter adapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    // 保存最近点击的项目
    private int currentClickedIndex = -1;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_teacher_register);

        initilizeProgressDialog();
        bindView();
        setRecycleView();
        getDataFromNet();
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("系统君正在拼命加载数据...");
        DataBaseQuery query = new DataBaseQuery(RegisterTeacherApply.CLASS_NAME);
        query.addWhereEqualTo(RegisterTeacherApply.S_STATE, 0); // 只获取需要处理的记录
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() < 1) {
                    dismissProgressDialog();
                    return;
                }

                for (Object obj: results) {
                    RegisterTeacherApply apply = (RegisterTeacherApply) obj;
                    mAllApplysList.add(apply);
                }
                //通知列表刷新
                adapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }


    private void setRecycleView() {
        adapter = new MyAdapter();
        mApplysListView.setLayoutManager(new LinearLayoutManager(this));
        mApplysListView.addItemDecoration(new MyDecoration(this));
        mApplysListView.setAdapter(adapter);
    }

    /**
     * 绑定xml组件
     */
    private void bindView() {
        mApplysListView = (RecyclerView) findViewById(R.id.all_problems_recycle_view);
    }



    /**
     * 自定义RecycleView的ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mName, mExtra, mApplyTime;
        private View mSavedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mExtra = (TextView) itemView.findViewById(R.id.extra);
            mApplyTime = (TextView) itemView.findViewById(R.id.apply_time_tv);
            mName = (TextView) itemView.findViewById(R.id.name);

            mSavedView = itemView;
        }
    }

    /**
     * 自定义RecycleView适配器
     */
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View itemView = inflater.inflate(R.layout.register_apply_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            RegisterTeacherApply apply = mAllApplysList.get(position);
            final String name = apply.getName();
            Date applyTime = apply.getCreatedAt();
            String time = getTimeFromDate(applyTime);
            String extra = apply.getExtra();

            holder.mName.setText(name);
            holder.mExtra.setText(extra);
            holder.mApplyTime.setText(time);

            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 更新点击下标
                    currentClickedIndex = position;

                    PopDialog.popMessageDialog(ApplyTeacherRegister.this, name + "想要创建教师用户，是否同意?", "拒绝", "同意",
                            new ConfirmMessage() {
                                @Override
                                public void confirm() {
                                    agree(position);
                                }
                            }, new CancelMessage() {
                                @Override
                                public void cancel() {
                                    disagree(position);
                                }
                            });
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllApplysList.size();
        }
    }

    private void agree(int position) {
        final RegisterTeacherApply apply = mAllApplysList.get(position);
        if (apply == null) return;

        showProgressDialog("正在同意该请求...");

        AVUser user = new AVUser();
        user.setUsername(apply.getUsername());
        user.setPassword(apply.getPassword());
        user.put(MyUser.S_NAME, apply.getName());
        user.put(MyUser.S_AUTHORITY, 1);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    showToast("教师用户新建成功！");
                    updateState(apply, 1);

                    dismissProgressDialog();
                } else {
                    if (e.getCode() == 202) {
                        showToast("操作失败！用户名重复注册！系统将拒绝本次请求！");
                        updateState(apply, 2);
                    } else {
                        Log.d("tag", e.getMessage());
                        showToast("操作失败！请检查网络是否连接？");
                    }

                    dismissProgressDialog();
                }
            }
        });

    }

    //更新申请记录状态
    private void updateState(RegisterTeacherApply apply, int state) {
        apply.setState(state);
        manager.saveInBackGround(apply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                // 刷新页面
                refresh();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    private void refresh() {
        if (currentClickedIndex == -1) return;
        mAllApplysList.remove(currentClickedIndex);
        adapter.notifyDataSetChanged();
    }

    private void disagree(int position) {
        RegisterTeacherApply apply = mAllApplysList.get(position);
        if (apply == null) return;

        apply.setState(2);
        showProgressDialog("正在拒绝该请求...");
        manager.saveInBackGround(apply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("操作成功!");
                refresh();

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });

    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ApplyTeacherRegister.this);
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


    //防止窗体句柄泄露
    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissProgressDialog();
    }

    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }

}
