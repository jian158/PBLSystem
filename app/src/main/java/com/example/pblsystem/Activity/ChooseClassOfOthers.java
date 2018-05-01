package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
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

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.List;

public class ChooseClassOfOthers extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "CreateClass";
    //Toast静态常量
    private static Toast toast;

    private RecyclerView mClassListView;
    private MyAdapter mAdapter;
    private List<ClassTeacher> mClassListViewData = new ArrayList<>();

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_class_of_others);

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
        query.addWhereEqualTo(ClassTeacher.S_AUTHROITY, 0);     //忽略代理班级
        query.addNotWhereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser());  //忽略我的班级
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
    }


    /**
     * 自定义ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTeacherName, mClassName;
        private View mSavedView;    //用于以后设置点击事件

        public MyViewHolder(View itemView) {
            super(itemView);

            mTeacherName = (TextView) itemView.findViewById(R.id.teacher_name_tv);
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

            AVUser user = (AVUser) mClassListViewData.get(position).getTargetTeacher();
            String teacherName = user.getString(MyUser.S_NAME);

            holder.mClassName.setText(theClassName);
            if (teacherName != null) {
                holder.mTeacherName.setText(teacherName);
            } else {
                holder.mTeacherName.setText("null");
            }

            //为列表项设置点击监听事件
            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopDialog.popMessageDialog(ChooseClassOfOthers.this, "你确定要代理该课堂么？", "点错了", "确定",
                            new ConfirmMessage() {
                                @Override
                                public void confirm() {
                                    clickTheItemAtPositoin(position);
                                }
                            }, null);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mClassListViewData.size();
        }
    }

    private void clickTheItemAtPositoin(int position) {
        ClassTeacher classTeacher = mClassListViewData.get(position);
        proxyOtherClass(classTeacher);
    }

    private void proxyOtherClass(ClassTeacher choosedClassTeacher) {
        showProgressDialog("正在进行数据检验...");

        //刷新数据，防止数据已经无效
        ClassRoom targetClass = (ClassRoom) choosedClassTeacher.getTargetClass();
        manager.fetchIfNeededInBackGround(targetClass, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                showProgressDialog("正在检查用户身份...");
                checkIfRepeatProxy(obj);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                showToast("数据已经失效...");

                dismissProgressDialog();
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void checkIfRepeatProxy(final AVObject obj) {
        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.addWhereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser());
        query.addWhereEqualTo(ClassTeacher.S_CLASS, obj);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {
                    showToast("系统检测到你已经代理过此班级！请勿重复操作！");
                    dismissProgressDialog();
                } else {
                    showProgressDialog("数据创建中...");

                    createNewObjAndSave(obj);
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                dismissProgressDialog();
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void createNewObjAndSave(AVObject obj) {
        final ClassTeacher createClass = new ClassTeacher();
        createClass.setTargetClass(obj);
        createClass.setAuthroity(1);    //代理
        createClass.setTargetTeacher(AVUser.getCurrentUser());
        manager.saveInBackGround(createClass, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("代理课堂成功！");
                refresh();

                updateCurrentClassIfNeed(createClass);

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);

                dismissProgressDialog();
            }
        });
    }

    /**
     * 更新当前教师用户的课堂
     */
    private void updateCurrentClassIfNeed(ClassTeacher newClassTeacher) {
        if (LoginActivity.sSelectedClass == null) {
            LoginActivity.sSelectedClass = newClassTeacher;
        }
    }

    private void refresh() {
        mClassListViewData.clear(); //首先清空数据
        getDataFromNet();
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ChooseClassOfOthers.this);
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
