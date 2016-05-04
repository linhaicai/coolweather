package com.linhaicai.weather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linhaicai.weather.R;
import com.linhaicai.weather.service.AutoUpdateService;
import com.linhaicai.weather.util.HttpCallbackListener;
import com.linhaicai.weather.util.HttpUtil;
import com.linhaicai.weather.util.Utility;

import org.w3c.dom.Text;

/**
 * Created by linhaicai on 2016/5/4.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;
    //显示城市名
    private TextView cityNameText;
    //显示发布时间
    private TextView publishText;
    //天气描述
    private TextView weatherDespText;
    //最高气温
    private TextView temp1;
    //最低气温
    private TextView temp2;
    //显示当前时间
    private TextView currentDateText;
    //更新天气按钮
    private Button refreshWeather;
    //切换城市按钮
    private Button switchCity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText=(TextView) findViewById(R.id.city_name);
        publishText=(TextView) findViewById(R.id.publish_text);
        weatherDespText=(TextView) findViewById(R.id.weather_desp);
        temp1=(TextView) findViewById(R.id.temp1);
        temp2=(TextView) findViewById(R.id.temp2);
        currentDateText= (TextView) findViewById(R.id.current_date);
        switchCity=(Button) findViewById(R.id.switch_city);
        refreshWeather=(Button) findViewById(R.id.refresh_weather);
        String countyCode=getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            Log.i("linhaicai","获取了值"+countyCode);
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            showWeather();
        }
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
    switch(v.getId()){
        case R.id.switch_city:
            Intent intent=new Intent(WeatherActivity.this,ChooseAreaActivity.class);
            intent.putExtra("from_weather_activity",true);
            startActivity(intent);
            finish();
            break;
        case R.id.refresh_weather:
            publishText.setText("同步中...");
            SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
            String weatherCode=prefs.getString("weather_code","");
            if(!TextUtils.isEmpty(weatherCode)){
                queryWeatherInfo(weatherCode);
            }
            break;
        default:
            break;
    }
    }
    private void queryWeatherCode(String countyCode){
        Log.i("linhaicai","到queryWeather"+countyCode);
        String address="http://www.weather.com.cn/data/list3/city" +
                countyCode + ".xml";
        queryFromServer(address,"countyCode");
    }
    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/adat/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address,"quertyweatherinfode --------"+address);
        queryFromServer(address, "weatherCode");
    }
    private void queryFromServer(final String address,final String type){
        Log.i("linhaicai","到queryFromServer"+address);
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.i("linhaicai","解析地址的返回值"+response+type);
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[] array=response.split("\\|");
                        if(array!=null&&array.length>0){
                            String weatherCode=array[1];
                            Log.i("linhaicai","再次解析"+array[1]);
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    Log.i("linhaicai","再次解析"+response);
                    Utility.handleWeatherResponse(WeatherActivity.this,
                            response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });

                }
            }

            @Override
            public void onError(Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    publishText.setText("同步失败");
                }
            });
            }
        });

    }
    //从文件中取值，直接显示天气
    private void showWeather(){
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1.setText(prefs.getString("temp1",""));
        temp2.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天" + prefs.getString("publish_time", "") +
                "发布");
        currentDateText.setText(prefs.getString("current_date",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent i=new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(i);
    }
}
