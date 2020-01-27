package com.akwares.park_it.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.akwares.park_it.R;


/**
 * Created by Akshay on 5/12/2016.
 */

public class SaveUser {

    private final String KEY_USERNAME;
    private final String KEY_PASSWORD;
    private final String KEY_EMAIL;
    private final String KEY_NAME;
    private final String KEY_MARK_LOG_IN;
    private final String PREF_NAME;
    private SharedPreferences pref;

    public SaveUser(Context context)
    {
        KEY_USERNAME = context.getResources().getString(R.string.KEY_USERNAME);
        KEY_EMAIL = context.getResources().getString(R.string.KEY_EMAIL);
        KEY_NAME = context.getResources().getString(R.string.KEY_NAME);
        KEY_PASSWORD = context.getResources().getString(R.string.KEY_PASSWORD);
        KEY_MARK_LOG_IN = context.getResources().getString(R.string.KEY_MARK_LOG_IN);
        PREF_NAME = context.getResources().getString(R.string.userPREF_NAME);

        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setUser(String usrnm, String pass, String em, String nm)
    {
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(KEY_USERNAME, usrnm);
        editor.putString(KEY_PASSWORD, pass);
        editor.putString(KEY_EMAIL, em);
        editor.putString(KEY_NAME, nm);

        editor.apply();
    }

    public void setName(String name)
    {
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(KEY_NAME, name);

        editor.apply();
    }

    public void setMarkLogin(boolean mark)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_MARK_LOG_IN, mark);

        editor.apply();
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public Boolean getMarkLogin() {
        return pref.getBoolean(KEY_MARK_LOG_IN, false);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getPassword() {
        return pref.getString(KEY_PASSWORD, "");
    }

    public String getName() {
        return pref.getString(KEY_NAME, "");
    }

    public boolean isLoggedIn(){

        if(pref.getString(KEY_USERNAME, "").toString().length() == 0)
        {
            return false;
        }
        return true;
    }

    public boolean logOut(){
        boolean now = true;

        pref.edit().clear().commit();

        now  = isLoggedIn();
        return !now;
    }
}
