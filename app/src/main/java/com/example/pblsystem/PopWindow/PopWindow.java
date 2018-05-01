package com.example.pblsystem.PopWindow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Activity.SetMySpeechProgress;
import com.example.pblsystem.Activity.SubmitMyWork;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;

import java.util.ArrayList;
import java.util.List;

public class PopWindow extends PopupWindow{
    private View mMenuView;

    private SetMySpeechProgress context;
    private static Toast toast;

    private TextView submitWorkTV, assignSpeakerTV, summaryWorkTV;

    private DataBaseManager manager = DataBaseManager.getInstance();

    public PopWindow(SetMySpeechProgress context) {
        super(context);
        this.context = context;
        inflateView(context);
        setContentView(mMenuView);
        bindView();
        setClickListenerForTV();
        setWindowUI();
        setTouchEvent();
    }


    private void inflateView(Activity context) {
        LayoutInflater inflater;
        inflater = LayoutInflater.from(context);
        mMenuView = inflater.inflate(R.layout.pop_window, null);
    }


    private void bindView() {
        submitWorkTV = (TextView) mMenuView.findViewById(R.id.submit_work_tv);
        assignSpeakerTV = (TextView) mMenuView.findViewById(R.id.assign_speaker_tv);
        summaryWorkTV = (TextView) mMenuView.findViewById(R.id.summary_work_tv);
    }


    private void setClickListenerForTV() {
        submitWorkTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitMyWork();
            }
        });
        assignSpeakerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assignSpeaker();
            }
        });
        summaryWorkTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitSummary();
            }
        });
    }

    /**
     * 提交我的分工
     */
    private void submitMyWork() {
        Intent intent = new Intent(context, SubmitMyWork.class);
        intent.putExtra(SubmitMyWork.EXTRA_TAG,
                context.getIntent().getStringExtra(SetMySpeechProgress.EXTRA_TAG));
        context.startActivity(intent);
    }

    /**
     * 指定演讲者
     */
    private void assignSpeaker() {
        checkIfTheLeader();
    }

    private void checkIfTheLeader() {
        context.showProgressDialog("正在核验身份...");

        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_LEADER, AVUser.getCurrentUser());
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {//组长
                    getAllMembersFromNet();
                } else {
                    showToast("你不是组长！无权进行此操作！");
                    context.dismissProgressDialog();
                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                context.dismissProgressDialog();
            }
        });
    }

    /**
     * 获取所有成员
     */
    private void getAllMembersFromNet() {
        Group group = (Group) context.getmProblemGroup().getGroup();
        if (group == null) {
            Log.d("tag", "无");
            return;
        }

        context.showProgressDialog("正在获取小组成员...");

        AVRelation relation = group.getRelation(Group.S_MEMBER);
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    popSpeakersSelectDialog(list);
                    context.dismissProgressDialog();
                } else {
                    Log.d("tag1", e.getMessage());
                    context.dismissProgressDialog();
                }
            }
        });
    }

    /**
     * 提交工作总结
     */
    private void submitSummary() {
        showToast("提交工作总结");
    }


    private void setWindowUI() {
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        setBackgroundDrawable(dw);
    }

    private void setTouchEvent() {
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    /**
     * 弹出对话框
     */
    private void popSpeakersSelectDialog(final List results) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(context);
        //获取对话框view
        View view = inflater.inflate(R.layout.dialog_select_class, null, false);
        //绑定组件
        ListView classesListView = (ListView) view.findViewById(R.id.dialog_class_list_view);
        ImageView dismissBtn = (ImageView) view.findViewById(R.id.dialog_cancel_imageview);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText("请选择一个演讲人");

        List<String> SpeakeresData = new ArrayList<>();
        for (Object obj: results) {
            AVUser user = (AVUser) obj;
            String name = (String) user.get(MyUser.S_NAME);
            SpeakeresData.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, SpeakeresData);

        classesListView.setAdapter(adapter);
        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();

        classesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateSpeaker(results.get(position), realDialog);
            }
        });

        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realDialog.dismiss();
            }
        });

    }

    private void updateSpeaker(Object obj, final AlertDialog dialog) {
        AVUser speaker = (AVUser) obj;
        ProblemGroup problemGroup = context.getmProblemGroup();
        Log.d("tag", problemGroup.getObjectId());
        problemGroup.setSpeaker(speaker);

        context.showProgressDialog("数据提交中...");
        manager.saveInBackGround(problemGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("操作成功！");
                context.dismissProgressDialog();
                dialog.dismiss();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                showToast("操作失败！");
                context.dismissProgressDialog();
            }
        });
    }

    /**
     * 弹出Toast
     */
    public void showToast(String msg) {
        if (toast == null) {//第一次初始化toast变量
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }

}
