package com.akwares.park_it.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.UpdateInfoRequest;
import com.akwares.park_it.activities.ChangePassword;
import com.akwares.park_it.activities.Login;
import com.akwares.park_it.activities.MainContainer;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class Account extends Fragment {

    View view;
    Context thisContext;

    Button logout;
    Button save;
    Button changePSW;
    EditText name;
    EditText mail;
    EditText usrnm;

    CommonFunctions cm;
    private SaveUser sv;
    private ProgressDialog updatingPD;

    public Account() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_account, container, false);
        thisContext = view.getContext();
        cm = new CommonFunctions();
        sv = new SaveUser(thisContext);

        logout = (Button) view.findViewById(R.id.logoutBtn);
        save = (Button) view.findViewById(R.id.btnSave);
        changePSW = (Button) view.findViewById(R.id.btnPSWChange);
        mail = (EditText) view.findViewById(R.id.txtEmail);
        usrnm = (EditText) view.findViewById(R.id.txtUsername);
        name = (EditText) view.findViewById(R.id.txtName);

        mail.setText(sv.getEmail().toString());
        usrnm.setText(sv.getUsername().toString());

        mail.setEnabled(false);
        usrnm.setEnabled(false);

        name.setText(sv.getName().toString());
        cm.disableButton(save);

        onClickLogout();
        onClickSave();

        changePSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisContext, ChangePassword.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateName();
            }
        });



        return view;
    }

    private void onClickSave() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                updatingPD = ProgressDialog.show(thisContext, "", getString(R.string.updatingInfo), true);
                                updateName();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
                builder.setMessage(R.string.sureDialog).setPositiveButton(R.string.positive, dialogClickListener)
                        .setNegativeButton(R.string.negative, dialogClickListener).show();

            }
        });
    }

    public void updateName()
    {
        final Handler logReqHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                updatingPD.dismiss();

                if(msg.obj == getString(R.string.cod_101))
                {
                    cm.enableButton(save);
                    cm.errorSnackbar(getActivity(), getString(R.string.err_noConn));
                }else {
                    if(msg.obj == getString(R.string.cod_0))
                    {
                        sv.setName(name.getText().toString());
                        ((MainContainer)getActivity()).setNameOnNav();
                        cm.successSnackbar(getActivity(), getString(R.string.updatingInfosuccess));
                    } else {

                        try {

                            String msgWeb = ((JSONObject)msg.obj).getString("error");

                            String mess = "";

                            if (msgWeb.equals("msg_12")) {
                                mess = getString(R.string.msg_12);
                            } else if (msgWeb.equals("msg_1")) {
                                mess = getString(R.string.msg_1);
                            }
                            cm.errorSnackbar(getActivity(), mess);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        cm.enableButton(save);
                    }
                }
                return false;
            }
        });


        Runnable r = new Runnable() {
            @Override
            public void run() {

                boolean isConn;
                final Message msgtoSend = logReqHandler.obtainMessage();
                Response.Listener<String> responseListener= new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                msgtoSend.obj = getString(R.string.cod_0);
                            } else {
                                msgtoSend.obj = jsonResponse;
                            }

                            if(isAdded()){

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

                        if(isAdded())  {
                            logReqHandler.sendMessage(msgtoSend);
                        }
                    }
                };


                String email = sv.getEmail();

                UpdateInfoRequest updateNameReq_Internal = new UpdateInfoRequest(email, name.getText().toString(), "", "", responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(thisContext);
                queue.add(updateNameReq_Internal);
            }
        };

        Thread loginThr = new Thread(r);
        loginThr.start();
    }

    private void onClickLogout() {
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                boolean logSituation = sv.logOut();
                                if(logSituation)
                                {
                                    cm.clearAll(thisContext);
                                    Intent in = new Intent(thisContext, Login.class);
                                    getActivity().finishAffinity();
                                    startActivity(in);
                                } else
                                {
                                    cm.errorSnackbar(getActivity(), getString(R.string.err_logout));
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
                builder.setMessage(R.string.sureDialog).setPositiveButton(R.string.positive, dialogClickListener)
                        .setNegativeButton(R.string.negative, dialogClickListener).show();

            }
        });
    }

    public void validateName()
    {
        boolean isValid = false;

        String expression = "^[a-z0-9_-]{3,15}$";
        String inputStr;

        expression = "^[a-zA-Z ]+$";
        inputStr = name.getText().toString();

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }

        if(!isValid){
            name.setError(getString(R.string.err_name));
            cm.disableButton(save);
        }
        else
        {

            if(name.getText().toString().trim().length() <= 3 || name.getText().toString().trim().length() > 20)
            {
                name.setError(getString(R.string.err_nameLenght));
            } else {

                if(name.getText().toString().equals(sv.getName().toString().trim())){
                    name.setError(getString(R.string.samename_err));
                } else {
                    name.setError(null);
                }
            }

            if((name.getText().toString()).trim().length() != 0 && !name.getText().toString().trim().equals(sv.getName().toString().trim()) && name.getError() == null) {
                cm.enableButton(save);
            } else {
                cm.disableButton(save);
            }
        }

    }


}
