package com.akwares.park_it.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akwares.park_it.Adapters.ParkingAreaAdapter;
import com.akwares.park_it.Preferences.SaveLocation;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.Utilities.PlaceInternal;
import com.akwares.park_it.VolleyReq.LoginRequest;
import com.akwares.park_it.VolleyReq.PlaceRequest;
import com.akwares.park_it.activities.Login;
import com.akwares.park_it.activities.SettingsActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapMain extends Fragment {

    private static final int REQUEST_CHECK_SETTINGS = 123;
    View view;
    FloatingActionButton locateme;
    FloatingActionButton filters;
    ProgressBar circleLoading;
    CommonFunctions cm;
    SaveLocation sl;
    Context thisContext;
    FloatingSearchView mSearchView;
    Button _check;
    final int LOCATION_REQ_TIMEOUT = 7000;
    NestedScrollView mainScrollView;
    RelativeLayout emptyListErr;

    // FOR GMAPS
    public Double loc_lat;
    public Double loc_long;
    MapView mMapView;
    public GoogleMap googleMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;
    Address address;
    Circle circle;
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    static final int CLOSE_FILTER_REQ_CODE = 1;


    JSONArray totPlaces;

    // FOR Places
    RecyclerView placeRecyclerView;
    LinearLayoutManager layoutManager;
    public RecyclerView.Adapter adapter;
    private ProgressDialog checkPD;
    SavedPlacesType sp;


    // THREADS
    Thread locateThr;
    Thread savedPlRetrive;


    // SLide up
    SlidingUpPanelLayout slideup;
    ImageView accent;
    private Marker mainMarker;

    //OTHER
    public boolean startInitalLocationProcedure;

    //total lenght of savedplaces
    int totalNSavedPlaces;


    LinearLayout dragView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        cm = new CommonFunctions();
        view = inflater.inflate(R.layout.fragment_main_map, container, false);
        thisContext = view.getContext();
        if (!Places.isInitialized()) {
            Places.initialize(thisContext, getString(R.string.google_maps_key));
        }
        locateme = (FloatingActionButton) view.findViewById(R.id.locateme);
        filters = (FloatingActionButton) view.findViewById(R.id.filter);
        circleLoading = (ProgressBar) view.findViewById(R.id.progressCircle);

        dragView = view.findViewById(R.id.dragView);

        dragView.setClipToOutline(true);

        CoordinatorLayout.LayoutParams coordinatorLayoutParams = (CoordinatorLayout.LayoutParams) (getActivity().findViewById(R.id.fragmentContainer)).getLayoutParams();
        coordinatorLayoutParams.setBehavior(null);

        circleLoading.setVisibility(View.INVISIBLE);

        mSearchView = (FloatingSearchView) view.findViewById(R.id.floating_search_view);


        emptyListErr = (RelativeLayout) view.findViewById(R.id.noResImg);
        emptyListErr.setVisibility(View.GONE);

        slideup = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        accent = (ImageView) view.findViewById(R.id.Slideup_accent);
        panelListener();

        sl = new SaveLocation(thisContext);
        sp = new SavedPlacesType(thisContext);

        mainScrollView = (NestedScrollView) view.findViewById(R.id.mainMapScroll);
        placeRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerPlaces);

        DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mSearchView.attachNavigationDrawerToMenuButton(mDrawerLayout);
        _check = (Button) view.findViewById(R.id.btnCheck);

        locationManager = (LocationManager) thisContext.getSystemService(LOCATION_SERVICE);

        onClickSearchBar();
        setupMyMap(savedInstanceState);
        onClickLocateMe();
        onClickFilter();
        onClickcheck();
        removeScrollviewOnMap();
        placeRecyclerView.setVisibility(View.GONE);
        //setUpList();   //Now in on click check
        scrollToTop();
        hideNavigation();

        if(startInitalLocationProcedure){
            startLocationProcedure();
            startInitalLocationProcedure = false;
        }

        return view;
    }

    public void hideNavigation()
    {

        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        view.setSystemUiVisibility(flags);

        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {

            @Override
            public void onSystemUiVisibilityChange(int visibility)
            {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.setSystemUiVisibility(flags);
                        }
                    }, 3000);
                }
            }
        });

    }

    public Double getLoc_lat() {
        return loc_lat;
    }

    public Double getLoc_long() {
        return loc_long;
    }

    public void setLoc_lat(Double loc_lat) {
        this.loc_lat = loc_lat;
    }

    public void setLoc_long(Double loc_long) {
        this.loc_long = loc_long;
    }

    public void panelListener(){


        slideup.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){

                    placeRecyclerView.setNestedScrollingEnabled(false);
                    if(accent.getRotation()!=180)
                    {
                        accent.animate()
                                .rotationBy(180)
                                .setDuration(200)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                    }
                    googleMap.setPadding(0, 100, 0, 100);
                }

                if (newState == SlidingUpPanelLayout.PanelState.ANCHORED)
                {
                    placeRecyclerView.setNestedScrollingEnabled(false);

                    int y = slideup.getPanelHeight()-slideup.getShadowHeight();

                    googleMap.setPadding(0, 0, 0, y+60);

                }

                if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    if(accent.getRotation()!=0)
                    {
                        accent.animate()
                                .rotationBy(-180)
                                .setDuration(200)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                    }
                    googleMap.setPadding(0, 100, 0, 100);
                }

                if(newState == SlidingUpPanelLayout.PanelState.DRAGGING){

                    googleMap.setPadding(0, 100, 0, 100);

                }
            }
        });
    }


    /** Settings UP **/

    private void setUpList(JSONArray jary)
    {
        if(jary.length() >= 1){
            placeRecyclerView.setVisibility(View.VISIBLE);
            layoutManager = new LinearLayoutManager(thisContext);
            placeRecyclerView.setLayoutManager(layoutManager);

            ArrayList<Object> mapPacket = new ArrayList<>(1);
            mapPacket.add(googleMap);
            mapPacket.add(circle);
            mapPacket.add(layoutManager);
            mapPacket.add(mainMarker);

            adapter = new ParkingAreaAdapter(thisContext, jary, view, "Map", mapPacket);
            placeRecyclerView.setAdapter(adapter);
        }
    }

    private void removeScrollviewOnMap()
    {
        ImageView transparentImageView = (ImageView) view.findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    private void scrollToTop()
    {
        mainScrollView.fullScroll(ScrollView.FOCUS_UP);
    }


    public ArrayList<Marker> putNewMarkers(JSONArray jary){

        JSONObject jobCurr;
        JSONObject currGeo;
        JSONObject currLoc;

        ArrayList<Marker> mkrs = new ArrayList<>();

        for (int i=0; i<jary.length(); i++){

            try {
                jobCurr = jary.getJSONObject(i);

                totPlaces.put(jobCurr);

                currGeo =  new JSONObject(jobCurr.getString("geometry"));



                currLoc = new JSONObject(currGeo.getString("location"));

                String real_type = jobCurr.getString("real_type");

                Marker tmp = googleMap.addMarker(new MarkerOptions().position(new LatLng(currLoc.getDouble("lat"), currLoc.getDouble("lng"))).title(jobCurr.getString("name"))
                        .icon(BitmapDescriptorFactory.fromResource(cm.getTypeResource_marker(thisContext, real_type))));

                mkrs.add(tmp);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mkrs;
    }

    private int totalPkThreadDone;
    private Handler pkRetriveHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if(msg.obj == getString(R.string.cod_101)) {
                checkPD.dismiss();
                _check.setClickable(true);
            } else {
                if(msg.obj == getString(R.string.cod_102)){
                    Toast.makeText(thisContext, getString(R.string.err_generic), Toast.LENGTH_LONG).show();
                }
                else {

                    if(msg.obj != null)
                    {
                        ArrayList<Object> totObj = (ArrayList<Object>)msg.obj;
                        JSONArray real = new JSONArray();

                        for (int i = 0; i<((JSONArray)totObj.get(0)).length(); i++)
                        {
                            try {
                                JSONObject newOBJ = ((JSONArray) totObj.get(0)).getJSONObject(i).put("real_type", totObj.get(1));
                                real.put(newOBJ);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        ArrayList<Marker> mkrs = putNewMarkers(real);

                        for (int i=0; i<real.length(); i++)
                        {
                            try {
                                real.getJSONObject(i).put("marker", mkrs.get(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        totalPkThreadDone++;
                        Log.d("here", totalPkThreadDone+ "    " +totalNSavedPlaces);

                        boolean flag = false;

                        if(totalPkThreadDone == totalNSavedPlaces){

                            checkPD.dismiss();
                            _check.setClickable(true);
                            totalPkThreadDone = 0;
                            flag = true;
                        }

                        if(adapter == null){
                            setUpList(totPlaces);
                        } else {
                            ((ParkingAreaAdapter)adapter).addPlaces(thisContext, real);
                            adapter.notifyDataSetChanged();

                            if(flag) {
                                ((ParkingAreaAdapter)adapter).sort();
                            }
                        }

                        if (adapter == null) {
                            emptyListErr.setVisibility(View.VISIBLE);
                        } else {
                            emptyListErr.setVisibility(View.GONE);
                        }


                    }
                }
            }
            return false;
        }
    });

    private void fetchPk(int rad, final String typ){

        checkPD.show();

        final RequestQueue queue = Volley.newRequestQueue(thisContext);
        final String key = thisContext.getResources().getString(R.string.google_maps_key);
        final int radius = rad;
        final String type = typ;

        final Message msgtoSend = pkRetriveHandler.obtainMessage();

        Runnable r = () -> {

            final String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+"location=" + loc_lat + "," + loc_long +"&radius="+radius+"&type="+type+"&sensor=true"+"&key="+key;

            Log.d("here", url);
            // prepare the Request
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                (JSONObject response) -> {
                    JSONArray allPlaces = new JSONArray();
                    try {
                        allPlaces = response.getJSONArray("results");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrayList<Object> totObj = new ArrayList<>();
                    totObj.add(allPlaces);
                    totObj.add(typ);

                    if(isAdded()){

                        msgtoSend.obj = totObj;
                        pkRetriveHandler.sendMessage(msgtoSend);
                    }
                },
                (VolleyError error) -> {
                    if(isAdded()){

                        msgtoSend.obj = getString(R.string.cod_101);
                        pkRetriveHandler.sendMessage(msgtoSend);
                    }
                }
            );

            queue.add(getRequest);
        };

        Thread fetchLocations = new Thread(r);
        fetchLocations.start();

    }

    private void onClickcheck() {

        _check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                checkPD = ProgressDialog.show(thisContext, "", getString(R.string.retrivingPK), true);
                _check.setClickable(false);

                int rad = sp.getRadius();

                if(loc_lat != null && loc_long != null){

                    if(cm.isOnline(thisContext)){

                        googleMap.clear();

                        setMainMarker();

                        totPlaces = new JSONArray();
                        drawCircle(rad);

                        if(adapter != null)
                        {
                            ((ParkingAreaAdapter)adapter).clear();
                        }

                        String[] pl = sp.getPlacesType();

                        if(pl == null)
                        {
                            getSavedPlaces();
                            pl = sp.getPlacesType();
                        }

                        totalNSavedPlaces = pl.length;

                        for (int i=0; i<pl.length; i++){
                            if(pl[i].compareTo("") != 0){
                                fetchPk(rad, pl[i]);
                            }
                        }
                    } else {
                        checkPD.dismiss();
                        _check.setClickable(true);
                        cm.errorSnackbar(getActivity(), getString(R.string.err_noConn));
                    }

                } else {

                    checkPD.dismiss();
                    _check.setClickable(true);
                    cm.errorSnackbar(getActivity(), getString(R.string.selectLoc));

                }


            }
        });
    }

    /***************/


    private boolean ifReturnedFromAnotherFragment() {

        boolean ret = false;

        if (loc_long != null && loc_lat != null) {

            getSavedPlaces();

            scrollToTop();
            takeMeToMyPlace();
            ret = true;
        }

        return ret;
    }


    private void onClickLocateMe() {
        locateme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationProcedure();
            }
        });
    }

    public void startLocationProcedure(){
        if(cm.isOnline(thisContext)){
            startAnimation();
            if (!runtime_permissions()) {
                getMyLocation();
            }
        } else {
            cm.errorSnackbar(getActivity(), getString(R.string.err_noConn));
        }
    }

    private void onClickSearchBar() {


        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .build(thisContext);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

            }

            @Override
            public void onFocusCleared() {
                mSearchView.clearQuery();

                if (address != null) {
                    mSearchView.setSearchText(address.getAddressLine(0));

                }

            }
        });
    }


    private void onClickFilter() {
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisContext, SettingsActivity.class);
                startActivityForResult(intent, CLOSE_FILTER_REQ_CODE);
                getActivity().overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
            }
        });


    }


    /** RESULTS FROM ACTIVITIES **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                scrollToTop();
                Place place = Autocomplete.getPlaceFromIntent(data);

                loc_lat = place.getLatLng().latitude;
                loc_long = place.getLatLng().longitude;
                takeMeToMyPlace();
                mSearchView.clearSearchFocus();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Toast.makeText(thisContext, getResources().getString(R.string.err_generic), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {

                if (mSearchView.isSearchBarFocused())
                    mSearchView.clearSearchFocus();
                // user canceled the operation.
            }

        } else {
            if(requestCode == CLOSE_FILTER_REQ_CODE)
            {
                hideNavigation();
            }

            if (requestCode == REQUEST_CHECK_SETTINGS){
                if(resultCode == Activity.RESULT_OK){
                    startLocationProcedure();
                }
            }
        }
    }

    /* Working on map */
    private void drawCircle(int rad) {

        if(loc_lat != null && loc_long != null){

            if(circle != null){
                circle.remove();
            }

            circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(loc_lat, loc_long))
                    .radius(rad)
                    .strokeColor(ContextCompat.getColor(thisContext, R.color.transpRed))
                    .fillColor(ContextCompat.getColor(thisContext, R.color.transpRed)));

            if(adapter != null){
                ((ParkingAreaAdapter)adapter).setCircle(circle);
            }

            float zoom = 16;

            if(rad == 500){
                zoom = 15;
            } else {
                if(rad == 100)
                {
                    zoom = 17.5f;
                }
            }

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(loc_lat, loc_long)).zoom(zoom).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            Toast.makeText(thisContext, getString(R.string.selectLoc), Toast.LENGTH_LONG).show();
        }

    }


    /** Go to selected location **/

    private void takeMeToMyPlace() {

        sl.setLastSearched(new LatLng(loc_lat, loc_long));

        if(circle != null){
            mainMarker.remove();
            circle.remove();
        } else {
            googleMap.clear();
        }

        final List<Address>[] addresses = new List[5];

        final Handler geoCodeHand = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.obj == getString(R.string.cod_1))
                {
                    stopAnimation();
                    Toast.makeText(thisContext, getResources().getString(R.string.err_validAddrees), Toast.LENGTH_LONG).show();
                    mSearchView.setSearchText("");
                } else {
                    if(msg.obj == getString(R.string.cod_0)) {
                        setMainMarker();
                    }
                }
                return false;
            }
        });

        Runnable r= new Runnable() {
            @Override
            public void run() {

                final Message msgtoSend = geoCodeHand.obtainMessage();

                try {
                    addresses[0] = geocoder.getFromLocation(loc_lat, loc_long, 1);
                    address = addresses[0].get(0);

                    if(isAdded()){

                        msgtoSend.obj = getString(R.string.cod_0);
                        geoCodeHand.sendMessage(msgtoSend);
                    }

                } catch (Exception e) {
                    loc_lat = null;
                    loc_long = null;

                    if(isAdded()){
                        msgtoSend.obj = getString(R.string.cod_1);
                        geoCodeHand.sendMessage(msgtoSend);
                    }
                }
            }
        };

        Thread th = new Thread(r);
        th.start();

    }

    private void setMainMarker() {
        if (address != null) {

            mainMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(loc_lat, loc_long)).title(address.getAddressLine(0))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

            int rad = sp.getRadius();

            float zoom = 16;

            if(rad == 500){
                zoom = 15;
            } else {
                if(rad == 100)
                {
                    zoom = 17.5f;
                }
            }

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(loc_lat, loc_long)).zoom(zoom).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mSearchView.setSearchText(address.getAddressLine(0) + ", " + address.getCountryName());

            if(adapter != null){
                ((ParkingAreaAdapter)adapter).setMainMarker(mainMarker);
            }

            slideup.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            stopAnimation();
        }
    }

    /** Setup My map (map from fragment) **/
    private void setupMyMap(Bundle savedInstanceState) {
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                geocoder = new Geocoder(thisContext, Locale.getDefault());

                googleMap.getUiSettings().setCompassEnabled(false);

                Location location = null;

                if (!ifReturnedFromAnotherFragment()) {
                    if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                    if (location != null) {
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(10).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        stopAnimation();
                    } else {

                        LatLng loc = sl.getLastSearched();

                        if(loc != null){
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(loc.latitude, loc.longitude)).zoom(10).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            stopAnimation();
                        }
                    }
                }

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        stopAnimation();
                        loc_long = latLng.longitude;
                        loc_lat = latLng.latitude;

                        takeMeToMyPlace();
                    }
                });


                googleMap.setPadding(0, 100, 0, 100);
                googleMap.setBuildingsEnabled(true);
            }
        });


    }

    /** ANIMATION SETTINGS **/

    boolean animIsSetted = false;

    public void startAnimation() {

        if(!animIsSetted){
            //mSearchView.setClickable(true);
            cm.disableButton(_check);
            locateme.setEnabled(false);
            locateme.setImageBitmap(null);
            circleLoading.setVisibility(View.VISIBLE);
            animIsSetted = true;
        }

    }

    public void stopAnimation() {

        if(animIsSetted){
            //mSearchView.setClickable(false);
            cm.enableButton(_check);
            locateme.setEnabled(true);
            locateme.setImageDrawable(getResources().getDrawable(R.drawable.locateme));
            circleLoading.setVisibility(View.INVISIBLE);
            animIsSetted = false;
        }

    }

    /** To get user location **/
    Handler mapLoadHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.obj == "takeme") {
                takeMeToMyPlace();
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

            return false;
        }
    });

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

    public void getMyLocation() {

        final Looper looper = Looper.myLooper();

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

                            loc_lat = location.getLatitude();
                            loc_long = location.getLongitude();
                            Message msg = mapLoadHandler.obtainMessage();

                            if(isAdded()){
                                msg.obj = "takeme";
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

        locateThr = new Thread(r);
        locateThr.start();

    }

    /* TO get saved places type on DB*/
    public void getSavedPlaces()
    {
        final Handler plReqHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(isAdded()) {

                    if(msg.obj == getString(R.string.cod_100))
                    {
                        Toast.makeText(thisContext, getString(R.string.err_noConn), Toast.LENGTH_LONG).show();
                    }else {
                        if(msg.obj == getString(R.string.cod_103)){
                            Toast.makeText(thisContext, getString(R.string.err_generic), Toast.LENGTH_LONG).show();
                        } else {
                            loadAllPlacesType((JSONArray)msg.obj);
                        }
                    }
                }
                return false;
            }
        });

        Runnable r = new Runnable() {
            @Override
            public void run() {

                final Message msgtoSend = plReqHandler.obtainMessage();

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

                                if(isAdded()){

                                    msgtoSend.obj = allSaved;
                                    plReqHandler.sendMessage(msgtoSend);
                                }

                            } else {

                                if(isAdded()){

                                    msgtoSend.obj = getString(R.string.cod_103);
                                    plReqHandler.sendMessage(msgtoSend);
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
                        if(isAdded()){

                            msgtoSend.obj = getString(R.string.cod_100);
                            plReqHandler.sendMessage(msgtoSend);
                        }
                    }
                };

                PlaceRequest plReq_Internal = new PlaceRequest(responseListener, errorListener);
                RequestQueue qu = Volley.newRequestQueue(thisContext);
                plReq_Internal.getPlaces(new SaveUser(thisContext).getEmail());
                qu.add(plReq_Internal);

            }
        };


        savedPlRetrive = new Thread(r);
        savedPlRetrive.start();
    }

    private void loadAllPlacesType(JSONArray PLACESfromWeb) {

        sp.removeAll();

        for (int i=0; i<PLACESfromWeb.length(); i++){
            try {
                if(!PLACESfromWeb.getString(i).split(":")[1].equals("null"))
                {
                    sp.addPlaceType(PLACESfromWeb.getString(i).split(":")[0]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                getMyLocation();
            } else {
                stopAnimation();
                cm.infoSnackbar(getActivity(), getResources().getString(R.string.hint_switchConnLoc));
            }
        }
    }


    /** Other autogenerated shit **/

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
