package com.brins.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;


/**
 * 用于引用各个实体类
 */
public class Weather {
    public String statu;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<ForeCast> foreCastList;

}
