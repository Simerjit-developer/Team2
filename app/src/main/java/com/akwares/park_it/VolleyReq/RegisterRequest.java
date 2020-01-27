package com.akwares.park_it.VolleyReq;

import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akshay on 17/11/2016.
 */
public class RegisterRequest extends StringRequest{

    private static final String REGISTER_URL = "http://deltaprak.altervista.org/Android/register_script.php";
    private Map<String, String> params;


    public RegisterRequest(String email, String name, String username, String password, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener)
    {
        super(Method.POST, REGISTER_URL, listener, errorListener);
        params = new HashMap<>();
        params.put("email", email);
        params.put("name", name);
        params.put("username", username);
        params.put("password", password);
    }

    @Override
    protected Map<String, String> getParams(){
        return params;
    }
}