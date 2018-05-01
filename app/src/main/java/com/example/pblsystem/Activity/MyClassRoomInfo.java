package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.avos.avoscloud.AVRole.className;

public class MyClassRoomInfo extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "MyClassRoomInfo";
    private TextView classNameTV;
    private RecyclerView classTeacher;
    private List<ClassTeacher> dataSource = new ArrayList<>();
    private MyAdapter adapter;

    private ClassRoom mClassRoom;

    private ProgressDialog mProgressBarDialog;

    private DataBaseManager manager = DataBaseManager.getInstance();

    //加入按钮
    private Button joinClass;
//    private Button exitClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class);
        initilizeProgressDialog();
        bindView();
        setRecycleView();
        getDateFromNet();

    }



    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }


    private void bindView() {
        classNameTV = (TextView) findViewById(R.id.class_name);
        classTeacher = (RecyclerView) findViewById(R.id.class_teacher);


        joinClass = (Button) findViewById(R.id.join_class);
        joinClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyClassRoomInfo.this,ModifyClass.class);
                startActivity(intent);
            }
        });

    }


    private void setRecycleView() {
        adapter = new MyAdapter();
        classTeacher.setLayoutManager(new LinearLayoutManager(this));
        classTeacher.setAdapter(adapter);
    }



    private void getDateFromNet() {
        getMyClass();
    }

    private void getMyClass() {
        showProgressDialog("正在读取数据...");

        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                mClassRoom = (ClassRoom) obj.get(MyUser.S_CLASS);
                getTheTeacher();

            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });

    }

    private void getTheTeacher() {

        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.includePointer(ClassTeacher.S_TEACHER);
        query.addWhereEqualTo(ClassTeacher.S_CLASS, mClassRoom);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    ClassTeacher classTeacher = (ClassTeacher) obj;
                    dataSource.add(classTeacher);
                }

                initializeUi();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void initializeUi() {
        if (mClassRoom == null) {
            dismissProgressDialog();
            return;
        }

        String className = mClassRoom.getMyClassName();
        classNameTV.setText(className);
        adapter.notifyDataSetChanged();
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView teacherName, ownType;
        private View savedView;
        public MyViewHolder(View itemView) {
            super(itemView);
            savedView = itemView;

            teacherName = (TextView) itemView.findViewById(R.id.teacher_name);
            ownType = (TextView) itemView.findViewById(R.id.type);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.class_teacher_item, parent, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ClassTeacher classTeacher = dataSource.get(position);
            AVUser teacher = (AVUser) classTeacher.getTargetTeacher();

            String teacherName = teacher.getString(MyUser.S_NAME);
            String type = "Null";
            int authority = classTeacher.getsAuthroity();
            if (authority == 0) {
                type = "创建者";
            } else {
                type = "代理者";
            }

            holder.teacherName.setText(teacherName);
            holder.ownType.setText(type);
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }
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


    @Override
    protected void onRestart() {
        Log.i("restart","resatrt");
        super.onRestart();
        dataSource.clear();
        getDateFromNet();
    }
}
