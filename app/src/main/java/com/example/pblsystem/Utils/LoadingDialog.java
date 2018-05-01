package com.example.pblsystem.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pblsystem.R;

/**
 * Created by 郭聪聪 on 2017/5/5.
 */

public class LoadingDialog {
    private static LoadingDialog loadingDialog;
    private Dialog dialog;
    private Context context;

    private LoadingDialog() {}

    public static LoadingDialog getLoadingDialog(Context context) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog();
            loadingDialog.dialog = loadingDialog.createLoadingDialog(context, "数据加载中...");
            loadingDialog.context = context;
        }

        return loadingDialog;
    }

    public void setCancelable(boolean isCancel) {
        dialog.setCancelable(isCancel);
    }

    public void show(String msg) {
        dialog = createLoadingDialog(context, msg);
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
        dialog = null;
    }

    public void setMessage(String msg) {
        dialog = createLoadingDialog(context, msg);
        dialog.show();
    }

    public Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.animator);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
        //loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;
    }

}
