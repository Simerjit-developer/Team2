package com.akwares.park_it.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.akwares.park_it.R;
import com.akwares.park_it.Preferences.SaveLocation;
import com.akwares.park_it.Preferences.SaveMyCarPos;
import com.akwares.park_it.Preferences.SaveParkings;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.VolleyReq.PlaceRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Akshay on 24/10/2016.
 */


public class CommonFunctions {

    public CommonFunctions()
    {
    }

    public void disableButton(Button btn)
    {
        btn.setEnabled(false);
        btn.getBackground().setAlpha(64);
    }

    public void enableButton(Button btn)
    {
        btn.setEnabled(true);
        btn.getBackground().setAlpha(255);
    }


    public boolean isLocPermissionEnabled(Context ctx) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    public boolean isOnline(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void successSnackbar(Activity ac, String msg)
    {
        Alerter.create(ac)
                .setTitle(ac.getApplicationContext().getString(R.string.alt_success))
                .setText(msg)
                .setBackgroundColor(R.color.materialGreen)
                .setDuration(3000)
                .setIcon(R.drawable.ic_check_circle_black_24dp)
                .show();

    }

    public void infoSnackbar(Activity ac, String msg)
    {
        Alerter.create(ac)
                .setTitle(ac.getApplicationContext().getString(R.string.alt_info))
                .setText(msg)
                .setDuration(3000)
                .setBackgroundColor(R.color.materialBlue)
                .setIcon(R.drawable.ic_info_black_24dp)
                .show();
    }


    public void errorSnackbar(Activity ac, String msg)
    {
        Alerter.create(ac)
                .setTitle(ac.getApplicationContext().getString(R.string.alt_error))
                .setText(msg)
                .setDuration(3000)
                .setBackgroundColor(R.color.Orange)
                .setIcon(R.drawable.ic_error_black_24dp)
                .show();


    }

    private static String convertToHex(byte[] data) throws java.io.IOException
    {
        StringBuffer sb = new StringBuffer();
        String hex = null;

        hex= Base64.encodeToString(data, 0, data.length, Base64.DEFAULT);

        sb.append(hex);

        return sb.toString();
    }
/*
    public String Sha1(String str)
    {
        String shaStr = null;
        MessageDigest mdSha1 = null;
        try
        {
            mdSha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            try {
                mdSha1.update(str.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        byte[] data = mdSha1.digest();
        try {
            shaStr=convertToHex(data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return shaStr;
    }
*/
    public boolean AlertDialog(Context context, final String title, String msg)
    {
        final Boolean[] res = {false};

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        res[0] = true;
                    }
                })
                .show();

        return res[0];
    }


    public Boolean isOpen(Context ctx, String placeType){

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY); //0-24
        int day = c.get(Calendar.DAY_OF_WEEK);
        Boolean toRet = false;

        // FALSE = good
        // TRUE = bad
        // NULL = allways open

        if(placeType.equals(ctx.getString(R.string.shopping_mall))){
            if(hour > 8 || hour < 22)
            {
                toRet = true;
            }
        } else if(placeType.equals(ctx.getString(R.string.museum))){
            if(hour > 8 || hour < 18)
            {
                toRet = true;
            }
        } else if(placeType.equals(ctx.getString(R.string.post_office))){
            if(hour > 8 || hour < 16)
            {
                toRet = true;
            }
        } else if(placeType.equals(ctx.getString(R.string.church))){
            if(day == 7 && (hour > 8 || hour < 13))
            {
                toRet = true;
            } else {
                toRet = null;
            }
        } else if(placeType.equals(ctx.getString(R.string.department_store))){
            if(hour > 8 || hour < 22)
            {
                toRet = true;
            }
        } else if(placeType.equals(ctx.getString(R.string.hospital))){
            toRet = null;
        } else if(placeType.equals(ctx.getString(R.string.parking))){
            toRet = null;
        } else if(placeType.equals(ctx.getString(R.string.school))){
            if((day < 7 && day >= 1) && (hour > 8 || hour < 13))
            {
                toRet = true;
            } else {
                toRet = null;
            }
        } else if(placeType.equals(ctx.getString(R.string.stadium))){
            toRet = null;
        } else if(placeType.equals(ctx.getString(R.string.train_station))){
            toRet = null;
        } else if(placeType.equals(ctx.getString(R.string.airport))){
            toRet = null;
        }

        return toRet;
    }
    /*
        CHECK IN BOOKMARKS

        for place details ---> https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJN1t_tDeuEmsRUsoyG83frY4&key=[API-KEY]

        dist, size, parking, timing
        fORMULA dist (tot = ) = sqrt(pow((lng1-lng2), 2)+pow((lat1-lat2), 2)); ** result > 0 ** --> 10p
        if("types contains parking") --> +3pt
        if("current situation == close") --> +4pt

     */

    /// get places type
    public void getPlaces(final Context thisContext)
    {
        final Handler pkReqHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.obj == thisContext.getString(R.string.cod_100))
                {
                    Toast.makeText(thisContext, thisContext.getString(R.string.err_noConn), Toast.LENGTH_LONG).show();
                }else {
                    if(msg.obj == thisContext.getString(R.string.cod_1)){
                        Toast.makeText(thisContext, thisContext.getString(R.string.err_generic), Toast.LENGTH_LONG).show();
                    } else {
                        loadAllPlacesType((JSONArray)msg.obj, thisContext);
                    }
                }
                return false;
            }
        });

        Runnable r = new Runnable() {
            @Override
            public void run() {

                final Message msgtoSend = pkReqHandler.obtainMessage();

                Response.Listener<String> responseListener= new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                JSONArray allSaved = new JSONArray();
                                try {
                                    allSaved = jsonResponse.getJSONArray("results");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                msgtoSend.obj = allSaved;
                                pkReqHandler.sendMessage(msgtoSend);

                            } else {

                                msgtoSend.obj = thisContext.getString(R.string.cod_1);
                                pkReqHandler.sendMessage(msgtoSend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                // add it to the RequestQueue

                Response.ErrorListener errorListener = new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        msgtoSend.obj = thisContext.getString(R.string.cod_100);
                        pkReqHandler.sendMessage(msgtoSend);
                    }
                };


                PlaceRequest plReq_Internal = new PlaceRequest(responseListener, errorListener);
                RequestQueue qu = Volley.newRequestQueue(thisContext);
                plReq_Internal.getPlaces(new SaveUser(thisContext).getEmail());
                qu.add(plReq_Internal);
            }
        };


        Thread savedPkRetrive = new Thread(r);
        savedPkRetrive.start();
    }

    private void loadAllPlacesType(JSONArray PLACESfromWeb, Context thisContext) {
        ArrayList<String> placesType = new ArrayList<>();
        SavedPlacesType pt = new SavedPlacesType(thisContext);
        pt.removeAll();

        for (int i=0; i<PLACESfromWeb.length(); i++){
            try {
                placesType.add(PLACESfromWeb.getString(i).split(":")[0]);
                if(!PLACESfromWeb.getString(i).split(":")[1].equals("null"))
                {
                    pt.addPlaceType(PLACESfromWeb.getString(i).split(":")[0]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String decodeAndTranslate(Context ctx, String type) {

        String toRet = "";

        if(type.equals(ctx.getString(R.string.shopping_mall))){
            toRet = ctx.getString(R.string.shopping_mall_T);
        } else if(type.equals(ctx.getString(R.string.museum))){
            toRet = ctx.getString(R.string.museum_T);
        } else if(type.equals(ctx.getString(R.string.post_office))){
            toRet = ctx.getString(R.string.post_office_T);
        } else if(type.equals(ctx.getString(R.string.church))){
            toRet = ctx.getString(R.string.church_T);
        } else if(type.equals(ctx.getString(R.string.department_store))){
            toRet = ctx.getString(R.string.department_store_T);
        } else if(type.equals(ctx.getString(R.string.hospital))){
            toRet = ctx.getString(R.string.hospital_T);
        } else if(type.equals(ctx.getString(R.string.parking))){
            toRet = ctx.getString(R.string.parking_T);
        } else if(type.equals(ctx.getString(R.string.school))){
            toRet = ctx.getString(R.string.school_T);
        } else if(type.equals(ctx.getString(R.string.stadium))){
            toRet = ctx.getString(R.string.stadium_T);
        } else if(type.equals(ctx.getString(R.string.train_station))){
            toRet = ctx.getString(R.string.train_station_T);
        } else if(type.equals(ctx.getString(R.string.airport))){
            toRet = ctx.getString(R.string.airport_T);
        }

        return toRet;
    }


    public int getTypeResource_marker(Context ctx, String type) {

        int toRet = R.drawable.savedparkingopt;

        if(type.equals(ctx.getString(R.string.shopping_mall))){
            toRet = R.drawable.shop_m;
        } else if(type.equals(ctx.getString(R.string.museum))){
            toRet = R.drawable.museum_m;
        } else if(type.equals(ctx.getString(R.string.post_office))){
            toRet = R.drawable.postoffice_m;
        } else if(type.equals(ctx.getString(R.string.church))){
            toRet = R.drawable.church_m;
        } else if(type.equals(ctx.getString(R.string.department_store))){
            toRet = R.drawable.shop_m;
        } else if(type.equals(ctx.getString(R.string.hospital))){
            toRet = R.drawable.hospital_m;
        } else if(type.equals(ctx.getString(R.string.parking))){
            toRet = R.drawable.parking_m;
        } else if(type.equals(ctx.getString(R.string.school))){
            toRet = R.drawable.school_m;
        } else if(type.equals(ctx.getString(R.string.stadium))){
            toRet = R.drawable.stadium_m;
        } else if(type.equals(ctx.getString(R.string.train_station))){
            toRet = R.drawable.station_m;
        } else if(type.equals(ctx.getString(R.string.airport))){
            toRet = R.drawable.airport_m;
        }

        return toRet;
    }



    // SECURITY

    public String SHA1(String text) {

        try {

            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF-8"),
                    0, text.length());
            byte[] sha1hash = md.digest();

            return toHex(sha1hash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String toHex(byte[] buf) {

        if (buf == null) return "";

        int l = buf.length;
        StringBuffer result = new StringBuffer(2 * l);

        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }

        return result.toString();

    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {

        sb.append(HEX.charAt((b >> 4) & 0x0f))
                .append(HEX.charAt(b & 0x0f));

    }

    public void clearAll(Context thisContext){
        SaveParkings s = new SaveParkings(thisContext);
        s.removeAll();
        SavedPlacesType sp = new SavedPlacesType(thisContext);
        sp.removeAll();
        SaveLocation sl = new SaveLocation(thisContext);
        sl.removeALL();
        SaveUser sv = new SaveUser(thisContext);
        sv.logOut();
        SaveMyCarPos svpos = new SaveMyCarPos(thisContext);
        svpos.removeALL();
    }

    public boolean hasNavBar (Context ctx)
    {
        return ViewConfiguration.get(ctx).hasPermanentMenuKey();
    }



    public void showUpdateDialog(final AppCompatActivity ac, final String url) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ac.finishAffinity();
                        openUrl(ac, url);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        ac.finishAffinity();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ac);
        builder.setMessage(R.string.update_dialog).setPositiveButton(R.string.update_now, dialogClickListener)
                .setNegativeButton(R.string.update_later, dialogClickListener)
                .setCancelable(false)
                .show();

    }

    public void openUrl(Activity ac, String url)
    {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ac.startActivity(intent);
    }


}
