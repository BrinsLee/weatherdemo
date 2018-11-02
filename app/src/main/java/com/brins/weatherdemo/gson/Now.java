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

    @SerializedName("pcpn")
    public String pcpn;
    @SerializedName("wind_dir")
    public String wind_dir;
    @SerializedName("wind_sc")
    public String wind_sc;
}
