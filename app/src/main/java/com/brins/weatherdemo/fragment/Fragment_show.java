package com.brins.weatherdemo.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.brins.weatherdemo.MainActivity;
import com.brins.weatherdemo.R;
import com.brins.weatherdemo.gson.ForeCast;
import com.brins.weatherdemo.gson.Weather;
import com.brins.weatherdemo.util.Utility;


public class Fragment_show extends Fragment {

    private TextView tv_temperature;
    private ImageView iv_weather;
    private TextView tv_info;
    String weatherinfo;
    private TextView tv_wet;
    private TextView tv_feel;
    //private TextView tv_wet,tv_rain,tv_pressure,tv_wind,tv_visible;
//final Weather weather= Utility.handleWather(responseText);

    public Fragment_show(String weatherinfo) {
        this.weatherinfo = weatherinfo;
        Log.i("weatherinfo1",weatherinfo);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_show,container,false);
        initConpent(view);
        if (weatherinfo!=null){
            Weather weather= Utility.handleWather(weatherinfo);
            Log.i("weatherinfo2",weatherinfo);
            showWeather(weather);
        }else {
            //Weather weather=Utility.handleWather(MainActivity.weatherId);

        }
        return view;
    }

    private void initConpent(View view) {

         iv_weather=view.findViewById(R.id.iv_weather);
         tv_temperature=view.findViewById(R.id.tv_temp);
         tv_info=view.findViewById(R.id.info);
         tv_wet=view.findViewById(R.id.wet);
         tv_feel=view.findViewById(R.id.feel);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    private Boolean showWeather(Weather weather) {

        /*if (tv_temperature.getText()!=null){
            tv_temperature.setText(" ");
            tv_update.setText(" ");
            tv_info.setText(" ");
        }*/
        String degree=weather
                .now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        String humiditytext=weather.now.humidity;
        String feel=weather.now.feel;
        //String updated=weather.basic.update.updateTime.split(" ")[1];
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
