package com.brins.weatherdemo.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brins.weatherdemo.MainActivity;
import com.brins.weatherdemo.R;
import com.brins.weatherdemo.gson.Weather;
import com.brins.weatherdemo.util.HttpUtil;
import com.brins.weatherdemo.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Fragment_show extends Fragment {

    private TextView tv_temperature;
    private ImageView iv_weather;
    private TextView tv_info;
    public String weatherId;
    //private TextView tv_wet,tv_rain,tv_pressure,tv_wind,tv_visible;


    public Fragment_show(String weatherId) {
        this.weatherId = weatherId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show,container,false);
        initConpent(view);
        return view;
    }

    private void initConpent(View view) {

         iv_weather=view.findViewById(R.id.iv_weather);
         tv_temperature=view.findViewById(R.id.tv_temp);
         tv_info=view.findViewById(R.id.info);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String weatherString=preferences.getString("weather",null);
        if (weatherString!=null){
            Weather weather= Utility.handleWather(weatherString);
            showWeather(weather);
        }else {
            //Weather weather=Utility.handleWather(MainActivity.weatherId);
            requestWeather(weatherId);
        }
    }

    private void requestWeather(final String weatherId) {
        String url="http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=34ea9718bb664420ba5b7ce53e1d160e";
        Log.i("url:",url);
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(),"获取数据失败111",Snackbar.LENGTH_SHORT).setAction("重试", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestWeather(weatherId);
                            }
                        }).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText=response.body().string();
                final Weather weather=Utility.handleWather(responseText);
                Log.i("text",responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null){
                            SharedPreferences.Editor editor=PreferenceManager
                                    .getDefaultSharedPreferences(getActivity()).edit();
                                    editor.putString("weather",responseText);
                                    editor.apply();
                                    showWeather(weather);

                        }else {
                            Snackbar.make(getView(),"获取数据失败222",Snackbar.LENGTH_SHORT).setAction("重试", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    requestWeather(weatherId);
                                }
                            }).show();
                        }
                    }
                });

            }
        });

    }

    private void showWeather(Weather weather) {

        String CityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime;
        String degree=weather
                .now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        tv_temperature.setText(degree);
        tv_info.setText(weatherInfo);
        Log.i("天气信息：",weatherInfo);
    }

    public String setweatherId(String weatherId){

        return weatherId;
    }
}
