package com.akwares.park_it.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.VolleyReq.UpdateInfoRequest;
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

public class ChangePassword extends AppCompatActivity {

    Context thisContext;
    CommonFunctions cm;

    @BindView(R.id.pswOld) EditText oldpwd;
    @BindView(R.id.pswNew) EditText newpwd;
    @BindView(R.id.pswNewRep) EditText newpwdR;
    @BindView(R.id.btnSavepsw)  Button save;
    private ProgressDialog updatingPD;
    private SaveUser sv;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.staybackactivity, R.anim.slidedownactivity);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ButterKnife.bind(this);

        thisContext = this;
        cm = new CommonFunctions();
        sv = new SaveUser(thisContext);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account");

        oldpwd.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate("old");
            }
        });

        newpwd.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate("new");
            }
        });

        newpwdR.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate("newRep");
            }
        });


        cm.disableButton(save);
        onClickSave();

    }


    private void onClickSave() {
        save.setOnClickListener((View v) -> {

            updatingPD = ProgressDialog.show(thisContext, "", getString(R.string.updatingInfo), true);
            updatePSW();
        });
    }


    public void updatePSW()
    {

        final Handler logReqHandler = new Handler((Message msg) -> {

            updatingPD.dismiss();
            if(msg.obj == getString(R.string.cod_101))
            {
                cm.enableButton(save);
                cm.errorSnackbar(ChangePassword.this, getString(R.string.err_noConn));
            }else {
                if(msg.obj == getString(R.string.cod_0))
                {
                    Toast.makeText(thisContext, R.string.PSWchanged, Toast.LENGTH_LONG).show();
                    finish();
                } else {

                    cm.errorSnackbar(ChangePassword.this , getString(R.string.oldPSWerr));
                    cm.enableButton(save);
                }
            }
            return false;
        });


        Runnable r = () -> {

            final Message msgtoSend = logReqHandler.obtainMessage();
            Response.Listener<String> responseListener= (String response) -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        msgtoSend.obj = getString(R.string.cod_0);
                    } else {
                        msgtoSend.obj = getString(R.string.cod_1);
                    }

                    logReqHandler.sendMessage(msgtoSend);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };


            Response.ErrorListener errorListener = (VolleyError error) -> {
                    msgtoSend.obj = getString(R.string.cod_101);
                    logReqHandler.sendMessage(msgtoSend);
            };


            String email = sv.getEmail();

            UpdateInfoRequest updateNameReq_Internal = new UpdateInfoRequest(email, "", cm.SHA1(oldpwd.getText().toString()), cm.SHA1(newpwd.getText().toString()), responseListener, errorListener);
            RequestQueue queue = Volley.newRequestQueue(thisContext);
            queue.add(updateNameReq_Internal);

        };

        Thread loginThr = new Thread(r);
        loginThr.start();
    }



    public void validate(String tipo)
    {
        boolean isValid = false;

        String expression = "^[a-z0-9_-]{3,15}$";

        String inputStr;

        if(tipo == "old"){
            //per username
            inputStr  = oldpwd.getText().toString();
        }
        else {
            if (tipo == "new"){
                // per password
                inputStr = newpwd.getText().toString();
            }
            else {
                //per nome
                inputStr = newpwdR.getText().toString();
            }
        }

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches()) {
            isValid = true;
        }

        if(!isValid){
            if(tipo=="old") {
                oldpwd.setError(getString(R.string.err_alphanumericCh));
            } else {
                if (tipo == "new") {
                    newpwd.setError(getString(R.string.err_alphanumericCh));
                } else {
                    newpwdR.setError(getString(R.string.err_name));
                }
            }

            cm.disableButton(save);
        }
        else
        {

            if (tipo == "old") {
                if(oldpwd.getText().toString().length() <= 5 || oldpwd.getText().toString().length() > 20)
                {
                    oldpwd.setError(getString(R.string.err_passwordLenght));
                }else {
                    oldpwd.setError(null);
                }
            }

            if (tipo == "new") {

                if(newpwd.getText().toString().length() <= 5 || newpwd.getText().toString().length() > 20)
                {
                    newpwd.setError(getString(R.string.err_passwordLenght));
                }else {
                    if(oldpwd.getText().toString().equals(newpwd.getText().toString()))
                    {
                        newpwd.setError(getString(R.string.oldpwdeqnew));
                    }else {
                        newpwd.setError(null);
                    }
                }
            }

            if(tipo == "newRep") {
                if(!newpwd.getText().toString().equals(newpwdR.getText().toString()))
                {
                    newpwdR.setError(getString(R.string.err_samePSW));
                }else {
                    newpwdR.setError(null);
                }
            }


            if((oldpwd.getError() == null && newpwdR.getError() == null && newpwd.getError() == null) && (oldpwd.getText().length() != 0 && newpwdR.getText().length() != 0 && newpwd.getText().length() != 0)) {
                cm.enableButton(save);
            } else {
                cm.disableButton(save);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.staybackactivity, R.anim.slidedownactivity);
        }

        return super.onOptionsItemSelected(item);
    }
}
