package com.example.weather;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class CityList  extends AppCompatActivity {
    ListView l1;
    ListView l2;
    Cursor c1;
    Cursor c2;
    MyAdapter a1;
    MyAdapter a2;
    private DB helper;
    private SQLiteDatabase db;
    String nowProvince="";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citylist);
        helper = new DB(this);
        db=helper.getReadableDatabase();
        l1=findViewById(R.id.list1);
        l2=findViewById(R.id.list2);
        l1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                c1.moveToPosition(i);
                nowProvince=c1.getString(0);
                setC2();
            }
        });
        l2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                c2.moveToPosition(i);
                Intent intent = getIntent();
                String cityId = c2.getString(1);
                intent.putExtra("backId", cityId);
                setResult(1,intent);
                finish();
            }
        });
    }

    public void setC1(){
        c1=db.query("city",new String[]{"province"},null,null,"province",null,null);
        a1 = new MyAdapter(this, c1);
        l1.setAdapter(a1);
    }
    public void setC2(){
        Log.d("","执行c2");
        c2=db.query("city",new String[]{"city","id"},"province=?",new String[]{nowProvince},null,null,null);
        if(!c2.moveToNext()){
            Log.d("","为空");
            return;
        }
        a2 = new MyAdapter(this, c2);
        l2.setAdapter(a2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setC1();
    }
}
