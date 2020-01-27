package com.akwares.park_it.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


/**
 * Created by Akshay on 04/11/2019.
 */

public class ParkedCars extends SQLiteOpenHelper {

    //Columns for table
    private static final String TABLE_NAME = "parked_cars_place";
    private static final String Id = "id";
    private static final String address = "address";
    private static final String parking_date = "parking_date";
    private static final String extra_notes = "extra_notes";
    private static final String latitude = "latitude";
    private static final String longitude = "longitude";


    public ParkedCars(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_tbl = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +" ( "
                             + Id +" int, "
                             + address + " varchar(255) NOT NULL, "
                             + extra_notes + " varchar(255), "
                             + parking_date + " timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                             + latitude + " double NOT NULL, "
                             + longitude + " double NOT NULL, "
                             + " PRIMARY KEY ("+Id+")) ";
        db.execSQL(create_tbl);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String drop_tbl = "DROP TABLE IF EXISTS "+ TABLE_NAME;

        db.execSQL(drop_tbl);
        onCreate(db);
    }

    public int getHighestId(){
        Cursor data  = getData();

        int lastId = 0;
        int i=0;

        while (data.moveToNext()){
            if(data.isLast()){
                lastId = data.getInt(0);
            }
            i++;
        }
        return lastId;
    }

    public boolean addData(String adrs, String notes, LatLng location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(address, adrs);
        values.put(extra_notes, notes);
        values.put(latitude, location.latitude);
        values.put(longitude, location.longitude);
        values.put(Id, getHighestId()+1);

        long result = db.insert(TABLE_NAME, null, values);
        //-1 ---> error
        if(result == -1)
                return false;

        return true;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM "+TABLE_NAME;

        Cursor data = db.rawQuery(query, null);
        return  data;
    }

    public void deleteRow(int id){
        Log.d("dhkbvsdvjlgbhsdv", "is d = "+id);
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM "+TABLE_NAME+" WHERE "+Id+" = "+id;

        db.execSQL(query);
    }

    public void truncateTable(){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM "+TABLE_NAME;

        db.execSQL(query);
    }

}
