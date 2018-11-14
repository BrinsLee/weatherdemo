package com.brins.weatherdemo.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brins.weatherdemo.Choosearea;
import com.brins.weatherdemo.MainActivity;
import com.brins.weatherdemo.db.Cities;
import com.brins.weatherdemo.db.Countries;
import com.brins.weatherdemo.db.Provinces;

import org.litepal.crud.DataSupport;

import com.brins.weatherdemo.Choosearea;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 用于查询省市县数据的工具类
 */
public class RequestFactory {

    static final String ADDRESS="http://guolin.tech/api/china/";
    //public static String weatherid;

    public static int queryProvinces(final String provincename) {


        List<Provinces> provincesList = DataSupport.findAll(Provinces.class);
        if (provincesList.size() > 0) {
            int provinceid=(DataSupport.where("provinceName=?",provincename).find(Provinces.class)).get(0).getProvinceCode();
            Log.i("provinceid_if",provinceid+"");
            return provinceid;
        } else {
            String address = ADDRESS;
            final int[] provinceid = new int[1];
           HttpUtil.sendRequest(address, new Callback() {

               @Override
               public void onFailure(Call call, IOException e) {

                   Log.i("error",e.toString());
               }

               @Override
               public void onResponse(Call call, Response response) throws IOException {

                   String responseText = response.body().string();
                   Log.i("response",responseText);
                   Utility.handleProvince(responseText);
                   provinceid[0] = (DataSupport.where("provinceName=?", provincename).find(Provinces.class)).get(0).getProvinceCode();
                   Log.i("provinceid1", (DataSupport.where("provinceName=?", provincename).find(Provinces.class)).get(0).getProvinceCode() + "");
                   return;
               }
           });
            Log.i("provinceid_else", provinceid[0] +"");
            return provinceid[0];
        }
    }



        /**
         * 查询所有区县数据，优先从数据库查询
         */
        public static String   queryCountries(final int citycode, final String cityname, int provincecode) {

            List<Countries> countries;
            countries=DataSupport.where("cityId=?",String .valueOf(citycode)).find(Countries.class);
            if (countries.size()>0){
                String weatherId=countries.get(0).getWeatherId();
                Log.i("weatherId_if",weatherId);
                return weatherId;

            }else {
                String address = ADDRESS + provincecode+"/"+citycode;
                final String[] weatherId = new String[1];
                HttpUtil.sendRequest(address, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("error",e.toString());

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseText = response.body().string();
                        Utility.handleCountry(responseText,citycode);
                            weatherId[0] = (DataSupport.where("countryName=?", cityname).find(Countries.class)).get(0).getWeatherId();

                    }
                });
                Log.i("weatherId_else", weatherId[0]);
                return weatherId[0];
            }
            }



        /**
         * 查询所有城市信息，优先从数据库查询
         */
        public static int queryCities(final int provinceid, final String cityname) {
            if (provinceid == 0) {return 0;}
                List<Cities> cities;
                cities = DataSupport.where("provinceId=?", String.valueOf(provinceid)).find(Cities.class);
                if (cities.size() > 0) {
                    int cityid = cities.get(0).getCityCode();
                    Log.i("cityid_if", cityid + "");
                    return cityid;


                } else {
                    String address = ADDRESS + provinceid;
                    final int[] cityid = new int[1];
                    HttpUtil.sendRequest(address, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i("error", e.toString());

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String responseText = response.body().string();
                            Utility.handleCity(responseText, provinceid);
                            cityid[0] = (DataSupport.where("cityName=?", cityname).find(Cities.class)).get(0).getCityCode();
                        }
                    });
                    Log.i("cityid_else", cityid[0] + "");
                    return cityid[0];

                }

            }

}
