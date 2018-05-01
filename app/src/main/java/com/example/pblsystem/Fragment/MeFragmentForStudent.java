package com.example.pblsystem.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SendCallback;
import com.example.pblsystem.Activity.CleanActivity;
import com.example.pblsystem.Activity.HistorySpeech;
import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Activity.MyClassRoomInfo;
import com.example.pblsystem.Activity.MyInfo;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.OnLine;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by 郭聪 on 2017/3/6.
 */
public class MeFragmentForStudent extends Fragment {
    private TextView mSpeechHistory;
    private TextView mMyClass, mExit;
    private TextView mUsername, mName;
    private RelativeLayout me;
    private View mSavedView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//如果该Fragment还没有绑定过View
            View view = inflater.inflate(R.layout.student_me_fragment, container, false);
            //保留view
            mSavedView = view;
            bindView(view);
            initializeUI();

        }
        return mSavedView;
    }


    private void bindView(View view) {
        mSpeechHistory = (TextView) mSavedView.findViewById(R.id.speech_history);
        mSpeechHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTheSpeechHistory();
            }
        });

        mMyClass = (TextView) mSavedView.findViewById(R.id.my_class);
        mMyClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyClassInfo();
            }
        });

        mUsername = (TextView) mSavedView.findViewById(R.id.username);
        mName = (TextView) mSavedView.findViewById(R.id.name);

        me = (RelativeLayout) mSavedView.findViewById(R.id.me);
        me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyInfo();
            }
        });

        mExit = (TextView) mSavedView.findViewById(R.id.exit);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitSystem();
            }
        });

//        TextView clean = (TextView) mSavedView.findViewById(R.id.clean);
//        clean.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clean();
//            }
//        });

    }

    private void clean() {
        Intent intent = new Intent(getActivity(), CleanActivity.class);
        startActivity(intent);
    }

    private void exitSystem() {
        SharedPreferences sp = getActivity().getSharedPreferences(LoginActivity.SAVED_USER_INFO,
                getActivity().MODE_PRIVATE);
        LoginActivity.sSelectedClass = null;
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
        clearOnlineInfo();
        enterLoginPage();   //回到登录界面
        getActivity().finish();
    }

    /**
     * 清除登录系统信息
     */
    private void clearOnlineInfo() {
        DataBaseQuery query = new DataBaseQuery(OnLine.CLASS_NAME);
        query.addWhereEqualTo(OnLine.S_USERNAME, AVUser.getCurrentUser().getUsername());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    DataBaseManager manager = DataBaseManager.getInstance();
                    manager.deleteInBackGround((AVObject) results.get(0));
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {

            }
        });
    }

    private void enterLoginPage() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }


    private void showMyInfo() {
        Intent intent = new Intent(getActivity(), MyInfo.class);
        startActivity(intent);
    }


    private void showTheSpeechHistory() {
        Intent intent = new Intent(getActivity(), HistorySpeech.class);
        startActivity(intent);
    }


    private void showMyClassInfo() {
        Intent intent = new Intent(getActivity(), MyClassRoomInfo.class);
        startActivity(intent);
    }


    private void initializeUI() {
        AVUser user = AVUser.getCurrentUser();
        String username = user.getUsername();
        String name = user.getString(MyUser.S_NAME);

        mUsername.setText(username);
        mName.setText(name);
    }

}
