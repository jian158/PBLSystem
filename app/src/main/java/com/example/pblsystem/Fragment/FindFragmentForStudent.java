package com.example.pblsystem.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetDataCallback;
import com.example.pblsystem.Activity.CreatePost;
import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Activity.MyPosts;
import com.example.pblsystem.Activity.Replies;
import com.example.pblsystem.Activity.ShowAllProblems;
import com.example.pblsystem.Activity.SpeechInteraction;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Posts;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by 郭聪 on 2017/3/6.
 */
public class FindFragmentForStudent extends Fragment {
    //常量，调试的Tag值
    private static final String TAG = "MainStudentActivity";
    //Toast静态常量
    private static Toast toast;
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_SHOW_APPLIES = 2;

    // 保存视图
    private View mSavedView;
    // 新建按钮
    private FloatingActionButton newPost;
    // 我的帖子
    private TextView myPostTV;
    // 列表
    private RecyclerView postsListView;
    private MyAdapter adapter;
    // 列表数据源
    private List<Posts> posts = new ArrayList<>();
    // 下拉刷新布局
    private SwipeRefreshLayout refreshLayout;

    private DataBaseManager manager = DataBaseManager.getInstance();
    private ProgressDialog mProgressBarDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//如果该Fragment还没有绑定过View
            View view = inflater.inflate(R.layout.student_find_fragment, container, false);
            //保留view
            mSavedView = view;
            initilizeProgressDialog();
            bindView();
            setAdapter();
            setRefreshLayout();
            getDataFromNet("数据加载中...");
        }
        return mSavedView;
    }

    private void setRefreshLayout() {
        // 设置刷新时颜色变化
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        // 设置刷新事件
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void setAdapter() {
        adapter = new MyAdapter();
        postsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postsListView.setAdapter(adapter);
    }

    private void getDataFromNet(String msg) {
        getMyClass(msg);
    }

    private void getMyClass(String msg) {
        showProgressDialog(msg);

        //如果是教师用户
        int authority = AVUser.getCurrentUser().getInt(MyUser.S_AUTHORITY);
        if (authority == 1) {
            if (LoginActivity.sSelectedClass == null) {
                dismissProgressDialog();
                return;
            }

            getData((ClassRoom) LoginActivity.sSelectedClass.getTargetClass());
            return;
        }

        //首先获取我所在的课堂
        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                ClassRoom myClass = (ClassRoom) obj.get(MyUser.S_CLASS);
                getData(myClass);
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                showToast(Constants.NET_ERROR_TOAST);
                dismissProgressDialog();
            }
        });
    }

    private void getData(ClassRoom myClass) {
        DataBaseQuery query = new DataBaseQuery(Posts.CLASS_NAME);
        query.addWhereEqualTo(Posts.S_CLASS, myClass);
        query.includePointer(Posts.S_OWNER);
        query.orderByDescendingDB(Posts.CREATED_AT);

        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    Posts post = (Posts) obj;
                    posts.add(post);
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
        postsListView = (RecyclerView) mSavedView.findViewById(R.id.posts);
        newPost = (FloatingActionButton) mSavedView.findViewById(R.id.create_post);
        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPost();
            }
        });
        myPostTV = (TextView) mSavedView.findViewById(R.id.my_post);
        myPostTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seeMyPosts();
            }
        });

        refreshLayout = (SwipeRefreshLayout) mSavedView.findViewById(R.id.refresh_layout);
    }

    private void seeMyPosts() {
        Intent intent = new Intent(getActivity(), MyPosts.class);
        startActivity(intent);
    }

    private void createNewPost() {
        Intent intent = new Intent(getActivity(), CreatePost.class);
        startActivityForResult(intent, REQUEST_CODE);
    }



    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView owner, likes, content, time;
        private ImageView image;
        private View saveView;

        public MyViewHolder(View itemView) {
            super(itemView);

            owner = (TextView) itemView.findViewById(R.id.owner);
            likes = (TextView) itemView.findViewById(R.id.likes);
            content = (TextView) itemView.findViewById(R.id.content);
            time = (TextView) itemView.findViewById(R.id.time);
            image = (ImageView) itemView.findViewById(R.id.image);

            saveView = itemView;

        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.post_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Posts post = posts.get(position);
            int likes = post.getLikes();
            String title = post.getTitle();
            AVUser owner = (AVUser) post.getOwner();
            Date createdAt = post.getCreatedAt();
            String createdAtStr = getTimeFromDate(createdAt);

            // 获取头像
            AVFile head = owner.getAVFile("head");
            // 后台更改头像
            setHeadImageInback(holder, head);

            holder.owner.setText(owner.getString(MyUser.S_NAME));
            holder.content.setText(title);
            holder.likes.setText("评论数:" + likes);
            holder.time.setText("发布于" + createdAtStr);

            holder.saveView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   showReplies(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }

    private void setHeadImageInback(final MyViewHolder holder, AVFile head) {
        if(head != null) {
            head.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, AVException e) {
                    if (e == null) {
                        // 根据字节流构建bitmap
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (image != null) {
                            // 设置头像
                            holder.image.setImageBitmap(image);
                        }
                    }
                }
            });
        } else {
            holder.image.setImageResource(R.drawable.user);
        }
    }

    private void showReplies(int positon) {
        Intent intent = new Intent(getActivity(), Replies.class);
        intent.putExtra(Replies.EXTRA_TAG, posts.get(positon).toString());
        startActivityForResult(intent, REQUEST_CODE_SHOW_APPLIES);
    }

    /**
     * 格式化日期
     * @param showTime
     * @return
     */
    private String getTimeFromDate(Date showTime) {
        //日期格式化
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        //定位时区
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String result = format.format(showTime);
        Log.d("tag", result);
        return result;
    }


    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(getActivity());
        mProgressBarDialog.setMessage("数据加载中...");
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

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case 1:
                    refresh();
                    break;

                case 2:
                    refresh();
                default:
                    break;
            }
        }
    }

    private void refresh() {
        posts.clear();
        adapter.notifyDataSetChanged();
        getDataFromNet("刷新中");

        //设置刷新停滞
        refreshLayout.setRefreshing(false);
    }
}
