package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String TAG="nb";
    TextView id;
    TextView locate;
    TextView t1;
    TextView t2;
    TextView t3;
    TextView t4;
    TextView t5;
    private DB helper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        id=(TextView) findViewById(R.id.cityid);
        locate=(TextView) findViewById(R.id.locate);
        t4=(TextView) findViewById(R.id.day);
        t5=(TextView) findViewById(R.id.time);
        t1=(TextView) findViewById(R.id.degree);
        t2=(TextView) findViewById(R.id.wet);
        t3=(TextView) findViewById(R.id.pm25);
        //
        helper = new DB(this);
        db=helper.getReadableDatabase();
        //天气sdk权限获取
        HeConfig.init("HE2112261727091613", "323ffd2bf3bb46bf85da7bc8ebb0ea35");
        HeConfig.switchToDevService();
        //读入城市csv
        //city();
    }

    public void click(View v){
        String nowId= id.getText().toString();
        if(!isCity(nowId))return;
        Cursor cursor = db.query("log", null, "id=?", new String[]{nowId}, null, null, null);
        if(cursor.moveToFirst()){
            Log.d("","读取记录");
            t4.setText(cursor.getString(1));
            t5.setText(cursor.getString(2));
            t1.setText(cursor.getString(3));
            t2.setText(cursor.getString(4));
            t3.setText(cursor.getString(5));
            cursor = db.query("city", new String[]{"id,province,city"}, "id=?", new String[]{nowId}, null, null, null);
            if(cursor.moveToFirst()){
                String province=cursor.getString(2)+"-"+cursor.getString(1);
                locate.setText(province);
            }
        }else{
            fresh(v);
            Log.d("","刷新记录");
        }

    }

    public void fresh(View v){
        String nowId= id.getText().toString();
        if(!isCity(nowId))return;
        Cursor cursor = db.query("city", new String[]{"id,province,city"}, "id=?", new String[]{nowId}, null, null, null);
        if(cursor.moveToFirst()){
                String province=cursor.getString(2)+"-"+cursor.getString(1);
                locate.setText(province);
        }

        QWeather.getWeatherNow(MainActivity.this, "CN"+nowId, Lang.ZH_HANS, Unit.METRIC, new QWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
            }
            @Override
            public void onSuccess(WeatherNowBean weatherBean) {
                Log.i(TAG, "getWeather onSuccess: " + new Gson().toJson(weatherBean));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                if (Code.OK == weatherBean.getCode()) {
                    WeatherNowBean.NowBaseBean now = weatherBean.getNow();
                    t1.setText(now.getTemp());
                    t2.setText(now.getHumidity());
                    t3.setText(now.getVis());
                    String[] day=date();
                    t4.setText(day[0]);
                    t5.setText(day[1]);
                } else {
                    //在此查看返回数据失败的原因
                    Code code = weatherBean.getCode();
                    Log.i(TAG, "failed code: " + code);
                }
            }
        });
        save();
    }

    public void city(){
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(getAssets().open("China-City-List-latest.csv"));
            BufferedReader reader = new BufferedReader(is);
            reader.readLine();//读取每行
            String line;
            while ((line = reader.readLine()) != null) {
                String temp[]=line.split(",");
                //Log.d("id",temp[0]);
                //Log.d("province",temp[2]);
                //Log.d("city",temp[7]);
                ContentValues cValue = new ContentValues();
                cValue.put("id",temp[0]);
                cValue.put("province",temp[2]);
                cValue.put("city",temp[7]);
                db.insert("city",null,cValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] date(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日-HH:mm:ss");
        Date date = new Date();
        String[] str = sdf.format(date).split("-");
        return str;
    }

    public void save(){
        String nowId= id.getText().toString();
        ContentValues cValue = new ContentValues();
        cValue.put("id",nowId);
        cValue.put("day",t4.getText().toString());
        cValue.put("time",t5.getText().toString());
        cValue.put("degree",t1.getText().toString());
        cValue.put("wet",t2.getText().toString());
        cValue.put("pm",t3.getText().toString());

        Cursor c=db.query("log", new String[]{"id"}, "id=?", new String[]{nowId}, null, null, null);
        if(c.moveToFirst()){
            db.update("log",cValue,"id="+nowId,null);
            Log.d("数据库操作：","已存在");
        }else{
            db.insert("log",null,cValue);
            Log.d("数据库操作：","未存在");
        }
    }

    public boolean isCity(String cityId){
        if(cityId.length()!=9){
            Toast.makeText(getApplicationContext(), "城市代码位数非九位", Toast.LENGTH_SHORT).show();
            return false;
        }
        Cursor cursor = db.query("city", new String[]{"id,province,city"}, "id=?", new String[]{cityId}, null, null, null);
        if(cursor.moveToFirst()){
            return true;
        }
        Toast.makeText(getApplicationContext(), "无对应代码城市", Toast.LENGTH_SHORT).show();
        return false;
    }
}
