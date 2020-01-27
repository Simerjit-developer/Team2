package com.akwares.park_it.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.akwares.park_it.Adapters.ParkingAreaAdapter;
import com.akwares.park_it.Preferences.SaveParkings;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.ParkingRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedParkings extends Fragment {

    View view;
    Context thisContext;
    ImageView Warningimg;
    TextView Warningtxt;
    SwipeRefreshLayout refDown;

    // FOR Places
    RecyclerView placeRecyclerView;
    LinearLayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    JSONArray placesAry;
    private SaveParkings svPK;
    private CommonFunctions cm;

    ProgressBar progressBar;

    PlacesClient placesClient;


    public SavedParkings() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_saved_pk, container, false);
        thisContext = view.getContext();
        placeRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerPks);

        if (!Places.isInitialized()) {
            Places.initialize(thisContext, getString(R.string.google_maps_key));
        }

        placesClient = Places.createClient(thisContext);


        Warningimg = (ImageView) view.findViewById(R.id.emptyWarningIMG);
        Warningtxt = (TextView)  view.findViewById(R.id.emptyWarningTXT);

        progressBar = (ProgressBar) view.findViewById(R.id.savedPKpbar);
        refDown = (SwipeRefreshLayout) view.findViewById(R.id.refDown);

        refDown.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary)
        );

        placesAry = new JSONArray();
        adapter = new ParkingAreaAdapter(thisContext, placesAry, view, "SavedParkings", null);
        placeRecyclerView.setAdapter(adapter);


        svPK = new SaveParkings(thisContext);
        cm = new CommonFunctions();

        showSearching();

        getPkFromDB();

        refDown.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                placesAry = new JSONArray();
                getPkFromDB();
            }
        });


        return view;
    }

    /** Retrive and save savedparkings **/
    private void getPkFromDB()
    {
        final Handler pkReqHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {


                if(refDown.isRefreshing()){
                    refDown.setRefreshing(false);
                }

                if(msg.obj == thisContext.getString(R.string.cod_100))
                {
                    notConnected();
                    Toast.makeText(thisContext, getString(R.string.err_noConn), Toast.LENGTH_LONG).show();
                }else {
                    if(msg.obj == thisContext.getString(R.string.cod_1)){
                        showWarning();
                        Toast.makeText(thisContext, getString(R.string.noPks), Toast.LENGTH_LONG).show();
                    } else {
                        if(msg.obj == thisContext.getString(R.string.cod_0)){
                            showResults();
                            fetchAllPks();
                        }
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

                                svPK.removeAll();

                                for (int i=0; i<allSaved.length(); i++)
                                {
                                    try {
                                        JSONObject newOBJ = allSaved.getJSONObject(i);
                                        try {
                                            svPK.addParking(newOBJ.getString("placeId"), newOBJ.getString("type"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                msgtoSend.obj = thisContext.getString(R.string.cod_0);

                                if(isAdded()) {
                                    pkReqHandler.sendMessage(msgtoSend);
                                }
                            } else {

                                svPK.removeAll();

                                msgtoSend.obj = thisContext.getString(R.string.cod_1);
                                if (isAdded()) {
                                    pkReqHandler.sendMessage(msgtoSend);
                                }
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

                        if(isAdded()) {
                            pkReqHandler.sendMessage(msgtoSend);
                        }
                    }
                };


                ParkingRequest pkReq_Internal = new ParkingRequest(responseListener, errorListener);
                SaveUser sv = new SaveUser(thisContext);
                pkReq_Internal.getParkings(sv.getEmail());
                RequestQueue qu = Volley.newRequestQueue(thisContext);
                qu.add(pkReq_Internal);
            }
        };


        Thread savedPkRetrive = new Thread(r);
        savedPkRetrive.start();

    }

    private void fetchAllPks() {
        SaveParkings svpk = new SaveParkings(thisContext);

        final String[] places = svpk.getSavedParkings();

        final Handler savedPkHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(Integer.parseInt(msg.obj.toString()) == places.length) {


                    setUpList();
                }

                return false;
            }
        });

        final int[] count = {0};

        if(places != null) // se esistono parcheggi
        {

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i=0; i<places.length; i++){

                        final String currPlaceId = places[i].split(":")[0];
                        final String realType = places[i].split(":")[1];

                        final JSONObject tmpJob = new JSONObject();

                        // Specify the fields to return.
                        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.RATING, Place.Field.ADDRESS, Place.Field.LAT_LNG);

// Construct a request object, passing the place ID and fields array.
                        FetchPlaceRequest request = FetchPlaceRequest.newInstance(currPlaceId, placeFields);

                        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                            Place currPlace = response.getPlace();


                            Log.d("Here", "getPkHandler " + placesAry.length());

                            JSONObject geometry = new JSONObject();
                            JSONObject location = new JSONObject();


                            try {

                                location.put("lat", currPlace.getLatLng().latitude);
                                location.put("lng", currPlace.getLatLng().longitude);
                                String loc = location.toString();
                                geometry.put("location", loc);
                                String geo = geometry.toString();
                                tmpJob.put("geometry", geo);
                                tmpJob.put("place_id", currPlaceId);
                                tmpJob.put("name", currPlace.getName());
                                tmpJob.put("rating", currPlace.getRating());
                                tmpJob.put("real_type", realType);
                                tmpJob.put("vicinity", currPlace.getAddress());

                                placesAry.put(tmpJob);


                                Message msgtoSend = savedPkHandler.obtainMessage();

                                count[0]++;
                                msgtoSend.obj = ""+count[0];
                                if(isAdded()){
                                    savedPkHandler.sendMessage(msgtoSend);
                                }

                            } catch (JSONException je) {
                                je.printStackTrace();
                            }

                            Log.i("found", "Place found: " + currPlace.getName());
                        }).addOnFailureListener((exception) -> {
                            Log.e("Issue", "Place not found: " + exception);
                        });

                    } //end for
                }
            };

            Thread th = new Thread(r);
            th.start();

        } else {
            showWarning();
        }
    }

    void notConnected()
    {
        Warningimg.setImageDrawable(thisContext.getResources().getDrawable(R.drawable.notconnected));
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
        Warningimg.setImageDrawable(thisContext.getResources().getDrawable(R.drawable.emptywarning));
        Warningtxt.setText(getString(R.string.noPks));
        Warningimg.setVisibility(View.VISIBLE);
        Warningtxt.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        placeRecyclerView.setVisibility(View.GONE);
    }

    private void setUpList()
    {
        if(placesAry.length() >= 1){

            showResults();
            layoutManager = new LinearLayoutManager(thisContext);
            placeRecyclerView.setLayoutManager(layoutManager);
            adapter = new ParkingAreaAdapter(thisContext, placesAry, view, "SavedParkings", null);
            placeRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
