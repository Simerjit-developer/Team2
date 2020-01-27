package com.akwares.park_it.Adapters;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CarPosition;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.Preferences.SaveMyCarPos;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Akshay on 21/01/2019.
 */
public class CarPositionAdapter extends RecyclerView.Adapter<CarPositionAdapter.ViewHolder> {


    public static final long LOCATION_REQ_TIMEOUT = 7000;
    private LocationManager locationManager;
    Context ctx;
    CommonFunctions cm;
    ArrayList<CarPosition> CarPositions;
    SaveMyCarPos svCarPosShared;
    View view;
    ImageView Warningimg;
    TextView Warningtxt;
    RecyclerView carPosRecyclerView;
    ProgressDialog LocPD;
    Activity currAc;

    public CarPositionAdapter(Context c, ArrayList caprPoss, View v, Activity activity) {
        ctx = c;
        cm = new CommonFunctions();
        svCarPosShared = new SaveMyCarPos(ctx);

        locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
        view = v;
        currAc = activity;

        carPosRecyclerView = view.findViewById(R.id.recycler_my_car_pos);
        Warningimg = view.findViewById(R.id.emptyWarningIMG);
        Warningtxt = view.findViewById(R.id.emptyWarningTXT);

        if(caprPoss == null){
            CarPositions = new ArrayList<>();
        } else {
            CarPositions = caprPoss;
        }
    }

    public void showWarning(){
        Warningimg.setImageDrawable(ctx.getResources().getDrawable(R.drawable.emptywarning));
        Warningtxt.setText(ctx.getString(R.string.noPks));
        Warningimg.setVisibility(View.VISIBLE);
        Warningtxt.setVisibility(View.VISIBLE);
        carPosRecyclerView.setVisibility(View.GONE);
    }

    public void showResults(){
        Warningimg.setVisibility(View.GONE);
        Warningtxt.setVisibility(View.GONE);
        carPosRecyclerView.setVisibility(View.VISIBLE);
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        TextView itemAddress;
        TextView itemDate;
        TextView itemDescription;

        Button takeme;
        Button remove;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemAddress = itemView.findViewById(R.id.carPosAddress);
            itemDate = itemView.findViewById(R.id.dateCarPos);
            itemDescription = itemView.findViewById(R.id.notesCarPos);

            takeme = itemView.findViewById(R.id.takeMeToCar);
            remove = itemView.findViewById(R.id.removeCarPos);

            remove.setOnClickListener((View v) -> {

                DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:

                            svCarPosShared.removeCarPosition(CarPositions.get(getAdapterPosition()).getId());
                            CarPositions.remove(getAdapterPosition());
                            notifyDataSetChanged();

                            if(CarPositions.size() == 0){
                                showWarning();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage(R.string.sureDialog).setPositiveButton(R.string.positive, dialogClickListener)
                        .setNegativeButton(R.string.negative, dialogClickListener).show();

            });
            
            takeme.setOnClickListener((View v) -> {

                if(cm.isOnline(ctx)){

                    if (cm.isLocPermissionEnabled(ctx)) {
                        LocPD = ProgressDialog.show(ctx, "", ctx.getString(R.string.routing), true);
                        getMyLocationAndTakeMeto(CarPositions.get(getAdapterPosition()).getPos());
                    } else {
                        cm.errorSnackbar(currAc, ctx.getString(R.string.err_ConnLoc));
                    }

                } else {
                    cm.errorSnackbar(currAc, ctx.getString(R.string.err_noConn));
                }

            });

        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_car_position_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int indx) {

        CarPosition tmpPlc = CarPositions.get(indx);

        viewHolder.itemAddress.setText(tmpPlc.getAddress());
        viewHolder.itemDate.setText(tmpPlc.getDtPos());
        viewHolder.itemDescription.setText(tmpPlc.getNotes());

    }

    private void openMapRoute(LatLng from, LatLng dest){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+from.latitude+","+from.longitude+"&daddr="+dest.latitude+","+dest.longitude));
        ctx.startActivity(intent);
    }

    public void getMyLocationAndTakeMeto(final LatLng dest) {

        final double[] loc_lat = {0};
        final double[] loc_long = {0};
        final Looper looper = Looper.myLooper();

        final Handler mapLoadHandler = new Handler((Message msg) -> {

            LocPD.dismiss();

            if (msg.obj == "takeme") {

                openMapRoute(new LatLng(loc_lat[0], loc_long[0]), dest);

            } else {

                if (msg.obj == ctx.getResources().getString(R.string.err_LocTimeout)) {
                    Toast.makeText(ctx, ctx.getResources().getString(R.string.err_LocTimeout), Toast.LENGTH_LONG).show();
                } else {

                    if (msg.obj == ctx.getResources().getString(R.string.err_generic)) {
                        cm.errorSnackbar(currAc, ctx.getResources().getString(R.string.err_generic));
                    } else {
                        cm.infoSnackbar(currAc, ctx.getResources().getString(R.string.err_ConnLoc));
                    }

                }
            }

            return false;

        });

        Runnable r = () -> {
            Looper.prepare();
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && cm.isOnline(ctx)) {

                final boolean[] flagLocated = {false};

                locationManager = (LocationManager) ctx.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network location provider.

                        //sl.setLastLocated(new LatLng(location.getLatitude(), location.getLongitude()));

                        loc_lat[0] = location.getLatitude();
                        loc_long[0] = location.getLongitude();
                        Message msg = mapLoadHandler.obtainMessage();
                        msg.obj = "takeme";
                        mapLoadHandler.sendMessage(msg);
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
                        msg.obj = ctx.getResources().getString(R.string.err_ConnLoc);
                        mapLoadHandler.sendMessage(msg);
                    }
                };

                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,  locationListener, looper);

                    final Handler myHandler = new Handler(looper);
                    myHandler.postDelayed(() -> {
                        if (!flagLocated[0]) {

                            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                locationManager.removeUpdates(locationListener);
                            }

                            Message msg = mapLoadHandler.obtainMessage();
                            msg.obj = ctx.getResources().getString(R.string.err_LocTimeout);
                            mapLoadHandler.sendMessage(msg);

                        }

                    }, LOCATION_REQ_TIMEOUT);


                } else {
                    Message msg = mapLoadHandler.obtainMessage();
                    msg.obj = ctx.getResources().getString(R.string.err_generic);
                    mapLoadHandler.sendMessage(msg);
                }

            } else {
                Message msg = mapLoadHandler.obtainMessage();
                msg.obj = ctx.getResources().getString(R.string.err_ConnLoc);
                mapLoadHandler.sendMessage(msg);
            }
        };

        Thread locateThr = new Thread(r);
        locateThr.start();
    }



    @Override
    public int getItemCount() {
        return CarPositions.size();
    }
}
