package com.akwares.park_it.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.RegisterRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterPage2 extends AppCompatActivity {

    @BindView(R.id.txtUsername) EditText _username;
    @BindView(R.id.txtName) EditText _name;
    String _email;
    @BindView(R.id.txtPass) EditText _password;

    @BindView(R.id.btnRegister) Button _register;
    CommonFunctions cm = new CommonFunctions();

    ProgressDialog dialog;
    boolean registered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page2);

        ButterKnife.bind(this);

        cm.disableButton(_register);

        _username.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate("username");
            }
        });

        _password.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate("password");

            }
        });

        _name.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate("name");
            }
        });



        Bundle ext = getIntent().getExtras();

        _email = ext.getString("email", "");

        _register.setOnClickListener((View v) -> {

            if(cm.isOnline(RegisterPage2.this)){

                cm.disableButton(_register);
                dialog = ProgressDialog.show(RegisterPage2.this, "", getResources().getString(R.string.registering), true);
                register();
            } else {
                cm.errorSnackbar(RegisterPage2.this, getString(R.string.err_noConn));
            }
        });

    }

    public void register()
    {

        final Handler regReqHandler = new Handler((Message msg) -> {

                if(msg.obj == getString(R.string.cod_100))
                {
                    dialog.dismiss();
                    cm.enableButton(_register);
                    cm.errorSnackbar(RegisterPage2.this, getString(R.string.err_noConn));
                }else {
                    if(registered)
                    {
                        onRegisterSuccess();
                    }else {
                        onRegisterFail((JSONObject) msg.obj);
                    }
                }
                return false;
        });

        Runnable r = () -> {

                final Message msgtoSend = regReqHandler.obtainMessage();
                final String email = _email;
                final String name = _name.getText().toString();
                final String username = _username.getText().toString();
                final String password = _password.getText().toString();

                Response.Listener<String> responseListener = (String response) -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        registered = jsonResponse.getBoolean("success");
                        msgtoSend.obj = jsonResponse;
                        regReqHandler.sendMessage(msgtoSend);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };

                Response.ErrorListener errorListener = (VolleyError error) -> {
                    msgtoSend.obj = getString(R.string.cod_100);
                    regReqHandler.sendMessage(msgtoSend);
                    Log.d("Here" , error.toString());
                };


                RegisterRequest registerReq_Internal = new RegisterRequest(email, name.trim(), username, cm.SHA1(password), responseListener, errorListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterPage2.this);
                queue.add(registerReq_Internal);

        };

        Thread registerThr = new Thread(r);
        registerThr.start();
    }

    public void onRegisterFail(JSONObject jb)
    {
        dialog.dismiss();
        cm.enableButton(_register);

        try {
            String msgWeb = jb.getString("error");

            String mess = "";

            if (msgWeb.equals("msg_11")) {
                mess = getString(R.string.msg_11);
            } else if (msgWeb.equals("msg_12")) {
                mess = getString(R.string.msg_12);
            } else if (msgWeb.equals("msg_13")) {
                mess = getString(R.string.msg_13);
            } else if (msgWeb.equals("msg_14")) {
                mess = getString(R.string.msg_14);
            } else if (msgWeb.equals("msg_15")) {
                mess = getString(R.string.msg_15);
            }  else if (msgWeb.equals("msg_16")) {
                mess = getString(R.string.msg_16);
            }

            cm.errorSnackbar(RegisterPage2.this, mess);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onRegisterSuccess()
    {
        dialog.dismiss();

        Intent intent = new Intent(getApplicationContext(), ActivateAccountActivity.class);
        startActivity(intent);
        finish();
    }

    public void validate(String tipo)
    {
        boolean isValid = false;

        String expression = "^[a-z0-9_-]{3,15}$";

        String inputStr;

        if(tipo == "username"){
            //per username
            inputStr  = _username.getText().toString();
        }
        else {
            if (tipo == "password"){
                // per password
                inputStr = _password.getText().toString();
            }
            else {
                //per nome
                expression = "^[a-zA-Z ]+$";
                inputStr = _name.getText().toString();
            }
        }

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }

        if(!isValid){
            if(tipo == "username") {
                _username.setError(getString(R.string.err_alphanumericCh));
            } else {
                if (tipo == "password") {
                    _password.setError(getString(R.string.err_alphanumericCh));
                } else {
                    _name.setError(getString(R.string.err_name));
                }
            }

            cm.disableButton(_register);
        }
        else
        {
            if(tipo == "username") {
                if(_username.getText().toString().length() <=5 || _username.getText().toString().length() > 20)
                {
                    _username.setError(getString(R.string.err_usernameLenght));
                } else {
                    _username.setError(null);
                }
            } else {
                if (tipo == "password") {
                    if(_password.getText().toString().length() <= 5 || _password.getText().toString().length() > 20)
                    {
                        _password.setError(getString(R.string.err_passwordLenght));
                    }else{
                        _password.setError(null);
                    }
                } else {
                    if(_name.getText().toString().length() <= 3 || _name.getText().toString().length() > 20)
                    {
                        _name.setError(getString(R.string.err_nameLenght));
                    } else {
                        _name.setError(null);
                    }
                }
            }

            if((_username.getError() == null && _password.getError() == null && _name.getError() == null) && (_username.getText().toString().trim().length() != 0 && _password.getText().length() != 0 && _name.getText().toString().trim().length() != 0)) {
                cm.enableButton(_register);
            } else {
                cm.disableButton(_register);
            }
        }

    }
}
