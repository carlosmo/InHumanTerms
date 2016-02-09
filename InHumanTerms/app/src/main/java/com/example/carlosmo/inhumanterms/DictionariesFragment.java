package com.example.carlosmo.inhumanterms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Carlos Mo on 20/11/2015.
 */
public class DictionariesFragment extends Fragment {
    DatabaseHelper db;

    public DictionariesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dictionaries, container, false);

        db = new DatabaseHelper(getActivity());

        // Get the list of dictionaries
        List<String> dictionariesList = Arrays.asList(getActivity().getResources().getStringArray(R.array.dictionary_names));

        // Configure the adapter and listview
        CustomDictionariesAdapter adapter = new CustomDictionariesAdapter(getActivity(), R.layout.custom_dictionaries_item, dictionariesList);

        ListView lv = (ListView) rootView.findViewById(R.id.dictionariesList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDictionary = (String) parent.getItemAtPosition(position);

                // Dislay error message if dictionary is not installed
                if (!db.isUserDictionaryInstalled(selectedDictionary)) {
                    Toast.makeText(getActivity(), selectedDictionary + " is not currently installed. ", Toast.LENGTH_SHORT).show();
                } else {
                    // Source: http://stackoverflow.com/questions/15392261/android-pass-dataextras-to-a-fragment
                    // Pass the selected dictionary to the fragment with bundles
                    Bundle bundle = new Bundle();
                    bundle.putString("dictionary", selectedDictionary);
                    bundle.putInt("index", position);

                    Fragment fragment = new ViewDictionaryFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return rootView;
    }
}
