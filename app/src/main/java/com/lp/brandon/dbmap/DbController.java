package com.lp.brandon.dbmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brand on 1/11/2016.
 */
public class DbController {
    private Context context;
    private AdminSQLiteOpenHelper openHelper;
    private ContentValues values;
    private SQLiteDatabase  database;

    public DbController(Context context) {
        this.context = context;
        openHelper = new AdminSQLiteOpenHelper(context);
    }

    public boolean insert(double dB, String latitude, String longitude){
        values=new ContentValues();
        values.put("dB",String.valueOf(dB));
        values.put("latitude",latitude);
        values.put("longitude",longitude);
        values.put("status","0");
        database = openHelper.getWritableDatabase();
        if(database.insert("dBmap",null,values)>0){
            database.close();
            return true;
        }
        database.close();
        return false;
    }

    public List<MarkerdBEntity> getAll(){
        database = openHelper.getReadableDatabase();
        Cursor row = database.rawQuery("select * from dBmap",null);
        if (row.moveToFirst()){
            List<MarkerdBEntity> list = new ArrayList<>();
            do {
                list.add(new MarkerdBEntity(Double.parseDouble(row.getString(1)),
                                            row.getString(2),
                                            row.getString(3),
                                            row.getString(4).equalsIgnoreCase("1")));
            }while (row.moveToNext());
            return list;
        }
        return null;
    }

    public List<MarkerdBEntity> getAllNoSend(){
        database = openHelper.getReadableDatabase();
        Cursor row = database.rawQuery("select * from dBmap where status=0",null);
        if (row.moveToFirst()){
            List<MarkerdBEntity> list = new ArrayList<>();
            do {
                list.add(new MarkerdBEntity(Double.parseDouble(row.getString(1)),
                        row.getString(2),
                        row.getString(3),
                        Boolean.valueOf(row.getString(4))));
            }while (row.moveToNext());
            return list;
        }
        return null;
    }

    public void changeStatus(){
        values = new ContentValues();
        values.put("status",1);
        database = openHelper.getWritableDatabase();
        Log.v("Brandon-lp","Filas afectadas en el sync->" +database.update("dBmap", values,"status=0",null));
        database.close();
    }
}
