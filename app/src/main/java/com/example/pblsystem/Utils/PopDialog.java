package com.example.pblsystem.Utils;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pblsystem.Interface.CancelMessage;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.DialogCancel;
import com.example.pblsystem.Interface.InputDialogConfirm;
import com.example.pblsystem.R;

/**
 * Created by 郭聪聪 on 2017/3/26.
 */

public class PopDialog {
    public static void popInputDialog(String title, String buttonMsg, Activity context,
                                      final InputDialogConfirm confirm, final DialogCancel cancel) {
        //标题
        TextView titleTextView;
        //内容
        final EditText inputEditText;
        //按钮
        Button confrimBtn;
        //xx按钮
        ImageView dismissBtn;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(context);
        //获取对话框view
        View view = inflater.inflate(R.layout.dialog_input_view, null, false);
        //绑定组件
        titleTextView = (TextView) view.findViewById(R.id.title);
        inputEditText = (EditText) view.findViewById(R.id.message);
        confrimBtn = (Button) view.findViewById(R.id.confirm);
        dismissBtn = (ImageView) view.findViewById(R.id.dismiss);
        //初始化组件
        titleTextView.setText(title);
        confrimBtn.setText(buttonMsg);
        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();
        //为按钮设置监听事件
        confrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirm != null) {
                    int resultCode = confirm.confirm(inputEditText.getText().toString());
                    if (resultCode == 0) {//输入数据合法
                        realDialog.dismiss();
                    }
                }
            }
        });

        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancel != null) {
                    cancel.cancel();
                }
                Log.d("tag", "销毁");
                //销毁对话框
                realDialog.dismiss();
            }
        });
    }

    /**
     * 弹出对话框
     */
    public static void popMessageDialog(Activity context, String msg, String negativeMsg, String positiveMsg,
                                 final ConfirmMessage confirm, final CancelMessage cancel) {
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
                confirm.confirm();
                realDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancel != null) {
                    cancel.cancel();
                }
                realDialog.dismiss();
            }
        });
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁对话框
                if (cancel != null) {
                    cancel.cancel();
                }
                realDialog.dismiss();

            }
        });
    }

    /**
     * 弹出对话框
     */
    public static void popWarning(Activity context, final ConfirmMessage confirm, final CancelMessage cancel) {
        //按钮
        TextView cancelBtn, confrimBtn;
        //xx按钮
        ImageView dismissBtn;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(context);
        //获取对话框view
        View view = inflater.inflate(R.layout.warining_dialog, null, false);

        confrimBtn = (TextView) view.findViewById(R.id.confirm);
        cancelBtn = (TextView) view.findViewById(R.id.cancel);
        dismissBtn = (ImageView) view.findViewById(R.id.dismiss);
        dialog.setView(view);

        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();;
        //为按钮设置监听事件
        confrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.confirm();
                realDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancel != null) {
                    cancel.cancel();
                }
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
     * 弹出确认对话框
     */
    public static void popConfirmDialog(Activity context, String msg) {
        //标题
        TextView titleTextView;
        //内容
        TextView contentTextView;
        //按钮
        TextView confrimBtn;
        //xx按钮
        ImageView dismissBtn;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(context);
        //获取对话框view
        View view = inflater.inflate(R.layout.confirm_dialog_view, null, false);
        //绑定组件
        titleTextView = (TextView) view.findViewById(R.id.title);
        contentTextView = (TextView) view.findViewById(R.id.message);
        confrimBtn = (TextView) view.findViewById(R.id.confirm);
        dismissBtn = (ImageView) view.findViewById(R.id.dismiss);
        //初始化组件
        titleTextView.setText("提示");
        contentTextView.setText(msg);
        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();
        //为按钮设置监听事件
        confrimBtn.setOnClickListener(new View.OnClickListener() {
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
}
