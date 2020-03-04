package com.akwares.park_it.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akwares.park_it.Adapters.CarPositionAdapter;
import com.akwares.park_it.Preferences.SaveMyCarPos;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CarPosition;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyCarPosition extends Fragment {

    public static final int REQUEST_CHECK_SETTINGS = 702;

    View view;
    Context thisContext;
    ImageView Warningimg;
    TextView Warningtxt;

    // FOR Positions
    ArrayList<CarPosition> placesAry;
    RecyclerView carPosRecyclerView;
    LinearLayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    SaveMyCarPos carPoss;
    ProgressBar addPBar;

    LocationManager locationManager;
    LocationListener locationListener;
    final int LOCATION_REQ_TIMEOUT = 7000;
    SaveMyCarPos carPosShared;

    //FOR LOCATION BAR
    Button alowLoc;
    LinearLayout topLocBar;

    String currrOperation;

    private CommonFunctions cm;

    FloatingActionButton addFab;

    public MyCarPosition() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_where_is_my_car, container, false);
        thisContext = view.getContext();
        carPosRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_my_car_pos);

        topLocBar = (LinearLayout) view.findViewById(R.id.locationEnabler);
        alowLoc = (Button) view.findViewById(R.id.enaAuthLoc);

        locationManager = (LocationManager) thisContext.getSystemService(LOCATION_SERVICE);
        Warningimg = (ImageView) view.findViewById(R.id.emptyWarningIMG);
        Warningtxt = (TextView)  view.findViewById(R.id.emptyWarningTXT);

        addPBar = (ProgressBar) view.findViewById(R.id.locatingPBar);
        addPBar.setVisibility(View.GONE);

        addFab = (FloatingActionButton) view.findViewById(R.id.addNewPos);
        carPosShared = new SaveMyCarPos(thisContext);
        carPoss = new SaveMyCarPos(thisContext);

        cm = new CommonFunctions();

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            topLocBar.setVisibility(View.VISIBLE);
        } else {
            topLocBar.setVisibility(View.GONE);
        }

        alowLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cm.isLocPermissionEnabled(thisContext)){
                    currrOperation = "enableOnly";
                    runtime_permissions();
                } else {
                    permissionEnabled();
                }
            }
        });

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cm.isOnline(thisContext)){
                    if (!runtime_permissions()) {
                        currrOperation = "takeMe";
                        getMyLocation();
                    }
                } else {
                    cm.errorSnackbar(getActivity(), getString(R.string.err_noConn));
                }
            }
        });

        //carPoss.removeALL();
        placesAry = new ArrayList<>();
        placesAry = carPoss.getAllCarPositions();

        if(placesAry != null){
            if(placesAry.size() > 0){
                showResults();
                setUpList();
            } else {
                showWarning();
            }
        }

        return view;
    }

    public void showResults(){
        Warningimg.setVisibility(View.GONE);
        Warningtxt.setVisibility(View.GONE);
        carPosRecyclerView.setVisibility(View.VISIBLE);
    }

    public void showWarning(){
        Warningtxt.setText(getString(R.string.noPks));
        Warningimg.setVisibility(View.VISIBLE);
        Warningtxt.setVisibility(View.VISIBLE);
        carPosRecyclerView.setVisibility(View.GONE);
    }

    private void setUpList()
    {
        if(placesAry != null){
            showResults();
            layoutManager = new LinearLayoutManager(thisContext);
            carPosRecyclerView.setLayoutManager(layoutManager);
            adapter = new CarPositionAdapter(thisContext, placesAry, view, getActivity());
            carPosRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    boolean animIsSetted = false;

    public void startAnimation() {

        if(!animIsSetted){
            //mSearchView.setClickable(true);
            addFab.setEnabled(false);
            addFab.setImageBitmap(null);
            addPBar.setVisibility(View.VISIBLE);
            animIsSetted = true;
        }

    }

    public void stopAnimation() {

        if(animIsSetted){
            //mSearchView.setClickable(false);
            addFab.setEnabled(true);
            addFab.setImageDrawable(getResources().getDrawable(R.drawable.caropt_white));
            addPBar.setVisibility(View.INVISIBLE);
            animIsSetted = false;
        }

    }

    @Override
    public void onDetach() {
        animIsSetted = false;
        super.onDetach();
    }

    /** For user permissions **/

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Integer.parseInt(getString(R.string.REQ_UNIQUE_CODE)));

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Integer.parseInt(getString(R.string.REQ_UNIQUE_CODE))) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                permissionEnabled();

                if(currrOperation == "takeMe") {
                    getMyLocation();
                }
            } else {
                stopAnimation();
                cm.infoSnackbar(getActivity(), getResources().getString(R.string.hint_switchConnLoc));
            }
        }
    }

    private void permissionEnabled() {
        topLocBar.setVisibility(View.GONE);
    }

    public void showDialog(final LatLng pos, final String address){

        stopAnimation();

        AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);

        final String blockCharacterSet = ":;";

        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        LayoutInflater inflater = (LayoutInflater) thisContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View v = inflater.inflate(R.layout.layout_dialog_view, null);  // this line
        builder.setView(v);
        final AlertDialog alert = builder.create();


        TextView adrs = (TextView) v.findViewById(R.id.address);
        adrs.setText(address);

        final EditText notesToCarPos = (EditText)v.findViewById(R.id.notesExtra);
        notesToCarPos.setFilters(new InputFilter[] { filter });

        TextView add = (TextView) v.findViewById(R.id.btnAdd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CarPosition tmp = new CarPosition(pos, address);
                tmp.setNotes(notesToCarPos.getText().toString());

                if(placesAry == null){
                    placesAry = new ArrayList<CarPosition>();
                }

                placesAry.add(tmp);
                setUpList();

                carPosShared.addNewCarPosition(tmp);
                alert.dismiss();
            }
        });

        TextView canc = (TextView)v.findViewById(R.id.btnCanc);
        canc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert.cancel();

            }
        });

        alert.show();

    }

    public void getMyLocation() {

        startAnimation();

        final double[] loc_lat = {0};
        final double[] loc_long = {0};
        final Looper looper = Looper.myLooper();

        final Handler mapLoadHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(isAdded()){

                    if (msg.obj == "takeme") {

                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(thisContext, Locale.getDefault());

                        try {
                            addresses = geocoder.getFromLocation(loc_lat[0], loc_long[0], 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            String address = addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getLocality()+", "+ addresses.get(0).getPostalCode()+", "+addresses.get(0).getCountryName(); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                            if(isAdded()){
                                showDialog(new LatLng(loc_lat[0], loc_long[0]), address);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {

                        stopAnimation();
                        if (msg.obj == getResources().getString(R.string.err_LocTimeout)) {
                            Toast.makeText(thisContext, getResources().getString(R.string.err_LocTimeout), Toast.LENGTH_LONG).show();
                        } else {

                            displayLocationSettingsRequest(thisContext);

                            if (msg.obj == getResources().getString(R.string.err_generic)) {
                                cm.errorSnackbar(getActivity(), getResources().getString(R.string.err_generic));
                            } else {
                                cm.infoSnackbar(getActivity(), getResources().getString(R.string.err_ConnLoc));
                            }

                        }
                    }
                }

                return false;
            }
        });

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && cm.isOnline(thisContext)) {

                    final boolean[] flagLocated = {false};

                    locationManager = (LocationManager) thisContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            // Called when a new location is found by the network location provider.

                            //sl.setLastLocated(new LatLng(location.getLatitude(), location.getLongitude()));

                            loc_lat[0] = location.getLatitude();
                            loc_long[0] = location.getLongitude();
                            Message msg = mapLoadHandler.obtainMessage();
                            msg.obj = "takeme";

                            if(isAdded()){
                                mapLoadHandler.sendMessage(msg);
                            }

                            flagLocated[0] = true;
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            locationManager.removeUpdates(this);
                        }


                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            locationManager.removeUpdates(this);
                            Message msg = mapLoadHandler.obtainMessage();
                            if(isAdded()){
                                msg.obj = getResources().getString(R.string.err_ConnLoc);
                                mapLoadHandler.sendMessage(msg);
                            }
                        }
                    };

                    if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,  locationListener, looper);

                        final Handler myHandler = new Handler(looper);
                        myHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (!flagLocated[0]) {

                                    if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        locationManager.removeUpdates(locationListener);
                                    }

                                    Message msg = mapLoadHandler.obtainMessage();
                                    if(isAdded()){
                                        msg.obj = getResources().getString(R.string.err_LocTimeout);
                                        mapLoadHandler.sendMessage(msg);
                                    }

                                }
                            }
                        }, LOCATION_REQ_TIMEOUT);


                    } else {
                        Message msg = mapLoadHandler.obtainMessage();
                        if(isAdded()){
                            msg.obj = getResources().getString(R.string.err_generic);
                            mapLoadHandler.sendMessage(msg);
                        }
                    }

                } else {
                    Message msg = mapLoadHandler.obtainMessage();
                    if(isAdded()){
                        msg.obj = getResources().getString(R.string.err_ConnLoc);
                        mapLoadHandler.sendMessage(msg);
                    }
                }
            }
        };

        Thread locateThr = new Thread(r);
        locateThr.start();

    }


    /** RESULTS FROM ACTIVITIES **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CHECK_SETTINGS){
            if(resultCode == Activity.RESULT_OK){
                getMyLocation();
            }
        }
    }


    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            startIntentSenderForResult(status.getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            cm.errorSnackbar(getActivity(), getString(R.string.err_generic));
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
