package com.akwares.park_it.VolleyReq;

import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akshay on 18/11/2016.
 */

public class UpdateInfoRequest extends StringRequest {

    private static final String INFO_UPDATE_REQUEST_URL = "http://deltaprak.altervista.org/Android/updateInfo_script.php";
    private Map<String, String> params;

    public UpdateInfoRequest(String email, String fullname, String old_psw, String password, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, INFO_UPDATE_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();

        if(!email.equals("")){
            params.put("email", email);
        }

        if(!fullname.equals("")){
            params.put("name", fullname);
        }

        if(!old_psw.equals("")){
            params.put("old_password", old_psw);
        }


        if(!password.equals("")){
            params.put("password", password);
        }

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
