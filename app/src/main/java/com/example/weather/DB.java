package com.example.weather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {
    private Context mContext;

    public DB(Context context) {
        super(context, "notes", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table city(id text primary key,province text,city text)";
        db.execSQL(sql);
        sql = "create table log(id text primary key,day text,time text,degree text,wet text,pm text)";
        db.execSQL(sql);
        sql = "create table star(id integer primary key,target text,name text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
