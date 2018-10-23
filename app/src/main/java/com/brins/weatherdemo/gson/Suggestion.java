package com.brins.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfortable comfortable;
    public class Comfortable{
        @SerializedName("txt")
        public String comfinfo;
    }

    @SerializedName("cw")
    public CarWash carWash;
    public class CarWash{
        @SerializedName("txt")
        public String washinfo;
    }

    public Sport sport;
    public class Sport{
        @SerializedName("txt")
        public String sportinfo;
    }
}
