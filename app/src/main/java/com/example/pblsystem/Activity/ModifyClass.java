package com.example.pblsystem.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;

import java.util.ArrayList;
import java.util.List;

public class ModifyClass extends AppCompatActivity {

    //选择班级下拉框组件
    private Spinner mSelectClassSpinner;
    //已经创建的班级
    private List<ClassTeacher> mAllClassList = new ArrayList<>();
    //下拉框数据源
    private List<String> mSpinnerData;

    private ProgressDialog mProgressBarDialog;

    //选择的下拉框的下标
    private int mChooseSpinnerIndex = 0;

    private Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_class);
        bindView();
        initilizeProgressDialog();
        getClassFromNet();
    }

    private void bindView() {
        mSelectClassSpinner = (Spinner) findViewById(R.id.select_class_spinner);


        submit= (Button) findViewById(R.id.class_modify);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyClass();
            }
        });
        submit.setEnabled(false);
    }
    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    /**
     * 初始化选择课堂下拉框
     */
    private void initlizeClassSpinner() {
        //初始化Spinner数据源
        mSpinnerData = new ArrayList<>();
        mSpinnerData.add("课堂");
        for (ClassTeacher classObj: mAllClassList) {
            ClassRoom classRoom = (ClassRoom) classObj.getTargetClass();
            AVUser teacher = (AVUser) classObj.getTargetTeacher();
            mSpinnerData.add(classRoom.getMyClassName() + "-----" + teacher.getString(MyUser.S_NAME));
        }
        //构建适配器，绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mSpinnerData);
        //设置Spinner样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectClassSpinner.setAdapter(adapter);
        mSelectClassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //设置选择的下拉框的下标
                mChooseSpinnerIndex = position;
                if(mChooseSpinnerIndex>0)
                    submit.setEnabled(true);
                else submit.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



    /**
     * 从云端获取所有的班级
     */
    private void getClassFromNet() {
        //加载数据之前，弹出进度对话框
        mProgressBarDialog.show();
        /*查询ClassTeacher表，标志位为0的课堂*/
        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.addWhereEqualTo(ClassTeacher.S_AUTHROITY, 0);
        query.includePointer(ClassTeacher.S_TEACHER);
        query.includePointer(ClassTeacher.S_CLASS);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                //获取读到的数据
                for (Object obj: results) {
                    ClassTeacher classTeacher = (ClassTeacher) obj;
                    mAllClassList.add(classTeacher);
                }
                //更新Spinner
                initlizeClassSpinner();
                //设置ProgressDialog不可见
                mProgressBarDialog.dismiss();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                //设置ProgressDialog不可见
                mProgressBarDialog.dismiss();
            }
        });

    }


    private void modifyClass(){
//        Log.i("Position",String.valueOf(mChooseSpinnerIndex));
        ClassRoom room= (ClassRoom) mAllClassList.get(mChooseSpinnerIndex-1).getTargetClass();
        AVUser user=AVUser.getCurrentUser();
        user.put("class",room);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Toast.makeText(ModifyClass.this,"切换成功！",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(ModifyClass.this,"切换失败！",Toast.LENGTH_LONG).show();
                }
            }
        }


        );
        Log.i("Class",room.getMyClassName());
    }

}
