package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Carlos Mo on 27/11/2015.
 *
 * */

// Section Indexer
// Reference: http://stackoverflow.com/questions/7129069/how-to-show-alphabetical-letters-on-side-of-android-listview
// Reference: http://twistbyte.com/tutorial/android-listview-with-fast-scroll-and-section-index

// Custom Cursor Adapter
// Reference: https://guides.codepath.com/android/Populating-a-ListView-with-a-CursorAdapter

/**
 * Adapter that exposes data from a Cursor to a ListView widget. The Cursor must include a column named "_id"
 * or this class will not work.
 */
// Source: http://www.rogcg.com/blog/2013/02/10/Implementing-a-ListView-with-AlphabetIndexer-and-CursorAdapter-on-Android#
public class CustomTermsCursorAdapter extends CursorAdapter implements SectionIndexer {
    AlphabetIndexer mAlphabetIndexer;

    public CustomTermsCursorAdapter(Context context, int layoutResourceId, Cursor cursor, String[] strings, int[] is) {
        super(context, cursor);

        mAlphabetIndexer = new AlphabetIndexer(cursor,
                cursor.getColumnIndex(context.getString(R.string.KEY_TERM)),
                " ABCDEFGHIJKLMNOPQRTSUVWXYZ");
        mAlphabetIndexer.setCursor(cursor);//Sets a new cursor as the data set and resets the cache of indices.

    }

    // Methods for Section Indexer

    /**
     * Returns the section array constructed from the alphabet provided in the constructor.
     */
    @Override
    public Object[] getSections() {
        return mAlphabetIndexer.getSections();
    }

    /**
     * Performs a binary search or cache lookup to find the first row that matches a given section's starting letter.
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        return mAlphabetIndexer.getPositionForSection(sectionIndex);
    }

    /**
     * Returns the section index for a given position in the list by querying the item and comparing it with all items
     * in the section array.
     */
    @Override
    public int getSectionForPosition(int position) {
        return mAlphabetIndexer.getSectionForPosition(position);
    }

    // Methods for Cursor Adapter

    /**
     * Makes a new view to hold the data pointed to by cursor.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View newView = inflater.inflate(
                R.layout.custom_term_item, parent, false);
        return newView;
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tv = (TextView) view.findViewById(R.id.term_item);
        // Extract properties from cursor
        String term = cursor.getString(cursor.getColumnIndexOrThrow(context.getString(R.string.KEY_TERM)));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(context.getString(R.string.KEY_ID)));
        // Populate fields with extracted properties
        tv.setText(term);
        // Set tag
        tv.setTag(id);
    }
}
