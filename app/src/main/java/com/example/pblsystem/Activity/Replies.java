package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Posts;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Fragment.FindFragmentForStudent;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.InputDialogConfirm;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class Replies extends AppCompatActivity {
    //常量，调试的Tag值
    private static final String TAG = "Replies";
    //Toast静态常量
    private static Toast toast;
    public static final String EXTRA_TAG = "post";
    public static final String EXTRA_TAG_FLAG = "flag";
    private ProgressDialog mProgressBarDialog;

    private RecyclerView repliesListView;
    private List<com.example.pblsystem.Class.Replies> replies = new ArrayList<>();
    private MyAdapter adapter;
    private TextView titleTV, contentTV, ownerTV;
    private ImageView imageHead;
    private Posts post;
    private EditText commentET;
    private TextView sendBtn;
    private RelativeLayout ownerLayout;
    private SwipeRefreshLayout refreshLayout;

    private int flag = 0;   // 表示是从哪个activity跳转来的，1表示MyPost， 0表示AllPost

    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies);

        try {
            initilizeProgressDialog();
            getDataFromIntent();
            bindView();
            iniilizeUi();
            setAdapter();
            getDataFromNet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDataFromNet() {
        showProgressDialog("数据加载中...");

        DataBaseQuery query = new DataBaseQuery(com.example.pblsystem.Class.Replies.S_CLASS_NAME);
        query.addWhereEqualTo(com.example.pblsystem.Class.Replies.S_OF_POST, post);
        query.includePointer(com.example.pblsystem.Class.Replies.S_OWNER);
        query.includePointer(com.example.pblsystem.Class.Replies.S_TARGET);
        query.includePointer("target.owner");

        query.orderByDescendingDB(com.example.pblsystem.Class.Replies.CREATED_AT);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    com.example.pblsystem.Class.Replies reply = (com.example.pblsystem.Class.Replies) obj;
                    replies.add(reply);
                }

                adapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void setAdapter() {

        adapter = new MyAdapter();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        repliesListView.setLayoutManager(layoutManager);
        repliesListView.setAdapter(adapter);

//        repliesListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            int lastFirstPositon = -1;
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                Log.d("tag", "dy:" + dy);
//
//                //Log.d("tag", "位置" + lastFirstPositon);
//
//                if (layoutManager.findFirstVisibleItemPosition() == 0 && lastFirstPositon != 1) {    // 顶部
//                    Log.d("tag", "上滑" + ownerLayout.getVisibility());
//                    if (ownerLayout.getVisibility() != View.VISIBLE) {
//                        ownerLayout.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                // 初始化上一次显示的一个列表项
//                lastFirstPositon = layoutManager.findFirstVisibleItemPosition();
//
//                if (dy > 0 && dy < 50){ // 下滑
//                    if (ownerLayout.getVisibility() != View.GONE) { // 如果控件当前不处于隐藏状态
//                        ownerLayout.setVisibility(View.GONE);
//                    }
//                }
//            }
//        });
    }

    private void iniilizeUi() {
        titleTV.setText(post.getTitle());
        contentTV.setText(post.getContent());
        ownerTV.setText(post.getOwner().getString(MyUser.S_NAME));


        // 获取头像
        AVFile head = post.getOwner().getAVFile("head");
        // 后台更改头像
        setHeadImageInback(imageHead, head);
    }

    private void getDataFromIntent() throws Exception {
        Intent intent = getIntent();
        String str = intent.getStringExtra(EXTRA_TAG);
        post = (Posts) AVObject.parseAVObject(str);
        flag = intent.getIntExtra(EXTRA_TAG_FLAG, 0);   //如果没有该key值，则默认为0
    }

    private void bindView() {
        repliesListView = (RecyclerView) findViewById(R.id.replies);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light

        );
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                refreshLayout.setRefreshing(false);
            }
        });
//        repliesListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 0) {   // 下滑
//                    replyLayout.setVisibility(View.GONE);
//                    showToast("下滑");
//                } else if (dy < 0) {    // 上滑
//                    replyLayout.setVisibility(View.VISIBLE);
//                    showToast("上滑");
//                }
//            }
//        });


        titleTV = (TextView) findViewById(R.id.title);
        contentTV = (TextView) findViewById(R.id.content);
        ownerTV = (TextView) findViewById(R.id.owner);
        imageHead = (ImageView) findViewById(R.id.image);
        ownerLayout = (RelativeLayout) findViewById(R.id.post_layout);

        commentET = (EditText) findViewById(R.id.comment);
        sendBtn = (TextView) findViewById(R.id.send);

        if (flag == 1) {
            commentET.setVisibility(View.GONE);   //隐藏回复UI
            sendBtn.setVisibility(View.GONE);
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputMsg = commentET.getText().toString();
                if (!TextUtils.isEmpty(inputMsg)) {
                    submit(inputMsg);
                } else {
                    showToast("请先输入评论内容");
                }

            }
        });
    }

    /**
     * 刷新
     */
    private void refresh() {
        replies.clear();
        adapter.notifyDataSetChanged();

        // 重新从网络下载数据
        getDataFromNet();
    }

    private void submit(String content) {
        final com.example.pblsystem.Class.Replies reply = new com.example.pblsystem.Class.Replies();
        reply.setOwner(AVUser.getCurrentUser());
        reply.setContent(content);
        reply.setOfPost(post);

        showProgressDialog("回复中...");

        manager.saveInBackGround(reply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("回复成功");

                dismissProgressDialog();

                // 更新回复数
                updatePostLikes();
                // 更新回复列表
                udpateReplyList(reply);
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });

    }

    private void udpateReplyList(com.example.pblsystem.Class.Replies reply) {
        replies.add(0, reply);
        adapter.notifyDataSetChanged();
    }

    private void updatePostLikes() {
        manager.fetchInBackGround(post, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                //原子更新
                post.increment(Posts.S_LIKES);
                post.setFetchWhenSave(true);
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            setResult(RESULT_OK);   //成功更新
                        }
                    }
                });
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView owner, target, content, time;
        private ImageView image;
        private View saveView;

        public MyViewHolder(View itemView) {
            super(itemView);

            owner = (TextView) itemView.findViewById(R.id.owner);
            target = (TextView) itemView.findViewById(R.id.target);
            content = (TextView) itemView.findViewById(R.id.content);
            time = (TextView) itemView.findViewById(R.id.time);
            image = (ImageView) itemView.findViewById(R.id.image);

            saveView = itemView;

        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplication());
            View view = inflater.inflate(R.layout.reply_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            com.example.pblsystem.Class.Replies reply = replies.get(position);
            com.example.pblsystem.Class.Replies target = (com.example.pblsystem.Class.Replies) reply.getTarget();

            String content = reply.getContent();
            AVUser owner = (AVUser) reply.getOwner();
            final String ownerStr = owner.getString(MyUser.S_NAME);
            Date createAt = reply.getCreatedAt();

            // 获取头像
            AVFile head = owner.getAVFile("head");
            // 后台更改头像
            setHeadImageInback(holder, head);

            holder.owner.setText(ownerStr);
            holder.content.setText(content);
            holder.time.setText("发表于" + getTimeFromDate(createAt));
            if (target != null) {
                holder.target.setVisibility(View.VISIBLE);
                String targetStr = target.getContent();
                holder.target.setText("@" + target.getOwner().getString(MyUser.S_NAME) + ": " + targetStr);
            } else {
                holder.target.setVisibility(View.GONE);
                Log.d("tag", "target为空" + position);
            }

            holder.saveView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (flag == 1)
                        return;

                    replyOthers(ownerStr, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return replies.size();
        }
    }

    private void setHeadImageInback(final MyViewHolder holder, AVFile head) {
        setHeadImageInback(holder.image, head);
    }

    private void setHeadImageInback(final ImageView view, AVFile head) {
        if(head != null) {
            head.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, AVException e) {
                    if (e == null) {
                        // 根据字节流构建bitmap
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (image != null) {
                            // 设置头像
                            view.setImageBitmap(image);
                        }
                    }
                }
            });
        } else {
            view.setImageResource(R.drawable.user);
        }
    }

    private void replyOthers(String ownerStr, final int position) {
        PopDialog.popInputDialog("回复:" + ownerStr, "回复", this, new InputDialogConfirm() {
            @Override
            public int confirm(String inputMsg) {
                if (TextUtils.isEmpty(inputMsg)) {
                    showToast("请先输入回复内容");
                    return -1;
                }

                submitReply(inputMsg, position);
                return 0;
            }
        }, null);
    }

    private void submitReply(String content, int position) {
        final com.example.pblsystem.Class.Replies reply = new com.example.pblsystem.Class.Replies();
        reply.setOwner(AVUser.getCurrentUser());
        reply.setContent(content);
        reply.setOfPost(post);
        reply.setTarget(replies.get(position));

        showProgressDialog("回复中...");

        manager.saveInBackGround(reply, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("回复成功");

                dismissProgressDialog();

                // 更新回复数
                updatePostLikes();
                // 更新回复列表
                udpateReplyList(reply);
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });
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
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("数据加载中...");
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

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }
}
