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
public class CustomDictionariesAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> dictionariesList;
    DatabaseHelper db;
    int layoutResID;

    public CustomDictionariesAdapter(Context context, int layoutResourceID, List<String> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.dictionariesList = listItems;
        this.layoutResID = layoutResourceID;
        db = new DatabaseHelper(context);
    }

    // Source: http://stackoverflow.com/questions/16453379/android-list-adapter-returns-wrong-position-in-getview
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        DictionariesItemHolder dictionariesHolder;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResID, parent, false);

            dictionariesHolder = new DictionariesItemHolder();
            dictionariesHolder.dictionaryRowIcon = (ImageView) view.findViewById(R.id.dictionaryRowIcon);
            dictionariesHolder.dictionaryTitle = (TextView) view.findViewById(R.id.dictionaryTitle);
            dictionariesHolder.manageDictionaryBtn = (ImageButton) view.findViewById(R.id.manageDictionaryBtn);

            dictionariesHolder.manageDictionaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer pos = (Integer) v.getTag();
                    final String dictionary = dictionariesList.get(pos);

                    // If the dictionary is already installed, delete it from database.
                    // Otherwise, install the dictionary.
                    if (db.isUserDictionaryInstalled(dictionary)) {
                        DeleteTask deleteTask = new DeleteTask((Activity)context, dictionary);
                        deleteTask.execute();
                    }
                    else {
                        InstallTask installTask = new InstallTask((Activity)context, dictionary);
                        installTask.execute();
                    }
                }
            });

            view.setTag(dictionariesHolder);
        } else {
            dictionariesHolder = (DictionariesItemHolder) view.getTag();
        }

        // Set dictionary title
        dictionariesHolder.dictionaryTitle.setText(dictionariesList.get(position));
        // Set dictionary icon
        // Source: http://stackoverflow.com/questions/6945678/android-storing-r-drawable-ids-in-xml-array
        dictionariesHolder.dictionaryRowIcon.setImageResource(context.getResources().obtainTypedArray(R.array.dictionary_icons).getResourceId(position, 0));
        // Set action icon
        if (db.isUserDictionaryInstalled(dictionariesList.get(position))) { dictionariesHolder.manageDictionaryBtn.setImageResource(R.drawable.fa_minus_circle); }
        else { dictionariesHolder.manageDictionaryBtn.setImageResource(R.drawable.fa_plus_circle); }
        dictionariesHolder.manageDictionaryBtn.setTag(position);

        return view;
    }

    // Conatiner for the dictionary row
    private static class DictionariesItemHolder {
        ImageView dictionaryRowIcon;
        TextView dictionaryTitle;
        ImageButton manageDictionaryBtn;
    }

    // Source: http://briandolhansky.com/blog/2013/7/11/snippets-android-async-progress
    // AysncsTask that installs a dictionary
    private class InstallTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private String dictionary;

        public InstallTask(Activity activity, String dictionary) {
            progressDialog = new ProgressDialog(activity);
            this.dictionary = dictionary;
        }

        @Override
        protected  void onPreExecute() {
            // Show the progress dialog
            progressDialog.setTitle("Installing " + dictionary);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            // Close the progress dialog
            if(progressDialog.isShowing()) {
                notifyDataSetChanged();
                progressDialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Long running background process -
                // Load the dictionary from CSV and insert terms into database
                db.loadTermsFromCSV(context.getString(R.string.TABLE_TERMS), dictionary);
                db.addUserDictionary(dictionary);
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }

            return null;
        }
    }

    // AysncsTask that deletes a dictionary
    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private String dictionary;

        public DeleteTask(Activity activity, String dictionary) {
            progressDialog = new ProgressDialog(activity);
            this.dictionary = dictionary;
        }

        @Override
        protected  void onPreExecute() {
            // Show the progress dialog
            progressDialog.setTitle("Deleting " + dictionary);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            // Close the progress dialog
            if(progressDialog.isShowing()) {
                notifyDataSetChanged();
                progressDialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Long running background process -
                // Delete all the terms belonging to the selected dictionary from the database
                db.deleteUserDictionary(dictionary);
            }
            catch (Exception e) { Log.e("tag", e.getMessage()); }

            return null;
        }
    }
}
