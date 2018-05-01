package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SetMyGroupInfo extends AppCompatActivity {
    public static final String MY_GROUP_EXTRA_INFO = "my_group_tag";
    private static Toast toast;
    public static final String LOG_TAG = "SetMyGroupInfo";
    private Group myGroup;  //我的小组
    private EditText groupNameInputEditText;
    private EditText groupPasswordInputEditText;
    private Spinner selectGroupLeaderSpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private RadioGroup selectGroupModeRadioGroup;
    private Button saveChangesBtn;

    private List<AVObject> groupMemberList;
    private AVObject groupOriginalLeader;
    private List<String> groupMemberSpinnerDataSource;
    private DataBaseManager manager = DataBaseManager.getInstance();

    private String originalGroupName, newGroupName;
    private int originalGroupLeaderIndexOfSpinner, newGroupLeaderIndexOfSpinner;
    private int originalMode, newMode;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_my_group_info);

        bindView();
        initilizeProgressDialog();
        bindDataSourceForSpinner();
        setCheckListenerForRadioGroup();
        setClickListennerForBtn();
        getExtraFromIntent();
        getOriginalDataFromNet();
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(SetMyGroupInfo.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    /**
     * 获取上一个avtivity传递过来的数据
     */
    private void getExtraFromIntent() {
        String serializedGroup = getIntent().getStringExtra(SetMyGroupInfo.MY_GROUP_EXTRA_INFO);
        try {
            myGroup = (Group) AVObject.parseAVObject(serializedGroup);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取小组原始信息
     */
    private void getOriginalDataFromNet() {
        if (myGroup == null) {
            return;
        }

        updataLocalGroupInfoFromNet();
    }

    /**
     * 同步云端数据
     */
    private void updataLocalGroupInfoFromNet() {
        showProgressDialog("数据加载中...");
        manager.fetchIfNeededInBackGround(myGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                myGroup = (Group) obj;

                getGroupMemberDataFromNet();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(LOG_TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 获取我的小组成员信息
     */
    private void getGroupMemberDataFromNet() {
        AVRelation<AVObject> relation = myGroup.getRelation(Group.S_MEMBER);
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    groupMemberList = list;

                    getGroupOriginalLeaderDataFromNet();
                } else {
                    Log.d(LOG_TAG, e.getMessage());
                    showToast(Constants.NET_ERROR_TOAST);
                    dismissProgressDialog();
                }
            }
        });
    }

    /**
     * 获得小组原来小组长的信息
     */
    private void getGroupOriginalLeaderDataFromNet() {
        manager.fetchInBackGround(myGroup, Group.S_LEADER, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                groupOriginalLeader = ((Group) obj).getLeader();

                inilizedUi();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(LOG_TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    /**
     * 根据获得的信息更新UI
     */
    private void inilizedUi() {
        if (myGroup == null) return;

        inilizedEditText();
        inilizedSpinner();
        inilizedRadioGroup();

        dismissProgressDialog();
    }

    private void inilizedSpinner() {
        updateSpinnerSourceData();
        setOriginalLeader();
    }

    private void setOriginalLeader() {
        int leaderIndexOfSpinner = 0;
        for (int i = 0; i < groupMemberList.size(); i++) {
            if (groupOriginalLeader.getObjectId().equals(groupMemberList.get(i).getObjectId())) {
                leaderIndexOfSpinner = i;
                originalGroupLeaderIndexOfSpinner = leaderIndexOfSpinner;
                Log.d(LOG_TAG, "小组长" + leaderIndexOfSpinner);
                break;
            }
        }
        selectGroupLeaderSpinner.setSelection(leaderIndexOfSpinner);
    }

    private void updateSpinnerSourceData() {
        for (int i = 0; i < groupMemberList.size(); i++) {
            groupMemberSpinnerDataSource.add(groupMemberList.get(i).getString(MyUser.S_NAME));
        }
        spinnerAdapter.notifyDataSetChanged();
    }

    private void inilizedRadioGroup() {
        int mode = myGroup.getFlag();
        originalMode = mode;
        int resId = -1;
        switch (mode) {
            case 0:
                resId = R.id.group_mode_open;
                break;
            case 1:
                resId = R.id.group_mode_hide;
                break;
            case 2:
                resId = R.id.group_mode_password;
                break;
        }
        selectGroupModeRadioGroup.check(resId);
    }

    private void inilizedEditText() {
        String groupName = myGroup.getName();
        originalGroupName = groupName;
        String password = myGroup.getPassword();
        groupNameInputEditText.setText(groupName);
        if (!password.equals("null")) {
            groupPasswordInputEditText.setText(password);
        }
    }

    /**
     * 为按钮添加事件监听
     */
    private void setClickListennerForBtn() {
        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
    }

    /**
     * 保存所做的修改
     */
    private void saveChanges() {
        showProgressDialog("拼命保存数据中...");
        getModifiedData();
        checkDataLegality();
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    /**
     * 检查数据合法性
     */
    private void checkDataLegality() {
        if (TextUtils.isEmpty(newGroupName)) {
            showToast("不要忘记输入小组名呦!");
            dismissProgressDialog();
            return;
        }
        if (!newGroupName.equals(originalGroupName)) {
            needToCheckTheNameLegality();
        } else {
            setNewToSavedGroupObj();
        }
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }


    private void needToCheckTheNameLegality() {
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_NAME, newGroupName);
        query.addWhereEqualTo(Group.S_CLASS, AVUser.getCurrentUser().get(MyUser.S_CLASS));
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {//已经有该名字的小组
                    showToast("客官，你的小组名已经被强占了，换个试试？");
                    dismissProgressDialog();
                } else {
                    setNewToSavedGroupObj();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(LOG_TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void setNewToSavedGroupObj() {
        myGroup.setName(newGroupName);
        myGroup.setFlag(newMode);
        if (newMode == 2) {
            if (checkPasswordLegility()) {
                Log.d(LOG_TAG, "密码" + groupPasswordInputEditText.getText().toString());
                myGroup.setPassword(groupPasswordInputEditText.getText().toString());
            } else {
                dismissProgressDialog();
                return;
            }

         }

        if (newGroupLeaderIndexOfSpinner != originalGroupLeaderIndexOfSpinner) {
            myGroup.setLeader(groupMemberList.get(newGroupLeaderIndexOfSpinner));
        }

        saveFinalGroupObj();

    }

    //保存数据
    private void saveFinalGroupObj() {
        manager.saveInBackGround(myGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("信息修改成功！");
                setResult(RESULT_OK);
                dismissProgressDialog();
                finish();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast("修改失败！" + Constants.NET_ERROR_TOAST);
                Log.d(LOG_TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private boolean checkPasswordLegility() {
        String password = groupPasswordInputEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            showToast("不要忘记输入密码呦！");
            return false;
        }

        return true;
    }

    private void getModifiedData() {
        newGroupName = groupNameInputEditText.getText().toString();
    }

    /**
     * 获取Spinner数据
     */
    private void bindDataSourceForSpinner() {
        groupMemberSpinnerDataSource = new ArrayList<>();

        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, groupMemberSpinnerDataSource);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectGroupLeaderSpinner.setAdapter(spinnerAdapter);
        selectGroupLeaderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newGroupLeaderIndexOfSpinner = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * 初始化组件
     */
    private void setCheckListenerForRadioGroup() {
        selectGroupModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int mode = -1;
                switch (checkedId) {
                    case R.id.group_mode_open:
                        mode = 0;
                        newMode = 0;
                        break;
                    case R.id.group_mode_hide:
                        mode = 1;
                        newMode = 1;
                        break;
                    case R.id.group_mode_password:
                        mode = 2;
                        newMode = 2;
                        break;
                }

                setPasswordEditTextShowOrHide(mode);
            }
        });
    }

    /**
     * 设置密码输入框隐藏或者出现
     */
    private void setPasswordEditTextShowOrHide(int mode) {
        if (mode != 2) {
            groupPasswordInputEditText.setVisibility(View.GONE);
        } else {
            groupPasswordInputEditText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 绑定xml中的组件
     */
    private void bindView() {
        groupNameInputEditText = (EditText) findViewById(R.id.group_name_edit_text);
        groupPasswordInputEditText = (EditText) findViewById(R.id.group_password_edit_text);
        selectGroupLeaderSpinner = (Spinner) findViewById(R.id.select_group_leader_spinner);
        selectGroupModeRadioGroup = (RadioGroup) findViewById(R.id.select_group_mode_radio_group);
        saveChangesBtn = (Button) findViewById(R.id.save_btn);
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
