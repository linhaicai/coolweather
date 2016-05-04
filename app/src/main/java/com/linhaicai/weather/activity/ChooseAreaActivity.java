package com.linhaicai.weather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.linhaicai.weather.R;
import com.linhaicai.weather.model.City;
import com.linhaicai.weather.model.CoolWeatherDB;
import com.linhaicai.weather.model.County;
import com.linhaicai.weather.model.Province;
import com.linhaicai.weather.util.HttpCallbackListener;
import com.linhaicai.weather.util.HttpUtil;
import com.linhaicai.weather.util.Utility;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhaicai on 2016/5/3.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView textView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    //获取存储数据库和取数据库的对象
    private CoolWeatherDB coolWeatherDB;
    //存储listview的数据
    private List<String> datalist=new ArrayList<String>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City>cityList;
    //村列表
    private List<County>countyList;
    //选中的省份
    private Province selectedProvince;

    //选中的市
    private City selectedCity;
    //选中的区
    private County selectedCounty;
    //选中的级别
    private int currentlevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean("city_selected", false)&&!getIntent().getBooleanExtra("from_weather_activity",false)) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chose_area);
        listView=(ListView) findViewById(R.id.list_view);
        textView=(TextView) findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(ChooseAreaActivity.this,android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        coolWeatherDB=CoolWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentlevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentlevel==LEVEL_CITY){
                    //选中的区等于获取的id
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if (currentlevel== LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,
                            WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        Log.i("linhaicai","--------------我是分割线--------------");
        provinceList=coolWeatherDB.loadProvinces();
        Log.i("linhaicai","--------------我是分割线1--------------");
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
                Log.i("linhaicai","---------"+province.getProvinceName()+"-----");
            }
            Log.i("linhaicai","--------------我是分割线3--------------");
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText("中国");
            currentlevel=LEVEL_PROVINCE;
        }else{
            Log.i("linhaicai","这里调用了服务的功能");
            queryFromServer(null,"province");
        }
    }
    private void queryCities(){
        cityList=coolWeatherDB.loadCities(selectedProvince.getId());
        Log.i("linhaicai","我是citylist"+cityList);
        if(cityList.size()>0){
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());
                Log.i("linhaicai","城市++++"+city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedProvince.getProvinceName());
            currentlevel=LEVEL_CITY;
        }else{
            Log.i("linhaicai","路过");
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    private void queryCounties(){
        countyList=coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            datalist.clear();
            for(County county: countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedCity.getCityName());
            currentlevel=LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }
    private void queryFromServer(final String code,final String type){
        Log.i("linhaicai","进入了服务"+code);
        String address;
        if(!TextUtils.isEmpty(code)){
            address= "http://www.weather.com.cn/data/list3/city" + code + ".xml";
            Log.i("linhaicai","这是给了城市和县的地址");
        }else{
            address="http://www.weather.com.cn/data/list3/city.xml";
            Log.i("linhaicai","这是用了重新给了地址");
        }
        showProgressDialog();
        Log.i("linhaicai","这里开始进入解析"+address);
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.i("linhaicai",response+"````````````");
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvincesResponse(coolWeatherDB,response);
                    Log.i("linhaicai","这是用了重新用JSON解析"+result+type);
                }else if("city".equals(type)){
                    result= Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                    Log.i("linhaicai","这是用了重新用JSON解析"+result+type);
                }else if("county".equals(type)){

                    result= Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                    Log.i("linhaicai","这是用了重新用JSON解析"+result+type);
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgessDialog();
                            if("province".equals(type)){
                                Log.i("linhaicai","这是用了回掉方法province");
                               queryProvinces();
                            }else if("city".equals(type)){
                                Log.i("linhaicai","这是用了回掉方法city");
                                queryCities();

                            }else if("county".equals(type)){
                                Log.i("linhaicai","这是用了回掉方法county");
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgessDialog();
                    Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                }
            });
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private  void closeProgessDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
       if(currentlevel==LEVEL_COUNTY){
           queryCities();

       }else if(currentlevel==LEVEL_CITY){
           queryProvinces();
       }else{

           finish();

       }
    }
}
