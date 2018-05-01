package com.example.pblsystem.Activity;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.Problem;
import com.example.pblsystem.Class.ProblemLibrary;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.DB.DataBaseQuery;
import com.example.pblsystem.Interface.ConfirmMessage;
import com.example.pblsystem.Interface.DeleteCallBackDB;
import com.example.pblsystem.Interface.FindCallBackDB;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ManagerDataBase extends AppCompatActivity {

    private DataBaseManager manager = DataBaseManager.getInstance();
    private Myadapter myadapter;
    private List<ProblemLibrary> problemList=new ArrayList<>();
    private ListView listView;
    private Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_data_base);
        listView= (ListView) findViewById(R.id.manager_database_listview);
        myadapter=new Myadapter(ManagerDataBase.this,problemList);
        listView.setAdapter(myadapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                PopDialog.popMessageDialog(ManagerDataBase.this, "你确定要删除该课题么？", "点错了", "确认",
                        new ConfirmMessage() {
                            @Override
                            public void confirm() {
                                final ProblemLibrary problem=problemList.get(position);
                                manager.deleteInBackGround(problem, new DeleteCallBackDB() {
                                    @Override
                                    public void deleteDoneSuccessful() {
                                        problemList.remove(position);
                                        myadapter.notifyDataSetChanged();
                                        Toast.makeText(ManagerDataBase.this,"删除成功",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void deleteDoneFailed(String exceptionMsg, int errorCode) {
                                        Toast.makeText(ManagerDataBase.this,"删除失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }, null);
            }

        });

        fiterAllProblemOfMyClass();
    }

    private void fiterAllProblemOfMyClass() {
        DataBaseQuery query = new DataBaseQuery(ProblemLibrary.CLASS_NAME);
        query.findInBackGroundDB(new FindCallBackDB() {
            @Override
            public void findDoneSuccessful(List results) {

                if (results.size() < 1) {
                    return;
                }

                for (Object obj: results) {
                    ProblemLibrary problem = (ProblemLibrary) obj;
                    problemList.add(problem);
                }

                myadapter.notifyDataSetChanged();
            }

            @Override
            public void findDoneFailed(String exceptionMsg, int errorCode) {

            }
        });
    }

    private class Myadapter extends BaseAdapter{

        private List<ProblemLibrary> list;
        private Context context;
        public Myadapter(Context context,List<ProblemLibrary> list){
            this.context=context;
            this.list=list;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView==null){
                convertView=View.inflate(context, R.layout.problems_list_item,null);
                holder=new Holder(convertView);
                convertView.setTag(holder);
            }
            else holder= (Holder) convertView.getTag();

            final ProblemLibrary problem = list.get(position);

            String problemTitle = problem.getTitle();
            int problemDifficuty = problem.getDifficutity();
            Date problemShowTime = problem.getSpeakTime();
            String time = getTimeFromDate(problemShowTime);

            holder.mProblemTitleTV.setText(problemTitle);
            holder.mProblemDifficutyTV.setText(String.valueOf(problemDifficuty) + "星");
            holder.mProblemShowTimeTV.setText(time);


            return convertView;
        }



        private String getTimeFromDate(Date showTime) {
            //日期格式化
            SimpleDateFormat format = new SimpleDateFormat("MM-dd");
            //定位时区
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            String result = format.format(showTime);
            Log.d("AllProblemtag", result);
            return result;
        }

        private class Holder {
            private TextView mProblemTitleTV, mProblemDifficutyTV, mProblemShowTimeTV;
            private ImageView imageView;

            public Holder(View itemView) {
                mProblemDifficutyTV = (TextView) itemView.findViewById(R.id.apply_extra_info_tv);
                mProblemShowTimeTV = (TextView) itemView.findViewById(R.id.apply_time_info_tv);
                mProblemTitleTV = (TextView) itemView.findViewById(R.id.problem_title_tv);
            }
        }
    }
}
