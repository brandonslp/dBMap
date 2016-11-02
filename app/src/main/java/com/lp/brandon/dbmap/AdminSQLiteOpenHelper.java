package com.lp.brandon.dbmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by brand on 1/11/2016.
 */
public class AdminSQLiteOpenHelper extends SQLiteOpenHelper{

    private static String BD="mapdB";
    private static String QUERY="create table dBmap(id integer primary key autoincrement," +
                                                        "dB real, " +
                                                        "latitude text," +
                                                        "longitude text," +
                                                        "status integer);";
    private static int version = 1;

    public AdminSQLiteOpenHelper(Context context) {
        super(context, BD, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //isn't necessary
        sqLiteDatabase.execSQL(QUERY);
    }
}
