package com.brins.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;

public class ForeCast {
    @SerializedName("date")
    public String Date;
    @SerializedName("tmp")
    public Temperature temperature;
    @SerializedName("cond")
    public Cond more;

    public class Temperature {

        public String max;
        public String min;
    }

    public class Cond{
        @SerializedName("txt_d")
        public String info;
    }
}
