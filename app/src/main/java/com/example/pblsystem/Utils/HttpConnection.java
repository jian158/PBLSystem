package com.example.pblsystem.Utils;


import android.app.Activity;
import android.util.Log;

import com.example.pblsystem.Interface.HttpError;
import com.example.pblsystem.Interface.HttpSuccess;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by 郭聪聪 on 2017/4/11.
 */

public class HttpConnection {

    public static void get(final Activity context, String url, final HttpSuccess success, final HttpError error) {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("Content-Type","application/json")
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, final IOException e) {
                // 主线程中执行回调函数
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error.error(e);
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        success.success(response);
                    }
                });
            }
        });

    }
}
