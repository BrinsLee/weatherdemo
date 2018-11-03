package com.brins.weatherdemo;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Text;
import com.brins.weatherdemo.db.Cities;
import com.brins.weatherdemo.db.Countries;
import com.brins.weatherdemo.db.MyLocation;
import com.brins.weatherdemo.fragment.Fragment_area;
import com.brins.weatherdemo.fragment.Fragment_show;
import com.brins.weatherdemo.gson.Weather;
import com.brins.weatherdemo.util.HttpUtil;
import com.brins.weatherdemo.util.RequestFactory;
import com.brins.weatherdemo.util.Utility;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {

    private LocationClient locationClient;
    //static TextView tv;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.time1)
     TextView time1;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.airlayout)
    LinearLayout airlayout;
    List<MyLocation> myLocations;
    public static LinearLayout forcast;
    MyLocation myLocation=null;
    static String City;
    static String Province;
    static String District;
    final String BINGURL="http://guolin.tech/api/bing_pic";
    private Fragment_show fragment_show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        forcast=findViewById(R.id.forcast_layout);
        myLocation=new MyLocation();
        if (Build.VERSION.SDK_INT>=21){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//实现状态栏沉浸
        }

        //设置Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar=getSupportActionBar();
        //获取时间
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM月dd日 HH:mm");
        Date date=new Date(System.currentTimeMillis());
        time1.setText(simpleDateFormat.format(date));
        //定位方法
        initLocation();
        SharedPreferences sha=PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final String weatherid=sha.getString("weatherid",null);
        if (weatherid!=null){

            //getFragmentManager().beginTransaction().remove(fragment_show).commit();
            fragment_show=new Fragment_show(weatherid);
            getFragmentManager().beginTransaction().replace(R.id.showlayout,fragment_show).commit();
        }
        //获取请求权限
        List<String> permissionlist=new ArrayList<>();
        requestpermissions();
        //使用sharedpreferences存储必应图片
        SharedPreferences pref=getSharedPreferences("bingpic",MODE_PRIVATE);
        String bingPic=pref.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic)
                    .into(new ViewTarget<View, GlideDrawable>(appbar) {
                        //括号里为需要加载的控件
                        @Override
                        public void onResourceReady(GlideDrawable resource,
                                                    GlideAnimation<? super GlideDrawable> glideAnimation) {
                            this.view.setBackground(resource.getCurrent());
                        }
                    });
        }else {
            loadBingPic();
        }

    }

    /**
     * 从网上加载每日一图
     */
    private void loadBingPic() {

        HttpUtil.sendRequest(BINGURL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String pic_url=response.body().string();
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .edit();
                editor.putString("bing_pic",pic_url);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(pic_url)
                                .into(new ViewTarget<View, GlideDrawable>(appbar) {
                                    //括号里为需要加载的控件
                                    @Override
                                    public void onResourceReady(GlideDrawable resource,
                                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                                        this.view.setBackground(resource.getCurrent());
                                    }
                                });
                    }
                });

            }
        });
    }

    /**
     * 百度定位，每五秒刷新一次位置
     */
    private void initLocation() {
        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationClickListener());
        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(5000);
        locationClient.setLocOption(option);

    }

    private void requestpermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION
                ,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else {
            requestLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){

            case 1: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "请允许使用权限", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                requestLocation();
                break;
            }
        }
    }

    private void requestLocation() {

        locationClient.start();
    }

     class MyLocationClickListener implements BDLocationListener{


        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            StringBuilder location =new StringBuilder();
            location.append("国家：").append(bdLocation.getCountry()).append("\n");
            location.append("省份：").append(bdLocation.getProvince()).append("\n");
            location.append("城市：").append(bdLocation.getCity()).append("\n");
            location.append("区县：").append(bdLocation.getDistrict()).append("\n");
            City=bdLocation.getCity().toString().substring(0,2);
            Province=bdLocation.getProvince().toString().substring(0,2);
            Log.i("省份",Province);
            Log.i("城市",City);
            RequestFactory.queryProvinces(Province);
            District=bdLocation.getDistrict();
            myLocation.setCity(City);
            myLocation.setProvince(Province);
            myLocation.setDistrict(District);
            myLocation.save();
            requestWeatherId(City);
            toolbar.setTitle(District);
            //tv.setText(location.toString());

        }
    }

    /**
     * @param city ：传入城市，用于获取weatherid
     */
    private void requestWeatherId(String city) {
        List<Cities> cityList=DataSupport.where("cityName=?",city).find(Cities.class);
        if (cityList.size()>0){

            int cityid=cityList.get(0).getId();
            String weatherId=(DataSupport.where("cityId=?",String .valueOf(cityid)).find(Countries.class).get(0).getWeatherId());
            fragment_show=new Fragment_show(weatherId);
            getFragmentManager().beginTransaction().add(R.id.showlayout,fragment_show).commit();
            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit();
            editor.putString("weatherid",weatherId);
            editor.apply();
            air(weatherId);

        }

    }

    private void air(final String weatherId) {

        String url="http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=34ea9718bb664420ba5b7ce53e1d160e";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this,"获取数据失败。",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText=response.body().string();
                final Weather weather= Utility.handleWather(responseText);
                if (weather!=null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View v= LayoutInflater.from(MainActivity.this).inflate(R.layout.air,airlayout,false);
                            TextView tvquentity=v.findViewById(R.id.airquentity);
                            TextView tvaqi=v.findViewById(R.id.aqi);
                            TextView tvpm25=v.findViewById(R.id.pm25);
                            tvquentity.setText(weather.aqi.city.qlty);
                            tvaqi.setText("AQI指数\n"+weather.aqi.city.aqi);
                            tvpm25.setText("PM2.5\n"+weather.aqi.city.pm25);
                            airlayout.addView(v);

                        }
                    });
                }

            }
        });
    }

    private void setToolbarTitle(final String district) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(district);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbarmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_location:
            {
                //appbar.setVisibility(View.GONE);
                //getFragmentManager().beginTransaction().replace(R.id.selectarea,new Fragment_area()).commit();
                startActivity(new Intent(MainActivity.this,Choosearea.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT>=21){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);//实现状态栏沉浸
        }
    }
}
