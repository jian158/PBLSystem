package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MyClasses extends AppCompatActivity {

    //常量，调试的Tag值
    private static final String TAG = "CreateClass";
    private static final int REQUEST_CODE = 1;

    //Toast静态常量
    private static Toast toast;

    private RecyclerView mClassListView;
    private MyAdapter mAdapter;
    private List<ClassTeacher> mClassListViewData = new ArrayList<>();

    private FloatingActionButton createNewClass;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_classes);

        initilizeProgressDialog();
        bindView();
        setRecycleView();
        getDataFromNet();
    }

    /**
     * 从云端获取数据
     */
    private void getDataFromNet() {
        showProgressDialog("正在拼命加载数据...");

        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.addWhereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser()); //找到我的班级
        query.includePointer(ClassTeacher.S_TEACHER);
        query.includePointer(ClassTeacher.S_CLASS);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    mClassListViewData.add((ClassTeacher) obj);
                }

                mAdapter.notifyDataSetChanged();    //通知适配器数据改变
                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                if (errorCode == 101) {
                    showToast("没有查询到数据...");
                } else {
                    showToast(Constants.NET_ERROR_TOAST);
                }
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();

            }
        });
    }

    private void setRecycleView() {
        mAdapter = new MyAdapter();

        mClassListView.setLayoutManager(new LinearLayoutManager(this));
        mClassListView.setAdapter(mAdapter);
    }

    private void bindView() {
        mClassListView = (RecyclerView) findViewById(R.id.all_class_list_view);
        createNewClass = (FloatingActionButton) findViewById(R.id.create_class);
        createNewClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewClass();
            }
        });
    }

    private void createNewClass() {
        Intent intent = new Intent(this, CreateClass.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 自定义ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView typeTv, mClassName;
        private View mSavedView;    //用于以后设置点击事件

        public MyViewHolder(View itemView) {
            super(itemView);

            typeTv = (TextView) itemView.findViewById(R.id.teacher_name_tv);
            mClassName = (TextView) itemView.findViewById(R.id.class_name_tv);

            mSavedView = itemView;
        }
    }

    /**
     * 自定义Adapter
     */
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.class_item_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ClassRoom theClass = (ClassRoom) mClassListViewData.get(position).getTargetClass();
            if (theClass == null) return;

            String theClassName = theClass.getMyClassName();

            int type = mClassListViewData.get(position).getsAuthroity();

            holder.mClassName.setText(theClassName);
            if (type == 0) {
                holder.typeTv.setText("创建者");
            } else {
                holder.typeTv.setText("代理者");
            }

            //为列表项设置点击监听事件
            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifyClassName(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mClassListViewData.size();
        }
    }

    private void modifyClassName(int position) {
        Intent intent = new Intent(this, InputClassName.class);
        String str = mClassListViewData.get(position).getTargetClass().toString();
        intent.putExtra(InputClassName.EXTRA_CLASS_ROOM, str);
        startActivityForResult(intent, REQUEST_CODE);
    }


    private void refresh() {
        mClassListViewData.clear(); //首先清空数据
        getDataFromNet();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                refresh();
            }
        }
    }
}
