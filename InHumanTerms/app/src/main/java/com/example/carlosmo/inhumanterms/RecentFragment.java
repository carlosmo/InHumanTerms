package com.example.carlosmo.inhumanterms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos Mo on 20/11/2015.
 */
public class RecentFragment extends Fragment {
    DatabaseHelper db;

    public RecentFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent, container, false);

        // Get all recent terms from database
        db = new DatabaseHelper(getActivity());
        List<TermListItem> recentsList = db.getAllTermListItems(getString(R.string.TABLE_RECENTS));

        // Configure the adapter and list view
        CustomRecentsAdapter adapter = new CustomRecentsAdapter(getActivity(), R.layout.custom_recent_item, recentsList);

        ListView lv = (ListView) rootView.findViewById(R.id.recentList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the term list item object
                TermListItem termListItem = (TermListItem) parent.getItemAtPosition(position);

                Bundle bundle = new Bundle();
                bundle.putInt("id", termListItem.getId());

                Fragment fragment = new ViewTermFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return rootView;
    }
}
