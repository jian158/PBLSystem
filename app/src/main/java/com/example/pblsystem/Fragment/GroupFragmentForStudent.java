package com.example.pblsystem.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Activity.ApplyGroup;
import com.example.pblsystem.Activity.MyGroupActivity;
import com.example.pblsystem.Class.FinishListener;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.DialogActivity.DialogApplyGroup;
import com.example.pblsystem.DialogActivity.PassWrodInput;
import com.example.pblsystem.DialogActivity.StuCreateDialog;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 郭聪 on 2017/3/6.
 */
public class GroupFragmentForStudent extends Fragment {
    //常量，调试的Tag值
    private static final String TAG = "SpeechFragment";
    //startActicityForResult 请求值
    private static final int REQUEST_CREATE_GROUP = 0;
    private static final int REQUEST_INPUT_PASSWORD = 1;
    private static final int REQUEST_ADD_MEMBER = 2;
    private static final int REQUEST_EXIT_GROUP = 3;
    //一些标志位
    public static final String CREATE_GROUP_EXTRA_TAG = "createGroup";
    public static final String INPUT_PASSWORD_EXTRA_TAG = "inputPassword";
    //Toast静态常量
    private static Toast toast;
    //HashMap的常量键值
    public static final String KEY_OBJECT = "object";

    //刷新小组列表的常量
    public static final int FIRST = 0;
    public static final int SECOND_OR_NEXT = 1;

    //所有小组的ListView
    private ListView mAllGroupListView;
    //搜索框
    private SearchView mSearchView;
    //新建小组
    private ImageView mCreateGroupImageView;
    //我的小组申请
    private ImageView mMyApplyImageView;
    //刷新按钮
    private ImageView mRefreshImageView;
    //保存视图，防止视图被销毁后，重新创建视图造成的消耗
    private View mSavedView;
    //我的小组名 TextView
    private TextView mMyGroupNameTextView;
    //我的小组人数 TextView
    private TextView mMyGroupMembersNumTextView;
    //ListView数据源
    private List<Map<String, Object>> mData;
    //ListView数据源备份
    private List<Map<String, Object>> mSavedData;
    //ListView适配器
    private MyListViewAdapter mAdapter;
    //圆形进度条对话框
    private ProgressDialog mProgressBarDialog;
    //保存当前用户实例
    private AVUser mUser;
    //保存我的小组 外层布局
    private RelativeLayout mMyGroupInfoLayout;
    //我的小组为空时，显示
    private TextView mEmptyGroupShowTextView;
    //保存最近点击的列表项
    private int mRecentlyClickedIndex = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//第一次绑定view
            View view = inflater.inflate(R.layout.student_group_fragment, container, false);
            //初始化当前用户
            mUser = AVUser.getCurrentUser();
            //保存视图
            mSavedView = view;
            bindView(view);
            initilizeProgressDialog();
            //实例化mData
            mData = new ArrayList<>();
            //备份
            mSavedData = mData;
            //先设置ListView的适配器，再加载数据
            setListView();
            getListData(GroupFragmentForStudent.FIRST, null);
            setSearchView();
            setMyGroupInfo();
        }
        return mSavedView;
    }

    /**
     * 设置我的小组信息
     */
    private void setMyGroupInfo() {
        AVUser user = AVUser.getCurrentUser();
        //云端查询数据
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        query.addWhereEqualTo(Group.S_MEMBER, user);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    //得到我的小组
                    Group myGroup = (Group) results.get(0);
                    //更新我的小组信息
                    //设置我所在的小组人数
                    mMyGroupMembersNumTextView.setText(String.valueOf(myGroup.getNum()) + "人");
                    //设置我所在的小组的名称
                    mMyGroupNameTextView.setText(myGroup.getName());
                    //设置布局可见
                    mMyGroupInfoLayout.setVisibility(View.VISIBLE);
                    mEmptyGroupShowTextView.setVisibility(View.GONE);
                } else if (results.size() == 0) {
                    //设置“空”布局可见
                    mMyGroupInfoLayout.setVisibility(View.GONE);
                    mEmptyGroupShowTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                mMyGroupInfoLayout.setVisibility(View.GONE);
                mEmptyGroupShowTextView.setVisibility(View.VISIBLE);
                mEmptyGroupShowTextView.setText("数据加载失败,检查一下网络吧");
            }
        });

    }


    /**
     * 设置SearchView
     */
    private void setSearchView() {
        mSearchView.setFocusable(false);
        //为SearchView添加事件监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * 输入完成
             * @param query
             * @return
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            /**
             * 输入中
             * @param newText
             * @return
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {//输入不为空
                    mAllGroupListView.setFilterText(newText);
                } else {
                    mAllGroupListView.clearTextFilter();
                }
                return true;
            }
        });
    }

    /**
     * 设置ListView
     */
    private void setListView() {
        //初始化适配器
        mAdapter = new MyListViewAdapter();
        //为ListView添加适配器
        mAllGroupListView.setAdapter(mAdapter);
        //ListView设置可过滤
        mAllGroupListView.setTextFilterEnabled(true);
        //所有小组列表添加监听事件
        mAllGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //重置最近点击位置
                mRecentlyClickedIndex = position;
                clickTheItem(position);

            }
        });
    }

    /**
     * 点击列表项，响应事件
     * @param position
     */
    private void clickTheItem(int position) {
        //获取小组对外权限值
        int flag = ((Group) mData.get(position).get(GroupFragmentForStudent.KEY_OBJECT)).getFlag();
        if (flag == Group.MODE_PSW) {//要密码
            //弹出密码框
            Intent intent = new Intent(getActivity().getApplicationContext(), PassWrodInput.class);
            startActivityForResult(intent, REQUEST_INPUT_PASSWORD);
        } else {
            //直接打开申请对话框
            openJoinGroupDialog();
        }
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(getActivity());
        mProgressBarDialog.setMessage("正在拼命加载数据...");
    }

    /**
     * 从云端获取小组数据
     */
    private void getListData(final int tag, final FinishListener finshListener) {
       if (tag != GroupFragmentForStudent.FIRST) {
           mData.clear();
       }
        //查询数据
        //创建查询实例
        DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
        //只查询我的课堂
        query.addWhereEqualTo(Group.S_CLASS, mUser.get(MyUser.S_CLASS));
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                Group itemGroup;
                for (Object obj: results) {//遍历查询到的数据
                    itemGroup = (Group)obj;
                    int flag = itemGroup.getFlag();
                    if (flag == Group.MODE_HIDE) {//该小组是隐藏小组，则不显示
                        Log.d(TAG, "跳过");
                        //跳过本次循环
                        continue;
                    }
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put(GroupFragmentForStudent.KEY_OBJECT, itemGroup);

                    //将map添加入mData
                    mData.add(map);
                }

                //数据拷贝完成
                if (tag != GroupFragmentForStudent.FIRST) {//第一次
                    //回调函数
                    finshListener.finish();
                }
                //唤醒Adapter更新数据
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                showToast(Constants.NET_ERROR_TOAST);
                if (finshListener != null) {
                    finshListener.finish();
                }
            }
        });
    }


    /**
     * 绑定xml中的组件
     */
    private void bindView(View view) {
        mMyGroupInfoLayout = (RelativeLayout) view.findViewById(R.id.group_my_group_layout);
        //为我的小组添加事件监听
        mMyGroupInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入我的小组信息页面
                Intent intent = new Intent(getActivity().getApplicationContext(), MyGroupActivity.class);
                startActivityForResult(intent, REQUEST_EXIT_GROUP);
            }
        });
        //默认没有小组
        mMyGroupInfoLayout.setVisibility(View.GONE);
        mEmptyGroupShowTextView = (TextView) view.findViewById(R.id.fragment_group_my_empty);
        mMyGroupNameTextView = (TextView) view.findViewById(R.id.fragment_group_my_group_name_text_view);
        mMyGroupMembersNumTextView = (TextView) view.findViewById(R.id.fragment_group_my_group_member_num_text_view);
        mAllGroupListView = (ListView) view.findViewById(R.id.fragment_group_list_view);
        mSearchView = (SearchView) view.findViewById(R.id.fragment_group_search);

        mRefreshImageView = (ImageView) view.findViewById(R.id.fragment_group_refresh);
        mCreateGroupImageView = (ImageView) view.findViewById(R.id.fragment_group_create_image_view);
        mMyApplyImageView = (ImageView) view.findViewById(R.id.fragment_group_my_apply_image_view);
        mCreateGroupImageView.setClickable(true);
        mMyApplyImageView.setClickable(true);
        //新建一个监听
        MyClickeListener clickeListener = new MyClickeListener();
        //为ImageView设置监听
        mCreateGroupImageView.setOnClickListener(clickeListener);
        mMyApplyImageView.setOnClickListener(clickeListener);
        mRefreshImageView.setOnClickListener(clickeListener);
    }

    /**
     * 自定义点击事件类
     */
    private class MyClickeListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //新建group
                case R.id.fragment_group_create_image_view:
                    createNewGroup();
                    break;
                //查询我的申请记录
                case R.id.fragment_group_my_apply_image_view:
                    showMyApplyRecord();
                    break;
                //刷新
                case R.id.fragment_group_refresh:
                    refresh();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 刷新界面
     */
    private void refresh() {
        //显示刷新进度
        mProgressBarDialog.setMessage("系统君正在拼命刷新中,请稍后！");
        mProgressBarDialog.show();

        //刷新我的小组信息
        setMyGroupInfo();
        //刷新所有小组列表
        getListData(GroupFragmentForStudent.SECOND_OR_NEXT, new MyFinishListenner());
    }

    /**
     * 实现FinishListener接口
     */
    private class MyFinishListenner implements FinishListener {

        @Override
        public void finish() {
            //刷新成功！关闭进度框，提示用户
            mProgressBarDialog.dismiss();
        }
    }

    /**
     * 查询申请记录
     */
    private void showMyApplyRecord() {
        //开启新的Acitivity页面
        Intent intent = new Intent(getActivity().getApplicationContext(), ApplyGroup.class);
        startActivityForResult(intent, REQUEST_ADD_MEMBER);
    }

    /**
     * 新建小组
     */
    private void createNewGroup() {
        //打开对话框
        Intent intent = new Intent(getActivity().getApplicationContext(), StuCreateDialog.class);
        startActivityForResult(intent, REQUEST_CREATE_GROUP);
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {//返回了正确的数据
            switch (requestCode) {
                //新建小组成功
                case REQUEST_CREATE_GROUP:
                    //获取新建小组的objectid
                    String name = data.getStringExtra(CREATE_GROUP_EXTRA_TAG);
                    //更新我的小组信息
                    updateMyGroupInfo(name, 1);
                    //刷新所有小组列表
                    getListData(GroupFragmentForStudent.SECOND_OR_NEXT, new FinishListener() {
                        @Override
                        public void finish() {
                            //do nothing
                        }
                    });
                    break;
                //输入密码返回后
                case REQUEST_INPUT_PASSWORD:
                    //获得输入的密码
                    String password = data.getStringExtra(INPUT_PASSWORD_EXTRA_TAG);
                    //检查密码
                    checkThePassword(password);
                    break;
                //新加入小组成员
                case REQUEST_ADD_MEMBER:
                    //刷新界面
                    refresh();
                    break;
                //退出小组成功
                case REQUEST_EXIT_GROUP:
                    Log.d(TAG, "回调了.");
                    refresh();  //刷新界面
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 检验输入的密码是否正确
     */
    private void checkThePassword(String password) {
        //获得正确的密码
        String rightPassword = ((Group) mData.get(mRecentlyClickedIndex).get(GroupFragmentForStudent.KEY_OBJECT)).getPassword();
        if (rightPassword.equals(password)) { //密码输入正确
            showToast("恭喜你输入正确.");
            //打开申请加入小组对话框
            openJoinGroupDialog();
        } else {
            showToast("客官，你输入的密码有误呦！");
        }
    }

    /**
     * 打开加入小组的申请对话框
     */
    private void openJoinGroupDialog() {
        //获取点击的小组对象
        Group clickedGroup = (Group) mData.get(mRecentlyClickedIndex).get(GroupFragmentForStudent.KEY_OBJECT);
        //序列化
        String serializedClickedGroup = clickedGroup.toString();
        Intent intent = new Intent(getActivity().getApplicationContext(), DialogApplyGroup.class);
        intent.putExtra(DialogApplyGroup.GROUP_EXTRA_FLAG, serializedClickedGroup);
        //开启对话框
        startActivity(intent);
    }

    /**
     * 更新我的小组信息
     * @param num, name
     */
    private void updateMyGroupInfo(String name, int num) {
        mMyGroupMembersNumTextView.setText(String.valueOf(num) + "人");
        //设置我所在的小组的名称
        mMyGroupNameTextView.setText(name);
        //设置布局可见
        mEmptyGroupShowTextView.setVisibility(View.INVISIBLE);
        mMyGroupInfoLayout.setVisibility(View.VISIBLE);
    }
    /**
     * 自定义ViewHoder
     */
    static class ViewHolder {
        //小组名称
        public TextView mGroupName;
        //小组人数
        public TextView mGroupNumber;
        //小组是否有密码
        public ImageView mLockImg;
    }

    /**
     * 自定义Listview的适配器  并且实现过滤接口
     */
    private class MyListViewAdapter extends BaseAdapter implements Filterable{
        //过滤器
        private MyFilter mFilter;
        private LayoutInflater mInflater = LayoutInflater.from(getActivity());
        @Override
        public int getCount() {
            Log.d(TAG, "列表条数" + mData.size());
            //返回数据源的数据条数
            return mData.size();

        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * 返回列表项的视图
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {//第一次绑定视图
                viewHolder = new ViewHolder();
                //解析视图
                convertView = mInflater.inflate(R.layout.group_all_group_item, parent, false);
                //绑定ViewHolder
                viewHolder.mGroupName = (TextView) convertView.findViewById(R.id.group_list_item_name);
                viewHolder.mGroupNumber = (TextView) convertView.findViewById(R.id.group_list_item_num);
                viewHolder.mLockImg = (ImageView) convertView.findViewById(R.id.group_list_item_lock);
                //将ViewHolder保存在视图中
                convertView.setTag(viewHolder);
            } else {//已经创建过视图
                //从视图中读出ViewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Group group = (Group) mData.get(position).get(GroupFragmentForStudent.KEY_OBJECT);
            if (group.getFlag() != Group.MODE_PSW) {//公开
                viewHolder.mLockImg.setImageDrawable(getResources().getDrawable(R.drawable.open));
            } else {//有密码
                viewHolder.mLockImg.setImageDrawable(getResources().getDrawable(R.drawable.lock));
            }
            //设置小组名
            viewHolder.mGroupName.setText(group.getName());
            //设置小组人数
            viewHolder.mGroupNumber.setText(String.valueOf(group.getNum()));

            //返回视图
            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new MyFilter();
            }
            return mFilter;
        }

        /**
         * 自定义类
         */
        class MyFilter extends Filter {
            //该方法在子线程中执行
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //过滤结果
                FilterResults results = new FilterResults();
                //过滤得到的数组
                List<Map<String, Object>> newValues = new ArrayList<>();
                //过滤字符串去掉首尾空格，且小写
                String filterString = constraint.toString().trim().toLowerCase();
                if (TextUtils.isEmpty(filterString)) {//空串
                    //恢复数据
                    newValues = mSavedData;
                    Log.d(TAG, "已经恢复数据...");
                } else {
                    //过滤数据
                    for (Map<String, Object> map: mSavedData) {
                        Group group = (Group) map.get(GroupFragmentForStudent.KEY_OBJECT);
                        //小组名称
                        String name = group.getName();
                        if (-1 != name.toLowerCase().indexOf(filterString)) {//小组名中含有过滤字符串
                            //添加过滤数据
                            newValues.add(map);
                        }
                    }
                }
                //保存过滤结果
                results.values = newValues;
                results.count = newValues.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mData = (List<Map<String,Object>>) results.values;
                Log.d(TAG, "公布数据..." + mData.size());
                if (results.count > 0) {//通知数据发生了改变
                    mAdapter.notifyDataSetChanged();
                } else {//通知数据失效
                    mAdapter.notifyDataSetInvalidated();
                }
            }
        }
    }

    /**
     * 弹出Toast
     */
    public void showToast(String msg) {
        if (toast == null) {//第一次初始化toast变量
            toast = Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {//toast实例已经存在
            toast.setText(msg);
        }
        //显示toast
        toast.show();
    }

}
