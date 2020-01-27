package com.akwares.park_it.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.akwares.park_it.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Terms extends AppCompatActivity {

    @BindView(R.id.termsView) WebView webview;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.staybackactivity, R.anim.slidedownactivity);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.terms));

        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("file:///android_asset/privacypolicy.html");
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
