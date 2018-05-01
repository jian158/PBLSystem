package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.AsyncListUtil;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.ObjectValueFilter;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MemberWork;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;

import java.util.List;

public class SubmitMyWork extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "SubmitMyWork";
    public static final String EXTRA_TAG = "problem_group";

    private SeekBar mContributionSeekBar;
    private EditText mBriefDescriptionET;
    private TextView mContributionPercent;
    private RelativeLayout mPromptLayout;
    private Button mSubmitBtn;

    private ProblemGroup mProblemGroup;
    private MemberWork mMemberWork;

    private String description;
    private int percent;

    private ProgressDialog mProgressBarDialog;
    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_my_work);

        try {
            initilizeProgressDialog();
            getIntentData();
            bindView();
            checkIfShowTheTopTips();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkIfShowTheTopTips() {
        showProgressDialog("正在加载中...");

        DataBaseQuery query = new DataBaseQuery(MemberWork.CLASS_NAME);
        query.addWhereEqualTo(MemberWork.S_PROBLEM, mProblemGroup);
        query.addWhereEqualTo(MemberWork.S_OWNER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() > 0) {//已经提交过
                    mMemberWork = (MemberWork) results.get(0);
                    initializeUiWithResult(results.get(0));
                }

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                if (errorCode == 101) {
                    //类还没有创建
                    Log.d(TAG, "类还没有创建");
                } else {
                    Log.d(TAG, exceptionMsg);
                }

                dismissProgressDialog();
            }
        });
    }

    /**
     * 已经有成员工作，据此更新界面
     * @param obj
     */
    private void initializeUiWithResult(Object obj) {
        //显示顶部提示语
        mPromptLayout.setVisibility(View.VISIBLE);
        //更新UI
        MemberWork work = (MemberWork) obj;
        String description = work.getDesription();
        int proportion = work.getProportion();
        mContributionSeekBar.setProgress(proportion);
        percent = proportion;
        mContributionPercent.setText(proportion+ "%");
        mBriefDescriptionET.setText(description);
    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mContributionSeekBar = (SeekBar) findViewById(R.id.seek_proportion);
        mPromptLayout = (RelativeLayout) findViewById(R.id.prompt_layout);
        mBriefDescriptionET = (EditText) findViewById(R.id.brief_description);
        mContributionPercent = (TextView) findViewById(R.id.contribution_percent);
        mSubmitBtn = (Button) findViewById(R.id.submit);

        //重复提交的 小提示默认隐藏
        mPromptLayout.setVisibility(View.GONE);

        mContributionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //设置比重值
                percent = i;
                mContributionPercent.setText(i + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitMyWork();
            }
        });
    }

    /**
     * 提交成员分工
     */
    private void submitMyWork() {
        if (!checkInputValid()) {
            return;
        }

        if (mMemberWork == null) {//新建成员分工
            createNewWork();
        } else {//更新成员分工
            updateMyWork();
        }
    }


    /**
     * 检查输入是否合法
     */
    private boolean checkInputValid() {
        description = mBriefDescriptionET.getText().toString();
        if (percent == 0) {
            showToast("请设置贡献比重！");
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            showToast("请输入工作内容简要介绍！");
            return false;
        }

        return true;
    }

    /**
     * 新增成员工作
     */
    private void createNewWork() {
        showProgressDialog("数据提交中...");

        MemberWork myWork = new MemberWork();
        myWork.setOwner(AVUser.getCurrentUser());
        myWork.setDescription(description);
        myWork.setProportion(percent);
        myWork.setProblemGroup(mProblemGroup);
        manager.saveInBackGround(myWork, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("提交成功！");
                checkIfUpdateProgressOfProblem();
                dismissProgressDialog();
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 检查是否需要更新课题研究的进度
     */
    private void checkIfUpdateProgressOfProblem() {
        //当所有人都提交了成员分工后，进度更新
        DataBaseQuery query = new DataBaseQuery(MemberWork.CLASS_NAME);
        query.addWhereEqualTo(MemberWork.S_PROBLEM, mProblemGroup);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                Group myGroup = (Group) mProblemGroup.getGroup();
                synchronizeGroup(myGroup, number);
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    /**
     * 更新group
     */
    private void synchronizeGroup(Group myGroup, final int number) {
        manager.fetchInBackGround(myGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                int memberNum = ((Group)obj).getNum();
                if (memberNum == number) {
                    //更新课题研究进度
                    updateProgress();
                }
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    /**
     * 更新课题进度
     */
    private void updateProgress() {
        Log.d(TAG, "进度更新");
        int currentSchedule = mProblemGroup.getSchedule();
        if (currentSchedule != 0) { //出现了不知名的错误
            return;
        }

        currentSchedule++;
        mProblemGroup.setSchedule(currentSchedule);
        manager.saveInBackGround(mProblemGroup);
    }

    /**
     * 更新成员工作
     */
    private void updateMyWork() {
        showProgressDialog("数据提交中...");

        mMemberWork.setDescription(description);
        mMemberWork.setProportion(percent);
        manager.saveInBackGround(mMemberWork, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("提交成功!");
                dismissProgressDialog();
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(SubmitMyWork.this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.setCancelable(false);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializedProblemGroup = intent.getStringExtra(EXTRA_TAG);
        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(serializedProblemGroup);
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
        dismissProgressDialog();    //销毁对话框，防止窗体泄露
    }
}
