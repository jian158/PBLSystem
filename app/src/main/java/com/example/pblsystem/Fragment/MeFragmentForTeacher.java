package com.example.pblsystem.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.ObjectValueFilter;
import com.example.pblsystem.Activity.AutoLogin;
import com.example.pblsystem.Activity.Evalution;
import com.example.pblsystem.Activity.HistorySpeech;
import com.example.pblsystem.Activity.LoginActivity;
import com.example.pblsystem.Activity.ManagerDataBase;
import com.example.pblsystem.Activity.MyClassRoomInfo;
import com.example.pblsystem.Activity.MyClasses;
import com.example.pblsystem.Activity.MyInfo;
import com.example.pblsystem.Activity.NewTeacher;
import com.example.pblsystem.Activity.SetEvalution;
import com.example.pblsystem.Activity.SetMySpeechProgress;
import com.example.pblsystem.Activity.SubmitMyWork;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.ClassTeacher;
import com.example.pblsystem.Class.FinishListener;
import com.example.pblsystem.Class.Group;
import com.example.pblsystem.Class.MemberWork;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.Class.OnLine;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemGroup;
import com.example.pblsystem.Class.SpeechEvaluation;
import com.example.pblsystem.Class.Tag;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.Interface.InputDialogConfirm;
import com.example.pblsystem.Interface.SaveCallBackDB;
import com.example.pblsystem.PopWindow.PopWindow;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.DataBaseClear;
import com.example.pblsystem.Utils.PopDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by 郭聪聪 on 2017/3/30.
 */

public class MeFragmentForTeacher extends Fragment {
    private TextView mSpeechHistory;
    private TextView mMyClass, mExit;
    private TextView mUsername, mName;
    private RelativeLayout me;
    private View mSavedView;
    private TextView setGroupMax;
    private TextView switchClass;
    private TextView exportScore;

    private Switch switchGroupMode;
    private TextView clearDataBase;
    private TextView managerDataBase;


    private static Toast toast;
    private ProgressDialog mProgressBarDialog;

    final List<ProblemGroup> problems = new ArrayList<>();
    final List<SpeechEvaluation> evalutions = new ArrayList<>();
    final List<MemberWork> memberWorks = new ArrayList<>();
    private WritableWorkbook wwb;
    private WritableSheet sheet;

    private DataBaseManager manager = DataBaseManager.getInstance();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mSavedView == null) {//如果该Fragment还没有绑定过View
            View view = inflater.inflate(R.layout.teacher_me_fragment, container, false);
            //保留view
            mSavedView = view;
            initilizeProgressDialog();
            bindView(view);
            initializeUI();
            getTagInfoFromNet();

        }
        return mSavedView;
    }

    private void getTagInfoFromNet() {
        if (LoginActivity.sSelectedClass == null) {
            return;
        }

        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(Tag.CLASS_NAME);
        query.addWhereEqualTo(Tag.S_CLASS, classRoom);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    switchGroupMode.setChecked(true);
                } else {
                    Tag tag = (Tag) results.get(0);
                    int mode = tag.getGroupMode();
                    if (mode == 1) {
                        switchGroupMode.setChecked(true);
                    }
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }


    private void bindView(View view) {
        mSpeechHistory = (TextView) mSavedView.findViewById(R.id.speech_history);
        mSpeechHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginActivity.sSelectedClass == null) {
                    showToast("你还没创建课堂!");
                    return;
                }
                showTheSpeechHistory();
            }
        });

        setGroupMax = (TextView) mSavedView.findViewById(R.id.set_group_max);
        setGroupMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginActivity.sSelectedClass == null) {
                    showToast("你还没创建课堂!");
                    return;
                }
                setGroupMaxValue();
            }
        });

        mMyClass = (TextView) mSavedView.findViewById(R.id.my_class);
        mMyClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyClassInfo();
            }
        });

        mUsername = (TextView) mSavedView.findViewById(R.id.username);
        mName = (TextView) mSavedView.findViewById(R.id.name);

        me = (RelativeLayout) mSavedView.findViewById(R.id.me);


        mExit = (TextView) mSavedView.findViewById(R.id.exit);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitSystem();
            }
        });

        switchGroupMode = (Switch) mSavedView.findViewById(R.id.switch_group_mode);
        switchGroupMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {  //打开小组自由组合模式
                    setGroupMode(1);
                } else {//关闭小组自由组合模式
                    setGroupMode(0);
                }
            }
        });

        clearDataBase = (TextView) mSavedView.findViewById(R.id.clear_database);
        clearDataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopDialog.popWarning(getActivity(), new ConfirmMessage() {
                    @Override
                    public void confirm() {
                        startClearDataBase();
                    }
                }, null);
            }
        });

        managerDataBase= (TextView) mSavedView.findViewById(R.id.manager_database);
        managerDataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ManagerDataBase.class);
                startActivity(intent);
            }
        });

        switchClass = (TextView) mSavedView.findViewById(R.id.switch_class);
        switchClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTheClass();
            }
        });

        exportScore = (TextView) mSavedView.findViewById(R.id.export_score);
        exportScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportScoreToExcel();
            }
        });

    }

    private void exportScoreToExcel() {
        try {
            exportScoreToExcelThorwException();
        } catch (IOException | WriteException e) {
            showToast("导出分数失败："+e.toString());
        }
    }

    /**
     *  导出成绩到excel表
     */
    private void exportScoreToExcelThorwException() throws IOException, WriteException {
        problems.clear();
        evalutions.clear();
        memberWorks.clear();


        // 新建xml文件
        File file = new File(Environment.getExternalStorageDirectory() + "/score.xls");
        if (!file.exists()) {
            // 文件不存在，新建文件
            file.createNewFile();
        }

        OutputStream os = new FileOutputStream(file, false);
        wwb = Workbook.createWorkbook(os);

        // 新建一个sheet
        sheet = wwb.createSheet("分数", 0);

        // 标题行
        String[] title = new String[] {"小组", "课题", "小组成员", "贡献比", "分数"};
        Label lable;
        for (int i = 0; i < title.length; i++) {
            lable = new Label(i, 0, title[i], getHeader(Colour.YELLOW));
            sheet.addCell(lable);
        }

        // 后台生成文件
        // 先获取所有的课题小组
        showProgressDialog("数据导出中...");

        DataBaseQuery query = new DataBaseQuery(ProblemGroup.CLASS_NAME);
        query.addWhereEqualTo(ProblemGroup.S_CLASS, LoginActivity.sSelectedClass.getTargetClass());
        query.includePointer(ProblemGroup.S_PROBLEM);
        query.includePointer(ProblemGroup.S_GROUP);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    ProblemGroup problemGroup = (ProblemGroup) obj;
                    problems.add(problemGroup);
                }

                getEvalutions(0);
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
                dismissProgressDialog();
            }
        });

    }

    /**
     *  后台查询分数
     * @param
     */

    private void getEvalutions(final int i) {
        DataBaseQuery query = new DataBaseQuery(SpeechEvaluation.CLASS_NAME);
        query.includePointer(SpeechEvaluation.S_PROBLEM_GROUP);
        query.setSkip(i * 1000);
        query.setLimit(1000);   // 返回1000条数据

        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                for (Object obj: results) {
                    SpeechEvaluation evalution = (SpeechEvaluation) obj;
                    evalutions.add(evalution);
                }

                if (results.size() == 1000) {
                    Log.d("tag", "一个1000");
                    getEvalutions(i+1);
                } else {
                    // 查询成员分工表
                    DataBaseQuery query = new DataBaseQuery(MemberWork.CLASS_NAME);
                    query.setLimit(500);
                    query.includePointer(MemberWork.S_OWNER);
                    query.includePointer(MemberWork.S_PROBLEM);
                    query.findInBackGroundDB(new FindCallBackDB() {
                        @Override
                        public void findDoneSuccessful(List results) {
                            for (Object obj: results) {
                                MemberWork memberWork = (MemberWork) obj;
                                memberWorks.add(memberWork);
                            }
                            // 数据加载完毕，写入Excel文档
                            writeExcel();
                        }

                        @Override
                        public void findDoneFailed(String exceptionMsg, int errorCode) {

                        }
                    });

                }


            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    /**
     * 向Excel文件中中写入数据
     */
    private void writeExcel() {
        // 数据准备就绪， 写入xml
        try {
            insertToExcel();
        } catch (WriteException e) {
            e.printStackTrace();
            Log.d("tag", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dismissProgressDialog();
        }
    }


    private void insertToExcel() throws WriteException, IOException {
        for (int i = 0; i < problems.size(); i++) {
            ProblemGroup problemGroup = problems.get(i);
            String title = getProblemTitle(problemGroup);
            String groupName = getGroupName(problemGroup);

            Label label;
            label = new Label(0, i+1, groupName);
            // 后台加载小组成员
            addGroupMembers(problemGroup, i + 1);
            sheet.addCell(label);
            label = new Label(1, i+1, title);
            sheet.addCell(label);

            List<SpeechEvaluation> matchEvalutions = getMatchEvalution(problems.get(i));
            Log.d("tag", "测试:" + matchEvalutions.size());

            for (int j = 0; j < matchEvalutions.size(); j++) {
                insertOneCell(i, j, matchEvalutions);
            }
        }

        /**
         * 这部分代码仅供调试时使用
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 等待10s
                    Thread.sleep(10000);
                    wwb.write();
                    wwb.close();
                    Log.d("tag", "数据流关闭");

                    // 回到主线程，弹窗提示
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PopDialog.popConfirmDialog(getActivity(), "score.xls文件已经生成，位于手机内部存储根目录下");
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Log.d("tag", "数据写入成功");
        dismissProgressDialog();
        showToast("导出成功");
    }

    /**
     * 后台添加小组成员
     */
    private void addGroupMembers(final ProblemGroup problemGroup, final int line) {
        // 获取小组
        Group group = (Group) problemGroup.getGroup();
        // 关联查询小组成员
        AVRelation<AVObject> relation = group.getRelation(Group.S_MEMBER);
        AVQuery<AVObject> query = relation.getQuery();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    // 最终写入单元格的小组成员字符串
                    String finalMemberStr = "";
                    // 最终写入单元格的贡献比例字符串
                    String finalPortionStr = "";
                    for (AVObject obj : list) {
                        // 强制转换成User类型
                        AVUser user = (AVUser) obj;
                        // 获取成员姓名、学号
                        String name = user.getString(MyUser.S_NAME);
                        String username = user.getUsername();
                        finalMemberStr = finalMemberStr + username + ":" + name + " ";

                        // 获取贡献比例
                        int portion = portition(user, problemGroup);
                        finalPortionStr = finalPortionStr + portion + ":";

                    }

                    // 写入小组成员单元格
                    Label groupMember = new Label(2, line, finalMemberStr);
                    try {
                        sheet.addCell(groupMember);
                    } catch (WriteException e1) {
                        e1.printStackTrace();
                    }

                    // 将贡献比例写入单元格
                    Label cell = new Label(3, line, finalPortionStr);
                    try {
                        sheet.addCell(cell);
                    } catch (WriteException e1) {
                        e1.printStackTrace();
                    }


                    Log.d("tag", "写入一个小组成功");
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取某个小组成员的贡献比
     * @param user
     * @param problemGroup
     * @return
     */
    private int portition(AVUser user, ProblemGroup problemGroup) {
        // 初始化贡献比例为0
        int portion = 0;
        // 遍历所有的分工记录，找到目标成员的贡献比
        for (MemberWork memberWork: memberWorks) {
            ProblemGroup problem = (ProblemGroup) memberWork.getProblem();
            AVUser owner = (AVUser) memberWork.getOwner();
            // 如果匹配成功
            if (user.getObjectId().equals(owner.getObjectId()) && problem.getObjectId().equals(problemGroup.getObjectId())) {
                portion = memberWork.getProportion();
                break;
            }
        }

        return portion;
    }

    private String getProblemTitle(ProblemGroup problemGroup) {
        Problem problem = (Problem) problemGroup.getProblem();
        String problemTitle = problem.getTitle();
        return problemTitle;
    }

    private String getGroupName(ProblemGroup problemGroup) {
        Group group = (Group) problemGroup.getGroup();
        String groupName = group.getName();
        return groupName;
    }

    private List<SpeechEvaluation> getMatchEvalution(ProblemGroup problemGroup) {
        List<SpeechEvaluation> matchEvalutions = new ArrayList<>();
        for (int i = 0; i < evalutions.size(); i++) {
            ProblemGroup ofProblemGroup = (ProblemGroup) evalutions.get(i).getProblemGroup();
            if (problemGroup.getObjectId().equals(ofProblemGroup.getObjectId())) {

                matchEvalutions.add(evalutions.get(i));
            }
        }

        return matchEvalutions;
    }

    private void insertOneCell(int i, int j, List<SpeechEvaluation> matchEvalutons) throws WriteException {
        SpeechEvaluation evalution = matchEvalutons.get(j);

        Colour cellColor = Colour.BLACK;
        if (evalution.getInt("report") == 1) {
            cellColor = Colour.RED;
        }
        if (evalution.getInt("flag") == 1) {
            cellColor = Colour.BLUE;
        }

        Label label = new Label(4+j, i+1, String.valueOf(evalution.getsScore()), getHeader(cellColor));
        sheet.addCell(label);
    }

    public static WritableCellFormat getHeader(Colour color) {
        WritableFont font = new WritableFont(WritableFont.TIMES, 10,
                WritableFont.BOLD);// 定义字体
        try {
            font.setColour(Colour.DARK_YELLOW);// 蓝色字体
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
            format.setBorder(Border.ALL, BorderLineStyle.THIN,
                    Colour.BLACK);// 黑色边框
            format.setBackground(color);// 黄色背景
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }


    /**
     * 弹出对话框，选择一个班级
     */
    private void selectTheClass() {
        DataBaseQuery query = new DataBaseQuery(ClassTeacher.CLASS_NAME);
        query.whereEqualTo(ClassTeacher.S_TEACHER, AVUser.getCurrentUser());
        query.includePointer(ClassTeacher.S_CLASS);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() > 1) {
                    popClassesSelectDialog(results);
                } else if (results.size() == 1) {
                    showToast("你只有一个课堂，无需切换课堂");
                }

            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);

                if (errorCode == 101) { //班级尚未创建
                } else {
                    showToast(Constants.NET_ERROR_TOAST);
                }
            }
        });
    }

    /**
     * 弹出对话框
     */
    private void popClassesSelectDialog(final List results) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        //解析器
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        //获取对话框view
        View view = inflater.inflate(R.layout.dialog_select_class, null, false);
        //绑定组件
        ListView classesListView = (ListView) view.findViewById(R.id.dialog_class_list_view);
        ImageView dismissBtn = (ImageView) view.findViewById(R.id.dialog_cancel_imageview);

        List<String> classesData = new ArrayList<>();
        for (Object obj: results) {
            ClassTeacher classTeacher = (ClassTeacher) obj;
            ClassRoom myclass = (ClassRoom) classTeacher.getTargetClass();
            classesData.add(myclass.getMyClassName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, classesData);

        classesListView.setAdapter(adapter);

        classesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateCurrentClassIfNeed((ClassTeacher)results.get(position));
                Intent intent = new Intent(getActivity(), AutoLogin.class);
                startActivity(intent);
            }
        });

        dialog.setView(view);
        //获得dialog，用于销毁
        final AlertDialog realDialog = dialog.show();;
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realDialog.dismiss();
            }
        });

    }

    /**
     * 更新当前教师用户的class
     */
    private void updateCurrentClassIfNeed(ClassTeacher newClassTeacher) {
        LoginActivity.sSelectedClass = newClassTeacher;
        //更新登陆信息
        SharedPreferences sp = getActivity().
                getSharedPreferences(LoginActivity.SAVED_USER_INFO, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        //edit.remove("classTeacher");
        edit.putString("classTeacher", LoginActivity.sSelectedClass.toString());
        edit.commit();
    }

    private void startClearDataBase() {
        showProgressDialog("数据库清理中...\n" +
                "这不需要很长的时间");
        DataBaseClear clear = DataBaseClear.getInstance(getActivity(), new FinishListener() {
            @Override
            public void finish() {
                dismissProgressDialog();
                Log.d("tag", "对话框关闭...");
                showToast("数据库清理完成");
            }
        });
        clear.startClear();
    }


    private void setGroupMode(final int mode) {
        if (LoginActivity.sSelectedClass == null) {
            showToast("你还没创建课堂!");
            return;
        }

        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        DataBaseQuery query = new DataBaseQuery(Tag.CLASS_NAME);
        query.addWhereEqualTo(Tag.S_CLASS, classRoom);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    createGroupTag(mode);
                } else {
                    updateGroupTag(results.get(0), mode);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });

    }

    private void createGroupTag(final int mode) {
        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        Tag tag = new Tag();
        tag.setOfClass(classRoom);
        tag.setGroupMode(mode);
        manager.saveInBackGround(tag, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                if (mode == 0) {
                    showToast("已关闭");
                } else {
                    showToast("已开启");
                }
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    private void updateGroupTag(Object obj, final int mode) {
        Tag tag = (Tag) obj;
        tag.setGroupMode(mode);
        manager.saveInBackGround(tag, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                if (mode == 0) {
                    showToast("已关闭");
                } else {
                    showToast("已开启");
                }
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }


    private void setGroupMaxValue() {
        PopDialog.popInputDialog("请输入小组人数上限值", "确认", getActivity(), new InputDialogConfirm() {
            @Override
            public int confirm(String inputMsg) {
                if (TextUtils.isEmpty(inputMsg)) {
                    showToast("请先输入数值！");
                    return -1;
                } else if (!isNumeric(inputMsg)) {
                    showToast("请输入纯数字！");
                    return -1;
                }

                updateTag(inputMsg);
                return 0;
            }
        }, null);
    }

    private void updateTag(final String inputMsg) {
        DataBaseQuery query = new DataBaseQuery(Tag.CLASS_NAME);
        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        query.addWhereEqualTo(Tag.S_CLASS, classRoom);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 0) {
                    createNewTag(inputMsg);
                } else {
                    updateTag(results, inputMsg);
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    private void createNewTag(String inputMsg) {
        ClassRoom classRoom = (ClassRoom) LoginActivity.sSelectedClass.getTargetClass();
        Tag tag = new Tag();
        tag.setOfClass(classRoom);
        tag.setMaxGroupNum(Integer.valueOf(inputMsg));
        manager.saveInBackGround(tag, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("设置成功!");
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });

    }

    private void updateTag(List results, String inputMsg) {
        Tag tag = (Tag) results.get(0);
        tag.setMaxGroupNum(Integer.valueOf(inputMsg));
        manager.saveInBackGround(tag, new SaveCallBackDB() {
            @Override
            public void saveDoneSuccessful() {
                showToast("设置成功!");
            }

            @Override
            public void saveDoneFailed(String exceptionMsg, int errorCode) {
                Log.d("tag", exceptionMsg);
            }
        });
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private void exitSystem() {
        SharedPreferences sp = getActivity().getSharedPreferences(LoginActivity.SAVED_USER_INFO,
                getActivity().MODE_PRIVATE);
        LoginActivity.sSelectedClass = null;    //很关键
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
        clearOnlineInfo();
        enterLoginPage();   //回到登录界面
        getActivity().finish();
    }

    /**
     * 清除登录系统信息
     */
    private void clearOnlineInfo() {
        DataBaseQuery query = new DataBaseQuery(OnLine.CLASS_NAME);
        query.addWhereEqualTo(OnLine.S_USERNAME, AVUser.getCurrentUser().getUsername());
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {
                if (results.size() == 1) {
                    DataBaseManager manager = DataBaseManager.getInstance();
                    manager.deleteInBackGround((AVObject) results.get(0));
                }
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {

            }
        });
    }

    private void enterLoginPage() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }



    private void showTheSpeechHistory() {
        Intent intent = new Intent(getActivity(), SetEvalution.class);
        startActivity(intent);
    }


    private void showMyClassInfo() {
        Intent intent = new Intent(getActivity(), MyClasses.class);
        startActivity(intent);
    }


    private void initializeUI() {
        AVUser user = AVUser.getCurrentUser();
        String username = user.getUsername();
        String name = user.getString(MyUser.S_NAME);

        mUsername.setText(username);
        mName.setText(name);
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

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(getActivity());
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    private void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }
}
