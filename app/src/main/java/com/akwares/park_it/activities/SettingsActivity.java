package com.akwares.park_it.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akwares.park_it.Adapters.PlacesTypeAdapter;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.PlaceRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    Context thisContext;

    CommonFunctions cm;

    SavedPlacesType pt;
    @BindView(R.id.recyclerTypes) RecyclerView placeRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    @BindView(R.id.pbar) ProgressBar progressBar;
    @BindView(R.id.warningtxt) TextView Warningtxt;
    @BindView(R.id.internalErrorIMG) ImageView Warningimg;
    @BindView(android.R.id.content) View view;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.staybackactivity, R.anim.slidedownactivity);
        super.onBackPressed();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        ButterKnife.bind(this);

        thisContext =getApplicationContext();
        cm = new CommonFunctions();
        pt = new SavedPlacesType(thisContext);

        setupSpinner();

        showSearching();

        getPlaces();

    }

    public void setupSpinner(){
        Spinner mts = view.findViewById(R.id.radSpinner);
        ArrayAdapter<CharSequence> foodadapter = ArrayAdapter.createFromResource(
                thisContext, R.array.circleRadius, R.layout.spinnertext);
        foodadapter.setDropDownViewResource(R.layout.spinnertext);
        mts.setAdapter(foodadapter);

        final String[] meter = getResources().getStringArray(R.array.circleRadius);

        for (int i=0; i<meter.length; i++){
            meter[i] = meter[i].replace(" m", "");
        }

        int i= 0;
        boolean flag = false;
        int pos = 0;

        while(i<meter.length && !flag){

            if(meter[i].equals(String.valueOf(pt.getRadius()))) {
                flag = true;
                pos = i;
            }
            i++;
        }

        mts.setSelection(pos);

        mts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

                pt.setRadius(Integer.parseInt(meter[position]));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void getPlaces()
    {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settingsOpt));

        final Handler pkReqHandler = new Handler((Message msg) -> {

            if(msg.obj == getString(R.string.cod_100))
            {
                notConnected();
                Toast.makeText(thisContext, getString(R.string.err_noConn), Toast.LENGTH_LONG).show();
            }else {
                if(msg.obj == getString(R.string.cod_1)){
                    showWarning();
                    Toast.makeText(thisContext, getString(R.string.err_generic), Toast.LENGTH_LONG).show();
                } else {
                    showResults();
                    loadAllPlacesType((JSONArray)msg.obj);
                }
            }
            return false;

        });

        Runnable r = () -> {

            final Message msgtoSend = pkReqHandler.obtainMessage();

            Response.Listener<String> responseListener= (String response) -> {
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

                            msgtoSend.obj = getString(R.string.cod_1);
                            pkReqHandler.sendMessage(msgtoSend);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            };
            // add it to the RequestQueue


            Response.ErrorListener errorListener = (VolleyError error) ->  {
                msgtoSend.obj = getString(R.string.cod_100);
                pkReqHandler.sendMessage(msgtoSend);
            };

            PlaceRequest plReq_Internal = new PlaceRequest(responseListener, errorListener);
            RequestQueue qu = Volley.newRequestQueue(thisContext);
            plReq_Internal.getPlaces(new SaveUser(thisContext).getEmail());
            qu.add(plReq_Internal);


        };


        Thread savedPkRetrive = new Thread(r);
        savedPkRetrive.start();
    }

    private void loadAllPlacesType(JSONArray PLACESfromWeb) {
        ArrayList<String> placesType = new ArrayList<>();
        Log.d("here", PLACESfromWeb.toString());
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

        if(placesType.size() >= 1){
            layoutManager = new LinearLayoutManager(thisContext);
            placeRecyclerView.setLayoutManager(layoutManager);
            adapter = new PlacesTypeAdapter(thisContext, placesType);
            placeRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    void notConnected()
    {
        Warningimg.setImageDrawable(thisContext.getDrawable(R.drawable.notconnected));
        Warningtxt.setText(getString(R.string.err_noConn));
        Warningimg.setVisibility(View.VISIBLE);
        Warningtxt.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        placeRecyclerView.setVisibility(View.GONE);
    }

    void showSearching() {
        Warningimg.setVisibility(View.GONE);
        Warningtxt.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        placeRecyclerView.setVisibility(View.GONE);
    }

    public void showResults(){
        Warningimg.setVisibility(View.GONE);
        Warningtxt.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        placeRecyclerView.setVisibility(View.VISIBLE);
    }

    public void showWarning(){
        Warningimg.setImageDrawable(thisContext.getDrawable(R.drawable.emptywarning));
        Warningtxt.setText(getString(R.string.err_generic));
        Warningimg.setVisibility(View.VISIBLE);
        Warningtxt.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        placeRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.staybackactivity, R.anim.slidedownactivity);
        }

        return super.onOptionsItemSelected(item);
    }
}
