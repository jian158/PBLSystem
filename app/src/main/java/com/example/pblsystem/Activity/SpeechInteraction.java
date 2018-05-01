package com.example.pblsystem.Activity;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.SpeechComment;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DBHelper;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
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

public class SpeechInteraction extends AppCompatActivity {
    // 常量，调试的Tag值
    private static final String TAG = "Replies";
    // tag
    public static final String EXTRA = "problemGroup";
    // Toast静态常量
    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    // 下拉刷新
    private SwipeRefreshLayout refreshLayout;
    // 列表
    private RecyclerView commentListView;
    // 数据
    private List<SpeechComment> commentList = new ArrayList<>();
    // 上一个activity传来的数据
    private ProblemGroup mProblemGroup;
    // 评论内容
    private EditText commentET;
    // 提交按钮
    private TextView submitTV;

    private MyAdapter adapter;
    private DataBaseManager manager = DataBaseManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_interaction);

        try {
            // 初始化进度对话框
            initilizeProgressDialog();
            bindView();
            getIntentData();
            configRecycleView();   // 配置RecycleView
            configRefreshLayout();  // 配置下拉刷新
            getDataFromNet("数据加载中...");
        } catch (Exception e) {
            Log.d("tag", e.getMessage());
        }
    }

    private void getIntentData() throws Exception {
        Intent intent = getIntent();
        String transferdStr = intent.getStringExtra(EXTRA);
        mProblemGroup = (ProblemGroup) AVObject.parseAVObject(transferdStr);
    }


    private void configRecycleView() {
        adapter = new MyAdapter();
        commentListView.setLayoutManager(new LinearLayoutManager(this));
        commentListView.setAdapter(adapter);
    }

    private void configRefreshLayout() {
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_light

        );

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                refreshLayout.setRefreshing(false); // 刷新停滞
            }
        });
    }

    private void refresh() {
        // TO DO refresh
        commentList.clear();
        adapter.notifyDataSetChanged();

        getDataFromNet("刷新中...");
    }


    private void getDataFromNet(String msg) {
        showProgressDialog(msg);

        DataBaseQuery query = new DataBaseQuery(SpeechComment.CLASS_NAME);
        query.addWhereEqualTo(SpeechComment.S_OF_PROBLEM_GROUP, mProblemGroup);
        query.includePointer(SpeechComment.S_OWNER);
        query.includePointer(SpeechComment.S_TARGET);
        query.includePointer("target.owner");

        query.orderByDescendingDB(SpeechComment.CREATED_AT);
        query.orderByDescendingDB(SpeechComment.S_LIKES);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    SpeechComment comment = (SpeechComment) obj;
                    commentList.add(comment);
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

    private void bindView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        commentListView = (RecyclerView) findViewById(R.id.comments);

        commentET = (EditText) findViewById(R.id.comment);
        submitTV = (TextView) findViewById(R.id.send);
        submitTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitCommnet();
            }
        });
    }

    private void submitCommnet() {
        String comment = commentET.getText().toString();
        if (TextUtils.isEmpty(comment)) {
            showToast("请先输入评论内容");
            return;
        }

        submit(comment);
    }

    private void submit(final String comment) {
        showProgressDialog("评论发表中...");

        final SpeechComment speechComment = new SpeechComment();
        speechComment.setContent(comment);
        speechComment.setOwner(AVUser.getCurrentUser());
        speechComment.setLikes(0);
        speechComment.setOfProblemGroup(mProblemGroup);
        manager.saveInBackGround(speechComment, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("发表成功");
                // 更新列表
                commentList.add(speechComment);
                adapter.notifyDataSetChanged();

                dismissProgressDialog();
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView owner, target, content, time, like;
        private ImageView image;
        private View saveView;

        public MyViewHolder(View itemView) {
            super(itemView);

            owner = (TextView) itemView.findViewById(R.id.owner);
            target = (TextView) itemView.findViewById(R.id.target);
            content = (TextView) itemView.findViewById(R.id.content);
            time = (TextView) itemView.findViewById(R.id.time);
            like = (TextView) itemView.findViewById(R.id.likes);
            image = (ImageView) itemView.findViewById(R.id.image);

            saveView = itemView;

        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplication());
            View view = inflater.inflate(R.layout.comment_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final SpeechComment comment = commentList.get(position);
            SpeechComment target = (SpeechComment) comment.getTarget();

            String content = comment.getContent();
            AVUser owner = (AVUser) comment.getOwner();
            final String ownerStr = owner.getString(MyUser.S_NAME);
            Date createAt = comment.getCreatedAt();
            // 获取头像
            AVFile head = owner.getAVFile("head");
            // 后台更改头像
            setHeadImageInback(holder, head);

            holder.owner.setText(ownerStr);
            holder.content.setText(content);
            holder.time.setText("发表于" + getTimeFromDate(createAt));
            holder.like.setText("" + comment.getLikes());

            if (target != null) {
                holder.target.setVisibility(View.VISIBLE);
                String targetStr = target.getContent();
                holder.target.setText("@" + target.getOwner().getString(MyUser.S_NAME) + ": " + targetStr);
            } else {
                holder.target.setVisibility(View.GONE);
                Log.d("tag", "target为空" + position);
            }

            // 如果没有点赞过， 显示暗色的心
            if (!haveClicked(comment.getObjectId())) {
                Drawable redLove = ContextCompat.getDrawable(SpeechInteraction.this, R.drawable.like_black);
                redLove.setBounds(0, 0, redLove.getMinimumWidth(), redLove.getMinimumHeight());
                holder.like.setCompoundDrawables(null, null, redLove, null);
            } else {
                Drawable redLove = ContextCompat.getDrawable(SpeechInteraction.this, R.drawable.like_red);
                redLove.setBounds(0, 0, redLove.getMinimumWidth(), redLove.getMinimumHeight());
                holder.like.setCompoundDrawables(null, null, redLove, null);
            }

            holder.saveView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    replyOthers(ownerStr, position);
                }
            });

            holder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (haveClicked(comment.getObjectId())) {
                        showToast("你已经点赞过了呦");
                        return;
                    }
                    // 开启一个小动画
                    ObjectAnimator animx = ObjectAnimator.ofFloat(holder.like, "scaleX", 1.0f, 1.1f, 1.0f);
                    ObjectAnimator animy = ObjectAnimator.ofFloat(holder.like, "scaleY", 1.0f, 1.1f, 1.0f);
                    animx.setDuration(500); // 1s
                    animy.setDuration(500); // 1s
                    animx.start();
                    animy.start();

                    // 变红心
                    Drawable redLove = ContextCompat.getDrawable(SpeechInteraction.this, R.drawable.like_red);
                    redLove.setBounds(0, 0, redLove.getMinimumWidth(), redLove.getMinimumHeight());
                    holder.like.setCompoundDrawables(null, null, redLove, null);

                    // 显示加1
                    int currrentLikes = comment.getLikes();
                    holder.like.setText("" + (currrentLikes+1));
                    // 更新数据库数据
                    updateLikes(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return commentList.size();
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

    private boolean haveClicked(String objId) {
        DBHelper helper = DBHelper.getDbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] COLUMS = {"id", "speechCommentId"};
        Cursor cursor = db.query(DBHelper.TABLE_NAME_PRAISE_RECORD, COLUMS,
                "speechCommentId=? and ownerId=?", new String[]{objId, AVUser.getCurrentUser().getObjectId()}, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Log.d("tag", cursor.getInt(0) + cursor.getString(1));
            }

            return true;
        } else {
            return false;
        }
    }

    private void updateLikes(int position) {
        // 点赞数 原子增加1
        final SpeechComment comment = commentList.get(position);
        comment.increment(SpeechComment.S_LIKES);
        comment.setFetchWhenSave(true);
        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //showToast("点赞成功");
                    // 将点赞记录计入数据库
                    insertRecord(comment);
                } else {
                    Log.d("tag", e.getMessage());
                }
            }
        });
    }

    private void insertRecord(SpeechComment comment) {
        DBHelper helper = DBHelper.getDbHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();  // 开启事务

        ContentValues values = new ContentValues();
        values.put("speechCommentId", comment.getObjectId());   // 评论id
        values.put("ownerId", AVUser.getCurrentUser().getObjectId());   // 用户id
        db.insertOrThrow(helper.TABLE_NAME_PRAISE_RECORD, null, values);

        db.setTransactionSuccessful();  // 插入数据成功
        db.endTransaction();    // 关闭事务
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

     void submitReply(String content, int position) {
        final SpeechComment speechComment = new SpeechComment();
        speechComment.setOfProblemGroup(mProblemGroup);
        speechComment.setLikes(0);
        speechComment.setOwner(AVUser.getCurrentUser());
        speechComment.setContent(content);
        speechComment.setTarget(commentList.get(position));

        showProgressDialog("回复中...");

        manager.saveInBackGround(speechComment, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("回复成功");

                dismissProgressDialog();

                // 更新回复列表
                commentList.add(speechComment);
                adapter.notifyDataSetChanged();
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
