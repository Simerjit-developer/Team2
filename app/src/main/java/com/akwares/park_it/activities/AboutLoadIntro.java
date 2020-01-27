package com.akwares.park_it.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.akwares.park_it.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class AboutLoadIntro extends AppIntro {

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_name), getString(R.string.D1), R.drawable.iconplain, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.mapOpt), getString(R.string.D3), R.drawable.appintroscreen1, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.savedParkingOpt), getString(R.string.D5), R.drawable.appintroscreen3, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.settingsOpt), getString(R.string.D6), R.drawable.appintroscreen4, getResources().getColor(R.color.colorPrimary)));

        showSkipButton(false);

    }
}
