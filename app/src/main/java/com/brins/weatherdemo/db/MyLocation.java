package com.brins.weatherdemo.db;

import org.litepal.crud.DataSupport;

public class MyLocation extends DataSupport {

    //private int id;
    private String Province;
    private String City;
    private String District;


    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getDistrict() {
        return District;
    }

    public void setDistrict(String district) {
        District = district;
    }
}
