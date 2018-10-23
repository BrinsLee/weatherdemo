package com.brins.weatherdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brins.weatherdemo.MainActivity;
import com.brins.weatherdemo.R;
import com.brins.weatherdemo.db.Cities;
import com.brins.weatherdemo.db.Countries;
import com.brins.weatherdemo.db.Provinces;
import com.brins.weatherdemo.util.HttpUtil;
import com.brins.weatherdemo.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class Choosearea extends AppCompatActivity {

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog dialog;
    @BindView(R.id.area_title)
    TextView tv_title;
    @BindView(R.id.bt_back)
    Button bt_back;
    @BindView(R.id.list)
    ListView list_area;
    private ArrayAdapter<String> arrayAdapter;
    private List<String>datalist=new ArrayList<>();
    private List<Provinces> provinces;
    private List<Cities> cities;
    private List<Countries> countries;
    private Provinces selectProvince;
    private Cities selectCity;
    private Countries selectCountry;
    private int currentLevel;
    final String ADDRESS="http://guolin.tech/api/china/";
    private String choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosearea);
        ButterKnife.bind(this);
        arrayAdapter=new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,datalist);
        list_area.setAdapter(arrayAdapter);
        list_area.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectProvince=provinces.get(i);
                    choice=list_area.getItemAtPosition(i).toString();
                    queryCities();
                }else  if (currentLevel==LEVEL_CITY){
                    selectCity=cities.get(i);
                    queryCountries();
                }
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:{
                        Intent intent=new Intent(Choosearea.this,MainActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case LEVEL_CITY:
                        queryProvinces();
                        break;
                    case  LEVEL_COUNTY:
                        queryCities();
                        break;
                }
            }

        });
        queryProvinces();

    }
    /**
     * 查询所有省级数据，优先数据库查询
     */
    private void queryProvinces() {

        tv_title.setText("中国");
        bt_back.setVisibility(View.VISIBLE);
        provinces= DataSupport.findAll(Provinces.class);
        if (provinces.size()>0){
            datalist.clear();
            for (Provinces province: provinces){
                datalist.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_area.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address=ADDRESS;
            Toast.makeText(Choosearea.this,address,Toast.LENGTH_LONG).show();
            queryFromServer(address,"province");
        }
    }

    /**
     * @param address  传入url地址
     * @param type 查询数据类型
     * 当本地数据库无数据时，从服务器查询数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(Choosearea.this,"加载失败，请检查网络",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText=response.body().string();
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvince(responseText);
                }else if ("city".equals(type)){
                    result=Utility.handleCity(responseText,selectProvince.getId());
                }else if ("country".equals(type)){
                    result=Utility.handleCountry(responseText,selectCity.getId());
                }
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示加载
     */
    private void showProgressDialog() {
        if (dialog==null){
            dialog=new ProgressDialog(Choosearea.this);
            dialog.setIcon(getResources().getDrawable(R.drawable.ic_cloud_download));
            dialog.setMessage("正在加载。。。");
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    /**
     * 查询所有区县数据，优先从数据库查询
     */
    private void queryCountries() {
        tv_title.setText(selectCity.getCityName());
        bt_back.setVisibility(View.VISIBLE);
        countries=DataSupport.where("cityId=?",String .valueOf(selectCity.getId())).find(Countries.class);
        if (countries.size()>0){
            datalist.clear();
            for (Countries country:countries){
                datalist.add(country.getCountryName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_area.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectProvince.getProvinceCode();
            int cityCode=selectCity.getCityCode();
            String address=ADDRESS+provinceCode+"/"+cityCode;
            Toast.makeText(Choosearea.this,address,Toast.LENGTH_LONG).show();
            queryFromServer(address,"country");
        }
    }

    /**
     * 查询所有城市信息，优先从数据库查询
     */
    private void queryCities() {
        tv_title.setText(selectProvince.getProvinceName());
        bt_back.setVisibility(View.VISIBLE);
        cities=DataSupport.where("provinceId=?",String .valueOf(selectProvince.getId())).find(Cities.class);
        if (cities.size()>0){
            datalist.clear();
            for (Cities city:cities){
                datalist.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_area.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectProvince.getProvinceCode();
            String address=ADDRESS+provinceCode;
            Toast.makeText(Choosearea.this,address,Toast.LENGTH_LONG).show();
            queryFromServer(address,"city");
        }

    }

}
