package com.brins.weatherdemo.util;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
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


    static String ADDRESS="http://guolin.tech/api/china/";
    static int provinceid=0;
    static int cityid=0;
    //public static String weatherid;

    public static void queryProvinces(String provincename) {


        List<Provinces> provinces = DataSupport.findAll(Provinces.class);
        if (provinces.size() > 0) {
            provinces = DataSupport.select("id").where("provinceName=?", provincename).find(Provinces.class);
            queryCities(provinces.get(0).getId(),provincename);
        } else {
            String address = ADDRESS;
            queryFromServer(address, "province",provincename);
        }
    }



        /**
         * @param address  传入url地址
         * @param type 查询数据类型
         * 当本地数据库无数据时，从服务器查询数据
         */
        private static void queryFromServer(String address, final String type , final String provincename) {

            HttpUtil.sendRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }


                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseText = response.body().string();
                    boolean result = false;
                    if ("province".equals(type)) {
                        result = Utility.handleProvince(responseText);
                        provinceid=(DataSupport.where("provinceName=?",provincename).find(Provinces.class)).get(0).getId();

                    } else if ("city".equals(type)) {
                        result = Utility.handleCity(responseText,provinceid);
                    } else if ("country".equals(type)) {
                        cityid=DataSupport.where("provinceId=?",String .valueOf(provinceid)).find(Cities.class).get(0).getId();
                        result = Utility.handleCountry(responseText, cityid);
                    }
                    if (result) {

                        if ("province".equals(type)) {
                            queryProvinces(provincename);
                        } else if ("city".equals(type)) {
                            queryCities(provinceid,provincename);
                        } else if ("country".equals(type)) {
                            queryCountries(cityid,provincename);
                        }
                    }
                }

            });
        }






        /**
         * 查询所有区县数据，优先从数据库查询
         */
        private static void  queryCountries(int cityid,String provincename) {

            List<Countries> countries;
            countries=DataSupport.where("cityId=?",String .valueOf(cityid)).find(Countries.class);
            if (countries.size()>0){
                countries.get(0).getWeatherId();


            }else {
                int provinceCode=DataSupport.where("id=?",String .valueOf(provinceid)).find(Provinces.class).get(0).getProvinceCode();
                int cityCode= DataSupport.where("id=?",String .valueOf(cityid)).find(Cities.class).get(0).getCityCode();
                String address=ADDRESS+provinceCode+"/"+cityCode;
                //Toast.makeText(Choosearea.this,address,Toast.LENGTH_LONG).show();
                queryFromServer(address,"country",provincename);
            }

        }

        /**
         * 查询所有城市信息，优先从数据库查询
         */
        public static void queryCities(int provinceid, String provincename) {
            List<Cities> cities;
            cities=DataSupport.where("provinceId=?",String .valueOf(provinceid)).find(Cities.class);
            if (cities.size()>0){
                queryCountries(cities.get(0).getId(),provincename);
            }else {
                int provinceCode=DataSupport.where("id=?",String .valueOf(provinceid)).find(Provinces.class)
                        .get(0).getProvinceCode();
                String address=ADDRESS+provinceCode;
                //Toast.makeText(Choosearea.this,address,Toast.LENGTH_LONG).show();
                queryFromServer(address,"city",provincename);
            }

        }


}
