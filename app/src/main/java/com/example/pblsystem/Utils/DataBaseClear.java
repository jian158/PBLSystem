package com.example.pblsystem.Utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.example.pblsystem.Class.ApplyJoinGroup;
import com.example.pblsystem.Class.FinishListener;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MemberWork;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.ProblemApplyTable;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.ProblemRevokeTable;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;

import java.util.List;

/**
 * Created by 郭聪聪 on 2017/4/2.
 */

public class DataBaseClear {
    private static DataBaseClear clear;
    private Activity context;
    private FinishListener callback;

    private int counter = 0;

    private DataBaseManager manager = DataBaseManager.getInstance();

    private DataBaseClear(Activity context, FinishListener callback) {
        this.context = context;
        this.callback = callback;
    }

    public static final DataBaseClear getInstance(Activity context, FinishListener callback) {
        if (clear == null) {
            clear = new DataBaseClear(context, callback);
        }
        return clear;
    }

    /**
     * 开始清理
     */
    public void startClear() {
        Thread clearGroupThread = new Thread(new ClearGroup());
        clearGroupThread.start();

        Thread clearApplyJoinGroup = new Thread(new ClearApplyJoinGroup());
        clearApplyJoinGroup.start();

        Thread clearMemberWork = new Thread(new ClearMemberWork());
        clearMemberWork.start();

        Thread clearProblemApplyTable = new Thread(new ClearProblemApplyTable());
        clearProblemApplyTable.start();

        Thread clearProblemGroup = new Thread(new ClearProblemGroup());
        clearProblemGroup.start();

        Thread clearProblemRevokeTable = new Thread(new ClearProblemRevokeTable());
        clearProblemRevokeTable.start();

        Thread clearEvalution = new Thread(new ClearSpeechEvalution());
        clearEvalution.start();
    }


    /**
     * 清理Group表的线程
     */
    private class ClearGroup implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(Group.CLASS_NAME);
            query.includePointer(Group.S_LEADER);
            query.includePointer(Group.S_CLASS);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteGroup(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });

        }
    }

    private void checkIfDeleteGroup(List results) {
        for (Object obj: results) {
            Group group = (Group) obj;
            AVObject classroom = group.getOfClass();
            AVObject leader = group.getLeader();

            if (classroom == null || leader == null) {
                manager.deleteInBackGround(group);
            }
        }

        checkIfConplete();
    }

    private void checkIfConplete() {
        synchronized (this) {
            counter ++;
            Log.d("tag", "完成数目" + counter);
            if (counter == 7) {
                counter = 0;
                callback.finish();
            }
        }
    }

    /**
     * 清理ApplyJoinGroup表的线程
     */
    private class ClearApplyJoinGroup implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(ApplyJoinGroup.CLASS_NAME);
            query.includePointer(ApplyJoinGroup.S_APPLY_USER);
            query.includePointer(ApplyJoinGroup.S_TARGET_GROUP);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteApplyJoinGroup(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });
        }
    }

    private void checkIfDeleteApplyJoinGroup(List results) {
        for (Object obj: results) {
            ApplyJoinGroup apply = (ApplyJoinGroup) obj;
            AVObject applyer = apply.getApplyUser();
            AVObject targetGroup = apply.getTargetGroup();

            if (applyer == null || targetGroup == null) {
                manager.deleteInBackGround(apply);
            }
        }

        checkIfConplete();
    }



    /**
     * 清理MemberWork表的线程
     */
    private class ClearMemberWork implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(MemberWork.CLASS_NAME);
            query.includePointer(MemberWork.S_PROBLEM);
            query.includePointer(MemberWork.S_OWNER);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteMemberWork(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });
        }
    }

    private void checkIfDeleteMemberWork(List results) {
        for (Object obj: results) {
            MemberWork work = (MemberWork) obj;
            AVObject owner = work.getOwner();
            AVObject prblemGroup = work.getProblem();

            if (owner == null || prblemGroup == null) {
                manager.deleteInBackGround(work);
            }
        }

        checkIfConplete();
    }

    /**
     * 清理ProblemApplyTable表的线程
     */
    private class ClearProblemApplyTable implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(ProblemApplyTable.CLASS_NAME);
            query.includePointer(ProblemApplyTable.S_PROBLEM);
            query.includePointer(ProblemApplyTable.S_GROUP);
            query.includePointer(ProblemApplyTable.S_CLASS);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteProblemApply(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });
        }
    }

    private void checkIfDeleteProblemApply(List results) {
        for (Object obj: results) {
            ProblemApplyTable apply = (ProblemApplyTable) obj;
            AVObject problem = apply.getProblem();
            AVObject group = apply.getGroup();
            AVObject classRoom = apply.getOfClass();

            if (problem == null || group == null || classRoom == null) {
                manager.deleteInBackGround(apply);
            }
        }

        checkIfConplete();
    }

    /**
     * 清理ProblemGroup表的线程
     */
    private class ClearProblemGroup implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
            query.includePointer(ProblemGroup.S_PROBLEM);
            query.includePointer(ProblemGroup.S_GROUP);
            query.includePointer(ProblemGroup.S_CLASS);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteProblemGroup(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });
        }
    }

    private void checkIfDeleteProblemGroup(List results) {
        for (Object obj: results) {
            ProblemGroup apply = (ProblemGroup) obj;
            AVObject problem = apply.getProblem();
            AVObject group = apply.getGroup();
            AVObject classRoom = apply.getOfClass();

            if (problem == null || group == null || classRoom == null) {
                manager.deleteInBackGround(apply);
            }
        }

        checkIfConplete();
    }

    /**
     * 清理ProblemRevokeTable表的线程
     */
    private class ClearProblemRevokeTable implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(ProblemRevokeTable.CLASS_NAME);
            query.includePointer(ProblemRevokeTable.S_PROBLEM);
            query.includePointer(ProblemRevokeTable.S_GROUP);
            query.includePointer(ProblemRevokeTable.S_CLASS);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteProblemRevokeApply(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });
        }

    }

    private void checkIfDeleteProblemRevokeApply(List results) {
        for (Object obj: results) {
            ProblemRevokeTable apply = (ProblemRevokeTable) obj;
            AVObject problem = apply.getProblem();
            AVObject group = apply.getGroup();
            AVObject classRoom = apply.getOfClass();

            if (problem == null || group == null || classRoom == null) {
                manager.deleteInBackGround(apply);
            }
        }

        checkIfConplete();
    }

    /**
     * 清理SpeechEvalution表的线程
     */
    private class ClearSpeechEvalution implements Runnable {
        @Override
        public void run() {
            DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
            query.includePointer(SpeechEvaluation.S_PROBLEM_GROUP);
            query.findInBackGroundDB(new FindCallBackDB() {
                @Override
                public void findDoneSuccessful(List results) {
                    checkIfDeleteSpeechEvalution(results);
                }

                @Override
                public void findDoneFailed(String exceptionMsg, int errorCode) {
                    Log.d("tag", exceptionMsg);
                }
            });
        }


    }

    private void checkIfDeleteSpeechEvalution(List results) {
        for (Object obj: results) {
            SpeechEvaluation evalution = (SpeechEvaluation) obj;
            AVObject problemGroup = evalution.getProblemGroup();

            if (problemGroup == null) {
                manager.deleteInBackGround(evalution);
            }
        }

        checkIfConplete();
    }

}
