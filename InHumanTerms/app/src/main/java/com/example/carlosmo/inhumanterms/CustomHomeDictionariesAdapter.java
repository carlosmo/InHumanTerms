package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Carlos Mo on 20/11/2015.
 */
public class CustomHomeDictionariesAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> dictionariesList;
    DatabaseHelper db;
    int layoutResID;

    public CustomHomeDictionariesAdapter(Context context, int layoutResourceID, List<String> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.dictionariesList = listItems;
        this.layoutResID = layoutResourceID;
        db = new DatabaseHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        DictionariesItemHolder dictionariesHolder;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResID, parent, false);

            dictionariesHolder = new DictionariesItemHolder();
            dictionariesHolder.homeDictionaryRowIcon = (ImageView) view.findViewById(R.id.homeDictionaryRowIcon);
            dictionariesHolder.homeDictionaryTitle = (TextView) view.findViewById(R.id.homeDictionaryTitle);

            view.setTag(dictionariesHolder);
        } else {
            dictionariesHolder = (DictionariesItemHolder) view.getTag();
        }

        // Set dictionary title
        dictionariesHolder.homeDictionaryTitle.setText(dictionariesList.get(position));
        // Set dictionary icon
        // Source: http://stackoverflow.com/questions/6945678/android-storing-r-drawable-ids-in-xml-array
        if (!dictionariesList.get(position).equals(context.getResources().getString(R.string.addNewDictionaryMessage))) {
            int index = Arrays.asList(context.getResources().getStringArray(R.array.dictionary_names)).indexOf(dictionariesList.get(position));
            dictionariesHolder.homeDictionaryRowIcon.setImageResource(context.getResources().obtainTypedArray(R.array.home_dictionary_icons).getResourceId(index, 0));
        }

        return view;
    }

    // Container for the dictionary row
    private static class DictionariesItemHolder {
        ImageView homeDictionaryRowIcon;
        TextView homeDictionaryTitle;
    }

}
