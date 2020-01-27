package com.akwares.park_it.Utilities;

import android.content.Context;

import com.akwares.park_it.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Akshay on 6/02/2019.
 */

public class PlaceInternal {

    String id;
    String name;
    LatLng location;
    String type;
    Boolean isSaved;
    String address;
    Marker marker;

    int VALUE;

    CommonFunctions cm;

    /** LEGENDA **

     1--> airport
     2--> museum
     3--> post_office
     4--> church
     5--> deparment_store
     6--> hospital
     7--> parking
     8--> school
     9--> stadium
     10--> train_station
     11--> shopping_mall

    ****/

    public PlaceInternal(String id, String name, LatLng location, String type, String address, Marker marker) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.address = address;
        this.marker = marker;

        VALUE = 0;

        cm = new CommonFunctions();

        this.type = type;
    }

    public int getValue(Context ctx){

        if(isSaved){
            VALUE = 20;
        } else {
            if (getType().equals(ctx.getString(R.string.parking))){
                VALUE = 15;
            } else {
                if(cm.isOpen(ctx, getType()) == null){ //sempre apert
                    VALUE = 10;
                } else {
                    if(cm.isOpen(ctx, getType()) == false){ //al momento chiusi
                        VALUE = 5;
                    } else {
                        VALUE = 3; //almomento aperti
                    }
                }
            }
        }

        return VALUE;

    }

    public Boolean getSaved() {
        return isSaved;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setSaved(Boolean saved) {
        isSaved = saved;
    }

    public void setMarker(Marker mr) {
        marker = mr;
    }

    public int getTypeResource(Context ctx) {


        int toRet = R.drawable.savedparkingopt;

        if(type.equals(ctx.getString(R.string.shopping_mall))){
            toRet = R.drawable.shop;
        } else if(type.equals(ctx.getString(R.string.museum))){
            toRet = R.drawable.museum;
        } else if(type.equals(ctx.getString(R.string.post_office))){
            toRet = R.drawable.postoffice;
        } else if(type.equals(ctx.getString(R.string.church))){
            toRet = R.drawable.church;
        } else if(type.equals(ctx.getString(R.string.department_store))){
            toRet = R.drawable.shop;
        } else if(type.equals(ctx.getString(R.string.hospital))){
            toRet = R.drawable.hospital;
        } else if(type.equals(ctx.getString(R.string.parking))){
            toRet = R.drawable.parking;
        } else if(type.equals(ctx.getString(R.string.school))){
            toRet = R.drawable.school;
        } else if(type.equals(ctx.getString(R.string.stadium))){
            toRet = R.drawable.stadium;
        } else if(type.equals(ctx.getString(R.string.train_station))){
            toRet = R.drawable.station;
        } else if(type.equals(ctx.getString(R.string.airport))){
            toRet = R.drawable.airport;
        }

        return toRet;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getAdress() {
        return address;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

}
