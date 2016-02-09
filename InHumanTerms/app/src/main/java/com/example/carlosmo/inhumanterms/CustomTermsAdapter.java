package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Carlos Mo on 23/11/2015.
 */

// Note: Old adapter that was used before switching to a custom cursor adapter due to performance
// Source: http://stackoverflow.com/questions/7129069/how-to-show-alphabetical-letters-on-side-of-android-listview
// Source: http://twistbyte.com/tutorial/android-listview-with-fast-scroll-and-section-index
public class CustomTermsAdapter extends ArrayAdapter<TermListItem> implements SectionIndexer {
    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;

    Context context;
    List<TermListItem> termsList;
    int layoutResID;

    public CustomTermsAdapter(Context context, int layoutResourceID, List<TermListItem> data)
    {
        super(context, layoutResourceID, data);
        this.context = context;
        this.termsList = data;
        this.layoutResID = layoutResourceID;

        alphaIndexer = new HashMap<String, Integer>();

        for (int i = 0; i < data.size(); i++) {
            TermListItem t = data.get(i);

            // get the first letter of the term
            String ch = t.getTerm().substring(0, 1).toUpperCase();

            // HashMap will prevent duplicates
            alphaIndexer.put(ch, i);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);

        /*
        for (int i = 0; i < sectionList.size(); i++)
            sections[i] = sectionList.get(i);
        */
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TermListItem termListItem = (TermListItem) this.termsList.get(position);

        if(convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResID, parent, false);
        }

        // Lookup view for data population
        TextView tv = (TextView) convertView.findViewById(R.id.term_item);

        // Populate data into template view
        tv.setText(termListItem.getTerm());
        tv.setTag(termListItem.getId());

        return convertView;
    }

    // Performs a binary search or cache lookup to find the first row that matches a given section's starting letter.
    public int getPositionForSection(int section)
    {
        return alphaIndexer.get(sections[section]);
    }

    // Returns the section index for a given position in the list by querying the item and comparing it with all items
    // in the section array.
    public int getSectionForPosition(int position)
    {
        for ( int i = sections.length - 1; i >= 0; i-- ) {
            if ( position >= alphaIndexer.get(sections[i])) {
                return i;
            }
        }
        return 0;

        // return 1;
    }

    // Returns the section array constructed from the alphabet provided in the constructor.
    public Object[] getSections()
    {
        return sections;
    }
}