package com.akwares.park_it.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.akwares.park_it.R;
import com.akwares.park_it.activities.Terms;


/**
 * A simple {@link Fragment} subclass.
 */
public class About extends Fragment {

    Button contact;
    Button terms;
    View view;
    private Button rateme;

    static final int CLOSE_FILTER_REQ_CODE = 1;

    public About() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_about, container, false);


        terms = (Button) view.findViewById(R.id.terms);
        contact = (Button) view.findViewById(R.id.contactMe);
        rateme = (Button) view.findViewById(R.id.rateme);

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.adminEmail) });
                Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.adminEmailSubject));
                startActivity(Intent.createChooser(Email, ""));
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent = new Intent(getContext(), Terms.class);
                        startActivityForResult(intent, CLOSE_FILTER_REQ_CODE);
                        getActivity().overridePendingTransition(R.anim.slideupactivity, R.anim.staybackactivity);
                    }
            });

        rateme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateME();
            }
        });


        return view;

    }

    public void rateME()
    {
        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.akwares.prak_it"); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}
