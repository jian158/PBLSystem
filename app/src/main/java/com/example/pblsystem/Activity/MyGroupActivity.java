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
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ExitGroupApply;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Tag;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.List;

public class MyGroupActivity extends AppCompatActivity {
    /*常量，调试的Tag值*/
    private static final String TAG = "MyGroupActivity";
    private static final int RESULT_UPDATE_GROUP_INFO = 1;
    /*Toast静态常量*/
    private static Toast toast;

    private ImageView setMyGroupInfoImageView;
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
    /*确认退出Button*/
    private Button mExitBtn;
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
        setContentView(R.layout.activity_my_group);

        initilizeProgressDialog();
        bindView();
        getDataAndinitilizeUi();
        setListView();
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

    /**
     * 初始化UI
     */
    private void getDataAndinitilizeUi() {
        AVUser user = AVUser.getCurrentUser();
        //打开对话框
        mProgressBarDialog.setMessage("数据加载中...");
        mProgressBarDialog.show();
        //云端查询数据
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, user);
        /*顺带读取该小组的小组长Pointer信息*/
        query.includePointer(Group.S_LEADER);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    setTextVIewUi(results.get(0));
                } else if (results.size() == 0){
                    Log.d(TAG, "我没有小组");
                    //隐藏进度框
                    mProgressBarDialog.dismiss();
                } else {
                    Log.d(TAG, "我的小组数超过了2个");
                    //隐藏进度框
                    mProgressBarDialog.dismiss();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                //隐藏进度框
                mProgressBarDialog.dismiss();
            }
        });
    }

    private void setTextVIewUi(Object obj) {
         /*初始化mGroup*/
        mGroup = (Group) obj;
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

                //隐藏进度框
                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 绑定View
     */
    private void bindView() {
        setMyGroupInfoImageView = (ImageView) findViewById(R.id.config_my_group_info_image_view);
        mMemberListView = (RecyclerView) findViewById(R.id.group_member_list_view);
        mGroupNameTextView = (TextView) findViewById(R.id.group_name);
        mGroupLeaderTextView = (TextView) findViewById(R.id.group_leader_name);
        mGroupNumTextView = (TextView) findViewById(R.id.group_number);
        mExitBtn = (Button) findViewById(R.id.exit_group_btn);
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopDialog.popMessageDialog(MyGroupActivity.this, "你确定要退出该小组么？", "点错了", "退意已决",
                        new ConfirmMessage() {
                            @Override
                            public void confirm() {
                                exitGroup();    //退出小组
                            }
                        }, null);
            }
        });

        setMyGroupInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenSetGroupInfoPage();
            }
        });
    }

    /**
     * 打开设置界面
     */
    private void OpenSetGroupInfoPage() {
        if (mGroup == null) {
            showToast(Constants.NET_ERROR_TOAST);
            return;
        }
        if (mGroupLeaderName != null && mGroupLeaderName.equals(AVUser.getCurrentUser().getString(MyUser.S_NAME))) {
            String serilizedGroup = mGroup.toString();
            Intent intent = new Intent(getApplicationContext(), SetMyGroupInfo.class);
            intent.putExtra(SetMyGroupInfo.MY_GROUP_EXTRA_INFO, serilizedGroup);
            startActivityForResult(intent, RESULT_UPDATE_GROUP_INFO);
        } else {
            showToast("对不起，你不是小组长，无权修改信息！");
        }
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
     * 退出小组
     */
    private void exitGroup() {
        /*
        思路：首先判断小组是否处于自由组合模式，然后将当前用户从所在小组移除，
        如果非组长，直接将该成员从成员列表中移除，否则将小组长信息也一并移除，
        并更换新的小组长。
        这里存在移除失败的可能，因为该用户可能通过换组功能已经进入另一个小组。
        */

        if (mGroup == null) {//数据异常，我没有小组
            return;
        }
        getMyClassRoom();

    }

    private void getMyClassRoom() {
        mProgressBarDialog.setMessage("数据检验中...");
        mProgressBarDialog.show();

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                checkIfAllow(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                mProgressBarDialog.dismiss();
            }
        });
    }

    /**
     * 检验是否处于自由组合模式
     */
    private void checkIfAllow(final ClassRoom classRoom) {
        DataBaseQuery query = new DataBaseQuery(Tag.CLASS_NAME);
        query.addWhereEqualTo(Tag.S_CLASS, classRoom);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {//可以自由组合
                    removeFromMember();
                } else {
                    Tag tag = (Tag) results.get(0);
                    int mode = tag.getGroupMode();
                    if (mode == 1) {    //可以自由组合
                        removeFromMember();
                    } else if (mode == 0) { //自由组合模式已经关闭
                        //showToast("你所在的课堂已经关闭该功能！");
                        PopDialog.popMessageDialog(MyGroupActivity.this, "对不起！你所在的课题已经关闭该功能！是否提交申请？",
                                "算了", "提交", new ConfirmMessage() {
                                    @Override
                                    public void confirm() {
                                        createExitApplyRecord(classRoom);
                                    }
                                }, null);
                        mProgressBarDialog.dismiss();
                    }
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                mProgressBarDialog.dismiss();
            }
        });

    }

    /**
     * 提交申请
     */
    private void createExitApplyRecord(ClassRoom classRoom) {
        mProgressBarDialog.setMessage("申请提交中...");
        mProgressBarDialog.dismiss();

        ExitGroupApply exitApply = new ExitGroupApply();
        exitApply.setOfClass(classRoom);
        exitApply.setApplyer(AVUser.getCurrentUser());
        exitApply.setState(0);  //初始状态
        exitApply.setInfo("我想退出这个小组，换另外一个小组");
        manager.saveInBackGround(exitApply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("申请提交成功！请耐心等待审核！");

                mProgressBarDialog.dismiss();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                mProgressBarDialog.dismiss();
            }
        });
    }

    private void removeFromMember() {
        //获取关系
        final AVRelation<AVObject> relation = mGroup.getRelation(Group.S_MEMBER);
        //从关系中移除成员
        relation.remove(AVUser.getCurrentUser());
        //保存
        mProgressBarDialog.setMessage("退出小组中..请稍后！");
        mProgressBarDialog.show();
        manager.saveInBackGround(mGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                //成员已经移除，更新数据库中的小组记录
                updateGroupInfo();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, "移除成员失败" + exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                //隐藏对话框
                mProgressBarDialog.dismiss();
            }
        });
    }

    private void updateGroupInfo() {
        //同步本地数据
        manager.fetchIfNeededInBackGround(mGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                //获取关系
                final AVRelation<AVObject> relation = mGroup.getRelation(Group.S_MEMBER);
                //小组成员数-1
                int preNum = mGroup.getNum();
                preNum--;
                mGroup.setNum(preNum);

                AVObject groupLeader = mGroup.getLeader();
                AVUser user = AVUser.getCurrentUser();

                if (groupLeader.getObjectId().equals(user.getObjectId())) {//我是小组长
                    Log.d(TAG, "我是小组长.");
                    whenIamTheLeader(relation);
                } else {
                    //更新小组信息
                    manager.saveInBackGround(mGroup, new SaveCallBackDB() {
                        @Override
                        public void saveDoneSuccessful() {
                            //关闭当前窗口
                            showToast("你已经退出该小组！");
                            setResult(RESULT_OK);
                            finish();

                            //隐藏进度框
                            mProgressBarDialog.dismiss();

                        }

                        @Override
                        public void saveDoneFailed(String exceptionMsg, int errorCode) {
                            Log.d(TAG, exceptionMsg);
                            showToast(Constants.NET_ERROR_TOAST);

                            //隐藏进度框
                            mProgressBarDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, "数据同步失败");
                //隐藏进度条
                mProgressBarDialog.dismiss();
            }
        });

    }

    private void whenIamTheLeader(AVRelation<AVObject> relation) {
        //查询小组成员
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {//查询成功
                    if (list.size() > 0) {
                        //将第一个小组成员置为小组长
                        mGroup.setLeader(list.get(0));
                        //更新小组信息
                        manager.saveInBackGround(mGroup, new SaveCallBackDB() {
                            @Override
                            public void saveDoneSuccessful() {
                                //关闭当前窗口
                                showToast("你已经退出该小组！");
                                setResult(RESULT_OK);
                                finish();

                                //隐藏进度框
                                mProgressBarDialog.dismiss();

                            }

                            @Override
                            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                                Log.d(TAG, exceptionMsg);
                                showToast(Constants.NET_ERROR_TOAST);

                                //隐藏进度框
                                mProgressBarDialog.dismiss();
                            }
                        });

                    } else {
                        //小组只有一人， 直接删除小组
                        manager.deleteInBackGround(mGroup, new DeleteCallBackDB() {
                            @Override
                            public void deleteDoneSuccessful() {
                                //删除成功
                                //关闭当前窗口
                                showToast("你已经退出该小组！");
                                setResult(RESULT_OK);
                                finish();

                                //隐藏进度框
                                mProgressBarDialog.dismiss();

                            }

                            @Override
                            public void deleteDoneFailed(String exceptionMsg, int errorCode) {
                                Log.d(TAG, "删除空小组失败");

                                //隐藏进度框
                                mProgressBarDialog.dismiss();
                            }
                        });
                    }

                } else {
                    Log.d(TAG, "查询小组成员失败.." + e.getMessage());
                    //隐藏进度条
                    mProgressBarDialog.dismiss();
                }
            }
        });
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(MyGroupActivity.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    public void refresh() {
        getDataAndinitilizeUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == RESULT_UPDATE_GROUP_INFO) {
            refresh();
        }
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
