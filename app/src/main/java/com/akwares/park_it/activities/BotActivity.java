package com.akwares.park_it.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.akwares.park_it.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BotActivity extends AppCompatActivity {

    @BindView(R.id.btn_send) FloatingActionButton sendMessage;
    @BindView(R.id.ed_msg) EditText enterMessage;
    @BindView(R.id.listViewMsg) ListView listView;
    List<String> listData;
    ArrayAdapter<String> arrayAdapter;

    Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);
        setTitle(R.string.open_chat);

        ButterKnife.bind(this);

        thisContext = this;

        sendMessage.setOnClickListener((View v) -> {
            String msg = enterMessage.getText().toString();
            if(!TextUtils.isEmpty(msg)){
                predict(msg);
            }else{
                Toast.makeText(thisContext, "Empty string !!", Toast.LENGTH_SHORT).show();
            }
        });

        listData = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(arrayAdapter);
    }

    public void predict(String msg){
        listData.add(msg);
        if ( msg.toLowerCase().contains("hi") || msg.toLowerCase().contains("hello") ){
            listData.add("Hi, How may I help you");
        }
        else if ( msg.toLowerCase().contains("location") ){
            listData.add("Check google location for precise location services or tap location icon on dashboard");
        }
        else{
            listData.add("No answer found !!");
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(arrayAdapter);
        enterMessage.setText("");
    }
}
