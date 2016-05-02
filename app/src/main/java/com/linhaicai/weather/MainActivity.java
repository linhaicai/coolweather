package com.linhaicai.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    TextView text;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text=(TextView) findViewById(R.id.textview);
        btn=(Button) findViewById(R.id.btn1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetWeather("http://www.weather.com.cn/data/cityinfo/101190404.html");
                GetWeather("http://www.weather.com.cn/data/cityinfo/101281903.html");
            }
        });
    }
    public void GetWeather(final String adress){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try {
                    URL url=new URL(adress);
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in=connection.getInputStream();
                    BufferedReader read=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;
                    while((line=read.readLine())!=null){
                        response.append(line);
                    }
                    Log.i("linhaicai","+++++++++++++++++"+response.toString());
                JsonParsing(response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void JsonParsing(String jsonData){

        try {


                JSONObject jsonObject=new JSONObject(jsonData);
            String weatherinfo=jsonObject.getString("weatherinfo");
            Log.i("linhaicai","++++++++"+weatherinfo);
            JSONObject js=new JSONObject(weatherinfo);

                  String city=js.getString("city");
                String temp1=js.getString("temp1");
                String temp2=js.getString("temp2");
                String weather=js.getString("weather");
                String ptime=js.getString("ptime");
                Log.i("linhaicai",city+"地区\n天气:"+weather+"\n最高气温："+temp1+"摄氏度\n最低气温；"+temp2+"摄氏度\n最近更新时间："+ptime);
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {

        }

    }
}
