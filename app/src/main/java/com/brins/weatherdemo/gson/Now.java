package com.brins.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;
public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public Cond more;

    public class Cond{
        @SerializedName("txt")
        public String info;
    }
    @SerializedName("fl")
    public String feel;
    @SerializedName("hum")
    public String humidity;

    @SerializedName("pcpn")//降水
    public String pcpn;
    @SerializedName("wind_dir")//风向
    public String wind_dir;
    @SerializedName("wind_sc")//风级
    public String wind_sc;
    @SerializedName("wind_spd")//风速km
    public String wind_speed;
    @SerializedName("pres")//气压
    public String press;

}
