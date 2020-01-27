package com.akwares.park_it.VolleyReq;

import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akshay on 19/02/2019.
 */

public class ParkingRequest extends StringRequest {

    private static final String LOGIN_REQUEST_URL = "http://deltaprak.altervista.org/Android/parking_script.php";
    private Map<String, String> params;

    public ParkingRequest(Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, LOGIN_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();
    }

    public void getParkings(String email){
        params.put("email", email);
        params.put("toDo", "0");
    }

    public void addParking(String email, double lat, double lng, String pid, String type){

        Double Rlat = Math.floor(lat * 100000) / 100000;
        Double Rlng = Math.floor(lng * 100000) / 100000;
        params.put("email", email);
        params.put("lat", Rlat+"");
        params.put("long", Rlng+"");
        params.put("placeId", pid);
        params.put("type", type);
        params.put("toDo", "1");
    }

    public void removeSavedParking(String email, double lat, double lng){

        Double Rlat = Math.floor(lat * 100000) / 100000;
        Double Rlng = Math.floor(lng * 100000) / 100000;
        params.put("email", email);
        params.put("lat", Rlat+"");
        params.put("long", Rlng+"");
        params.put("toDo", "2");
    }

    public void removeAllSavedParkings(String email){
        params.put("email", email);
        params.put("toDo", "3");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
