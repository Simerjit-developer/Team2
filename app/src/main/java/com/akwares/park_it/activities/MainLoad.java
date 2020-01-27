package com.akwares.park_it.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.akwares.park_it.Preferences.SaveUser;
import com.akwares.park_it.R;

public class MainLoad extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SaveUser sv = new SaveUser(getApplicationContext());

        Intent intent;

        if(!sv.isLoggedIn()) {
            intent = new Intent(getApplicationContext(), AboutMain.class);
        } else {
            intent = new Intent(getApplicationContext(), MainContainer.class);
        }

        overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
        startActivity(intent);
        finish();

    }


}

