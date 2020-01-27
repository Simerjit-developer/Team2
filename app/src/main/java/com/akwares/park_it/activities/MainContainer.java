package com.akwares.park_it.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.akwares.park_it.BuildConfig;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.LoginRequest;
import com.akwares.park_it.fragments.About;
import com.akwares.park_it.fragments.Account;
import com.akwares.park_it.fragments.Help;
import com.akwares.park_it.fragments.MapMain;
import com.akwares.park_it.fragments.MyCarPosition;
import com.akwares.park_it.fragments.SavedParkings;
import com.akwares.park_it.fragments.Settings;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.AppBarLayout;

import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainContainer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    public View view;
    SaveUser sv;
    CommonFunctions cm = new CommonFunctions();
    Toolbar toolbar;
    NavigationView navigationView;
    AppBarLayout appBarLayout;

    String username;
    String password;

    int currFrag;

    // fragments
    MapMain mapfragment;
    About aboutfragment;
    SavedParkings savedPkFragment;
    MyCarPosition myCarPosition;
    Settings settingsFragment;
    Account accountFragment;
    Help helpFragment;

    DrawerLayout _rootLayout;
    FrameLayout mylayout;
    public static boolean showAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        showAd = false;
        view = findViewById(android.R.id.content);


        _rootLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setCollapsible(false);
        toolbar.setVisibility(View.VISIBLE);

        mylayout = (FrameLayout) findViewById(R.id.fragmentContainer);

        AppBarLayout.LayoutParams p = new AppBarLayout.LayoutParams(_rootLayout.getLayoutParams());
        p.setMargins(0, 70,0,0);
        p.setScrollFlags(0);
        toolbar.setLayoutParams(p);


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        appBarLayout = (AppBarLayout)findViewById(R.id.appBar);


        mapfragment = new MapMain();
        aboutfragment = new About();
        savedPkFragment = new SavedParkings();
        settingsFragment = new Settings();
        accountFragment = new Account();
        helpFragment = new Help();
        myCarPosition = new MyCarPosition();

        final Menu menu = navigationView.getMenu();
        MenuItem item = menu.getItem(0);
        onNavigationItemSelected(item);

        currFrag = R.id.mapOpt;

        mapfragment.startInitalLocationProcedure = true;

        //checkVersion();
        sv = new SaveUser(MainContainer.this);


        if(!sv.isLoggedIn()){

            Intent intent = new Intent(getApplicationContext(), AboutMain.class);
            startActivity(intent);
            finish();

        }

        username = sv.getUsername();
        password = sv.getPassword();

        view = findViewById(android.R.id.content);

        if(!sv.getMarkLogin())
        {

            cm.successSnackbar(MainContainer.this,  getString(R.string.successLogin));

            sv.setMarkLogin(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setNameOnNav();

        SavedPlacesType sp = new SavedPlacesType(getApplicationContext());

        if (sp.getPlacesType() == null){
            cm.getPlaces(getApplicationContext());
        }

        final android.os.CountDownTimer Count = new android.os.CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                checkUserExists();
            }
        };

        Count.start();

    }


    public void setNameOnNav(){

        View header = navigationView.getHeaderView(0);
        TextView _nameTV = (TextView)header.findViewById(R.id.userName);
        _nameTV.setText(sv.getName());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!mapfragment.isVisible()) {
                uncheckAllMenuItems();
                final Menu menu = navigationView.getMenu();
                MenuItem item = menu.getItem(0);
                onNavigationItemSelected(item);
            } else {
                super.onBackPressed();
            }
        }
    }

    public void prepareMapFragment()
    {
        Double lat = mapfragment.getLoc_lat();
        Double lng = mapfragment.getLoc_long();

        if(lat != null && lng != null)
        {
            mapfragment = new MapMain();
            mapfragment.setLoc_lat(lat);
            mapfragment.setLoc_long(lng);
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        if(currFrag != item.getItemId()){
            uncheckAllMenuItems();

            int id = item.getItemId();
            currFrag = id;
            if(id != R.id.mapOpt)
            {
                if(mapfragment.googleMap != null){
                    mapfragment.googleMap.clear();
                }
            }

            if (id == R.id.mapOpt) {
                prepareMapFragment();
                changeFragment(getString(R.string.mapOpt), mapfragment, item);
                appBarLayout.setVisibility(View.GONE);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            } else {

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


                if (id == R.id.savedParkingOpt) {
                    changeFragment(getString(R.string.savedParkingOpt), savedPkFragment, item);
                } else if (id == R.id.carOpt) {
                    changeFragment(getString(R.string.carOpt), myCarPosition, item);
                } else if (id == R.id.accountOpt){
                    changeFragment(getString(R.string.accountOpt), accountFragment, item);
                } else if (id == R.id.settingsOpt) {
                    changeFragment(getString(R.string.settingsOpt), settingsFragment, item);
                } else if (id == R.id.helpOpt) {
                    changeFragment(getString(R.string.helpOpt), helpFragment, item);
                } else if (id == R.id.aboutOpt) {
                    changeFragment(getString(R.string.aboutOpt), aboutfragment, item);
                }


            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(String title, Fragment frag, MenuItem item)
    {


        CoordinatorLayout.LayoutParams coordinatorLayoutParams = (CoordinatorLayout.LayoutParams) ((FrameLayout)view.findViewById(R.id.fragmentContainer)).getLayoutParams();
        coordinatorLayoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        getSupportActionBar().setTitle(title);
        appBarLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, frag, title);
        transaction.commit();
        item.setChecked(true);


    }

    private void uncheckAllMenuItems() {
        final Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.hasSubMenu()) {
                SubMenu subMenu = item.getSubMenu();
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    subMenuItem.setChecked(false);
                }
            } else {
                item.setChecked(false);
            }
        }
    }


    public void checkUserExists()
    {

        final Handler logReqHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.obj == getString(R.string.cod_1))
                {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    cm.clearAll(MainContainer.this);
                                    Intent in = new Intent(MainContainer.this, Login.class);
                                    startActivity(in);
                                    finishAffinity();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainContainer.this);
                    builder.setMessage(R.string.err_nomoreuser).setPositiveButton(R.string.positive_respose, dialogClickListener)
                            .setCancelable(false).show();
                }
                return false;
            }
        });


        Runnable r = new Runnable() {
            @Override
            public void run() {

                final Message msgtoSend = logReqHandler.obtainMessage();
                Response.Listener<String> responseListener= new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (!success) {
                                msgtoSend.obj = getString(R.string.cod_1);
                                logReqHandler.sendMessage(msgtoSend);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                Response.ErrorListener errorListener = new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        msgtoSend.obj = getString(R.string.cod_101);
                        logReqHandler.sendMessage(msgtoSend);
                    }
                };


                LoginRequest loginReq_Internal = new LoginRequest(username, password, responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(MainContainer.this);
                queue.add(loginReq_Internal);
                int vers = BuildConfig.VERSION_CODE;

            }
        };

        Thread loginThr = new Thread(r);
        loginThr.start();
    }

    private Bundle checkVersion() {

        final Bundle bundle = new Bundle();
        final CommonFunctions cm = new CommonFunctions();

        Response.Listener<String> responseListener= new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if(!success){
                        String url = jsonResponse.getString("url");
                        cm.showUpdateDialog(MainContainer.this, url);
                    } else {
                        showAd = jsonResponse.getBoolean("ads");
                        if(showAd){
                            //loadAd();
                        }
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
                params.put("username", sv.getUsername()+"");
                return params;
            };
        };
        RequestQueue queue = Volley.newRequestQueue(MainContainer.this);
        queue.add(myReq);

        return bundle;

    }

}
