package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ApplyJoinGroup;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.FinishListener;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Tag;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.MyDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ApplyGroup extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "ApplyGroup";
    //常量标志位：0待处理 1 通过 2拒绝
    public final int NONE = 0;
    public final int SUCCESSFUL = 1;
    public final int FAIL = 2;

    //Toast静态常量
    private static Toast toast;
    //我的申请记录列表
    private RecyclerView mMyRecordsListView;
    //待处理的申请记录列表
    private RecyclerView mApplysToDealListView;

    //ListView的适配器
    private RecyclerView.Adapter mMyRecordsListViewAdapter, mApplysToDealListViewAdapter;
    //数据源
    private List<AVObject> mMyRecordsDataList, mApplysToDealDataList;

    ViewPager mViewPager;   //视图切换器
    List<View> mViewList;   //视图集合
    LayoutInflater mInflater;
    View mApplysTodeal;
    View myApplys;
    TextView mFirstTitleTV, mSecondTitleTV;
    MyPageAdapter mViewPagerAdapter;

    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //数据库管理器
    DataBaseManager manager = DataBaseManager.getInstance();

    private Group mMyGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_group);
        try {
            initilizeProgressDialog();
            addViewsForViewPager();
            bindView();
            setViewPager();
            getData();
            setAdapter();
        } catch (NullPointerException e) {  //可能存在空指针异常
            e.getMessage();
        }
    }



    private void addViewsForViewPager() {
        mViewList = new ArrayList<>();
        mInflater = getLayoutInflater();
        mApplysTodeal = mInflater.inflate(R.layout.activity_apply_group_deal, null, false);
        myApplys = mInflater.inflate(R.layout.activity_apply_group_my_applys, null, false);
        mViewList.add(mApplysTodeal);
        mViewList.add(myApplys);
    }

    private void setViewPager() {
        mViewPagerAdapter = new MyPageAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mFirstTitleTV.setBackgroundColor(Color.parseColor("#7f2aad"));
                        mFirstTitleTV.setTextColor(Color.parseColor("#e3d42b"));
                        mSecondTitleTV.setBackgroundColor(Color.parseColor("#f2f1f1"));
                        mSecondTitleTV.setTextColor(Color.parseColor("#7f7c7c"));
                        break;
                    case 1:
                        mSecondTitleTV.setBackgroundColor(Color.parseColor("#7f2aad"));
                        mSecondTitleTV.setTextColor(Color.parseColor("#e3d42b"));
                        mFirstTitleTV.setBackgroundColor(Color.parseColor("#f2f1f1"));
                        mFirstTitleTV.setTextColor(Color.parseColor("#7f7c7c"));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 自定义PagerAdapter
     */
    private class MyPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewList.get(position);
            if (view == null) {
                Log.d(TAG, "空" + position);
            }
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

    }


    /**
     * 设置适配器
     */
    private void setAdapter() {
        mMyRecordsListViewAdapter= new MyRecordsListViewAdapter();
        //设置线性布局
        mMyRecordsListView.setLayoutManager(new LinearLayoutManager(this));
        mMyRecordsListView.setAdapter(mMyRecordsListViewAdapter);
        mMyRecordsListView.addItemDecoration(new MyDecoration(this));

        mApplysToDealListViewAdapter = new MyApplyDealListViewAdapter();
        mApplysToDealListView.setLayoutManager(new LinearLayoutManager(this));
        mApplysToDealListView.setAdapter(mApplysToDealListViewAdapter);
        mMyRecordsListView.addItemDecoration(new MyDecoration(this));
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(ApplyGroup.this);
        mProgressBarDialog.setMessage("数据加载中...");
    }


    /**
     * 获取数据
     */
    private void getData() {
        getMyRecordsFromNet();
        getMyApplysFromNet();
    }




    private void getMyApplysFromNet() {
        mApplysToDealDataList = new ArrayList<>();
        //获取我的待处理记录
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        //我是组长的小组
        query.addWhereEqualTo(Group.S_LEADER, AVUser.getCurrentUser());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    getMyApplysToDealInBackground(results.get(0));
                } else {
                    Log.d(TAG, "many groups" + results.size());
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);

            }
        });
    }

    private void getMyApplysToDealInBackground(Object obj) {
        //得到我的小组
        Group myGroup = (Group) obj;
        mMyGroup = myGroup; //保存我的小组
        //获取待处理的申请
        DataBaseQuery query = new DataBaseQuery(ApplyJoinGroup.CLASS_NAME);
        //目标是我的小组申请
        query.addWhereEqualTo(ApplyJoinGroup.S_TARGET_GROUP, myGroup);
        //状态尚未处理
        query.addWhereEqualTo(ApplyJoinGroup.S_STATE, NONE);
        //将申请人的信息获取
        query.includePointer(ApplyJoinGroup.S_APPLY_USER);
        //将申请的目标小组信息获取
        query.includePointer(ApplyJoinGroup.S_TARGET_GROUP);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                mApplysToDealDataList = results;
                if (results.size() > 0) {//存在数据
                    //通知ListView数据有改变
                    mApplysToDealListViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void getMyRecordsFromNet() {
        mMyRecordsDataList = new ArrayList<>();
        //获取我的申请记录
        DataBaseQuery query = new DataBaseQuery(ApplyJoinGroup.CLASS_NAME);
        //我的申请
        query.addWhereEqualTo(ApplyJoinGroup.S_APPLY_USER, AVUser.getCurrentUser());
        //顺带查出申请的小组
        query.includePointer(ApplyJoinGroup.S_TARGET_GROUP);
        //查询结果按照时间降序
        query.orderByDescendingDB("createdAt");
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() > 0) {//有数据
                    //复制数据源
                    mMyRecordsDataList = results;
                    //数据改变通知ListView
                    mMyRecordsListViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                Log.d(TAG, exceptionMsg);
            }

        });
    }

    /**
     * 绑定组件
     */
    private void bindView() {
        mApplysToDealListView = (RecyclerView) mApplysTodeal.findViewById(R.id.my_applys_to_deal_list_view);
        mMyRecordsListView = (RecyclerView) myApplys.findViewById(R.id.my_apply_recored_list_view);

        mViewPager = (ViewPager) findViewById(R.id.apply_group_view_pager);
        mFirstTitleTV = (TextView) findViewById(R.id.apply_title);
        mSecondTitleTV = (TextView) findViewById(R.id.my_apply_title);

        mFirstTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });

        mSecondTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });
    }

    /**
     * 自定义ViewHolder
     */
    private class MyRecordsViewHolder extends RecyclerView.ViewHolder {
        //目标小组名
        public TextView mTargetGroupName;
        //申请类型
        public TextView mRecordTypeTextView;
        //申请结果
        public TextView mResultTextView;
        //申请时间
        public TextView mApplyTimeTextView;

        public MyRecordsViewHolder(View itemView) {
            super(itemView);
            mRecordTypeTextView = (TextView) itemView.findViewById(R.id.record_sort);
            mTargetGroupName = (TextView) itemView.findViewById(R.id.target_group_text_view);
            mResultTextView = (TextView) itemView.findViewById(R.id.result_text_view);
            mApplyTimeTextView = (TextView) itemView.findViewById(R.id.create_time_text_view);
        }
    }

    /**
     * 自定义ViewHolder
     */
    private class MyApplyDealViewHolder extends RecyclerView.ViewHolder {
        //申请人姓名
        public TextView mName;
        //申请时间
        public TextView mApplyTimeTextView;
        //同意按钮
        public Button mAgreeBtn;
        //保存itemView
        public View mItemView;
        public TextView mExtraInfo;

        public MyApplyDealViewHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.user_name_text_view);
            mApplyTimeTextView = (TextView) itemView.findViewById(R.id.create_time_text_view);
            mAgreeBtn = (Button) itemView.findViewById(R.id.agree_apply_btn);
            mExtraInfo = (TextView) itemView.findViewById(R.id.apply_item_extra_info_text_view);
            //保存view
            mItemView = itemView;
        }



    }

    /**
     * 自定义我的记录适配器
     */
    private class MyRecordsListViewAdapter extends RecyclerView.Adapter<MyRecordsViewHolder> {

        @Override
        public MyRecordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //列表项视图
            View view = null;
            //新建解析布局器
            LayoutInflater inflater = LayoutInflater.from(ApplyGroup.this);
            view = inflater.inflate(R.layout.activity_apply_group_records_list_item, parent, false);
            return new MyRecordsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyRecordsViewHolder holder, int position) {
            holder.mRecordTypeTextView.setText("[加组]");
            //获得列表项对应的数据对象
            ApplyJoinGroup record = (ApplyJoinGroup) mMyRecordsDataList.get(position);
            //状态
            int state = -1;
            String mResult;
            state = record.getState();
            switch (state) {
                case SUCCESSFUL:
                    mResult = "已通过";
                    break;
                case NONE:
                    mResult = "正在处理";
                    break;
                case FAIL:
                    mResult = "已拒绝";
                    break;
                default:
                    mResult = "无";
                    break;
            }
            //获得目标小组对象
            Group targetGroup = record.getTargetGroup();
            if (targetGroup == null) {
                holder.mTargetGroupName.setText("无效的小组");
            } else {
                holder.mTargetGroupName.setText(targetGroup.getName());
            }
            holder.mResultTextView.setText(mResult);
            //日期格式化
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            //定位时区
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            String applyTime = format.format(record.getCreatedAt());
            //设置申请时间
            holder.mApplyTimeTextView.setText(applyTime);
        }

        @Override
        public int getItemCount() {
            return mMyRecordsDataList.size();
        }
    }

    /**
     * 自定义我的记录适配器
     */
    private class MyApplyDealListViewAdapter extends RecyclerView.Adapter<MyApplyDealViewHolder> {

        @Override
        public MyApplyDealViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //列表项视图
            View view = null;
            //新建解析布局器
            LayoutInflater inflater = LayoutInflater.from(ApplyGroup.this);
            view = inflater.inflate(R.layout.activity_apply_group_deal_list_view_item, parent, false);
            return new MyApplyDealViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyApplyDealViewHolder holder, final int position) {
            //获得列表项对应的数据对象
            ApplyJoinGroup dealObj = (ApplyJoinGroup) mApplysToDealDataList.get(position);
            //获得申请人对象
            AVUser user = (AVUser) dealObj.getApplyUser();
            holder.mName.setText(user.getString(MyUser.S_NAME));

            holder.mExtraInfo.setText("附加信息:" + dealObj.getExtraInfo());  //获得附加信息
            //日期格式化
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            //定位时区
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            String applyTime = format.format(dealObj.getCreatedAt());
            //设置申请时间
            holder.mApplyTimeTextView.setText(applyTime);
            //设置监听事件
            holder.mAgreeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //同意申请
                    agreeApply(position);

                    holder.mAgreeBtn.setClickable(false); //设置不可点击
                }
            });
            //为itemview添加点击事件监听
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AgreeOrNot(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mApplysToDealDataList.size();
        }
    }

    /**
     * 同意或者拒绝别人的加组申请
     * @param position
     */
    private void AgreeOrNot(final int position) {
        //标题
        TextView titleTextView;
        //内容
        TextView contentTextView;
        //按钮
        TextView cancelBtn, confrimBtn;
        //xx按钮
        ImageView dismissBtn;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //解析器
        LayoutInflater inflater = LayoutInflater.from(this);
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
        contentTextView.setText("该用户想要加入你的小组，是否同意？");
        confrimBtn.setText("同意");
        cancelBtn.setText("残忍拒绝");
        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();;
        //为按钮设置监听事件
        confrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agreeApply(position);
                realDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disAgree(position);
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
     * 拒绝申请
     * @param position
     */
    private void disAgree(final int position) {
        /*
        拒绝申请只需要将申请的状态置为2即可
         */
        //获得处理的对象
        final ApplyJoinGroup dealObj = (ApplyJoinGroup) mApplysToDealDataList.get(position);
        //更改状态
        dealObj.setState(FAIL);
        //保存数据
        //开启进度框
        showProgressDialog("正在处理中...");
        manager.saveInBackGround(dealObj, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("已经残忍拒绝了这位童鞋！");
                //删除本地数据
                mApplysToDealDataList.remove(position);
                //通知列表更新
                mApplysToDealListViewAdapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);

                //隐藏进度条
                dismissProgressDialog();
            }
        });
    }

    /**
     * 点击了待处理列表中的确认按钮
     * @param position
     */
    private void agreeApply(final int position) {
        findMyClass(position);

    }

    /**
     * 获取我所在的课堂
     */
    private void findMyClass(final int position) {
        showProgressDialog("数据检验中...");

        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                checkIfTheGroupIsFull(myClass, position);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private void checkIfTheGroupIsFull(ClassRoom myClass, final int position) {
        DataBaseQuery query = new DataBaseQuery(Tag.CLASS_NAME);
        query.addWhereEqualTo(Tag.S_CLASS, myClass);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    int max = ((Tag) results.get(0)).getMaxGroupNum();
                    if (max == 0) { //教师尚未设置人数
                        max = 4;    //默认设置小组人数为4
                    }

                    int nowMembers = mMyGroup.getNum();
                    if (nowMembers >= max) {//小组人数已满
                        showToast("小组人数已满，已经拒绝该请求!");
                        disAgree(position);

                    } else {
                        checkIfHaveTheGroup(position);
                    }
                } else if(results.size() == 0){
                    checkIfHaveTheGroup(position);
                } else {
                    Log.d(TAG, "TAG表出现问题...");
                    dismissProgressDialog();
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void checkIfHaveTheGroup(final int position) {
        //获得处理的对象
        final ApplyJoinGroup dealObj = (ApplyJoinGroup) mApplysToDealDataList.get(position);
        //获取申请人
        final AVUser user = dealObj.getApplyUser();
        //获取目标小组
        final Group myGroup = dealObj.getTargetGroup();

        //判断申请人是否已经在某个小组中
        showProgressDialog("正在核对申请人的信息...");
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, user);
        query.countInBackgroundDB(new CountCallBackDB() {
            @Override
            public void CountDoneSuccessful(int number) {
                if (number > 0) {//该用户已经是某个小组的成员
                    showToast("该用户已经是某个小组的成员了！系统已经残忍拒绝了他！");
                    //拒绝本次申请
                    dealObj.setState(FAIL);
                    manager.saveInBackGround(dealObj);
                    //删除本地数据
                    mApplysToDealDataList.remove(position);
                    //通知列表更新
                    mApplysToDealListViewAdapter.notifyDataSetChanged();
                    //隐藏进度框
                    dismissProgressDialog();
                } else {//用户身份核验成功
                    addUserToMyGroup(myGroup, user, dealObj, position);

                }
            }

            @Override
            public void CountDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void addUserToMyGroup(final Group myGroup, AVUser user, final ApplyJoinGroup dealObj, final int position) {
        showProgressDialog("正在加入新的小伙伴，请稍后...");
        AVRelation<AVObject> relation = myGroup.getRelation(Group.S_MEMBER);
        //将该用户放入小组成员中
        relation.add(user);
        //刷新目标小组对象
        manager.fetchIfNeededInBackGround(myGroup, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                //增加小组成员人数
                int preNumber = myGroup.getInt(Group.S_NUM);
                preNumber++;
                myGroup.setNum(preNumber);
                //保存
                manager.saveInBackGround(myGroup, new SaveCallBackDB() {
                    @Override
                    public void saveDoneSuccessful() {
                        showToast("新伙伴招募成功！");
                        //更改申请记录状态
                        updateRecordState(SUCCESSFUL, dealObj);
                        //移除该项
                        mApplysToDealDataList.remove(position);
                        //通知列表适配器
                        mApplysToDealListViewAdapter.notifyDataSetChanged();

                        //隐藏进度框
                        mProgressBarDialog.dismiss();

                        setResult(RESULT_OK);
                    }

                    @Override
                    public void saveDoneFailed(String exceptionMsg, int errorCode) {
                        showToast(Constants.NET_ERROR_TOAST);
                        Log.d(TAG, exceptionMsg);
                        dismissProgressDialog();
                    }
                });
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    //更改记录状态
    private void updateRecordState(int state, AVObject record) {
        //更改状态位
        ApplyJoinGroup joinRecord = (ApplyJoinGroup) record;
        joinRecord.setState(state);
        manager.saveInBackGround(record);
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
}
