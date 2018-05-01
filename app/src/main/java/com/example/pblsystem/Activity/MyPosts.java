package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetDataCallback;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.Posts;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Fragment.FindFragmentForStudent;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.PopDialog;

import java.util.ArrayList;
import java.util.List;

public class MyPosts extends AppCompatActivity {

    //常量，调试的Tag值
    private static final String TAG = "MyPosts";
    //Toast静态常量
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    private RecyclerView myPostsListView;   // 显示数据的Listview
    private List<Posts> myPostsList = new ArrayList<>(); // 数据源
    private MyAdapter adapter;


    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        initilizeProgressDialog();
        bindView();
        configRecycleView();
        getDataFromNet(); // 从网络获取数据
    }

    private void bindView() {
        myPostsListView = (RecyclerView) findViewById(R.id.my_posts);
    }


    private void configRecycleView() {
        adapter = new MyAdapter();
        myPostsListView.setLayoutManager(new LinearLayoutManager(this));
        myPostsListView.setAdapter(adapter);
    }

    private void getDataFromNet() {
        showProgressDialog("数据加载中...");

        DataBaseQuery query = new DataBaseQuery(Posts.CLASS_NAME);
        query.addWhereEqualTo(Posts.S_OWNER, AVUser.getCurrentUser());  // 帖子的拥有者是我
        query.orderByDescendingDB(Posts.CREATED_AT);    // 按发帖时间降序排列
        query.includePointer(Posts.S_OWNER);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    dismissProgressDialog();
                    return;
                }


                for (Object obj: results) {
                    Posts post = (Posts) obj;
                    myPostsList.add(post);
                }

                // 通知数据改变
                adapter.notifyDataSetChanged();

                // 隐藏进度条
                dismissProgressDialog();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
                dismissProgressDialog();
            }
        });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView owner, likes, content;
        private View saveView;
        private ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);

            owner = (TextView) itemView.findViewById(R.id.owner);
            likes = (TextView) itemView.findViewById(R.id.likes);
            content = (TextView) itemView.findViewById(R.id.content);
            image = (ImageView) itemView.findViewById(R.id.image);

            saveView = itemView;

        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MyPosts.this);
            View view = inflater.inflate(R.layout.post_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Posts post = myPostsList.get(position);
            int likes = post.getLikes();
            String title = post.getTitle();
            AVUser owner = (AVUser) post.getOwner();


            // 获取头像
            AVFile head = post.getOwner().getAVFile("head");
            // 后台更改头像
            setHeadImageInback(holder.image, head);

            holder.owner.setText(owner.getString(MyUser.S_NAME));
            holder.content.setText(title);
            holder.likes.setText("评论数:" + likes);

            holder.saveView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showReplies(position);
                }
            });

            holder.saveView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopDialog.popMessageDialog(MyPosts.this, "是否要删除这个帖子？", "不了", "删除",
                            new ConfirmMessage() {
                                @Override
                                public void confirm() {
                                    deletePost(position);
                                }
                            }, null);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return myPostsList.size();
        }
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

    // 删除已经发布的帖子
    private void deletePost(final int position) {
        showProgressDialog("删除中...");

        final Posts post = myPostsList.get(position);

        manager.deleteInBackGround(post, new DeleteCallBackDB() {
            @Override
            public void deleteDoneSuccessful() {
                showToast("帖子删除成功");
                // 隐藏进度条
                dismissProgressDialog();

                // 附带删除该帖子所有的评论
                deleteAllComments(post);
                // 同步更新列表数据
                updateShownList(position);
            }

            @Override
            public void deleteDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                // 隐藏进度条
                dismissProgressDialog();
            }
        });
    }

    private void updateShownList(int position) {
        // 移除删除的数据项
        myPostsList.remove(position);
        // 通知适配器
        adapter.notifyDataSetChanged();
    }

    private void deleteAllComments(Posts post) {
        DataBaseQuery query = new DataBaseQuery(com.example.pblsystem.Class.Replies.S_CLASS_NAME);
        query.addWhereEqualTo(com.example.pblsystem.Class.Replies.S_OF_POST, post);     // 找到该帖子的评论
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    com.example.pblsystem.Class.Replies reply = (com.example.pblsystem.Class.Replies) obj;
                    // 后台删除评论
                    manager.deleteInBackGround(reply);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);
            }
        });
    }

    private void showReplies(int positon) {
        Intent intent = new Intent(this, Replies.class);
        intent.putExtra(Replies.EXTRA_TAG, myPostsList.get(positon).toString());
        intent.putExtra(Replies.EXTRA_TAG_FLAG, 1); // 1 代表 “我的帖子”，取消回复功能
        startActivity(intent);
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
