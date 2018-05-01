package com.example.pblsystem.PopWindow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pblsystem.Activity.MyProblemInfo;
import com.example.pblsystem.Activity.MyScore;
import com.example.pblsystem.Activity.ProblemInfo;
import com.example.pblsystem.Activity.SetMySpeechProgress;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.R;

/**
 * Created by 郭聪聪 on 2017/3/29.
 */

public class PopWindowForMySpeech extends PopupWindow {

    private View mMenuView;

    private MyProblemInfo context;
    private static Toast toast;

    private TextView queryScore, revokeProblem;

    private ProgressDialog mProgressBarDialog;
    private DataBaseManager manager = DataBaseManager.getInstance();

    public PopWindowForMySpeech(MyProblemInfo context) {
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
        mMenuView = inflater.inflate(R.layout.pop_window_for_problem_info, null);
    }


    private void bindView() {
        queryScore = (TextView) mMenuView.findViewById(R.id.query_score);
        revokeProblem = (TextView) mMenuView.findViewById(R.id.revoke_problem);
    }


    private void setClickListenerForTV() {
        queryScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryScore();
            }
        });
        revokeProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revokeProblem();
            }
        });
    }

    private void revokeProblem() {
        popMessageDialog("你确定要撤销该课题么？", "取消", "确定");
    }

    private void queryScore() {
        Intent intent = new Intent(context, MyScore.class);
        intent.putExtra(MyScore.EXTRA_TAG,
                context.getIntent().getStringExtra(context.PROBLEM_EXTRA_TAG));
        context.startActivity(intent);
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
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(context);
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
                context.revokeMyProblem();
                realDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(context);
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
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }
}
