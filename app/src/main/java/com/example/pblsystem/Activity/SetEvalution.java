package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.EvaluationStandard;
import com.example.pblsystem.Class.EvalutionDescription;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.InputDialogConfirm;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.view.View.GONE;

public class SetEvalution extends AppCompatActivity {

    //常量，调试的Tag值
    private static final String TAG = "SetEvalution";
    private static final int REQUEST_CODE = 1;

    //Toast静态常量
    private static Toast toast;

    private RecyclerView allItemsListView;
    private MyAdapter mAdapter;
    private EvaluationStandard mStandard;
    private List<EvalutionDescription> mSourceData = new ArrayList<>();

    private FloatingActionButton createNewitem;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_evalution);

        initilizeProgressDialog();

        try {
            bindView();
            setRecycleView();
            getDataFromNet();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从云端获取数据
     */
    private void getDataFromNet() throws Exception {
        showProgressDialog("正在拼命加载数据...");
        if (LoginActivity.sSelectedClass == null) {
            showToast("你还没有创建班级！");
            dismissProgressDialog();
            return;
        }

        final ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(EvaluationStandard.CLASS_NAME);
//        query.addWhereEqualTo(EvaluationStandard.S_CLASS, classRoom); //找到我的班级

        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    createStandard(classRoom);
                    return;
                }

                    EvaluationStandard standard = (EvaluationStandard) results.get(0);
                    mStandard = standard;
                    Log.d(TAG, "nujm" + standard.getScore());
                    AVRelation ralation = standard.getRelation(EvaluationStandard.S_DESCRIPTION);
                    AVQuery<EvalutionDescription> query = ralation.getQuery();
                    query.findInBackground(new FindCallback<EvalutionDescription>() {
                        @Override
                        public void done(List<EvalutionDescription> list, AVException e) {
                            if (e == null) {
                                mSourceData = list;
                                mAdapter.notifyDataSetChanged();    //通知数据改变

                                dismissProgressDialog();
                            } else {
                                dismissProgressDialog();
                            }
                        }
                    });
                
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

    private void createStandard(ClassRoom classRoom) {
        final EvaluationStandard standard = new EvaluationStandard();
        standard.setOfClass(classRoom);
        standard.setScore(50);  //默认值为50
        manager.saveInBackGround(standard, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                mStandard = standard;
                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                createNewitem.setVisibility(GONE);
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void setRecycleView() {
        mAdapter = new MyAdapter();

        allItemsListView.setLayoutManager(new LinearLayoutManager(this));
        allItemsListView.setAdapter(mAdapter);
    }

    private void bindView() {
        allItemsListView = (RecyclerView) findViewById(R.id.all_class_list_view);
        createNewitem = (FloatingActionButton) findViewById(R.id.create_class);
        createNewitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createItem();
            }
        });
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private void createItem() {
        Intent intent = new Intent(this, CreateEvalutionItem.class);
        intent.putExtra(CreateEvalutionItem.EXTRA_STNDARD, mStandard.toString());
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 自定义ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTV, descriptionTV, score;
        private View mSavedView;    //用于以后设置点击事件

        public MyViewHolder(View itemView) {
            super(itemView);

            titleTV = (TextView) itemView.findViewById(R.id.title);
            descriptionTV = (TextView) itemView.findViewById(R.id.description);
            score = (TextView) itemView.findViewById(R.id.score);

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
            View view = inflater.inflate(R.layout.evalution_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            EvalutionDescription description = mSourceData.get(position);
            String title = description.getDescriptionTitle();
            String detail = description.getDescriptionDetails();
            holder.descriptionTV.setText(detail);
            holder.titleTV.setText(title);
            holder.score.setText(description.getScore() + "分");
            //为列表项设置点击监听事件
            holder.mSavedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifyItem(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSourceData.size();
        }
    }

    private void modifyItem(int position) {
        Intent intent = new Intent(this, CreateEvalutionItem.class);
        String str = mSourceData.get(position).toString();
        intent.putExtra(CreateEvalutionItem.EXTRA_TAG, str);
        startActivityForResult(intent, REQUEST_CODE);
    }


    private void refresh() {
        mSourceData.clear(); //首先清空数据
        mAdapter.notifyDataSetChanged();
        try {
            getDataFromNet();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
