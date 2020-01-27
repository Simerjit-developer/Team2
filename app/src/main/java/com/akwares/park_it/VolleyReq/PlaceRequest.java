package com.akwares.park_it.VolleyReq;

import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akshay on 19/02/2019.
 */

public class PlaceRequest extends StringRequest {

    private static final String PLACE_REQUEST_URL = "http://deltaprak.altervista.org/Android/place_script.php";
    private Map<String, String> params;

    public PlaceRequest(Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, PLACE_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();
    }
    // "getUserPlaces", "addAllPlaces", "applychanges"

    public void getPlaces(String email){
        params.put("email", email);
        params.put("toDo", "0");
    }

    public void addAllPlaces(String email){
        params.put("email", email);
        params.put("toDo", "1");
    }

    public void addPlace(String email, String place){

        params.put("email", email);
        params.put("place", place);
        params.put("toDo", "2");
    }


    public void removeSavedPlace(String email, String place){
        params.put("email", email);
        params.put("place", place);
        params.put("toDo", "3");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
