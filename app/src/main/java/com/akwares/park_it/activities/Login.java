package com.akwares.park_it.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.LoginRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Login extends AppCompatActivity {

    @BindView(R.id.txtUsername) EditText _usernameText;
    @BindView(R.id.etxtPass) EditText _passwordText;
    @BindView(R.id.btnLogin) Button _loginButton;
    @BindView(R.id.buttonReg) TextView _signupLink;

    Intent in;
    Context thisContext;

    String username;
    String password;
    boolean loggedIN;

    ProgressDialog dialog;

    CommonFunctions cm = new CommonFunctions();

    int cr1 = 0;//password lenght
    int cr2 = 0;//password lenght

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        thisContext = Login.this;


        cm.disableButton(_loginButton);


        _loginButton.setOnClickListener((View v) -> {

            if(cm.isOnline(thisContext)){

                dialog = ProgressDialog.show(Login.this, "", getResources().getString(R.string.logging), true);

                username = _usernameText.getText().toString();
                password = _passwordText.getText().toString();
                cm.disableButton(_loginButton);
                _signupLink.setEnabled(false);
                login();
            } else {
                cm.errorSnackbar(Login.this, getString(R.string.err_noConn));
            }
        });


        _signupLink.setOnClickListener((View v) -> {
            // Start the Signup activity
            cm.disableButton(_loginButton);
            Intent intent = new Intent(getApplicationContext(), RegisterPage1.class);
            startActivity(intent);
            finish();
        });


        _passwordText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cr1 = _passwordText.getText().toString().length();

                if(cr1 <= 5 || cr2 <= 5 || cr1 > 20 || cr2 >20) {
                    cm.disableButton(_loginButton);
                }
                else
                    cm.enableButton(_loginButton);
            }

        });

        _usernameText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cr2 = _usernameText.getText().toString().length();

                if(cr1 <= 5 || cr2 <= 5 || cr1 > 20 || cr2 >20) {
                    cm.disableButton(_loginButton);
                }
                else
                    cm.enableButton(_loginButton);
            }

        });

        in= new Intent(getApplicationContext(), MainContainer.class);
    }

    Handler logReqHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.obj == getString(R.string.cod_101))
            {
                dialog.dismiss();
                cm.enableButton(_loginButton);
                _signupLink.setEnabled(true);
                cm.errorSnackbar(Login.this , getString(R.string.err_noConn));
            }else {
                if(loggedIN)
                {
                    onLoginSuccess((JSONObject) msg.obj);
                }else {
                    onLoginFailed((JSONObject) msg.obj);
                }
            }
            return false;
        }
    });

    public void login()
    {
        Runnable r = () -> {

                final Message msgtoSend = logReqHandler.obtainMessage();
                Response.Listener<String> responseListener= (String response) -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        loggedIN = jsonResponse.getBoolean("success");

                        msgtoSend.obj = jsonResponse;
                        logReqHandler.sendMessage(msgtoSend);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };

                Response.ErrorListener errorListener = (VolleyError error) -> {

                    msgtoSend.obj = getString(R.string.cod_101);
                    logReqHandler.sendMessage(msgtoSend);
                };


                LoginRequest loginReq_Internal = new LoginRequest(username, cm.SHA1(password), responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(Login.this);
                queue.add(loginReq_Internal);
        };

        Thread loginThr = new Thread(r);
        loginThr.start();
    }

    public void onLoginSuccess(JSONObject jo){

        dialog.dismiss();

        SaveUser sv = new SaveUser(Login.this);

        try {
            sv.setUser(_usernameText.getText().toString(), cm.SHA1(_passwordText.getText().toString()), jo.getString("email"), jo.getString("name"));
            sv.setMarkLogin(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SavedPlacesType sp = new SavedPlacesType(thisContext);
        sp.setRadius(500);

        finishAffinity();
        startActivity(in);
    }



    public void onLoginFailed(JSONObject jo) {

        dialog.dismiss();

        try {
            String msgWeb = jo.getString("error");

            String mess = "";

            if (msgWeb.equals("msg_8")) {
                mess = getString(R.string.msg_8);
            } else if (msgWeb.equals("msg_1")) {
                mess = getString(R.string.msg_1);
            } else if (msgWeb.equals("msg_9")) {
                mess = getString(R.string.msg_9);
            } else if (msgWeb.equals("msg_10")) {
                mess = getString(R.string.msg_10);
            } else if (msgWeb.equals("msg_18")) {
                mess = getString(R.string.msg_18);
            }

            cm.errorSnackbar(Login.this, mess);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        cm.enableButton(_loginButton);
        _signupLink.setEnabled(true);
    }


}