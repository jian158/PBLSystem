package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ApplyJoinGroup;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ProblemInfo extends AppCompatActivity {
    /*常量，调试的Tag值*/
    private static final String TAG = "MyGroupActivity";
    /*Toast静态常量*/
    private static Toast toast;

    public static final String PROBLEM_EXTRA_TAG = "prolem_xtra";

    public TextView mProblemTitleTV, mProblemIntroductionTV, mProblemDifficutyTV, mProblemTimeTV;
    public Button mApplyButton;

    private Problem mProblem;
    private Group mMyGroup;
    private ClassRoom mMyClassRoom;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_info);

        initilizeProgressDialog();
        bindView();
        setClickListennerForBtn();
        getIntentData();
    }

    private void setClickListennerForBtn() {
        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopDialog.popMessageDialog(ProblemInfo.this, "你确定要申请该课题么？", "点错了", "确认",
                        new ConfirmMessage() {
                            @Override
                            public void confirm() {
                                submitApplyProblemTable();
                            }
                        }, null);
            }
        });
    }


    private void submitApplyProblemTable() {
        /**
         * 1 判断我是否是组长
         * 2 判断是否已经申请过该课题，且待审核，如果拥有则退出
         * 3 判断是否已经拥有该课题，如果拥有则退出
         * 4 提交申请
         */
        checkIfIamTheLeader();
    }

    /**
     * 判断我是否是组长
     */
    private void checkIfIamTheLeader() {
        showProgressDialog("正在检测操作权限...");

        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_LEADER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() > 0) {
                    mMyGroup = (Group) results.get(0);
                    //checkIfHaveTheProblem();
                    checkIfHaveTheApply();

                } else {
                    showToast("你不是小组长！没有权限进行此操作！");

                    dismissProgressDialog();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                if (errorCode != 101) {
                    showToast(Constants.NET_ERROR_TOAST);

                    dismissProgressDialog();
                } else {
                    //checkIfHaveTheProblem();
                    checkIfHaveTheApply();
                }
            }
        });
    }

    /**
     * 检测是否已经申请过该课题
     */
    private void checkIfHaveTheApply() {
        showProgressDialog("正在检测数据...");

        DataBaseQuery query = new DataBaseQuery(ProblemApplyTable.CLASS_NAME);
        query.addWhereEqualTo(ProblemApplyTable.S_PROBLEM, mProblem);
        query.addWhereEqualTo(ProblemApplyTable.S_GROUP, mMyGroup);
        query.addWhereEqualTo(ProblemApplyTable.S_STATE, 0);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    showToast("系统检测到你已经申请过该课题！请勿重复申请！");

                    dismissProgressDialog();
                } else {
                    checkIfHaveTheProblem();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                if (errorCode != 101) {
                    showToast(Constants.NET_ERROR_TOAST);

                    dismissProgressDialog();
                } else {
                    checkIfHaveTheProblem();
                }
            }
        });

    }
    /**
     * 检验是否已经拥有该课题
     */
    private void checkIfHaveTheProblem() {
        showProgressDialog("正在检测数据...");

        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_PROBLEM, mProblem);
        query.addWhereEqualTo(ProblemGroup.S_GROUP, mMyGroup);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    showToast("系统检测到你已经拥有该课题！请勿重复申请！");

                    dismissProgressDialog();
                } else {
                    createApplyProblemTable();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                if (errorCode != 101) {
                    showToast(Constants.NET_ERROR_TOAST);

                    dismissProgressDialog();
                } else {
                    createApplyProblemTable();
                }
            }
        });
    }

    /**
     * 生成申请表并提交
     * @param
     */
    private void createApplyProblemTable() {
        findMyClassRoom();
    }

    private void findMyClassRoom() {
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                mMyClassRoom = (ClassRoom) ((AVUser) obj).get(MyUser.S_CLASS);
                createEntityAndSave();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void createEntityAndSave() {
        showProgressDialog("申请提交中,请稍后...");

        ProblemApplyTable applyTable = new ProblemApplyTable();
        applyTable.setOfClass(mMyClassRoom);
        applyTable.setProblem(mProblem);
        applyTable.setGroup(mMyGroup);
        applyTable.setState(0); //初始状态
        applyTable.setExtraInfo("我想要申请这个课题.");
        manager.saveInBackGround(applyTable, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("申请提交成功！请耐心等待审核！");

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String serializedProblem = intent.getStringExtra(PROBLEM_EXTRA_TAG);
        try {
            mProblem = (Problem) AVObject.parseAVObject(serializedProblem);
            getDataFromNet(mProblem);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "反序列化异常.");
        }
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet(Problem problem) {
        showProgressDialog("数据加载中...");

        manager.fetchIfNeededInBackGround(problem, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                getDataAndInitializeUi(obj);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void getDataAndInitializeUi(AVObject obj) {
        Problem problem = (Problem) obj;
        String title = problem.getTitle();
        String introduction = problem.getIntroduction();
        int difficuty = problem.getDifficutity();
        Date showTime = problem.getSpeakTime();
        String dateStr = getTimeFromDate(showTime);

        mProblemTitleTV.setText(title);
        mProblemIntroductionTV.setText(introduction);
        mProblemDifficutyTV.setText(String.valueOf(difficuty));
        mProblemTimeTV.setText(String.valueOf(dateStr));

        dismissProgressDialog();
    }


    /**
     * 绑定组件
     */
    private void bindView() {
        mProblemTitleTV = (TextView) findViewById(R.id.problem_title_tv);
        mProblemIntroductionTV = (TextView) findViewById(R.id.group_name_tv);
        mProblemDifficutyTV = (TextView) findViewById(R.id.apply_extra_info_tv);
        mProblemTimeTV = (TextView) findViewById(R.id.apply_time_info_tv);
        mApplyButton = (Button) findViewById(R.id.apply_problem_btn);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ProblemInfo.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    /**
     * 弹出对话框
     */
    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }

}
