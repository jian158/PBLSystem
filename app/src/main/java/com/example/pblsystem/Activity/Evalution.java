package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.ObjectValueFilter;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.DescriptionScore;
import com.example.pblsystem.Class.EvaluationStandard;
import com.example.pblsystem.Class.EvalutionDescription;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CancelMessage;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;
import com.example.pblsystem.Utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Evalution extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "Evalution";
    public static final String EXTRA_TAG = "group_problem";

    private ProgressDialog mProgressBarDialog;
    private DataBaseManager manager = DataBaseManager.getInstance();
    private EvaluationStandard mEvalutionStandard;
    private List<EvalutionDescription> details = new ArrayList<>();
    private List<DescriptionScore> evalutionResultDetail = new ArrayList<>();
    private ProblemGroup mProblemGroup;
    private List<SeekBar> seekBars = new ArrayList<>();

    private EditText extraCommnet;
    private TextView prompt;

    private LinearLayout mainLayout;

    private SpeechEvaluation mPreEvalution;

    boolean checkingIfHaveScored = true;    // “检测是否已经评价过课题操作” 是否完成


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evalution);
        try {
            initilizeProgressDialog();
            getIntentData();
            bindView();
            checkIfCanScore();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkIfCanScore() {
        if (mProblemGroup == null) return;
        // 获取最新的数据
        manager.fetchInBackGround(mProblemGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                // 判断是否开放打分
                int canScore = mProblemGroup.getInt("canScore");
                if (canScore == 0) {    /*不能打分*/
                    showToast("该演讲尚未开放打分，请耐心等待");
                } else {    /*能打分*/
                    createEvalutionTable();
                }
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                showToast("出现错误了！");
            }
        });
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(Evalution.this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    /**
     * 获取传递的数据
     */
    private void getIntentData() throws Exception {
        String str = getIntent().getStringExtra(EXTRA_TAG);
        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(str);
    }

    /**
     * 绑定布局
     */
    private void bindView() {
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        prompt = (TextView) findViewById(R.id.prompt);
        prompt.setVisibility(View.GONE);
    }


    /**
     * 生成评价表单
     */
    private void createEvalutionTable() {
        if (isTeacher()) {
            ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
            if (classRoom == null) return;

            fiterEvalutionDescription(classRoom);
        } else {
            getMyClass();
        }
    }

    private boolean isTeacher() {
        if (LoginActivity.sSelectedClass == null) {
            return false;
        }

        return true;
    }

    /**
     * 获取我所在的课堂
     */
    private void getMyClass() {
        showProgressDialog("查询课堂中...");

        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                fiterEvalutionDescription(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 根据我所在的班级搜索评价细则
     * @param myClass
     */
    private void fiterEvalutionDescription(ClassRoom myClass) {
        showProgressDialog("正在检验数据...");

        DataBaseQuery query = new DataBaseQuery(EvaluationStandard.CLASS_NAME);
        query.addWhereEqualTo(EvaluationStandard.S_CLASS, myClass);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    mEvalutionStandard = (EvaluationStandard) results.get(0);
                    getEvalutionDetails();
                } else if (results.size() == 0 ) {
                    showToast("你所在的课堂尚未配置评价表，暂时无法评分！");

                    dismissProgressDialog();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void getEvalutionDetails() {
        showProgressDialog("正在生成评价表...");

        AVRelation relation = mEvalutionStandard.getRelation(EvaluationStandard.S_DESCRIPTION);
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    //获取评价标准
                    for (AVObject obj: list) {
                        details.add((EvalutionDescription) obj);

                        //用于生成评价的单项打分
                        DescriptionScore  descriptionScore = new DescriptionScore();
                        descriptionScore.setDescription((EvalutionDescription) obj);
                        evalutionResultDetail.add(descriptionScore);
                    }

                    createTable();
                    checkIfHaveScored();
                } else {
                    Log.d(TAG, e.getMessage());
                    dismissProgressDialog();
                }
            }
        });
    }


    private void createTable() {
        for (int i = 0; i < details.size(); i++) {
            createOneDetail(i);
        }

        createBottomUI();
    }


    private void createOneDetail(int position) {
        final EvalutionDescription description = details.get(position);
        String descriptionTitle = description.getDescriptionTitle();
        String descriptionDetails = description.getDescriptionDetails();

        RelativeLayout outerLayout = new RelativeLayout(this);
       // outerLayout.setBackgroundColor(Color.parseColor("#a5a0a0"));
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        tvLayoutParams.setMargins(0, Util.dp2Px(15, this), 0, 0);
        outerLayout.setLayoutParams(tvLayoutParams);


        RelativeLayout.LayoutParams titleParams =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        TextView title = new TextView(this);
        title.setText(descriptionTitle);
        title.setTextColor(Color.parseColor("#401764"));
        //设置字体加粗
        TextPaint paint = title.getPaint();
        paint.setFakeBoldText(true);
        title.setTextSize(Util.sp2px(6, this));
        title.setId(position + 1);
        title.setLayoutParams(titleParams);

        //分数显示
        RelativeLayout.LayoutParams scoreParams =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        scoreParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        scoreParams.setMargins(0, Util.dp2Px(4, this), Util.dp2Px(5, this), 0);
        final TextView score = new TextView(this);
        score.setText("当前分数:0");
        score.setTextColor(Color.parseColor("#262b92"));
        score.setTextSize(Util.sp2px(6, this));
        score.setLayoutParams(scoreParams);

        //细则说明
        final TextView descriptionTV = new TextView(this);
        RelativeLayout.LayoutParams descriptionLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        descriptionLayoutParams.addRule(RelativeLayout.BELOW, title.getId());
        descriptionLayoutParams.setMargins(0, Util.dp2Px(4, this), 0, 0);
        descriptionTV.setId(position + 100);
        descriptionTV.setText(descriptionDetails);
        descriptionTV.setTextColor(Color.parseColor("#979191"));
        descriptionTV.setTextSize(Util.sp2px(6, this));
        descriptionTV.setLayoutParams(descriptionLayoutParams);
        descriptionTV.setVisibility(View.GONE); //默认不可见

        //添加右侧下拉按钮
        final ImageView spreadView = new ImageView(this);
        spreadView.setImageResource(R.drawable.pull);
        spreadView.setPadding(Util.dp2Px(10, this), 0, Util.dp2Px(10, this), 0);
        RelativeLayout.LayoutParams spreadViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        spreadViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, title.getId());
        spreadViewLayoutParams.setMargins(Util.dp2Px(9, this), 0, 0, 0);
        Log.d(TAG, title.getId() + "id");
        spreadView.setLayoutParams(spreadViewLayoutParams);
        spreadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (descriptionTV.getVisibility() == View.GONE) {
                    descriptionTV.setVisibility(View.VISIBLE);
                    spreadView.setImageResource(R.drawable.close);
                } else {
                    descriptionTV.setVisibility(View.GONE);
                    spreadView.setImageResource(R.drawable.pull);
                }
            }
        });


        //打分控件
        RelativeLayout.LayoutParams seekBarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        seekBarLayoutParams.addRule(RelativeLayout.BELOW, descriptionTV.getId());
        seekBarLayoutParams.setMargins(0, Util.dp2Px(6, this), 0, 0);
        SeekBar scoreSeekBar = new SeekBar(this);
        scoreSeekBar.setId(position + 200);
        scoreSeekBar.setLayoutParams(seekBarLayoutParams);
        scoreSeekBar.setMax(100);
        scoreSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                score.setText("当前分数:" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBars.add(scoreSeekBar); //获取引用

        //添加一个分割线
        RelativeLayout.LayoutParams dividerParams =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                Util.dp2Px(1, this));
        dividerParams.addRule(RelativeLayout.BELOW, scoreSeekBar.getId());
        dividerParams.setMargins(0, Util.dp2Px(5, this), 0, 0);
        TextView divider = new TextView(this);
        divider.setBackgroundColor(Color.parseColor("#dddddd"));
        divider.setLayoutParams(dividerParams);


        //添加
        outerLayout.addView(title);
        outerLayout.addView(spreadView);
        outerLayout.addView(score);
        outerLayout.addView(descriptionTV);
        outerLayout.addView(scoreSeekBar);
        outerLayout.addView(divider);
        mainLayout.addView(outerLayout);

    }

    /**
     * 设置底部UI
     */
    private void createBottomUI() {
        //添加文字评价输入框
        extraCommnet = new EditText(this);
        LinearLayout.LayoutParams editLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        editLayoutParams.setMargins(0, Util.dp2Px(30, this), 0, 0);
        extraCommnet.setHint("请给点建议(选填)");
        extraCommnet.setTextSize(Util.sp2px(7, this));
        extraCommnet.setLayoutParams(editLayoutParams);

        //添加提交按钮
        Button btn = new Button(this);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        btn.setText("提交");
        btn.setBackgroundResource(R.drawable.button);
        btn.setLayoutParams(buttonParams);
        buttonParams.setMargins(0, Util.dp2Px(20, this), 0, 0);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitScore();
            }
        });

        mainLayout.addView(extraCommnet);
        mainLayout.addView(btn);

    }

    /**
     * 检验是否已经评价过
     */
    private void checkIfHaveScored() {
        showProgressDialog("检验身份中...");

        DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
        query.addWhereEqualTo(SpeechEvaluation.S_OWNER, AVUser.getCurrentUser());
        query.addWhereEqualTo(SpeechEvaluation.S_PROBLEM_GROUP, mProblemGroup);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() != 0) {
                    showTheTopTips();
                    setPreScore(results.get(0));
                } else {
                    checkingIfHaveScored = false;
                }

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void showTheTopTips() {
        prompt.setVisibility(View.VISIBLE);
    }

    private void setPreScore(Object obj) {
        mPreEvalution = (SpeechEvaluation) obj;
        getEvalutionDetailScore();
    }

    private void getEvalutionDetailScore() {
        if (mPreEvalution == null) return;

        DataBaseQuery query = new DataBaseQuery(DescriptionScore.CLASS_NAME);
        query.addWhereEqualTo(DescriptionScore.S_SPEECH_EVALUTION, mPreEvalution);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                evalutionResultDetail.clear();  //清空
                for (Object obj: results) {
                    evalutionResultDetail.add((DescriptionScore) obj);
                }

                match();

                checkingIfHaveScored = false;
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    /**
     * 匹配原则和历史打分
     */
    private void match() {
        List<DescriptionScore> list = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            int index = indexOfScoreDetails(i);
            if (index == -1) {
                DescriptionScore score = new DescriptionScore();
                score.setDescription(details.get(i));
                score.setSpeechEvalution(mPreEvalution);
                score.setScore(0);
                list.add(score);
            } else {
                list.add(evalutionResultDetail.get(index));
            }
        }

        evalutionResultDetail = list;   //匹配结束

        initialezeSeekBar();
    }

    private int indexOfScoreDetails(int position) {
        EvalutionDescription description = details.get(position);
        for (int i = 0; i < evalutionResultDetail.size(); i++) {
            if (evalutionResultDetail.get(i).getDescription().getObjectId().equals(description.getObjectId())) {
                return i;
            }
        }

        return -1;
    }

    private void initialezeSeekBar() {
        for (int i = 0; i < seekBars.size(); i++) {
            SeekBar seekbar = seekBars.get(i);
            int score;
            try {
                DescriptionScore descriptionScore = evalutionResultDetail.get(i);
                score = descriptionScore.getScore();
            } catch (Exception e) {
                score = 0;
            }
            seekbar.setProgress(score);
        }
    }


    /**
     * 打分
     */
    private void submitScore() {
        if (mProblemGroup == null) return;
        if (!checkingIfHaveScored) {
            checkIdentify();
        } else {
            showToast("系统正在加载你的上一次评分，请勿进行其它操作...");
        }
    }

    private void checkIdentify() {//检测是否是自己的课题
        showProgressDialog("正在检验身份...");
        //获取我的小组
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    Group group = (Group) results.get(0);
                    Group targetGroup = (Group) mProblemGroup.getGroup();
                    if (targetGroup == null) {
                        dismissProgressDialog();
                        return;
                    }

                    if (group.getObjectId().equals(targetGroup.getObjectId())) {//自己小组
                        showToast("不能评价自己的课题呦！");
                        dismissProgressDialog();
                    } else {
                        setEvalution();
                    }
                } else if (results.size() == 0) {//没有小组
                    setEvalution();
                } else {
                    dismissProgressDialog();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void setEvalution() {
        if (checkTheData()) {
            int finalScore = getFinalScore(); //未判断seekbar.size为0
            if (finalScore!=-1&&finalScore < 60) {
                PopDialog.popMessageDialog(Evalution.this, "系统检测到你给的分数过低，是否确认？", "取消", "确定",
                        new ConfirmMessage() {
                            @Override
                            public void confirm() {
                                uploadScore();
                            }
                        }, new CancelMessage() {
                            @Override
                            public void cancel() {
                                dismissProgressDialog();
                            }
                        });
            } else {
                uploadScore();
            }
        } else {
            dismissProgressDialog();
        }
    }

    private void uploadScore() {
        if (mPreEvalution == null) {
            createNewRecord();
        } else {
            updateRecord();
        }
    }

    private void updateRecord() {
        showProgressDialog("评价更新中...");
        if (TextUtils.isEmpty(extraCommnet.getText().toString())) {
            mPreEvalution.setCommentText("无");
        } else {
            mPreEvalution.setCommentText(extraCommnet.getText().toString());
        }
        int finalScore = getFinalScore();
        mPreEvalution.setScore(finalScore);
        manager.saveInBackGround(mPreEvalution, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("评价完成！");
                dismissProgressDialog();

                //更新评价项细则
                updateDetailScore();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void updateDetailScore() {
        for (int i = 0; i < evalutionResultDetail.size(); i++) {
            manager.saveInBackGround(evalutionResultDetail.get(i));
        }
    }

    private void createNewRecord() {
        final SpeechEvaluation evalution = new SpeechEvaluation();
        if (TextUtils.isEmpty(extraCommnet.getText().toString())) {
            evalution.setCommentText("无");
        } else {
            evalution.setCommentText(extraCommnet.getText().toString());
        }
        evalution.setScore(getFinalScore());
        evalution.setOwner(AVUser.getCurrentUser());
        evalution.setProblemGroup(mProblemGroup);
        if (isTeacher()) {
            evalution.setFlag(1);   //代表教师评价
        } else {
            evalution.setFlag(0);   //代表学生评价
        }

        showProgressDialog("评价提交中...");
        manager.saveInBackGround(evalution, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("评价完成！");
                mPreEvalution = evalution;  //显示已评分
                showTheTopTips();
                dismissProgressDialog();

                // 添加评价细节至数据库
                addDetailsToDataBase(evalution);
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void addDetailsToDataBase(SpeechEvaluation evalution) {
        for (int i = 0; i < evalutionResultDetail.size(); i++) {
            evalutionResultDetail.get(i).setSpeechEvalution(evalution);
            manager.saveInBackGround(evalutionResultDetail.get(i), new SaveCallBackDB() {
                @Override
                public void saveDoneSuccessful() {
                    Log.d("tag", "保存成功");
                }

                @Override
                public void saveDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", "异常" + exceptionMsg);
                }
            });
        }
    }

    private int getFinalScore() {
        if(seekBars.size()==0)
            return -1;
        int finalScore = 0;
        for (int i = 0; i < seekBars.size(); i++) {
            finalScore += seekBars.get(i).getProgress();

            // 添加评价细则分数
            evalutionResultDetail.get(i).setScore(seekBars.get(i).getProgress());
        }
        return finalScore / seekBars.size();
    }

    /**
     * 检验输入是否完整
     */
    private boolean checkTheData() {
        for (int i = 0; i < seekBars.size(); i++) {
            if (seekBars.get(i).getProgress() == 0) {
                showToast("第" + (i+1) + "项还没有给分呢！");
                return false;
            }
        }

        return true;
    }


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
        dismissProgressDialog();    //销毁对话框，防止窗体泄露
    }
}
