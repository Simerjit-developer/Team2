package com.akwares.park_it.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.akwares.park_it.BuildConfig;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AboutMain extends AppCompatActivity {

    CommonFunctions cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        cm = new CommonFunctions();

        //checkVersion();

        Intent appIntro = new Intent(this, AboutLoadIntro.class);
        startActivity(appIntro);
    }

    public void logActivity(View view)
    {
        Intent intent = new Intent(this, Login.class);
        overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
        startActivity(intent);
    }

    public void RegActivity(View view)
    {
        Intent intent = new Intent(this, RegisterPage1.class);
        overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
        startActivity(intent);
    }


    private Bundle checkVersion() {

        final Bundle bundle = new Bundle();

        Response.Listener<String> responseListener= new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if(!success){
                        String url = jsonResponse.getString("url");
                        cm.showUpdateDialog(AboutMain.this, url);
                    } else {
                        bundle.putBoolean("versionOK", true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        StringRequest myReq = new StringRequest(Request.Method.POST,
                "http://deltaprak.altervista.org/Android/checkVersion.php",
                responseListener, null)
        {

            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("version", BuildConfig.VERSION_CODE+"");
                return params;
            };
        };
        RequestQueue queue = Volley.newRequestQueue(AboutMain.this);
        queue.add(myReq);

        return bundle;

    }
}
