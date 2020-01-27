package com.akwares.park_it.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;


/**
 * Created by Akshay on 18/02/2019.
 */

public class SavedPlacesType {

    static private String KEY_PLACES_TYPE = "";
    static private String KEY_RADIUS = "";
    static private String PREF_NAME = "";
    static private SharedPreferences pref;
    private CommonFunctions cm;

    public SavedPlacesType(Context context)
    {
        KEY_PLACES_TYPE = context.getResources().getString(R.string.KEY_PLACES_TYPE);
        KEY_RADIUS = context.getResources().getString(R.string.KEY_RADIUS);
        PREF_NAME = context.getResources().getString(R.string.placeTypePREF_NAME);

        cm = new CommonFunctions();

        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addPlaceType(String type)
    {
        SharedPreferences.Editor editor = pref.edit();

        if(pref.getString(KEY_PLACES_TYPE, "") == ""){
            editor.putString(KEY_PLACES_TYPE, sanitizeTypePred(type));
        } else {

            if (!exists(type)){
                editor.putString(KEY_PLACES_TYPE, sanitizeTypePred(pref.getString(KEY_PLACES_TYPE, "") + ";" + type));
            }

        }

        editor.apply();
    }

    public int getRadius() {

        int rad = pref.getInt(KEY_RADIUS, 500);

        return rad;
    }

    public void setRadius(int rad)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_RADIUS, rad);

        editor.apply();
    }

    public String[] getPlacesType() {
        if(pref.getString(KEY_PLACES_TYPE, "") == ""){
            return null;
        } else {
            return pref.getString(KEY_PLACES_TYPE, "").split(";");
        }
    }

    public boolean exists(String type){

        if(type.indexOf(";") >= 0)
        {
            StringBuilder sb = new StringBuilder(type);
            sb.deleteCharAt(type.length()-1);
            type = sb.toString();
        }


        if (pref.getString(KEY_PLACES_TYPE, "").contains(type)){

            return true;
        }
        return false;
    }

    public void removeAll(){
        pref.edit().clear().commit();
    }

    public void removeType(String typ){


        SharedPreferences.Editor editor = pref.edit();
        if (exists(typ)){

            String tot = pref.getString(KEY_PLACES_TYPE, "");
            removeAll();

            tot = tot.replace(typ, "");

            tot = sanitizeTypePred(tot);

            editor.putString(KEY_PLACES_TYPE, tot);
            editor.apply();

        }

    }

    private String sanitizeTypePred(String tot)
    {
        if(tot.substring(tot.length()-1).equals(";"))
        {
            StringBuilder sb = new StringBuilder(tot);
            sb.deleteCharAt(tot.length()-1);
            tot = sb.toString();
        }

        if(tot.contains(";;")){
           tot = tot.replace(";;", ";");
        }

        if(tot.substring(0,1).equals(";")) {
            StringBuilder sb = new StringBuilder(tot);
            sb.deleteCharAt(0);
            tot = sb.toString();
        }

        return tot;
    }
}
