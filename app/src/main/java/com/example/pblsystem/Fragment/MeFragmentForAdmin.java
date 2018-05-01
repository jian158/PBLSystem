package com.example.pblsystem.Fragment;

/**
 * Created by 郭聪聪 on 2017/4/7.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Activity.ApplyTeacherRegister;
import com.example.pblsystem.Activity.HistorySpeech;
import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Activity.MainActivityForAdmin;
import com.example.pblsystem.Activity.ManagerDataBase;
import com.example.pblsystem.Activity.MyInfo;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.RegisterTeacherApply;
import com.example.pblsystem.R;


/**
 * Created by 郭聪 on 2017/3/6.
 */
public class MeFragmentForAdmin extends Fragment {
    private TextView mApplyDeal;
    private TextView mExit;
    private TextView mUsername, mName;
    private RelativeLayout me;
    private View mSavedView;
    private LinearLayout manager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//如果该Fragment还没有绑定过View
            View view = inflater.inflate(R.layout.admin_fragment_me, container, false);
            //保留view
            mSavedView = view;
            bindView(view);
            initializeUI();

        }
        return mSavedView;
    }


    private void bindView(View view) {
        mApplyDeal = (TextView) mSavedView.findViewById(R.id.register_apply);
        mApplyDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showApplys();
            }
        });

        manager= (LinearLayout) mSavedView.findViewById(R.id.manager_data_layout_admin);
        manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ManagerDataBase.class);
                startActivity(intent);
            }
        });

        mUsername = (TextView) mSavedView.findViewById(R.id.username);
        mName = (TextView) mSavedView.findViewById(R.id.name);

        me = (RelativeLayout) mSavedView.findViewById(R.id.me);

        mExit = (TextView) mSavedView.findViewById(R.id.exit);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitSystem();
            }
        });

    }

    private void exitSystem() {
        //SharedPreferences sp = getActivity().getSharedPreferences(LoginActivity.SAVED_USER_INFO,
                //getActivity().MODE_PRIVATE);
        LoginActivity.sSelectedClass = null;
//        SharedPreferences.Editor edit = sp.edit();
//        edit.clear();
//        edit.commit();
        enterLoginPage();   //回到登录界面
//        getActivity().finish();
    }

    private void enterLoginPage() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }


    private void showMyInfo() {
        Intent intent = new Intent(getActivity(), MyInfo.class);
        startActivity(intent);
    }


    private void showApplys() {
        Intent intent = new Intent(getActivity(), ApplyTeacherRegister.class);
        startActivity(intent);
    }



    private void initializeUI() {
        AVUser user = AVUser.getCurrentUser();
        String username = user.getUsername();
        String name = user.getString(MyUser.S_NAME);

        if (user.getInt(MyUser.S_AUTHORITY) != 2) return;   // 如果不是管理员则不理
        mUsername.setText(username);
        mName.setText(name);
    }

}
