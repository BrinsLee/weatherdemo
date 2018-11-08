package com.brins.weatherdemo.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.brins.weatherdemo.gson.Weather;
import com.brins.weatherdemo.util.HttpUtil;
import com.brins.weatherdemo.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ServiceRequest extends Service {
    private String url;
    private String weatherid;
    private MyBinder myBinder=new MyBinder();
    public ServiceRequest() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void updateWeather(String url) {

        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("request","请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final  String responseText=response.body().string();
                if (responseText!=null){
                    SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(ServiceRequest.this).edit();
                    editor.putString("weatherupdate",responseText);
                    editor.apply();
                }
            }
        });
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        String weatherid=intent.getStringExtra("weatherid");
        url="http://guolin.tech/api/weather?cityid="
                +weatherid+"&key=34ea9718bb664420ba5b7ce53e1d160e";
        updateWeather(url);
        return myBinder;
    }

   public class MyBinder extends Binder{

       public ServiceRequest getService() {
               // 返回当前对象LocalService,这样我们就可在客户端端调用Service的公共方法了
               return ServiceRequest.this;
           }


    }
}
