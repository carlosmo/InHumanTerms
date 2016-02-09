package com.example.carlosmo.inhumanterms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Carlos Mo on 04/12/2015.
 */
public class SuggestionsFragment extends Fragment {
    DatabaseHelper db;

    public SuggestionsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_suggestions, container, false);

        // Get bundle extras
        Bundle bundle = this.getArguments();
        String search = bundle.getString("search");

        // Get all suggestions from database
        db = new DatabaseHelper(getActivity());
        List<TermListItem> suggestionsList = db.getAllTermListItemsWithFilter(getString(R.string.TABLE_TERMS), search);

        // If the search term yields no results from the database
        if (suggestionsList.isEmpty()) {
            TextView didYouMean = (TextView) rootView.findViewById(R.id.didYouMean);
            didYouMean.setText(getString(R.string.noResults) + " " + search + ".");
        }
        else {
            // Configure the adapter and list view
            CustomSuggestionsAdapter adapter = new CustomSuggestionsAdapter(getActivity(), R.layout.custom_suggestion_item, suggestionsList);

            ListView lv = (ListView) rootView.findViewById(R.id.suggestionList);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the term list item object
                    TermListItem termListItem = (TermListItem) parent.getItemAtPosition(position);

                    // Add term to recent searches
                    db.addTermWithId(db.getTerm(termListItem.getId(), getString(R.string.TABLE_TERMS)), getString(R.string.TABLE_RECENTS));

                    // Display the details of the selected term
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
        }

        return rootView;
    }
}
