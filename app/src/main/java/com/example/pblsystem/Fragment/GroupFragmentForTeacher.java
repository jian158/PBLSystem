package com.example.pblsystem.Fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Activity.ShowAllExitGroupApplys;
import com.example.pblsystem.Activity.ShowGroupInfo;
import com.example.pblsystem.Activity.SubmitMyWork;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 郭聪聪 on 2017/3/30.
 */

public class GroupFragmentForTeacher extends Fragment {
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;
    //常量，调试的Tag值
    private static final String TAG = "GroupFragmentForTeacher";
    private RecyclerView allGroupListView;
    private List<Group> sourceData = new ArrayList<>();

    private MyAdapter adapter;

    private View mSavedView;
    private FloatingActionButton refresh;
    private ImageView applyDeal;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//如果该Fragment还没有绑定过View
            View view = inflater.inflate(R.layout.teacher_group_fragment, container, false);
            //保留view
            mSavedView = view;
            bindView();
            initilizeProgressDialog();
            setAdapter();
            getDataFromNet();

        }
        return mSavedView;
    }

    private void bindView() {
        allGroupListView = (RecyclerView) mSavedView.findViewById(R.id.all_group);
        refresh = (FloatingActionButton) mSavedView.findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        applyDeal = (ImageView) mSavedView.findViewById(R.id.apply_deal);
        applyDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAllApplys();
            }
        });
    }

    private void showAllApplys() {
        Intent intent = new Intent(getActivity(), ShowAllExitGroupApplys.class);
        startActivity(intent);
    }

    private void refresh() {
        sourceData.clear();
        adapter.notifyDataSetChanged();
        getDataFromNet();
    }

    private void setAdapter() {
        adapter = new MyAdapter();
        allGroupListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        allGroupListView.setAdapter(adapter);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTV, numberTV;
        private ImageView image;
        private View savedView;

        public MyViewHolder(View itemView) {
            super(itemView);

            savedView = itemView;
            nameTV = (TextView) itemView.findViewById(R.id.group_list_item_name);
            numberTV = (TextView) itemView.findViewById(R.id.group_list_item_num);
            image = (ImageView) itemView.findViewById(R.id.group_list_item_lock);

        }
    }

    public void getDataFromNet() {

        if (LoginActivity.sSelectedClass == null) {
            return;
        }

        showProgressDialog("数据加载中...");

        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_CLASS, classRoom);
        query.addNotWhereEqualTo(Group.S_FLAG, Group.MODE_HIDE);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    Group group = (Group) obj;
                    sourceData.add(group);
                }

                adapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.group_all_group_item, parent, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Group group = sourceData.get(position);

            String name = group.getName();
            int number = group.getNum();
            int mode = group.getFlag();

            holder.nameTV.setText(name);
            holder.numberTV.setText(number + "人");

            if (mode == Group.MODE_OPEN) {
                holder.image.setImageResource(R.drawable.open);
            } else {
                holder.image.setImageResource(R.drawable.lock);
            }

            holder.savedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showGroupInfo(position);
                }
            });

        }

        @Override
        public int getItemCount() {
            return sourceData.size();
        }
    }

    private void showGroupInfo(int position) {
        Group group = sourceData.get(position);
        if (group == null) {
            return;
        }

        String serializeStr = group.toString();
        Intent intent = new Intent(getActivity(), ShowGroupInfo.class);
        intent.putExtra(ShowGroupInfo.EXTRA_TAG, serializeStr);
        startActivity(intent);
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(getActivity());
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
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
            toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();    //销毁对话框，防止窗体泄露
    }
}
