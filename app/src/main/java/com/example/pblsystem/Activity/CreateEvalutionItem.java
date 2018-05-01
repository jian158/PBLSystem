package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVRelation;
import com.example.pblsystem.Class.EvaluationStandard;
import com.example.pblsystem.Class.EvalutionDescription;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;

public class CreateEvalutionItem extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "CreateEvalutionItem";
    public static final String EXTRA_STNDARD = "Standard";

    public static final String EXTRA_TAG = "item";
    //Toast静态常量
    private static Toast toast;

    private ProgressDialog mProgressBarDialog;

    private TextView titleTV, descriptionTV, promptTV, scoreLabel;
    private Button submitBtn;
    private SeekBar score;

    private EvalutionDescription evalutionDescription;
    private EvaluationStandard standard;
    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_evalution_item);
        try {
            bindView();
            initilizeProgressDialog();
            getIntentData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindView() {
        titleTV = (TextView) findViewById(R.id.title);
        descriptionTV = (TextView) findViewById(R.id.detail);
        promptTV = (TextView) findViewById(R.id.prompt);

        submitBtn = (Button) findViewById(R.id.save);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (evalutionDescription == null) {
                    createNew();
                } else {
                    update();
                }
            }
        });

        scoreLabel = (TextView) findViewById(R.id.score_label);
        score = (SeekBar) findViewById(R.id.score);
        score.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                scoreLabel.setText("单项分值:" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void createNew() {
        showProgressDialog("数据提交中...");

        if (checkData()) {
            final EvalutionDescription newItem = new EvalutionDescription();
            newItem.setDescriptionTitle(titleTV.getText().toString());
            newItem.setDescriptionDetails(descriptionTV.getText().toString());
            newItem.setScore(score.getProgress());

            manager.saveInBackGround(newItem, new SaveCallBackDB() {
                @Override
                public void saveDoneSuccessful() {
                    addToStandard(newItem);
                }

                @Override
                public void saveDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d(TAG, exceptionMsg);

                    dismissProgressDialog();
                }
            });

        } else {
            dismissProgressDialog();
        }
    }

    private void addToStandard(EvalutionDescription newItem) {
        AVRelation relation = standard.getRelation(EvaluationStandard.S_DESCRIPTION);
        relation.add(newItem);
        manager.saveInBackGround(standard, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("新建成功!");
                setResult(RESULT_OK);

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void update() {
        showProgressDialog("数据更新中...");

        if (checkData()) {
            evalutionDescription.setDescriptionTitle(titleTV.getText().toString());
            evalutionDescription.setDescriptionDetails(descriptionTV.getText().toString());
            evalutionDescription.setScore(score.getProgress());

            manager.saveInBackGround(evalutionDescription, new SaveCallBackDB() {
                @Override
                public void saveDoneSuccessful() {
                    showToast("修改成功!");
                    setResult(RESULT_OK);

                    dismissProgressDialog();
                }

                @Override
                public void saveDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d(TAG, exceptionMsg);
                    dismissProgressDialog();
                }
            });
        } else {
            dismissProgressDialog();
        }
    }

    private boolean checkData() {
        String title = titleTV.getText().toString();
        String detail = descriptionTV.getText().toString();
        int progress = score.getProgress();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(detail)) {
            showToast("请先输入数据");
            return false;
        }

        if (progress == 0) {
            showToast("请先设置该项的分值");
            return false;
        }

        return true;
    }


    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String str = intent.getStringExtra(EXTRA_TAG);
        if (str != null) {
            evalutionDescription = (EvalutionDescription) AVObject.parseAVObject(str);
            initializeUI();
        }

        String standardStr = intent.getStringExtra(EXTRA_STNDARD);
        standard = (EvaluationStandard) AVObject.parseAVObject(standardStr);
    }

    private void initializeUI() {
        promptTV.setText("温馨提示:再次保存将会覆盖原有数据.");
        submitBtn.setText("修改");
        titleTV.setText(evalutionDescription.getDescriptionTitle());
        descriptionTV.setText(evalutionDescription.getDescriptionDetails());
        score.setProgress(evalutionDescription.getScore());
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("数据加载中...");
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
}
