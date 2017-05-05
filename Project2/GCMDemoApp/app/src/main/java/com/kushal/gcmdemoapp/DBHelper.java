package com.kushal.gcmdemoapp;

/**
 * Created by KUSHAL on 03-May-17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Events";
    // Contacts table name
    private static final String TABLE = "broadcasts";
    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LNG = "longitude";
    private static final String KEY_MSG = "message";
    private static final String KEY_STUFF= "stuff";
    private static final String KEY_HOT= "hotness";
    private static final String KEY_TIME= "time";
    Context c;
    SQLiteDatabase db;
    public DBHelper(Context context)//constructor
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //Log.d("KUSHAL","DbHelper Constructor");
        c=context;

    }

    @Override
    public void onCreate(SQLiteDatabase dbs) {
        // TODO Auto-generated method stub
        Log.d("KUSHAL","onCreateDatabase");
        db=dbs;
        String DATABASE_CREATE=new String();
        //"create table playlist1 (_id integer primary key autoincrement,name text not null,path text not null);";


        try{

            DATABASE_CREATE="create table "+TABLE+" (_id integer primary key autoincrement,latitude text,longitude text,message text,hotness text,stuff text,time text);";
            dbs.execSQL(DATABASE_CREATE);

            //Toast.makeText(c,"Database created", Toast.LENGTH_SHORT).show();
            Log.d("KUSHAL","Datatable(marker) Created");}
        catch(SQLException e){Log.d("KUSHAL","SQLEXCEPTION occured");}
    }


    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    //=========entries for table containing markers==================
    public void insertdata(String msg, String lt,String ln,String hotness, String stuff)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String time = dateFormat.format(date);
        db=this.getWritableDatabase();
        ContentValues val=new ContentValues();
        val.put(KEY_LAT, lt);
        val.put(KEY_LNG, ln);
        val.put(KEY_MSG, msg);
        val.put(KEY_STUFF, stuff);
        val.put(KEY_HOT, hotness);
        val.put(KEY_TIME,time);

        long r_id=db.insert(TABLE, null, val);
        if(r_id != -1){
            Log.d("KUSHAL","NEW entry ADDED!");
        }
    }

    public int getcount()
    {
        //Log.d("KUSHAL","GETTING COUNT of DB entries");
        db=getReadableDatabase();
        Cursor mc=db.query(TABLE, new String[] {"_id", KEY_LAT,KEY_LNG,KEY_MSG,KEY_HOT,KEY_STUFF,KEY_TIME}, null, null, null, null, null);
        return(mc.getCount());
    }

    public Cursor getdata()       //list of created data tables
    {	Cursor mc;
        db=getReadableDatabase();
        mc=db.query(TABLE, new String[] {"_id",KEY_LAT,KEY_LNG,KEY_MSG,KEY_HOT,KEY_STUFF,KEY_TIME}, null, null, null, null,KEY_TIME+" DESC");
        return mc;
    }


    public void deleteData()
    {
        db=getWritableDatabase();
        try{int r=db.delete(TABLE,null,null);}
        catch(SQLException e){Log.d("KUSHAL","SqlException occurred:Couldn't perform delete operation.");}
    }


}

