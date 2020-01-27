package com.akwares.park_it.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CarPosition;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.Utilities.PlaceInternal;
import com.akwares.park_it.Preferences.SaveMyCarPos;
import com.akwares.park_it.Preferences.SaveParkings;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.VolleyReq.ParkingRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Akshay on 21/01/2019.
 */
public class ParkingAreaAdapter extends RecyclerView.Adapter<ParkingAreaAdapter.ViewHolder>{

    View view;
    Context ctx;
    CommonFunctions cm;
    ArrayList<PlaceInternal> parkings;
    SlidingUpPanelLayout slideup;
    SaveParkings sv;
    boolean isMap;
    GoogleMap map;
    Circle circle;
    LinearLayoutManager layoutManager;
    int rwRealh;
    ArrayList<Integer> workingOn;
    Marker mainMkr;
    RelativeLayout noResImg;

    //FOR CAR POS
    ArrayList<CarPosition> savedCarPos;
    SaveMyCarPos carPosShared;

    public ParkingAreaAdapter(Context c, JSONArray allPlaces, View view, String isMap, ArrayList mapPkt) {
        ctx = c;
        cm = new CommonFunctions();
        sv = new SaveParkings(ctx);
        this.view = view;
        workingOn = new ArrayList<>(1);

        if(isMap.compareTo("Map") == 0){
            this.isMap = true;
            this.map = (GoogleMap) mapPkt.get(0);
            setOnMarkerClick();
            circle = (Circle) mapPkt.get(1);
            layoutManager = (LinearLayoutManager) mapPkt.get(2);
            mainMkr = (Marker) mapPkt.get(3);

        } else {
            this.isMap = false;
        }

        carPosShared = new SaveMyCarPos(ctx);

        savedCarPos = carPosShared.getAllCarPositions();

        parkings = doShit(allPlaces);

        slideup = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
    }

    public void setMainMarker(Marker m){
        mainMkr = m;
    }

    private void setSlideListenAndHeight(){

        View card = view.findViewById(R.id.placeCard);
        final RecyclerView rw = (RecyclerView) view.findViewById(R.id.recyclerPlaces);
        final ViewGroup.LayoutParams params = rw.getLayoutParams();

        if(rw.getHeight() > rwRealh){
            rwRealh = rw.getHeight();
        }

        int h = 140;

        int tmp = 0;

        if(card != null){

           tmp = card.getHeight() + (card.getHeight()/6);
        }

        if(tmp > h){
            h= tmp;
        }

        final int finalH = h;

        slideup.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                params.height = rwRealh;

                if (newState == SlidingUpPanelLayout.PanelState.ANCHORED)
                {
                    params.height = finalH;
                }

                rw.setLayoutParams(params);
            }
        });
    }

    public void errorImgHelper(){

    }

    public ArrayList doShit(JSONArray allPlaces){
        ArrayList<PlaceInternal> list = new ArrayList<>();
        PlaceInternal tmpOBJ;

        for (int i=0; i < allPlaces.length(); i++)
        {
            JSONObject obj = null;
            try {
                obj = allPlaces.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String real_type = "not available";

            try {
                real_type = obj.getString("real_type");
            }  catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject geometry = new JSONObject(obj.getString("geometry"));
                JSONObject location = new JSONObject(geometry.getString("location"));

                Marker mkr = null;
                if(isMap){
                    mkr = (Marker) obj.get("marker");
                }

                tmpOBJ = new PlaceInternal(obj.getString("place_id"), obj.getString("name"), new LatLng(location.getDouble("lat"), location.getDouble("lng")), real_type, obj.getString("vicinity"), mkr);

                String type = real_type;

                if(sv.exists(obj.getString("place_id"), type)){
                    tmpOBJ.setSaved(true);
                }else {
                    tmpOBJ.setSaved(false);
                }

                list.add(tmpOBJ);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public void setCircle(Circle c){ circle = c; }

    public void addPlaces(Context c, JSONArray allPlaces)
    {
        ctx = c;
        if(parkings == null)
        {
            cm = new CommonFunctions();
            parkings = doShit(allPlaces);

        } else {
            ArrayList<PlaceInternal> tmp = doShit(allPlaces);

            for (int i = 0; i<tmp.size(); i++) {
                if(!exists(tmp.get(i)))
                {
                    parkings.add(tmp.get(i));
                }
            }
        }

        showErrorIfNoRes();
    }

    public void showErrorIfNoRes(){
        noResImg = (RelativeLayout) view.findViewById(R.id.noResImg);
        if(parkings.size() > 0) {
            noResImg.setVisibility(View.GONE);
        } else {
            noResImg.setVisibility(View.VISIBLE);
        }
    }

    public boolean exists(PlaceInternal pk)
    {
        for (int i=0; i<parkings.size(); i++)
        {
            if(pk.getName().equals(parkings.get(i).getName()))
                return true;
        }

        return false;
    }

    public void sort(){
        Collections.sort(parkings, new Comparator<PlaceInternal>() {
            @Override
            public int compare(PlaceInternal o1, PlaceInternal o2) {
                return o2.getValue(ctx) - o1.getValue(ctx);
            }
        });

        setSlideListenAndHeight();
    }


    private void setOnMarkerClick() {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                int possInAry = -1;

                int i =0;
                boolean flag = false;
                while (i<parkings.size() && !flag){
                    if (parkings.get(i).getMarker().getTitle().compareTo(marker.getTitle()) == 0){
                        flag = true;
                        possInAry = i;
                    }
                    i++;
                }

                if(possInAry != -1){
                    showCardFromSlideUp(possInAry);
                }


                return false;
            }
        });
    }

    public ArrayList<PlaceInternal> getParkings(){
        return parkings;
    }


    public void showCardFromSlideUp(int position){

        slideup.setAnchorPoint(0.23f);
        slideup.setMinFlingVelocity(300);
        slideup.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        if(circle != null){
            circle.remove();
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(parkings.get(position).getLocation()).zoom(20).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



        parkings.get(position).getMarker().showInfoWindow();

        //TODO: scroll recycler to position


        layoutManager.scrollToPositionWithOffset(position, 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView itemTitle;
        TextView itemType;
        TextView itemAddress;
        TextView itemDist;
        ImageView itemIcon;
        ImageView itemSave;
        ImageView itemCarSave;
        ProgressBar pB;

        public ViewHolder(View itemView) {
            super(itemView);
            itemTitle = (TextView)itemView.findViewById(R.id.titleCardView);
            itemIcon = (ImageView) itemView.findViewById(R.id.place_icon);
            itemType = (TextView) itemView.findViewById(R.id.type);
            itemSave = (ImageView) itemView.findViewById(R.id.star);
            itemCarSave = (ImageView) itemView.findViewById(R.id.addCar);
            itemAddress = (TextView) itemView.findViewById(R.id.address);
            pB = (ProgressBar) itemView.findViewById(R.id.pkSavePbar);
            itemDist = (TextView) itemView.findViewById(R.id.distance);


            if(isMap){
                itemCarSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int posinary = getAdapterPosition();
                        PlaceInternal plc = parkings.get(posinary);

                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(ctx, Locale.getDefault());
                        String address = plc.getAdress();

                        try {
                            addresses = geocoder.getFromLocation(plc.getLocation().latitude, plc.getLocation().longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            address = addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getLocality()+", "+ addresses.get(0).getPostalCode()+", "+addresses.get(0).getCountryName(); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        addNewCarPos(address, plc.getLocation());

                    }
                });
            } else {
                itemCarSave.setImageDrawable(null);
            }


            itemSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int pos= getAdapterPosition();

                    Boolean flag = false;
                    int i = 0;

                    while(i<workingOn.size() && !flag){
                        if(workingOn.get(i) == pos)
                        {
                            flag = true;
                        }
                        i++;
                    }

                    if (!flag) {
                        showLoading();

                        workingOn.add(pos);

                        PlaceInternal plc = parkings.get(pos);

                        if(itemSave.getDrawable().getConstantState() == ctx.getResources().getDrawable(R.drawable.saveno).getConstantState()){
                            itemSave.setImageResource(R.drawable.saveyes);
                            parkings.get(pos).setSaved(true);
                            addNewPk(pos, plc.getLocation().latitude, plc.getLocation().longitude, plc.getId(), plc.getType());
                        }else{
                            itemSave.setImageResource(R.drawable.saveno);
                            parkings.get(pos).setSaved(false);
                            removePk(pos, plc.getLocation().latitude, plc.getLocation().longitude, plc.getId(), plc.getType());
                        }
                    }

                }
            });


            if(isMap){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            int position = getAdapterPosition();

                            LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);

                            //element 2 up to 3 px from top
                            layoutManager.scrollToPositionWithOffset(position, 3);
                            showCardFromSlideUp(position);

                        }

                });
            }
        }


        public void addNewPk(final int posInRw, final double lat, final double lng, final String pid, final String typ){
            final Handler pkReqHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    int index = -1;

                    for(int i = 0; i< workingOn.size(); i++){
                        if(workingOn.get(i) == posInRw){
                            index = i;
                        }
                    }

                    if(index != -1){
                        hideLoading();
                        workingOn.remove(index);
                    }

                    if(msg.obj == ctx.getString(R.string.cod_100))
                    {
                        itemSave.setImageResource(R.drawable.saveno);
                        parkings.get(posInRw).setSaved(false);
                        Toast.makeText(ctx, R.string.err_noConn, Toast.LENGTH_LONG).show();
                    }else {

                        String mess = "";

                        if (msg.obj.toString().equals("msg_2")) {
                            mess = ctx.getString(R.string.msg_2);
                        } else if (msg.obj.toString().equals("msg_1")) {
                            mess = ctx.getString(R.string.msg_1);
                        }

                        Toast.makeText(ctx, mess, Toast.LENGTH_LONG).show();
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

                                if (jsonResponse.getBoolean("success")){
                                    sv.addParking(pid, typ);
                                }

                                msgtoSend.obj = jsonResponse.getString("msg");
                                pkReqHandler.sendMessage(msgtoSend);

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
                            msgtoSend.obj = ctx.getString(R.string.cod_100);
                            pkReqHandler.sendMessage(msgtoSend);
                        }
                    };


                    ParkingRequest pkReq_Internal = new ParkingRequest(responseListener, errorListener);
                    SaveUser sv = new SaveUser(ctx);
                    pkReq_Internal.addParking(sv.getEmail(), lat, lng, pid, typ);
                    RequestQueue qu = Volley.newRequestQueue(ctx);
                    qu.add(pkReq_Internal);
                }
            };


            Thread saveNewPk = new Thread(r);
            saveNewPk.start();
        }

        public void hideLoading(){
            pB.setVisibility(View.INVISIBLE);
        }

        public void showLoading(){
            pB.setVisibility(View.VISIBLE);
        }

        public void addNewCarPos(final String address, final LatLng loc){

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

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

            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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
                    CarPosition tmp = new CarPosition(loc, address);
                    tmp.setNotes(notesToCarPos.getText().toString());
                    carPosShared.addNewCarPosition(tmp);
                    alert.dismiss();

                    itemCarSave.setImageDrawable(null);
                    itemCarSave.setClickable(false);

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

        public void removePk(final int posInRw, final double lat, final double lng, final String pid, final String typ){

            final Handler pkReqHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    int index = -1;

                    for(int i = 0; i< workingOn.size(); i++){
                        if(workingOn.get(i) == posInRw){
                            index = i;
                        }
                    }

                    if(index != -1){
                        hideLoading();
                        workingOn.remove(index);
                    }

                    if(msg.obj == ctx.getString(R.string.cod_100))
                    {
                        itemSave.setImageResource(R.drawable.saveyes);
                        parkings.get(posInRw).setSaved(true);
                        Toast.makeText(ctx, R.string.err_noConn, Toast.LENGTH_LONG).show();
                    }else {
                        String mess = "";

                        if (msg.obj.toString().equals("msg_5")) {
                            mess = ctx.getString(R.string.msg_5);
                        } else if (msg.obj.toString().equals("msg_1")) {
                            mess = ctx.getString(R.string.msg_1);
                        }

                        Toast.makeText(ctx, mess, Toast.LENGTH_LONG).show();

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

                                if (jsonResponse.getBoolean("success")){
                                    sv.removeParking(pid, typ);
                                }

                                msgtoSend.obj = jsonResponse.getString("msg");
                                pkReqHandler.sendMessage(msgtoSend);
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
                            msgtoSend.obj = ctx.getString(R.string.cod_100);
                            pkReqHandler.sendMessage(msgtoSend);
                        }
                    };


                    ParkingRequest pkReq_Internal = new ParkingRequest(responseListener, errorListener);
                    SaveUser sv = new SaveUser(ctx);
                    pkReq_Internal.removeSavedParking(sv.getEmail(), lat, lng);
                    RequestQueue qu = Volley.newRequestQueue(ctx);
                    qu.add(pkReq_Internal);

                }
            };


            Thread saveNewPk = new Thread(r);
            saveNewPk.start();
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_parking_card, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int indx) {

        PlaceInternal tmpPlc = parkings.get(indx);

        viewHolder.itemTitle.setText(tmpPlc.getName());
        viewHolder.itemIcon.setImageResource(tmpPlc.getTypeResource(ctx));
        viewHolder.itemType.setText(ctx.getString(R.string.type)+cm.decodeAndTranslate(ctx, tmpPlc.getType()));
        viewHolder.itemAddress.setText(ctx.getString(R.string.address)+tmpPlc.getAdress());

        if(isMap) {
            float[] res = new float[1];
            Location.distanceBetween(parkings.get(indx).getLocation().latitude, parkings.get(indx).getLocation().longitude,
                    mainMkr.getPosition().latitude, mainMkr.getPosition().longitude, res);

            int defRes = (int)res[0];
            String unit = "m";

            while(defRes >= 1000){
                defRes = defRes/1000;
                unit = "km";
            }

            viewHolder.itemDist.setText(ctx.getString(R.string.approxDist)+ defRes+unit);
        } else {
            viewHolder.itemDist.setText(ctx.getString(R.string.approxDistNA));
        }

        if(tmpPlc.getSaved()){
            viewHolder.itemSave.setImageResource(R.drawable.saveyes);
        } else {
            viewHolder.itemSave.setImageResource(R.drawable.saveno);
        }

        if(savedCarPos != null){
            for (int i=0; i<savedCarPos.size(); i++){

                if(tmpPlc.getLocation().longitude == savedCarPos.get(i).getPos().longitude && tmpPlc.getLocation().latitude == savedCarPos.get(i).getPos().latitude){
                    viewHolder.itemCarSave.setImageDrawable(null);
                    viewHolder.itemCarSave.setClickable(false);
                }
            }
        }

        boolean flag = false;
        int i=0;

        while(!flag && i<workingOn.size()) {
            if(workingOn.get(i) == indx) {
                flag = true;
            }
            i++;
        }

        if(flag){
            viewHolder.pB.setVisibility(View.VISIBLE);
        } else {
            viewHolder.pB.setVisibility(View.INVISIBLE);
        }

    }


    public void clear() {
        int size = this.parkings.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.parkings.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    @Override
    public int getItemCount() {
        return parkings.size();
    }

}
