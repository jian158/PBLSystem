package com.example.pblsystem.DB;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.SaveCallBackDB;

/**
 * Created by 郭聪 on 2017/3/11.
 */
public class DataBaseManager {
    private static DataBaseManager mDbManager;

    private DataBaseManager(){}

    public static DataBaseManager getInstance() {
        if (mDbManager == null) {
            mDbManager = new DataBaseManager();
        }
        return mDbManager;
    }

    /**
     * 后台保存数据
     * @param obj
     */
    public void saveInBackGround(AVObject obj, final SaveCallBackDB callback) {
        obj.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    callback.saveDoneSuccessful();
                } else {
                    callback.saveDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }

    public void saveInBackGround(AVObject obj) {
        obj.saveInBackground();
    }

    public void fetchInBackGround(AVObject obj, final FetchCallBackDB callback) {
        obj.fetchInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    callback.fetchDoneSuccessful(avObject);
                } else {
                    callback.fetchDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }

    public void fetchIfNeededInBackGround(AVObject obj, final FetchCallBackDB callback) {
        obj.fetchIfNeededInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    callback.fetchDoneSuccessful(avObject);
                } else {
                    callback.fetchDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }

    public void deleteInBackGround(AVObject obj, final DeleteCallBackDB callback) {
        obj.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    callback.deleteDoneSuccessful();
                } else {
                    callback.deleteDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }

    public void deleteInBackGround(AVObject obj) {
        obj.deleteInBackground();
    }

    public void fetchInBackGround(AVObject obj, String key, final FetchCallBackDB callback) {
        obj.fetchInBackground(key, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    callback.fetchDoneSuccessful(avObject);
                } else {
                    callback.fetchDoneFailed(e.getMessage(), e.getCode());
                }
            }
        });
    }
}
