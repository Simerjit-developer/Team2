package com.akwares.park_it.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.akwares.park_it.R;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Akshay on 5/12/2016.
 */

public class SaveLocation {

    private final String KEY_LAST_SEARCHED;
    private final String KEY_LS_DATE;
    private final String PREF_NAME;
    private SharedPreferences pref;

    public SaveLocation(Context context)
    {
        KEY_LAST_SEARCHED = context.getResources().getString(R.string.KEY_LAST_SEARCHED);
        KEY_LS_DATE = context.getResources().getString(R.string.KEY_LS_DATE);
        PREF_NAME = context.getResources().getString(R.string.locationPREF_NAME);

        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public LatLng getLastSearched(){
        String[] loc = pref.getString(KEY_LAST_SEARCHED, "").split(":");
        LatLng locR;


        if(!loc[0].equals("") && !loc[1].equals("")){
            Double lat = Double.parseDouble(loc[0]);
            Double lng = Double.parseDouble(loc[1]);

            locR = new LatLng(lat, lng);
        } else {
            locR = null;
        }

        return locR;
    }


    public void setLastSearched(LatLng loc){

        String latlong = loc.latitude+":"+loc.longitude;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = sdf.format(new Date());

        SharedPreferences.Editor editor = pref.edit();

        editor.putString(KEY_LAST_SEARCHED, latlong);
        editor.putString(KEY_LS_DATE, currentDate);

        editor.apply();
    }

    public void removeALL(){
        pref.edit().clear().commit();
    }

}
