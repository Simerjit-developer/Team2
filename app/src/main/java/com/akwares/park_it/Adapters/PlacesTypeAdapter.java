package com.akwares.park_it.Adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.VolleyReq.PlaceRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Akshay on 21/01/2019.
 */
public class PlacesTypeAdapter extends RecyclerView.Adapter<PlacesTypeAdapter.ViewHolder> {

    Context ctx;
    CommonFunctions cm;
    ArrayList<String> types;
    SavedPlacesType sp;
    ArrayList<Integer> workingOn;

    public PlacesTypeAdapter(Context c, ArrayList placesType/* SONO tutti i possibili */) {
        ctx = c;
        cm = new CommonFunctions();

        sp = new SavedPlacesType(ctx); //per quelli selezionati da utente
        types = placesType; //sono tutti i possibili

        workingOn = new ArrayList<>(1);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView itemTitle;
        ImageView itemIcon;
        ProgressBar progress;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemTitle = (TextView)itemView.findViewById(R.id.placeName);
            itemIcon = (ImageView) itemView.findViewById(R.id.tick);
            progress = (ProgressBar) itemView.findViewById(R.id.addingOrRemovingProgress);

            itemView.setOnClickListener(new View.OnClickListener() {
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

                    if (!flag)
                    {

                        String plc = types.get(pos);

                        if(plc.compareTo("parking") != 0){

                            workingOn.add(pos);

                            showLoading();

                            boolean exists = false;
                            String[] saved = sp.getPlacesType();
                            if(saved!=null)
                                for (int k =0; k<saved.length; k++){
                                    if (plc.equals(saved[k])){
                                        exists = true;
                                    }
                                }



                            if(!exists){
                                itemIcon.setImageResource(R.drawable.choose);
                                sp.addPlaceType(plc);
                                addNewPl(plc, pos);
                            }else{
                                itemIcon.setImageResource(R.drawable.nochoose);
                                sp.removeType(plc);
                                removePl(plc, pos);
                            }

                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.err_cantRemParkingPLace), Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });

        }

        public void hideLoading(){
            progress.setVisibility(View.INVISIBLE);
        }

        public void showLoading(){
            progress.setVisibility(View.VISIBLE);
        }

        public void addNewPl(final String place, final int pos){

            final Handler plReqHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    int index = -1;

                    for(int i = 0; i< workingOn.size(); i++){
                        if(workingOn.get(i) == pos){
                            index = i;
                        }
                    }

                    if(index != -1){
                        workingOn.remove(index);
                        hideLoading();
                    }

                    if(msg.obj == ctx.getString(R.string.cod_100))
                    {
                        sp.removeType(place);
                        itemIcon.setImageResource(R.drawable.nochoose);
                        Toast.makeText(ctx, R.string.err_noConn, Toast.LENGTH_LONG).show();
                    }else {
                        String mess = "";

                        if(msg.obj == ctx.getString(R.string.cod_1)){
                            sp.removeType(place);
                            itemIcon.setImageResource(R.drawable.nochoose);
                            mess = ctx.getString(R.string.msg_1);
                        }

                        if (msg.obj.toString().equals("msg_6")) {
                            mess = ctx.getString(R.string.msg_6);
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

                    final Message msgtoSend = plReqHandler.obtainMessage();

                    Response.Listener<String> responseListener= new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {

                                JSONObject jsonResponse = new JSONObject(response);

                                String mess = jsonResponse.getString("msg");

                                if (jsonResponse.getBoolean("success")){
                                    sp.addPlaceType(place);
                                } else {
                                    mess = ctx.getString(R.string.cod_1);
                                }

                                msgtoSend.obj = mess;
                                plReqHandler.sendMessage(msgtoSend);

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
                            plReqHandler.sendMessage(msgtoSend);
                        }
                    };


                    PlaceRequest plReq_Internal = new PlaceRequest(responseListener, errorListener);
                    SaveUser sv = new SaveUser(ctx);
                    plReq_Internal.addPlace(sv.getEmail(), place);
                    RequestQueue qu = Volley.newRequestQueue(ctx);
                    qu.add(plReq_Internal);
                }
            };


            Thread saveNewPl = new Thread(r);
            saveNewPl.start();
        }

        public void removePl(final String typ, final int pos){

            final Handler plReqHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {

                    int index = -1;

                    for(int i = 0; i< workingOn.size(); i++){
                        if(workingOn.get(i) == pos){
                            index = i;
                        }
                    }

                    if(index != -1){
                        workingOn.remove(index);
                        hideLoading();
                    }


                    if(msg.obj == ctx.getString(R.string.cod_100))
                    {
                        sp.addPlaceType(typ);
                        itemIcon.setImageResource(R.drawable.choose);
                        Toast.makeText(ctx, R.string.err_noConn, Toast.LENGTH_LONG).show();
                    }else {

                        String mess = "";

                        if(msg.obj == ctx.getString(R.string.cod_1)){
                            mess = ctx.getString(R.string.msg_1);
                            sp.addPlaceType(typ);
                            itemIcon.setImageResource(R.drawable.choose);
                        }

                        if (msg.obj.toString().equals("msg_7")) {
                            mess = ctx.getString(R.string.msg_7);
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

                    final Message msgtoSend = plReqHandler.obtainMessage();

                    Response.Listener<String> responseListener= new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {

                                JSONObject jsonResponse = new JSONObject(response);

                                String mess = jsonResponse.getString("msg");

                                if (!jsonResponse.getBoolean("success")){

                                    mess = ctx.getString(R.string.cod_1);
                                }

                                msgtoSend.obj = mess;
                                plReqHandler.sendMessage(msgtoSend);
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
                            plReqHandler.sendMessage(msgtoSend);
                        }
                    };


                    PlaceRequest plReq_Internal = new PlaceRequest(responseListener, errorListener);
                    SaveUser sv = new SaveUser(ctx);
                    plReq_Internal.removeSavedPlace(sv.getEmail(), typ);
                    RequestQueue qu = Volley.newRequestQueue(ctx);
                    qu.add(plReq_Internal);
                }
            };


            Thread saveNewPl = new Thread(r);
            saveNewPl.start();
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_placeype_card, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int indx) {

        String tmpPlc = types.get(indx);

        viewHolder.itemTitle.setText(cm.decodeAndTranslate(ctx, tmpPlc));

        if(sp.exists(tmpPlc)){
            viewHolder.itemIcon.setImageResource(R.drawable.choose);
        } else {
            viewHolder.itemIcon.setImageResource(R.drawable.nochoose);
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
            viewHolder.progress.setVisibility(View.VISIBLE);
        } else {
            viewHolder.progress.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return types.size();
    }
}
