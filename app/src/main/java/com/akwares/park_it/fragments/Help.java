package com.akwares.park_it.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.akwares.park_it.Preferences.SavedPlacesType;
import com.akwares.park_it.R;
import com.akwares.park_it.Utilities.CommonFunctions;
import com.akwares.park_it.activities.BotActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class Help extends Fragment {

    View view;
    Context thisContext;

    CommonFunctions cm;

    SavedPlacesType pt;
    FloatingActionButton btn_openChatbot;


    public Help() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_help, container, false);
        thisContext = view.getContext();
        cm = new CommonFunctions();
        pt = new SavedPlacesType(thisContext);

        btn_openChatbot = view.findViewById(R.id.fab_bot_chat);

        btn_openChatbot.setOnClickListener((View v) -> {

            Intent chatBot = new Intent(thisContext, BotActivity.class);
            startActivity(chatBot);
        });

        return view;
    }

}
