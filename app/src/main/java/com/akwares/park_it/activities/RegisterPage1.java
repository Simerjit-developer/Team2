package com.akwares.park_it.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RegisterPage1 extends AppCompatActivity {

    @BindView(R.id.txtEmail) EditText _email;
    @BindView(R.id.btnNext) Button _next;
    @BindView(R.id.buttonLog) Button _login;
    @BindView(R.id.acceptTermsCheckBox) CheckBox termsAccept;

    @BindView(R.id.termsTxtView) TextView tvTerms;

    CommonFunctions cm = new CommonFunctions();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page1);

        ButterKnife.bind(this);

        tvTerms.setOnClickListener((View v) -> {

            Intent intent = new Intent(RegisterPage1.this, Terms.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
        });

        cm.disableButton(_next);

        _email.addTextChangedListener(new TextWatcher(){

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (s.length() == 0)
                    cm.disableButton(_next);
                else {
                    if (!validateEmail(_email.getText().toString())){
                        cm.disableButton(_next);
                    } else
                        cm.enableButton(_next);

                }
            }


        });

        _next.setOnClickListener((View v) -> {

            if(termsAccept.isChecked()){
                // Start the Signup activity
                String email = _email.getText().toString();
                Intent intent = new Intent(getApplicationContext(), RegisterPage2.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                cm.AlertDialog(RegisterPage1.this, getString(R.string.terms), getString(R.string.termsDialog));
            }
        });

        _login.setOnClickListener((View v) -> {
            // Start the Login activity

            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

    }

    public boolean validateEmail(String email)
    {
        if(email.isEmpty())
            return  false;
        else
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();

    }

}
