package com.akwares.park_it.Utilities;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Akshay on 02/07/2019.
 */

public class CarPosition {

    LatLng pos;
    String dtPos;
    String address;
    String notes;
    int id;


    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CarPosition(LatLng pos, String address) {
        this.pos = pos;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
        dtPos = sdf.format(new Date());
        this.address = address;
        notes = "none";
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LatLng getPos() {
        return pos;
    }

    public String getDtPos() {
        return dtPos;
    }

    public void setDtPos(String dt) {
        this.dtPos = dt;
    }

    public void setPos(LatLng pos) {
        this.pos = pos;
    }
}
