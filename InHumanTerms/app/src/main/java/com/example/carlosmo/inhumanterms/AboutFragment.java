package com.example.carlosmo.inhumanterms;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by Carlos Mo on 22/11/2015.
 */
public class AboutFragment extends Fragment{
    public AboutFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        // Configure connect buttons
        ImageButton linkedinBtn = (ImageButton)rootView.findViewById(R.id.linkedinBtn);
        linkedinBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.linkedin));

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        ImageButton githubBtn = (ImageButton) rootView.findViewById(R.id.githubBtn);
        githubBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.github));

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        ImageButton personalsiteBtn = (ImageButton) rootView.findViewById(R.id.personalsiteBtn);
        personalsiteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.personalsite));

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        return rootView;
    }
}
