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
}
