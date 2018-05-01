package com.example.pblsystem.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by 郭聪聪 on 2017/4/30.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "pbl.db";
    public static final String TABLE_NAME_PRAISE_RECORD = "PraiseRecord";
    private static DBHelper dbHelper;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 记录表属性：用户id， 已经评论过的评论id
        String sql = "create table if not exists " + TABLE_NAME_PRAISE_RECORD + " (id integer primary key autoincrement" +
                ", speechCommentId text, ownerId text);";
        sqLiteDatabase.execSQL(sql);
        Log.d("tag", "创建数据库");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d("tag", "更新");
    }

    public static DBHelper getDbHelper(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }

        return dbHelper;
    }
}
