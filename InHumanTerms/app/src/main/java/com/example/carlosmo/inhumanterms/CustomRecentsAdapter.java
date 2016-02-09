package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Carlos Mo on 04/12/2015.
 */
public class CustomRecentsAdapter extends ArrayAdapter<TermListItem> {
    Context context;
    List<TermListItem> termsList;
    int layoutResID;

    public CustomRecentsAdapter(Context context, int layoutResourceID, List<TermListItem> data)
    {
        super(context, layoutResourceID, data);
        this.context = context;
        this.termsList = data;
        this.layoutResID = layoutResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TermListItem termListItem = (TermListItem) this.termsList.get(position);

        if(convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResID, parent, false);
        }

        // Lookup view for data population
        TextView tv = (TextView) convertView.findViewById(R.id.recent_item);

        // Populate data into template view
        tv.setText(termListItem.getTerm());
        tv.setTag(termListItem.getId());

        return convertView;
    }
}
