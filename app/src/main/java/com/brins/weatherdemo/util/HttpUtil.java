package com.brins.weatherdemo.util;

import java.lang.ref.SoftReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

     public static void sendRequest(String address,okhttp3.Callback callback){

        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);

    }
}
