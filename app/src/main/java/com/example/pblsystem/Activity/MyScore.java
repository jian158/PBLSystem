package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.ObjectValueFilter;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MyScore extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "MyScore";
    public static final String EXTRA_TAG = "problemGroup";

    private TextView finalScore, minScore, maxScore, prompt;
    private TextView seeAllEvalutions;

    private ProblemGroup mProblemGroup;
    private List<SpeechEvaluation> dataSave = new ArrayList<>();

    private ProgressDialog mProgressBarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_score);

        try {
            initilizeProgressDialog();
            getIntentData();
            bindView();
            getDataFromNet();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializedProblemGroup = intent.getStringExtra(EXTRA_TAG);
        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(serializedProblemGroup);
    }


    private void bindView() {
        finalScore = (TextView) findViewById(R.id.final_score);
        minScore = (TextView) findViewById(R.id.minScore);
        maxScore = (TextView) findViewById(R.id.maxScore);
        prompt = (TextView) findViewById(R.id.prompt_text);
        seeAllEvalutions = (TextView) findViewById(R.id.see_all_evalutions);
        seeAllEvalutions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AVUser user = AVUser.getCurrentUser();
                int identify = user.getInt(MyUser.S_AUTHORITY);
                if (identify == 0) {
                    getMyClass();
                }
                showAllEvalutions();
            }
        });

    }

    private void checkIfScoreEnoughProblems(final ClassRoom myClass, final Group myGroup) {
        DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
        query.addWhereEqualTo(SpeechEvaluation.S_OWNER, AVUser.getCurrentUser());   // 我参与评价的数据
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(final int scoredNumber) {
                DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
                query.addWhereEqualTo(ProblemGroup.S_CLASS, myClass);
                query.addNotWhereEqualTo(ProblemGroup.S_GROUP, myGroup);
                query.includePointer(ProblemGroup.S_PROBLEM);
                query.findInBackGroundDB(new FindCallBackDB() {
                    @Override
                    public void findDoneSuccessful(List results) {
                        int totalNumber = getTotalNumber(results);
                        if (scoredNumber >= totalNumber) {
                            showAllEvalutions();
                        } else {
                            PopDialog.popMessageDialog(MyScore.this, "系统检测到你尚未评价完所有的演讲，是否现在去评价？",
                                    "不了", "马上去", new ConfirmMessage() {
                                        @Override
                                        public void confirm() {
                                            Intent intent = new Intent(MyScore.this, HistorySpeech.class);
                                            startActivity(intent);
                                        }
                                    }, null);
                        }

                        dismissProgressDialog();
                    }

                    @Override
                    public void findDoneFailed(String exceptionMsg, int errorCode) {
                        Log.d("tag", exceptionMsg);

                        dismissProgressDialog();
                    }
                });
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    private int getTotalNumber(List results) {
        int number = 0;
        for (Object obj: results) {
            ProblemGroup problemGroup = (ProblemGroup) obj;
            Problem problem = (Problem) problemGroup.getProblem();
            if (checkIfTheToday(problem)) {
                number++;
            }
        }

        return number;
    }

    private boolean checkIfTheToday(Problem problem) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        Date date = problem.getSpeakTime();
        c.setTime(problem.getSpeakTime());

        int speakYear = c.get(Calendar.YEAR);
        int speakMonth = c.get(Calendar.MONTH) + 1;
        int speakDay = c.get(Calendar.DAY_OF_MONTH);

        Date currentTime = new Date();
        c.setTime(currentTime);

        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        int currrentDay = c.get(Calendar.DAY_OF_MONTH);

        if (currentYear > speakYear) {
            return true;
        } else if (currentYear == speakYear) {
            if (currentMonth > speakMonth) {
                return true;
            } else if (currentMonth == speakMonth) {
                if (currrentDay >= speakDay) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void getMyClass() {
        showProgressDialog("正在检验身份...");

        DataBaseManager manager = DataBaseManager.getInstance();
        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                getMyGroup(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void getMyGroup(final ClassRoom myClass) {
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    Group myGroup = (Group) results.get(0);
                    checkIfScoreEnoughProblems(myClass, myGroup);
                } else if (results.size() == 0) {
                    showToast("未查询到你所在的小组");
                    dismissProgressDialog();
                } else {
                    Log.d("tag", "出错了");
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

    private void showAllEvalutions() {
        Intent intent = new Intent(this, ShowAllEvalutions.class);
        String str = mProblemGroup.toString();
        intent.putExtra(ShowAllEvalutions.EXTRA_TAG, str);
        startActivity(intent);
    }

    /**
     * 从网络获取数据
     */
    private void getDataFromNet() {
        if (mProblemGroup == null) {
            return;
        }

        showProgressDialog("正在统计数据...");

        DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
        query.addWhereEqualTo(SpeechEvaluation.S_PROBLEM_GROUP, mProblemGroup);
        query.addWhereEqualTo(SpeechEvaluation.S_FLAG, 0);
        query.setLimit(500);    //限制每个课题最多有500个人评价
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                //将所有的数据保存为成员变量
                for (Object obj: results) {
                    SpeechEvaluation speechEvalution = (SpeechEvaluation) obj;
                    dataSave.add(speechEvalution);
                }

                initializeUI(); //根据查询到的数据初始化UI
                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void initializeUI() {
        int maxScoreValue = getMaxScore();
        int minScoreValue = getMinScore();
        int finalScoreValue = getAverageScore();
        int evalutionNumberValue = dataSave.size();


        maxScore.setText(String.valueOf(maxScoreValue));
        minScore.setText(String.valueOf(minScoreValue));
        finalScore.setText(String.valueOf(finalScoreValue));
        prompt.setText("共有" + evalutionNumberValue + "人参与评价");

    }

    private int getMaxScore() {
        int max = 0;

        for (int i = 0; i < dataSave.size(); i++) {
            int score = dataSave.get(i).getsScore();
            if (score > max) {
                max = score;
            }
        }

        return max;
    }

    private int getMinScore() {
        if (dataSave.size() == 0) {
            return 0;
        }

        int min = dataSave.get(0).getsScore();

        for (int i = 1; i < dataSave.size(); i++) {
            int score = dataSave.get(i).getsScore();
            if (score < min) {
                min = score;
            }
        }

        return min;

    }

    private int getAverageScore() {
        if (dataSave.size() == 0) {
            return 0;
        }

        int sum = 0;

        for (int i = 0; i < dataSave.size(); i++) {
            int score = dataSave.get(i).getsScore();
            sum += score;
        }

        return sum / dataSave.size();
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    public void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    public void dismissProgressDialog() {
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
}
