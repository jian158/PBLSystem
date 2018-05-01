package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.AsyncListUtil;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.ProblemRevokeTable;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ShowApplyInfoDetail extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "ShowApplyInfoDetail";
    public static final String EXTRA_TAG = "problemApplyTableEtra";
    public static final String EXTRA_TAG_MODE = "mode";

    private TextView mProblemTitleTV, mGroupNameTV, mExtraInfoTV, mApplyTimeTV;
    private Button mAgreeBtn, mDisAgreeBtn;
    private TextView mShowGroupInfoTV;

    private ProblemApplyTable mApplyTable;
    private ProblemRevokeTable mRevokeTable;
    private Problem mProblem;
    private Group mGroup;
    private ClassRoom mClass;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    private DataBaseManager manager = DataBaseManager.getInstance();

    private int mode;   //模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_apply_info_detail);

        try {
            initilizeProgressDialog();
            bindView();
            getDataFromIntent();
            setClickListenner();
            initilizeUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void bindView() {
        mProblemTitleTV = (TextView) findViewById(R.id.problem_title_tv);
        mGroupNameTV = (TextView) findViewById(R.id.group_name_tv);
        mApplyTimeTV = (TextView) findViewById(R.id.apply_time_info_tv);
        mExtraInfoTV = (TextView) findViewById(R.id.apply_extra_info_tv);
        mShowGroupInfoTV = (TextView) findViewById(R.id.show_group_info_detail_tv);

        mAgreeBtn = (Button) findViewById(R.id.agree_btn);
        mDisAgreeBtn = (Button) findViewById(R.id.disagree_btn);
    }

    /**
     * 注意，获取传递值可能失败
     */
    private void getDataFromIntent() throws Exception {
        Intent intent = getIntent();
        mode = intent.getIntExtra(EXTRA_TAG_MODE, -1);
        String serializedObj = intent.getStringExtra(EXTRA_TAG);
        if (mode == 0) {
            mApplyTable = (ProblemApplyTable) AVObject.parseAVObject(serializedObj);
        } else if (mode == 1) {
            mRevokeTable = (ProblemRevokeTable) AVObject.parseAVObject(serializedObj);
        }
    }

    private void setClickListenner() {
        mAgreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == 0) {
                    agreeApplyProblem();
                } else if (mode == 1) {
                    agreeRevokeProblem();
                }
            }
        });
        
        mDisAgreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == 0) {
                    disAgreeApplyProblem();
                } else if (mode == 1) {
                    disagreeRevokeProblem();
                }
            }
        });
        
        mShowGroupInfoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupDetailInfo();
            }
        });
        
    }

    /**
     * 拒绝申请
     */
    private void disAgreeApplyProblem() {
        showProgressDialog("正在拒绝...");

        mApplyTable.setState(2); //置为拒绝状态

        manager.saveInBackGround(mApplyTable, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("已经拒绝该申请！");

                setResult(RESULT_OK);
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

    /**
     * 拒绝申请
     */
    private void disAgreeWithoutToast() {
        if (mGroup == null) return;

        showProgressDialog("正在拒绝该申请...");

        mApplyTable.setState(2); //置为拒绝状态

        manager.saveInBackGround(mApplyTable, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                setResult(RESULT_OK);
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

    private void disagreeRevokeProblem() {
        if (mGroup == null) return;

        showProgressDialog("正在拒绝...");

        mRevokeTable.setState(2); //置为拒绝状态

        manager.saveInBackGround(mRevokeTable, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("已经拒绝该申请！");

                setResult(RESULT_OK);
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


    private void agreeApplyProblem() {
        if (mGroup == null) return;

        checkIfReachTheMaxNum();
    }

    private void checkIfReachTheMaxNum() {
        showProgressDialog("正在检验数据合法性...");

        final int max = mProblem.getTimes();
        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_CLASS, mClass);    //只查询我的班级
        query.addWhereEqualTo(ProblemGroup.S_PROBLEM, mProblem);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number >= max) {
                    showToast("该课题申请次数已达上限！已经拒绝本次申请！");
                    disAgreeWithoutToast();
                } else {
                    checkIfHaveTheProblem();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void checkIfHaveTheProblem() {
        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_PROBLEM, mProblem);
        query.addWhereEqualTo(ProblemGroup.S_GROUP, mGroup);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    showToast("该申请无效，已经拒绝！");
                    updateApplyState(2);    //拒绝
                    setResult(RESULT_OK);
                    dismissProgressDialog();
                    finish();
                } else {
                    createObjAndSave();
                    updateApplyState(1);
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                if (errorCode == 101) {
                    createObjAndSave();
                    updateApplyState(1);
                } else {
                    showToast(Constants.NET_ERROR_TOAST);

                    dismissProgressDialog();
                }
            }
        });
    }

    private void updateApplyState(int state) {
        mApplyTable.setState(state);
        manager.saveInBackGround(mApplyTable, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                Log.d(TAG, "状态修改成功！");
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void createObjAndSave() {
        ProblemGroup problemGorup = new ProblemGroup();
        problemGorup.setClass(mClass);
        problemGorup.setGroup(mGroup);
        problemGorup.setProblem(mProblem);
        problemGorup.setSchedule(0);

        manager.saveInBackGround(problemGorup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("操作成功！");

                setResult(RESULT_OK);
                finish();
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

    private void agreeRevokeProblem() {
        if (mGroup == null) return;
        checkInvalid();
    }

    private void checkInvalid() {
        showProgressDialog("正在检验数据合法性...");

        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_PROBLEM, mProblem);
        query.addWhereEqualTo(ProblemGroup.S_GROUP, mGroup);
        query.findInBackGroundDB(new FindCallBackDB() {

            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    showToast("无效的申请.");
                    updateRevokeState(2);

                    setResult(RESULT_OK);
                    dismissProgressDialog();
                    finish();

                } else {
                    deleteRecords(results.get(0));
                    updateRevokeState(1);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {

            }
        });
    }

    private void deleteRecords(Object obj) {
        ProblemGroup record = (ProblemGroup) obj;
        manager.deleteInBackGround(record, new DeleteCallBackDB() {
            @Override
            public void deleteDoneSuccessful() {
                showToast("操作成功.");
                setResult(RESULT_OK);
                dismissProgressDialog();
                finish();
            }

            @Override
            public void deleteDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void updateRevokeState(int state) {
        mRevokeTable.setState(state);
        manager.saveInBackGround(mRevokeTable, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                Log.d(TAG, "状态修改成功！");
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void showGroupDetailInfo() {
        if (mGroup == null) {
            return;
        }

        String serializeStr = mGroup.toString();
        Intent intent = new Intent(getApplicationContext(), ShowGroupInfo.class);
        intent.putExtra(ShowGroupInfo.EXTRA_TAG, serializeStr);
        startActivity(intent);
    }


    /**
     * 初始化UI
     */
    private void initilizeUI() {
        if (mApplyTable == null && mode == 0 || mRevokeTable == null && mode == 1 || mode == -1) {
            return;
        }

        String applyTime;
        String extraInfo;
        if (mode == 0) {
            mGroup = (Group) mApplyTable.getGroup();
            mProblem = (Problem) mApplyTable.getProblem();
            mClass = (ClassRoom) mApplyTable.getOfClass();
            applyTime = getTimeFromDate(mApplyTable.getCreatedAt());
            extraInfo = mApplyTable.getExtraInfo();
        } else {
            mGroup = (Group) mRevokeTable.getGroup();
            mProblem = (Problem) mRevokeTable.getProblem();
            mClass = (ClassRoom) mRevokeTable.getOfClass();
            applyTime = getTimeFromDate(mRevokeTable.getCreatedAt());
            extraInfo = mRevokeTable.getExtraInfo();
        }

        String problemTitle = mProblem.getTitle();
        String groupName = mGroup.getName();

        mProblemTitleTV.setText(problemTitle);
        mGroupNameTV.setText(groupName);
        mApplyTimeTV.setText(applyTime);
        mExtraInfoTV.setText(extraInfo);

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

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowApplyInfoDetail.this);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();

    }
}
