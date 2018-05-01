package com.example.pblsystem.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;
import com.example.pblsystem.Class.ClassRoom;
import com.example.pblsystem.Class.MyUser;
import com.example.pblsystem.DB.DataBaseManager;
import com.example.pblsystem.Interface.FetchCallBackDB;
import com.example.pblsystem.Interface.InputDialogConfirm;
import com.example.pblsystem.R;
import com.example.pblsystem.Utils.Constants;
import com.example.pblsystem.Utils.PopDialog;
import com.example.pblsystem.Utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MyInfo extends AppCompatActivity {
    private static Toast toast;
    public static final String TAG = "MyInfo";
    public static final int REQUEST_TAG = 1;
    public static final int REQUEST_PICK = 2;
    public static final int REQUEST_CUTTING = 3;

    private TextView usernameTV, passwordTV, questionTV, nameTV, phoneTV, classNameTV, emailTv;
    private ClassRoom mClassRoom;
    // 头像
    private ImageView headImage;

    private DataBaseManager manager = DataBaseManager.getInstance();
    private ProgressDialog mProgressBarDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        initilizeProgressDialog();
        bindView();
        getDataFromNet();
    }


    private void bindView() {
        usernameTV = (TextView) findViewById(R.id.username);
        nameTV = (TextView) findViewById(R.id.name);
        classNameTV = (TextView) findViewById(R.id.class_room);
        phoneTV = (TextView) findViewById(R.id.phone);
        headImage = (ImageView) findViewById(R.id.head);
        headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhoto();
            }
        });

        passwordTV = (TextView) findViewById(R.id.password);
        questionTV = (TextView) findViewById(R.id.question);

        phoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputPhoneNumber();
            }
        });

        passwordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyPassword();
            }
        });

        questionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSecurity();
            }
        });

        emailTv = (TextView) findViewById(R.id.email);
        emailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEmail();
            }
        });
    }

    /**
     * 从图库选择图片
     */
    private void pickPhoto() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_PICK);
    }

    private void setEmail() {
        PopDialog.popInputDialog("设置邮箱", "确定", this, new InputDialogConfirm() {
            @Override
            public int confirm(String inputMsg) {
                if (TextUtils.isEmpty(inputMsg)) {
                    Toast.makeText(MyInfo.this, "请先输入邮箱！", Toast.LENGTH_SHORT).show();
                    return -1;
                }

                saveData(inputMsg);
                return 0;
            }
        }, null);
    }

    private void saveData(String email) {
        AVUser user = AVUser.getCurrentUser();
        user.setEmail(email);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Toast.makeText(MyInfo.this, "邮箱绑定成功！", Toast.LENGTH_SHORT).show();
                } else {
                    switch (e.getCode()) {
                        case 125:
                            Toast.makeText(MyInfo.this, "绑定失败！该邮箱不合法！", Toast.LENGTH_SHORT).show();
                            break;
                        case 203:
                            Toast.makeText(MyInfo.this, "绑定失败！该邮箱已经被占用！", Toast.LENGTH_SHORT).show();
                        default:
                            Toast.makeText(MyInfo.this, "邮箱绑定失败，请检查网络是否连接！", Toast.LENGTH_SHORT).show();
                            break;
                    }

                    Log.d("tag", e.getMessage());
                }
            }
        });
    }


    private void modifyPassword() {
        Intent intent = new Intent(this, ModifyPassword.class);
        startActivity(intent);
    }

    private void inputPhoneNumber() {
        Intent intent = new Intent(this, InputPhoneNumber.class);
        startActivityForResult(intent, REQUEST_TAG);
    }

    private void addSecurity() {
        Intent intent = new Intent(this, InputPasswordSecurity.class);
        startActivity(intent);
    }




    private void getDataFromNet() {
        getMyClass();
    }

    private void getMyClass() {
        showProgressDialog("系统加载中...");

        manager.fetchInBackGround(AVUser.getCurrentUser(), MyUser.S_CLASS, new FetchCallBackDB() {
            @Override
            public void fetchDoneSuccessful(AVObject obj) {
                mClassRoom = (ClassRoom) obj.get(MyUser.S_CLASS);
                initializeUi();

                dismissProgressDialog();
            }

            @Override
            public void fetchDoneFailed(String exceptionMsg, int errorCode) {
                Log.d(TAG, exceptionMsg);

                dismissProgressDialog();
            }
        });
    }

    private void initializeUi() {
        if (mClassRoom == null) {
            dismissProgressDialog();
            return;
        }

        AVUser user = AVUser.getCurrentUser();
        String username = user.getUsername();
        String name = user.getString(MyUser.S_NAME);
        String className = mClassRoom.getMyClassName();
        String phone = user.getMobilePhoneNumber();


        usernameTV.setText(username);
        nameTV.setText(name);
        classNameTV.setText(className);

        if (phone == null) {
            phoneTV.setText("未设置");
        } else {
            phoneTV.setText(phone);
        }

        // 初始化头像
        setHeadImage();
    }


    /**
     * 设置头像
     */
    private void setHeadImage() {
//        Bitmap head = getFromLocal();
//        if (null == head) { // 本地没有图片
//            getFromNet();
//        } else {
//            headImage.setImageBitmap(head);
//            //showToast("找到本地图片");
//        }
        // 从云端获取头像
        getFromNet();
    }


//    /**
//     * 本地获取图片
//     * @return
//     */
//    private Bitmap getFromLocal() {
//        /*手机内部存储根路径*/
//        String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
//        Bitmap localImage = BitmapFactory.decodeFile(externalStorageDirectory + "/head.jpg");
//
//        return localImage;
//    }

    /**
     * 从云端下载图片
     */
    private void getFromNet() {
        AVFile file = AVUser.getCurrentUser().getAVFile("head");
        if (file == null) { // 如果当前用户尚且没有设置头像
            return;
        }
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, AVException e) {
                if (e == null) {
                    // 根据字节流构建bitmap
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    // 将文件保存到本地
                    //saveLocal(image);

                    // 重新设置头像
                    // Bitmap head = getFromLocal();
                    if (image != null) {
                        headImage.setImageBitmap(image);
                    }


                }
            }
        });
    }

    /**
     * 初始化进度条对话框
     */
    private void initilizeProgressDialog() {
        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("系统君正在拼命加载数据.");
    }

    public void showProgressDialog(String msg) {
        mProgressBarDialog.setMessage(msg);
        mProgressBarDialog.show();
    }

    public void dismissProgressDialog() {
        mProgressBarDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        //接收手机号返回
        if (requestCode == REQUEST_TAG) {
            String phoneNumber = data.getStringExtra("phone_number");
            if (phoneNumber != null) {
                phoneTV.setText(phoneNumber);
            }
        }

        // 选择图片返回
        if (requestCode == REQUEST_PICK) {
            // 裁剪图片
            startPhotoZoom(data.getData());
        }

        // 处理裁剪后的图片
        if (requestCode == REQUEST_CUTTING) {
            setPicToView(data);
        }
    }

    /**
     * 裁剪头像后，保存到本地，并设置头像
     * @param pickingData
     */
    private void setPicToView(Intent pickingData) {
        Bundle extras = pickingData.getExtras();
        if (extras != null) {
            /*获取剪切后的bitmap图片*/
            Bitmap photo = extras.getParcelable("data");
            headImage.setImageBitmap(photo);
            // 将裁剪后的头像上传到云端
            saveToCloud(photo);
            // 将裁剪后的头像保存到本地
            // saveLocal(photo);
        }
    }

//    private void saveLocal(Bitmap photo) {
//        /*手机内部存储根路径*/
//        String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
//        /*新建文件*/
//        File imageFile = new File(externalStorageDirectory + "/head.jpg");
//        if (!imageFile.exists()) {
//            try {
//                imageFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        FileOutputStream fout = null;
//        /*打开文件输入流，将图片数据写入文件*/
//        try {
//            fout = new FileOutputStream(imageFile);
//            photo.compress(Bitmap.CompressFormat.PNG, 100, fout);
//
//            fout.flush();
//            fout.close();
//            Log.d("tag", "文件保存成功");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Log.d("tag", "文件找不到");
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d("tag", "IO异常");
//        }
//    }

    /**
     * 将文件保存到云端
     */
    private void saveToCloud(Bitmap photo) {
        // 将bitmap构建字节流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();
        // 上传文件到云端

        showProgressDialog("头像上传中...");

        final AVFile file = new AVFile("head.jpg", datas);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    showToast("头像已经上传到云端");
                    // 更新用户表中我的头像字段
                    udpateMyHeadImage(file);
                }

                dismissProgressDialog();
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                showProgressDialog("进度：" + integer + "%");
            }
        });

    }

    private void udpateMyHeadImage(AVFile file) {
        AVUser user = AVUser.getCurrentUser();
        user.put("head", file);
        // 后台保存
        user.saveInBackground();
    }

    /**
     * 开始图片裁剪
     * @param uri
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        /*设置裁剪*/
        intent.putExtra("crop", "true");

        /*设置宽高的比例*/
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        /*设置宽和高*/
        intent.putExtra("outputX", Util.dp2Px(60, this));
        intent.putExtra("outputY", Util.dp2Px(60, this));

        /*设置圆形裁剪*/
        intent.putExtra("circleCrop", "true");

        /*返回数据*/
        intent.putExtra("return-data", true);

        startActivityForResult(intent, REQUEST_CUTTING);
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


}
