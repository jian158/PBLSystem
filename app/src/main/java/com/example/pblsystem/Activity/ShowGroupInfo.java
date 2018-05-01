package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ShowGroupInfo extends AppCompatActivity {
    /*常量，调试的Tag值*/
    private static final String TAG = "ShowGroupInfo";
    /*Toast静态常量*/
    private static Toast toast;

    public static final String EXTRA_TAG = "group";

    /*小组名TV*/
    private TextView mGroupNameTextView;
    /*小组长名TV*/
    private TextView mGroupLeaderTextView;
    /*小组人数TV*/
    private TextView mGroupNumTextView;
    /*小组成员ListView*/
    private RecyclerView mMemberListView;
    /*小组成员列表适配器*/
    private MyAdapter mMemberListViewAdapter;
    /*小组名、小组长、小组人数*/
    private String mGroupName, mGroupLeaderName, mGroupNum;
    /*小组成员数据源*/
    private List<AVObject> mMembersDataList;
    /*我的小组对象*/
    private Group mGroup;
    /*圆形进度条对话框*/
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    private DataBaseManager manager = DataBaseManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group_info);

        initilizeProgressDialog();
        bindView();
        getDataFromIntent();
        setListView();
    }

    /**
     * 获取intent数据
     */
    private void getDataFromIntent() {
        Intent intent = getIntent();
        String serializeStr = intent.getStringExtra(EXTRA_TAG);
        try {
            mGroup = (Group) AVObject.parseAVObject(serializeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fetchGroupLeader();
    }

    private void fetchGroupLeader() {
        if (mGroup == null) {
            return;
        }

        mProgressBarDialog.show();

        manager.fetchInBackGround(mGroup, Group.S_LEADER, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                mGroup = (Group) obj;
                setTextVIewUi();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);

                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 设置RecycleView
     */
    private void setListView() {
        /*初始化数据源，必须，否则可能出现空指针异常*/
        mMembersDataList = new ArrayList<>();
        /*初始化adapter*/
        mMemberListViewAdapter = new MyAdapter();
        /*重要：为listview设置布局*/
        mMemberListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        /*为Listview设置适配器*/
        mMemberListView.setAdapter(mMemberListViewAdapter);
    }



    private void setTextVIewUi() {
        mGroupNameTextView.setText(mGroup.getName());
        mGroupNumTextView.setText(String.valueOf(mGroup.getNum()));
        /*获取小组长对象*/
        AVObject leader = mGroup.getLeader();
        /*小组长姓名*/
        String leaderName = leader.getString(MyUser.S_NAME);
        mGroupLeaderName = leaderName;
        mGroupLeaderTextView.setText(leaderName);
        AVRelation<AVObject> relation = mGroup.getRelation(Group.S_MEMBER);
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    Log.d(TAG, "成员数：" + list.size());
                    if (list.size() > 0) {
                        /*获取数据源*/
                        mMembersDataList = list;
                        /*重要：唤醒Listview适配器，通知其数据改变*/
                        mMemberListViewAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d(TAG, e.getMessage());
                    showToast(Constants.NET_ERROR_TOAST);
                }

                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 绑定View
     */
    private void bindView() {
        mMemberListView = (RecyclerView) findViewById(R.id.group_member_list_view);
        mGroupNameTextView = (TextView) findViewById(R.id.group_name);
        mGroupLeaderTextView = (TextView) findViewById(R.id.group_leader_name);
        mGroupNumTextView = (TextView) findViewById(R.id.group_number);

    }


    /**
     * 自定义ViewHolder
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {
        /*成员用户名TV*/
        public TextView mMemberUsernameTv;
        /*成员姓名*/
        public TextView mMemberNameTV;
        public MyViewHolder(View itemView) {
            super(itemView);
            /*绑定布局*/
            mMemberNameTV = (TextView) itemView.findViewById(R.id.member_name);
            mMemberUsernameTv = (TextView) itemView.findViewById(R.id.member_username);
        }
    }

    /**
     * 自定义适配器
     */
    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            /*解析view*/
            View view = inflater.inflate(R.layout.group_member_item_list, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            /*获取用户名*/
            String username = ((AVUser) mMembersDataList.get(position)).getUsername();
            /*获取姓名*/
            String name = mMembersDataList.get(position).getString(MyUser.S_NAME);
            //设置列表项内容
            holder.mMemberUsernameTv.setText(username);
            holder.mMemberNameTV.setText(name);
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "renshu:" + mMembersDataList.size());
            return mMembersDataList.size();
        }
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowGroupInfo.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
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
