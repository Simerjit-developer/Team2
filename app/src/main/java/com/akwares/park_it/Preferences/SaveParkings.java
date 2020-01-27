package com.akwares.park_it.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.akwares.park_it.R;

/**
 * Created by Akshay on 18/02/2019.
 */

public class SaveParkings {

    private final String KEY_PARKINGS_IDS;
    private final String PREF_NAME;
    private SharedPreferences pref;

    public SaveParkings(Context context)
    {
        KEY_PARKINGS_IDS = context.getResources().getString(R.string.KEY_PARKINGS_IDS);
        PREF_NAME = context.getResources().getString(R.string.parkingPREF_NAME);

        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addParking(String id, String type)
    {
        SharedPreferences.Editor editor = pref.edit();

        if(pref.getString(KEY_PARKINGS_IDS, "") == ""){
            editor.putString(KEY_PARKINGS_IDS, id + ":" + type);
            editor.apply();
        } else {

            if (!exists(id, type)){
                editor.putString(KEY_PARKINGS_IDS, pref.getString(KEY_PARKINGS_IDS, "") + ";" + id + ":" + type);
                editor.apply();
            }
        }
    }

    public String[] getSavedParkings() {
        if(pref.getString(KEY_PARKINGS_IDS, "") == ""){
            return null;
        } else {
            return pref.getString(KEY_PARKINGS_IDS, "").split(";");
        }
    }

    public boolean exists(String id, String type){

        if (pref.getString(KEY_PARKINGS_IDS, "").contains(id + ":" + type)){
            return true;
        }
        return false;
    }

    public void removeAll(){
        pref.edit().clear().commit();
    }

    public void removeParking(String pid, String typ){
        SharedPreferences.Editor editor = pref.edit();

        if (exists(pid, typ)){

            String tot = pref.getString(KEY_PARKINGS_IDS, "");
            removeAll();

            if(exists(pid, typ+";"))//non è l utimo elemento
            {
                tot.replace(pid+typ+";", "");
            } else {
                tot.replace(pid+typ, "");
            }

            editor.putString(KEY_PARKINGS_IDS, tot);
            editor.apply();
        }

    }
}
