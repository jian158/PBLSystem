package com.example.pblsystem.DB;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.example.pblsystem.Interface.CountCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;

import java.util.List;

/**
 * Created by 郭聪 on 2017/3/11.
 */
public class DataBaseQuery extends AVQuery<AVObject> {
    public DataBaseQuery(String theClassName) {
        super(theClassName);
    }

    /**
     * 后台查询数据
     * @return
     */
    public void findInBackGroundDB(final FindCallBackDB callback) {
        findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    callback.findDoneSuccessful(list);
                } else {
                    callback.findDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }


    /**
     * 添加查询条件
     * @param key
     * @param value
     */
    public void addWhereEqualTo(String key, Object value) {
        whereEqualTo(key, value);
    }

    public void addNotWhereEqualTo(String key, Object value) {
        whereNotEqualTo(key, value);
    }

    /**
     * 关联查询Pointer
     * @param
     */
    public void includePointer(String key) {
        include(key);
    }

    /**
     * 结果排序
     */
    public void orderByDescendingDB(String key) {
        orderByDescending(key);
    }
    public void orderByAscendingDB(String key) {
        orderByAscending(key);
    }


    public void countInBackgroundDB(final CountCallBackDB callback) {
        countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    callback.CountDoneSuccessful(i);
                } else {
                    callback.CountDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }
}
