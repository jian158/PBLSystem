package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Class.ApplyJoinGroup;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ExitGroupApply;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CancelMessage;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.MyDecoration;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ShowAllExitGroupApplys extends AppCompatActivity {
    /*常量，调试的Tag值*/
    private static final String TAG = "ShowAllExitGroupApplys";
    /*Toast静态常量*/
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    private RecyclerView allApplys;
    private List<ExitGroupApply> dataSource = new ArrayList<>();
    private MyAdapter adapter;

    private Group mGroup;
    private ExitGroupApply apply;

    private DataBaseManager manager = DataBaseManager.getInstance();
    private int currentClickIndex = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_exit_group_applys);
        bindView();
        initilizeProgressDialog();
        setAdapterForRecycleView();
        getDataFromNet();
    }

    private void getDataFromNet() {
        if (LoginActivity.sSelectedClass == null) {
            return;
        }
        showProgressDialog("正在加载数据...");

        ClassRoom classroom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(ExitGroupApply.CLASS_NAME);
        query.addWhereEqualTo(ExitGroupApply.S_CLASS, classroom);
        query.addWhereEqualTo(ExitGroupApply.S_STATE, 0);
        query.includePointer(ExitGroupApply.S_APPLYER); //附带加载出申请人的信息

        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    ExitGroupApply avObj = (ExitGroupApply) obj;
                    dataSource.add(avObj);
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


    private void bindView() {
        allApplys = (RecyclerView) findViewById(R.id.all_applys);
    }


    private void setAdapterForRecycleView() {
        adapter = new MyAdapter();
        allApplys.setLayoutManager(new LinearLayoutManager(this));
        allApplys.setAdapter(adapter);
        allApplys.addItemDecoration(new MyDecoration(this));
    }
    /**
     * 自定义ViewHolder
     */
    private class MyViewholder extends RecyclerView.ViewHolder {
        //申请人姓名
        public TextView mName;
        //申请时间
        public TextView mApplyTimeTextView;
        public TextView prompt;
        //同意按钮
        public Button mAgreeBtn;
        //保存itemView
        public View mItemView;
        public TextView mExtraInfo;

        public MyViewholder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.user_name_text_view);
            mApplyTimeTextView = (TextView) itemView.findViewById(R.id.create_time_text_view);
            mAgreeBtn = (Button) itemView.findViewById(R.id.agree_apply_btn);
            mExtraInfo = (TextView) itemView.findViewById(R.id.apply_item_extra_info_text_view);
            prompt = (TextView) itemView.findViewById(R.id.prompt_label);
            prompt.setText("请求退出小组");
            //保存view
            mItemView = itemView;
        }



    }

    /**
     * 自定义我的记录适配器
     */
    private class MyAdapter extends RecyclerView.Adapter<MyViewholder> {

        @Override
        public MyViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            //列表项视图
            View view = null;
            //新建解析布局器
            LayoutInflater inflater = LayoutInflater.from(ShowAllExitGroupApplys.this);
            view = inflater.inflate(R.layout.activity_apply_group_deal_list_view_item, parent, false);
            return new MyViewholder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewholder holder, final int position) {
            //获得列表项对应的数据对象
            ExitGroupApply exitApply = dataSource.get(position);
            //获得申请人对象
            AVUser user = (AVUser) exitApply.getApplyer();
            holder.mName.setText(user.getString(MyUser.S_NAME));

            holder.mExtraInfo.setText("附加信息:" + exitApply.getInfo());  //获得附加信息
            //日期格式化
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            //定位时区
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            final String applyTime = format.format(exitApply.getCreatedAt());
            //设置申请时间
            holder.mApplyTimeTextView.setText(applyTime);
            //设置监听事件
            holder.mAgreeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentClickIndex = position;
                    //同意申请
                    agreeApply(position);

                    holder.mAgreeBtn.setClickable(false); //设置不可点击
                }
            });
            //为itemview添加点击事件监听
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentClickIndex = position;
                    PopDialog.popMessageDialog(ShowAllExitGroupApplys.this, "是否同意该申请？", "拒绝", "同意",
                            new ConfirmMessage() {
                                @Override
                                public void confirm() {
                                    agreeApply(position);
                                }
                            }, new CancelMessage() {
                                @Override
                                public void cancel() {
                                    disagreeApply(position);
                                }
                            });
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataSource.size();
        }
    }

    private void disagreeApply(int position) {
        showProgressDialog("处理中...");

        ExitGroupApply apply = dataSource.get(position);
        apply.setState(2);
        manager.saveInBackGround(apply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("已拒绝");

                refresh();
                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void agreeApply(int position) {
        apply = dataSource.get(position);   //重要的初始化

        getMyGroup();
    }

    private void getMyGroup() {
        showProgressDialog("处理中...");

        AVUser user = (AVUser) apply.getApplyer();
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, user);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    showToast("操作成功！");

                    updateState();
                    refresh();
                    dismissProgressDialog();
                } else {
                    mGroup = (Group) results.get(0);
                    removeFromMember();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void removeFromMember() {
        AVUser user = (AVUser) apply.getApplyer();
        //获取关系
        final AVRelation<AVObject> relation = mGroup.getRelation(Group.S_MEMBER);
        //从关系中移除成员
        relation.remove(user);

        manager.saveInBackGround(mGroup, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                //成员已经移除，更新数据库中的小组记录
                updateGroupInfo();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, "移除成员失败" + exceptionMsg);
                showToast("操作失败！请检查网络是否连接！");
                //隐藏对话框
                dismissProgressDialog();
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
                AVUser user = (AVUser) apply.getApplyer();

                if (groupLeader.getObjectId().equals(user.getObjectId())) {//我是小组长
                    Log.d(TAG, "我是小组长.");
                    whenIamTheLeader(relation);
                } else {
                    //更新小组信息
                    manager.saveInBackGround(mGroup, new SaveCallBackDB() {
                        @Override
                        public void saveDoneSuccessful() {
                            //关闭当前窗口
                            showToast("操作成功！");

                            updateState();
                            refresh();
                            dismissProgressDialog();

                        }

                        @Override
                        public void saveDoneFailed(String exceptionMsg, int errorCode) {
                            Log.d(TAG, exceptionMsg);

                            dismissProgressDialog();
                        }
                    });
                }
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, "数据同步失败");
                //隐藏进度条
                dismissProgressDialog();
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
                                showToast("操作成功！");

                                updateState();
                                refresh();
                                dismissProgressDialog();

                            }

                            @Override
                            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                                Log.d(TAG, exceptionMsg);

                                dismissProgressDialog();
                            }
                        });

                    } else {
                        //小组只有一人， 直接删除小组
                        manager.deleteInBackGround(mGroup, new DeleteCallBackDB() {
                            @Override
                            public void deleteDoneSuccessful() {
                                showToast("操作成功!");

                                updateState();
                                refresh();
                                dismissProgressDialog();
                            }

                            @Override
                            public void deleteDoneFailed(String exceptionMsg, int errorCode) {
                                Log.d(TAG, "删除空小组失败");

                                dismissProgressDialog();
                            }
                        });
                    }

                } else {
                    Log.d(TAG, "查询小组成员失败.." + e.getMessage());

                    dismissProgressDialog();
                }
            }
        });
    }

    private void updateState() {
        if (apply == null) return;
        apply.setState(1);
        manager.saveInBackGround(apply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                Log.d(TAG, "成功!");
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void refresh() {
        if (currentClickIndex == -1) return;
        dataSource.remove(currentClickIndex);
        adapter.notifyDataSetChanged();
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ShowAllExitGroupApplys.this);
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
            toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();    //销毁对话框，防止窗体泄露
    }
}
