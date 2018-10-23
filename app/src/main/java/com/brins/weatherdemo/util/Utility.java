package com.brins.weatherdemo.util;

import android.support.annotation.NonNull;

import com.brins.weatherdemo.db.Cities;
import com.brins.weatherdemo.db.Countries;
import com.brins.weatherdemo.db.Provinces;
import com.brins.weatherdemo.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;

public class Utility {

    /**
     * Handle province boolean.
     *
     * @param response 解析处理服务器省份响应数据，并添加到省级表
     * @return the boolean
     */
    public static boolean handleProvince(@NonNull String response){

        if (!response.isEmpty()){

            try {
                JSONArray provincearray=new JSONArray(response);
                for (int i=0;i<provincearray.length();i++){
                    JSONObject provincesJSONObject=provincearray.getJSONObject(i);
                    Provinces provinces=new Provinces();
                    provinces.setProvinceName(provincesJSONObject.getString("name"));
                    provinces.setProvinceCode(provincesJSONObject.getInt("id"));
                    provinces.save();
                }
                return true;
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;

    }

    /**
     * Handle city boolean.
     *
     * @param response the response 解析处理服务器市级数据,并添加到市级表
     * @return the boolean
     */
    public static boolean handleCity(@NonNull String response,int provinceId){

        if (!response.isEmpty()){
            try {
                JSONArray cityarray = new JSONArray(response);
                for (int i=0;i<cityarray.length();i++){
                    JSONObject cityobject=cityarray.getJSONObject(i);
                    Cities cities=new Cities();
                    cities.setCityName(cityobject.getString("name"));
                    cities.setCityCode(cityobject.getInt("id"));
                    cities.setProvinceId(provinceId);
                    cities.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }




    /**
     * Handle country boolean.
     *
     * @param response the response 解析处理服务器县级数据,并添加到县级表
     * @return the boolean
     */
    public static boolean handleCountry(@NonNull String response,int cityId){

        if (!response.isEmpty()){
            try {
                JSONArray countryarray = new JSONArray(response);
                for (int i=0;i<countryarray.length();i++){
                    JSONObject countryobject=countryarray.getJSONObject(i);
                    Countries countries=new Countries();
                    countries.setCountryName(countryobject.getString("name"));
                    countries.setWeatherId(countryobject.getString("weather_id"));
                    countries.setCityId(cityId);
                    countries.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWather(String response){

        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
