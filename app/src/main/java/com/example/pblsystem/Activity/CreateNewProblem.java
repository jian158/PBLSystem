package com.example.pblsystem.Activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class CreateNewProblem extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "CreateNewProblem";
    public static final String EXTRA_TAG = "problem";
    //Toast静态常量
    private static Toast toast;

    private EditText mProblemTitleTV, mProblemIntroductionTV;
    private Spinner mSetTimesSpinner, mSetDifficutySpinner;
    private Button mCreateNewProblemBtn;
    private ImageView mSetSpeechTimeImageView;
    private TextView selectTime;

    private String[] mTimesSpinnerResourceData = new String[] {"1", "2", "3", "4", "5", "不限制", "自定义"};
    private String[] mDifficutySpinnerResourceData = new String[] {"1星", "2星", "3星", "4星", "5星"};

    private int mTimes = 1;
    private int mDiffficuty = 1;

    private String mProblemTitle, mProblemIntroduction;
    private Date mSpeechDate = null;

    private Problem mProblem = null;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();

    private Drawable mSavedDrawableBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_problem);
        try {
            initilizeProgressDialog();
            bindView();
            setSpinner();
            getIntentData();
            setClickListenerForBtn();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 为按钮添加事件
     */
    private void setClickListenerForBtn() {
        mCreateNewProblemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProblem == null) {
                    createNewProblem();
                } else {
                    updateProblem();
                }
            }
        });

        mSetSpeechTimeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDataPicker();
            }
        });
    }

    private void updateProblem() {
        getDate();  //获取输入框中的数据
        if (checkData()) {
            if (mProblem.getTitle().equals(mProblemTitle)) {
                update();
            } else {
                toCheckTitle();
            }
        }
    }

    private void update() {
        showProgressDialog("正在更新课题...");

        Problem newProblem = mProblem;
        newProblem.setTitle(mProblemTitle);
        newProblem.setIntroduction(mProblemIntroduction);
        newProblem.setSpeakTime(mSpeechDate);
        newProblem.setTimes(mTimes);
        newProblem.setDifficutity(mDiffficuty);

        manager.saveInBackGround(newProblem, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("更新课题成功!");
                setResult(RESULT_OK);   //通知更新页面
                dismissProgressDialog();
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    /**
     * 显示日期选择器
     */
    private void showDataPicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                getSpeechDate(year, monthOfYear, dayOfMonth);
            }
        }, mYear, mMonth, mDay);
        datePicker.show();
    }

    private void getSpeechDate(int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth);
        mSpeechDate = c.getTime();
        selectTime.setText(getTimeFromDate(mSpeechDate));
    }

    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }

    /**
     * 新建课题
     */
    private void createNewProblem() {
        if (LoginActivity.sSelectedClass == null) {
            showToast("由于你尚未创建或者代理课堂，不能新建课题.");
            return;
        }

        getDate();
        if (checkData()) {
            checkTitle();
        }
    }

    private void toCheckTitle() {
        showProgressDialog("正在检验数据...");

        ClassRoom myClass = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(Problem.CLASS_NAME);
        query.addWhereEqualTo(Problem.S_CLASS, myClass);
        query.addWhereEqualTo(Problem.S_TITLE, mProblemTitle);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    showToast("你已经创建过同名的课题，请修改课题名称！");
                    dismissProgressDialog();
                } else {
                    update();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 检验课题题目是否合法
     */
    private void checkTitle() {
        Log.d("tag", "check title1");
        mSavedDrawableBtn = mCreateNewProblemBtn.getBackground();
        disableBtn();
        showProgressDialog("正在检验数据合法性...");

        ClassRoom myClass = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(Problem.CLASS_NAME);
        query.addWhereEqualTo(Problem.S_CLASS, myClass);
        query.addWhereEqualTo(Problem.S_TITLE, mProblemTitle);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    showToast("你已经创建过同名的课题，请修改课题名称！");
                    dismissProgressDialog();
                    enableBtn();
                } else {
                    createOneProblemObjAndSave();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                if (errorCode != 101) {
                    showToast("创建课题失败！请检查一下网络吧！");
                    dismissProgressDialog();
                    enableBtn();
                } else {
                    createOneProblemObjAndSave();
                }
            }
        });

    }

    private void enableBtn() {
        mCreateNewProblemBtn.setClickable(true);
        mCreateNewProblemBtn.setBackground(mSavedDrawableBtn);
    }

    private void disableBtn() {
        mCreateNewProblemBtn.setClickable(false);
        mCreateNewProblemBtn.setBackgroundColor(Color.GRAY);
    }


    /**
     * 新建一个problem
     */
    private void createOneProblemObjAndSave() {
        showProgressDialog("正在新建课题，请稍后...");

        final Problem newProblem = new Problem();
        newProblem.setOfClass(LoginActivity.sSelectedClass.getTargetClass());
        newProblem.setTitle(mProblemTitle);
        newProblem.setIntroduction(mProblemIntroduction);
        newProblem.setSpeakTime(mSpeechDate);
        newProblem.setTimes(mTimes);
        newProblem.setDifficutity(mDiffficuty);

        manager.saveInBackGround(newProblem, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("新建课题成功.");
                /*将新建的课题同步到课题库*/
                addProblemToLibrary(newProblem);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
                enableBtn();
            }
        });
    }

    private void addProblemToLibrary(Problem newProblem) {
        ProblemLibrary library = new ProblemLibrary();
        library.setTitle(newProblem.getTitle());
        library.setIntroduction(newProblem.getIntroduction());
        library.setDifficutity(newProblem.getDifficutity());
        library.setTimes(newProblem.getTimes());
        library.setSpeakTime(newProblem.getSpeakTime());
        /*后台保存数据*/
        manager.saveInBackGround(library);

    }


    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }


    /**
     * 获取输入框数据
     */
    private void getDate() {
        mProblemTitle = mProblemTitleTV.getText().toString();
        mProblemIntroduction = mProblemIntroductionTV.getText().toString();
    }

    /**
     * 检验数据的合法性
     * @return
     */
    private boolean checkData() {
        if (TextUtils.isEmpty(mProblemTitle)) {
            showToast("请输入课题标题");
            return false;
        };

        if (TextUtils.isEmpty(mProblemIntroduction)) {
            showToast("请输入课题简介");
            return false;
        }

        if (mSpeechDate == null) {
            popMessageDialog("你还没有设置演讲时间，是否现在去设置", "不用了", "去设置");
            return false;
        }

        return true;
    }

    private void setSpinner() {
        //构建适配器，绑定数据源
        ArrayAdapter<String> adapterForTimesSpinner = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mTimesSpinnerResourceData);
        //设置Spinner样式
        adapterForTimesSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSetTimesSpinner.setAdapter(adapterForTimesSpinner);
        mSetTimesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 4) {
                    mTimes = position + 1;
                } else if(position == 5) {
                    mTimes = -1;    //无限制
                } else if (position == 6) {
                    popInputDialog("自定义数字", "取消", "确定");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> adapterForDifficutySpinner = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mDifficutySpinnerResourceData);
        //设置Spinner样式
        adapterForDifficutySpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSetDifficutySpinner.setAdapter(adapterForDifficutySpinner);
        mSetDifficutySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDiffficuty = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mCreateNewProblemBtn = (Button) findViewById(R.id.create_new_problem_btn);
        mProblemIntroductionTV = (EditText) findViewById(R.id.problem_introduction_et);
        mProblemTitleTV = (EditText) findViewById(R.id.problem_title_et);
        mSetTimesSpinner = (Spinner) findViewById(R.id.apply_max_times_spinner);
        mSetDifficutySpinner = (Spinner) findViewById(R.id.problem_difficuty_spinner);
        mSetSpeechTimeImageView = (ImageView) findViewById(R.id.set_speech_time_image_view);
        selectTime = (TextView) findViewById(R.id.select_time);
        selectTime.setText(""); // 初始化空

    }


    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String serializeObj = intent.getStringExtra(EXTRA_TAG);
        if (serializeObj == null) { //新建课题

        } else {//修改课题
            mProblem = (Problem) AVObject.parseAVObject(serializeObj);
            initializeUI();
        }

    }

    private void initializeUI() {
        mProblemTitleTV.setText(mProblem.getTitle());
        mProblemIntroductionTV.setText(mProblem.getIntroduction());
        mCreateNewProblemBtn.setText("保存修改");

        mSetDifficutySpinner.setSelection(mProblem.getDifficutity() - 1);

        int times = mProblem.getTimes();
        if (times <= 5 && times > 0) {
            mSetTimesSpinner.setSelection(times - 1);
        } else if (times == -1) {
            mTimes = -1;
        } else {
            mTimes = times;
        }

        mSpeechDate = mProblem.getSpeakTime();
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(CreateNewProblem.this);
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


    /**
     * 弹出对话框
     */
    public void popMessageDialog(String msg, String negativeMsg, String positiveMsg) {
        //标题
        TextView titleTextView;
        //内容
        TextView contentTextView;
        //按钮
        TextView cancelBtn, confrimBtn;
        //xx按钮
        ImageView dismissBtn;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(this);
        //获取对话框view
        View view = inflater.inflate(R.layout.dialog_view, null, false);
        //绑定组件
        titleTextView = (TextView) view.findViewById(R.id.title);
        contentTextView = (TextView) view.findViewById(R.id.message);
        confrimBtn = (TextView) view.findViewById(R.id.confirm);
        cancelBtn = (TextView) view.findViewById(R.id.cancel);
        dismissBtn = (ImageView) view.findViewById(R.id.dismiss);
        //初始化组件
        titleTextView.setText("提示");
        contentTextView.setText(msg);
        confrimBtn.setText(positiveMsg);
        cancelBtn.setText(negativeMsg);
        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();;
        //为按钮设置监听事件
        confrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDataPicker();
                realDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whenNotSetSpeechDate();
                realDialog.dismiss();
            }
        });
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁对话框
                realDialog.dismiss();
            }
        });
    }

    /**
     * 弹出输入对话框
     * @param title
     * @param negativeMsg
     * @param positiveMsg
     */
    public void popInputDialog(String title, String negativeMsg, String positiveMsg) {
        //标题
        TextView titleTextView;
        //内容
        final EditText inputEditText;
        //按钮
        Button confrimBtn;
        //xx按钮
        ImageView dismissBtn;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(this);
        //获取对话框view
        View view = inflater.inflate(R.layout.dialog_input_view, null, false);
        //绑定组件
        titleTextView = (TextView) view.findViewById(R.id.title);
        inputEditText = (EditText) view.findViewById(R.id.message);
        confrimBtn = (Button) view.findViewById(R.id.confirm);
        dismissBtn = (ImageView) view.findViewById(R.id.dismiss);
        //初始化组件
        titleTextView.setText(title);
        confrimBtn.setText(positiveMsg);
        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();;
        //为按钮设置监听事件
        confrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputMsg = inputEditText.getText().toString();
                if (TextUtils.isEmpty(inputMsg)) {
                    showToast("请输入自定义数字.");
                } else if(!isNumeric(inputMsg)){
                    showToast("你输入的数字不合法");
                } else {
                    mTimes = Integer.parseInt(inputMsg);
                    showToast("自定义成功.");
                    realDialog.dismiss();
                }
            }
        });

        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetTimesSpinner.setSelection(0);
                //销毁对话框
                realDialog.dismiss();
            }
        });
    }




    private void whenNotSetSpeechDate() {
        mSpeechDate = Calendar.getInstance().getTime(); //默认时间
        checkTitle();
    }

    /**
     * 判断一个字符串是否为纯数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }
}
