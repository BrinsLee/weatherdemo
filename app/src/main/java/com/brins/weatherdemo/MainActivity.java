package com.brins.weatherdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.brins.weatherdemo.MyAdapter.DataBean;
import com.brins.weatherdemo.MyAdapter.RecyclerViewAdapter;
import com.brins.weatherdemo.db.MyLocation;
import com.brins.weatherdemo.db.Provinces;
import com.brins.weatherdemo.fragment.Fragment_show;
import com.brins.weatherdemo.gson.Weather;
import com.brins.weatherdemo.services.ServiceRequest;
import com.brins.weatherdemo.util.HttpUtil;
import com.brins.weatherdemo.util.RequestFactory;
import com.brins.weatherdemo.util.Utility;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import org.litepal.crud.DataSupport;

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
    private ServiceRequest serviceRequest;
    private ServiceRequest.MyBinder myBinder;
    private boolean mBound=false;
    private static Intent startService;
    private static ServiceConnection connection;
    private int imageId[]={R.drawable.ic_pcpo,R.drawable.ic_press,R.drawable.ic_wind};
    private String title[]={"降雨量/mm","气压/pa"," "};
    private String data[];
    List<DataBean> dataBeanList=new ArrayList<>();
    private RecyclerViewAdapter adapter;
    @BindView(R.id.recycle)
    RecyclerView recyclerView;
    private int ProvinceId;
    private int CityId;
    private boolean isFirst;
    final String ADDRESS="http://guolin.tech/api/china/";

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
//定位方法
        initLocation();
        //设置Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar actionBar=getSupportActionBar();
        //获取时间
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM月dd日 HH:mm");
        Date date=new Date(System.currentTimeMillis());
        time1.setText(simpleDateFormat.format(date));
         connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                myBinder=(ServiceRequest.MyBinder)iBinder;
                serviceRequest=myBinder.getService();
                mBound=true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Toast.makeText(MainActivity.this,"请求数据失败请手动刷新。",Toast.LENGTH_SHORT).show();
                mBound=false;
            }
        };

        SharedPreferences sha=PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final String weatherid=sha.getString("weatherid",null);
        if (weatherid!=null){
            StartService(weatherid);
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
//启动服务
    public void StartService(String weatherid) {

        Log.i("done","已调用StartService()");
        startService=new Intent(MainActivity.this,ServiceRequest.class);
        startService.putExtra("weatherid",weatherid);
        bindService(startService,connection, Context.BIND_AUTO_CREATE);
        String responseText=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("weatherupdate",null);
        android.app.FragmentTransaction fragmentTransaction= getFragmentManager().beginTransaction();
        fragment_show=new Fragment_show(responseText);
        getFragmentManager().beginTransaction().replace(R.id.showlayout,fragment_show).commit();
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
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClient.setLocOption(option);

    }

    private void requestpermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[] {
                Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION},1);
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
                        return;
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

            City=bdLocation.getCity().toString().substring(0,2);
            Log.i("city",City);
            Province=bdLocation.getProvince().toString().substring(0,2);
            District=bdLocation.getDistrict();
            ProvinceId=RequestFactory.queryProvinces(Province);
            List<Provinces> provincesList = DataSupport.findAll(Provinces.class);
            CityId=RequestFactory.queryCities(ProvinceId,City);
            myLocation.setCity(City);
            myLocation.setProvince(Province);
            myLocation.setDistrict(District);
            myLocation.save();
            requestWeatherId(CityId,City,ProvinceId);
            toolbar.setTitle(District);

        }
    }

    private void requestWeatherId(int citycode, final String cityname,int provincecode) {
        Log.i("done","已调用requestWeatherId()");

        if (citycode!=0){
            Log.i("citycode",citycode+"");
            String weatherId=RequestFactory.queryCountries(citycode,cityname,provincecode);
            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit();
            editor.putString("weatherid",weatherId);
            editor.apply();
            editor.commit();
            Log.i("weatherid",weatherId);
            air(weatherId);

            StartService(weatherId);


        }else {
            Log.i("error","citycode=0");
        }

    }

    private void air(final String weatherId) {

        Log.i("done","已调用air()");
        String url="http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=5030dcb8ea054ddfae3467918610e56b";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
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
                            title[2]=weather.now.wind_dir;
                            data=new String[]{weather.now.pcpn,weather.now.press,weather.now.wind_sc+"级"};
                            for (int i=0;i<title.length;i++){
                                DataBean dataBean=new DataBean(imageId[i],title[i],data[i]);
                                dataBeanList.add(dataBean);

                            }
                            adapter=new RecyclerViewAdapter(MainActivity.this,dataBeanList);
                            LinearLayoutManager linearLayout=new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(linearLayout);
                            recyclerView.setAdapter(adapter);
                            Log.i("location","完成一次定位");



                        }
                    });
                }

            }
        });
    }//准备精简掉

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
        unbindService(connection);
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
