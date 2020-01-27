package com.akwares.park_it.Preferences;

import android.content.Context;
import android.database.Cursor;

import com.akwares.park_it.SQLite.ParkedCars;
import com.akwares.park_it.Utilities.CarPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Akshay on 02/07/2019.
 */

public class SaveMyCarPos {

    Context ctx;
    ParkedCars dbH;

    public SaveMyCarPos(Context context)
    {
        ctx = context;
        dbH = new ParkedCars(ctx);
    }

    public void addNewCarPosition(CarPosition pos){

        dbH.addData(pos.getAddress(), pos.getNotes(), new LatLng(pos.getPos().latitude, pos.getPos().longitude));

    }

    public ArrayList<CarPosition> getAllCarPositions(){
        Cursor data = dbH.getData();
        ArrayList<CarPosition> ary = null;
        CarPosition tmp;

        int flag = 0;

        while (data.moveToNext()){

            if(flag == 0){
                flag = 1;
                ary = new ArrayList<>();
            }

            tmp = new CarPosition(new LatLng(data.getDouble(4), data.getDouble(5)), data.getString(1));
            tmp.setDtPos(data.getString(3));
            tmp.setNotes(data.getString(2));
            tmp.setId(data.getInt(0));
            ary.add(tmp);
        }

        return ary;
    }

    public void removeCarPosition(int car_pos_id) {
        dbH.deleteRow(car_pos_id);
    }

    public void removeALL(){
        dbH.truncateTable();
    }

}
