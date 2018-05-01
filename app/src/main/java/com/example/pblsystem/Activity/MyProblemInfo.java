package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.ProblemRevokeTable;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.DialogCancel;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.InputDialogConfirm;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.PopWindow.PopWindow;
import com.example.pblsystem.PopWindow.PopWindowForMySpeech;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MyProblemInfo extends AppCompatActivity {
    /*常量，调试的Tag值*/
    private static final String TAG = "MyProblemInfo";
    /*Toast静态常量*/
    private static Toast toast;

    public static final String PROBLEM_EXTRA_TAG = "prolem_xtra";

    public TextView mProblemTitleTV, mProblemIntroductionTV, mSpeakerTV,
            mProblemDifficutyTV, mProblemTimeTV, mSetMyProblemProgressTV;
    private ImageView mPopWindow;

    private Problem mProblem;
    private ProblemGroup mProblemGroup;

    private Switch setCanScore;


    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_problem_info);

        initilizeProgressDialog();
        bindView();
        getIntentData();
        setClickListennerForBtn();
    }

    private void setClickListennerForBtn() {
        mSetMyProblemProgressTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetMySpeechProgress.class);
                intent.putExtra(SetMySpeechProgress.EXTRA_TAG,
                        MyProblemInfo.this.getIntent().getStringExtra(PROBLEM_EXTRA_TAG));
                startActivity(intent);
            }
        });


        mPopWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopWindowForMySpeech window = new PopWindowForMySpeech(MyProblemInfo.this);
                window.showAtLocation(MyProblemInfo.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
            }
        });
    }


    private void getIntentData() {
        Intent intent = getIntent();
        String serializedProblemGroup = intent.getStringExtra(PROBLEM_EXTRA_TAG);
        try {
            mProblemGroup = (ProblemGroup) AVObject.parseAVObject(serializedProblemGroup);
            mProblem = (Problem) mProblemGroup.getProblem();
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
                updateProblemGroup(obj);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void updateProblemGroup(final AVObject problem) {
        manager.fetchIfNeededInBackGround(mProblemGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                getDataAndInitializeUi(problem);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                getDataAndInitializeUi(problem);
            }
        });
    }

    private void getDataAndInitializeUi(AVObject obj) {
        Problem problem = (Problem) obj;
        String title = problem.getTitle();
        String introduction = problem.getIntroduction();
        String speakerName = null;
        AVUser speaker = (AVUser) mProblemGroup.getsSpeaker();
        if (speaker == null) {
            speakerName = "无";
        } else {
            speakerName = speaker.getString(MyUser.S_NAME);
        }
        int difficuty = problem.getDifficutity();
        Date showTime = problem.getSpeakTime();
        String dateStr = getTimeFromDate(showTime);

        mProblemTitleTV.setText(title);
        mProblemIntroductionTV.setText(introduction);
        mProblemDifficutyTV.setText(String.valueOf(difficuty));
        mProblemTimeTV.setText(String.valueOf(dateStr));
        mSpeakerTV.setText(speakerName);

        int canScore = mProblemGroup.getInt("canScore");
        if (canScore == 0) {
            setCanScore.setChecked(false);
        } else {
            setCanScore.setChecked(true);
        }

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

    /**
     * 绑定组件
     */
    private void bindView() {
        mProblemTitleTV = (TextView) findViewById(R.id.problem_title_tv);
        mProblemIntroductionTV = (TextView) findViewById(R.id.group_name_tv);
        mProblemDifficutyTV = (TextView) findViewById(R.id.apply_extra_info_tv);
        mProblemTimeTV = (TextView) findViewById(R.id.apply_time_info_tv);
        mSetMyProblemProgressTV = (TextView) findViewById(R.id.set_my_speech_progress);
        mSpeakerTV = (TextView) findViewById(R.id.speaker_tv);
        mPopWindow = (ImageView) findViewById(R.id.pop_window);
        setCanScore = (Switch) findViewById(R.id.open_switch);
        setCanScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chageCanScoreState();
            }
        });

    }

    private void chageCanScoreState() {
        int canScore = mProblemGroup.getInt("canScore");
        if (canScore == 0) {
            setCanScore.setChecked(true);
            updateState(1);
        } else {
            setCanScore.setChecked(false);
            updateState(0);
        }
    }

    private void updateState(final int state) {
        DataBaseManager manager = DataBaseManager.getInstance();
        mProblemGroup.put("canScore", state);
        manager.saveInBackGround(mProblemGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                if (state == 1) {
                    showToast("已开启");
                } else {
                    showToast("已关闭");
                }
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                if (state == 1) {
                    showToast("开启失败");
                    setCanScore.setChecked(false);
                } else {
                    showToast("关闭失败");
                    setCanScore.setChecked(true);
                }
            }
        });
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(MyProblemInfo.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    /**
     * 撤销课题
     */
    public void revokeMyProblem() {
        /**
         * 1 判断是否是组长
         * 2 判断课题进度是否是0
         * 3 提交撤销申请
         */
        checkIfTheLeader();
    }

    private void checkIfTheLeader() {
        showProgressDialog("正在检验身份...");

        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_LEADER, AVUser.getCurrentUser());
        query.includePointer(Group.S_CLASS);   //顺带查询班级
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    showToast("你不是小组长，没有权限！");

                    dismissProgressDialog();
                } else {
                    Group group = (Group) results.get(0);
                    ClassRoom classRoom = (ClassRoom) group.getOfClass();

                    checkIfTheProblemNoProgress(group, classRoom);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    private void checkIfTheProblemNoProgress(final Group group, final ClassRoom classRoom) {
        manager.fetchInBackGround(mProblemGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ProblemGroup problemGroup = (ProblemGroup) obj;
                mProblemGroup = problemGroup;   //更新全局数据
                if (problemGroup.getSchedule() == 0) {
                    revokeDirectly();
                } else {
                    sendRevokeApply(group, classRoom);
                }
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    /**
     * 直接撤销
     */
    private void revokeDirectly() {
        manager.deleteInBackGround(mProblemGroup, new DeleteCallBackDB() {
            @Override
            public void deleteDoneSuccessful() {
                showToast("撤销成功！");
                setResult(RESULT_OK);
                finish();
                dismissProgressDialog();
            }

            @Override
            public void deleteDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    private void sendRevokeApply(Group group, ClassRoom classRoom) {
        final ProblemRevokeTable revokeRecord = new ProblemRevokeTable();
        revokeRecord.setOfClass(classRoom);
        revokeRecord.setGroup(group);
        revokeRecord.setProblem(mProblem);
        revokeRecord.setState(0);   //待处理
        PopDialog.popInputDialog("撤销原因", "提交", this, new InputDialogConfirm() {
            @Override
            public int confirm(String inputMsg) {
                if (TextUtils.isEmpty(inputMsg)) {
                    showToast("输入不可为空.");
                    return 1;
                }
                revokeRecord.setExtraInfo(inputMsg);
                submitRevokeRecord(revokeRecord);
                return 0;
            }
        }, new DialogCancel() {
            @Override
            public void cancel() {
                dismissProgressDialog();
            }
        });
    }

    private void submitRevokeRecord(ProblemRevokeTable revokeRecord) {
        showProgressDialog("正在提交撤销申请...");
        manager.saveInBackGround(revokeRecord, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("申请已经提交，请等待教师审核.");
                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }


    /**
     * 弹出对话框
     */
    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.setCancelable(false);
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

}

