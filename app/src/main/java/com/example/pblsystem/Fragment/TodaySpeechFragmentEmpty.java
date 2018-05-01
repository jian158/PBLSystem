package com.example.pblsystem.Fragment;

/**
 * Created by 郭聪 on 2017/3/6.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.AVObject;
import com.example.pblsystem.R;

/**
 * 今日演讲为空
 */
public class TodaySpeechFragmentEmpty extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_speech_empty, container, false);
        return view;
    }


}
