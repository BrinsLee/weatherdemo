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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brins.weatherdemo.MainActivity;
import com.brins.weatherdemo.R;
import com.brins.weatherdemo.gson.ForeCast;
import com.brins.weatherdemo.gson.Weather;
import com.brins.weatherdemo.util.HttpUtil;
import com.brins.weatherdemo.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Fragment_show extends Fragment {

    private TextView tv_temperature;
    private ImageView iv_weather;
    private TextView tv_info;
    public String weatherId;
    private TextView tv_wet;
    private TextView tv_feel;
    private TextView tv_update;
    private ImageButton bt_update;
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
         tv_wet=view.findViewById(R.id.wet);
         tv_feel=view.findViewById(R.id.feel);
         tv_update=view.findViewById(R.id.update);
         bt_update=view.findViewById(R.id.update_bt);
         bt_update.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 requestWeather(weatherId);
             }
         });
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
                +weatherId+"&key=34ea9718bb664420ba5b7ce53e1d160e";//http://guolin.tech/api/weather?cityid=CN101280101&key=34ea9718bb664420ba5b7ce53e1d160e
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null){
                            SharedPreferences.Editor editor=PreferenceManager
                                    .getDefaultSharedPreferences(getActivity()).edit();
                                    editor.putString("weather",responseText);
                                    editor.apply();
                                    if(showWeather(weather))
                                        Toast.makeText(getActivity(), "刷新完成", Toast.LENGTH_SHORT).show();;

                        }else {
                            Snackbar.make(getView(),"获取数据失败2333",Snackbar.LENGTH_SHORT).setAction("重试", new View.OnClickListener() {
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

    private Boolean showWeather(Weather weather) {

        if (tv_temperature.getText()!=null){
            tv_temperature.setText(" ");
            tv_update.setText(" ");
            tv_info.setText(" ");
        }
        String CityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime;
        String degree=weather
                .now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        String humiditytext=weather.now.humidity;
        String feel=weather.now.feel;
        String updated=weather.basic.update.updateTime.split(" ")[1];
        tv_temperature.setText(degree);
        tv_info.setText(weatherInfo);
        if (weatherInfo.equals("晴")){
            iv_weather.setImageResource(R.drawable.ic_sunny);
        }else if (weatherInfo.equals("多云")||weatherInfo.equals("阴"))
        {
            iv_weather.setImageResource(R.drawable.ic_clound);
        }else iv_weather.setImageResource(R.drawable.ic_rain);
        tv_wet.setText("相对湿度:"+humiditytext+"%");
        tv_feel.setText("体感温度:"+feel+"℃");
        tv_update.setText("  已更新 "+updated);
        MainActivity.forcast.removeAllViews();
        for (ForeCast foreCast:weather.foreCastList){
            View view=LayoutInflater.from(getActivity()).inflate(R.layout.forcast,MainActivity.forcast,false);
            TextView datetext=view.findViewById(R.id.date_text);
            TextView infotext=view.findViewById(R.id.info_text);
            TextView maxtext=view.findViewById(R.id.maxtemp_text);
            TextView mintext=view.findViewById(R.id.mintemp_text);
            datetext.setText(foreCast.Date);
            infotext.setText(foreCast.more.info);
            maxtext.setText(foreCast.temperature.max+"℃");
            mintext.setText(foreCast.temperature.min+"℃");
            MainActivity.forcast.addView(view);
        }
        return true;
    }

}
