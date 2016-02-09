package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;

/**
 * Created by Carlos Mo on 20/11/2015.
 */
public class ViewDictionaryFragment extends Fragment {
    DatabaseHelper db;
    String dictionary;

    public ViewDictionaryFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_view_dictionary, container, false);

        db = new DatabaseHelper(getActivity());

        //String dictionary;
        int index;

        // Source: http://stackoverflow.com/questions/15392261/android-pass-dataextras-to-a-fragment
        Bundle bundle = this.getArguments();
        dictionary = bundle.getString("dictionary");
        index = bundle.getInt("index");

        // Set dictionary title
        TextView tv = (TextView) rootView.findViewById(R.id.viewDictionaryTitle);
        tv.setText(dictionary);

        // Set dictionary icon
        ImageView iv = (ImageView) rootView.findViewById(R.id.viewDictionaryIcon);
        iv.setImageResource(getResources().obtainTypedArray(R.array.home_dictionary_icons).getResourceId(index, 0));

        /*
        // Execute async task to set up listview
        LoadListTask loadListTask = new LoadListTask(getActivity(), dictionary);
        loadListTask.execute();
        */

        // Execute async task to load the list view
        LoadListTaskByCursor loadListTaskByCursor = new LoadListTaskByCursor(getActivity(), dictionary);
        loadListTaskByCursor.execute();

        /*
        Thread t = new Thread() {
            public void run() {
                // do the long operation on this thread
                final Cursor cursor =  db.getAllTermListItemsByDictionaryCursor(getString(R.string.TABLE_TERMS), dictionary);

                // Create a runnable to run on the UI thread.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = (ListView) rootView.findViewById(R.id.viewDictionaryList);

                        lv.setAdapter(new CustomTermsCursorAdapter(getActivity(),
                                R.layout.custom_term_item,
                                cursor,
                                new String[]{getString(R.string.KEY_ID), getString(R.string.KEY_TERM)},
                                new int[]{R.id.term_item}));

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // Get the cursor, positioned to the corresponding row in the result set
                                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                                // Get the term id from this row in the database.
                                int termId = cursor.getInt(cursor.getColumnIndexOrThrow(getString(R.string.KEY_ID)));

                                Bundle bundle = new Bundle();
                                bundle.putInt("id", termId);

                                Fragment fragment = new ViewTermFragment();
                                fragment.setArguments(bundle);
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction()
                                        .add(R.id.container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }
                });
            }
        };

        // spawn thread
        t.start();
        // wait for thread to finish
        try {
            t.join();
        }
        catch (InterruptedException e) { Log.e(dictionary, e.getMessage()); }
        */

        /*
        // Hide the progress bar
        ProgressBar myProgressBar = (ProgressBar) rootView.findViewById(R.id.myProgressBar);
        myProgressBar.setVisibility(View.INVISIBLE);
        */

        return rootView;
    }


    // AsyncTask that queries the database and loads the list view using a cursor
    // Source: http://codereview.stackexchange.com/questions/44789/using-an-asynctask-to-populate-a-listview-in-a-fragment-from-a-sqlite-table
    private class LoadListTaskByCursor extends AsyncTask<Void, Void, Cursor> {
        private String dictionary;
        private Activity activity;


        public LoadListTaskByCursor (Activity activity, String dictionary) {
            this.dictionary = dictionary;
            this.activity = activity;
        }

        @Override
        protected  void onPreExecute() { }

        @Override
        protected void onPostExecute(Cursor result) {
            // Configure and adapater and listview
            ListView lv = (ListView) activity.findViewById(R.id.viewDictionaryList);

            lv.setAdapter(new CustomTermsCursorAdapter(activity,
                    R.layout.custom_term_item,
                    result,
                    new String[]{getString(R.string.KEY_ID), getString(R.string.KEY_TERM)},
                    new int[]{R.id.term_item}));

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the cursor, positioned to the corresponding row in the result set
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                    // Get the term id from this row in the database.
                    int termId = cursor.getInt(cursor.getColumnIndexOrThrow(getString(R.string.KEY_ID)));

                    Bundle bundle = new Bundle();
                    bundle.putInt("id", termId);

                    Fragment fragment = new ViewTermFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .add(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            // Temporary solution to dismiss progress bar upon completion of loading listview
            // Note: To fix - the blocked UI does not allow the progress bar to animate
            lv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);

                    // Hide the progress bar
                    ProgressBar myProgressBar = (ProgressBar) activity.findViewById(R.id.myProgressBar);
                    myProgressBar.setVisibility(View.INVISIBLE);
                }
            });

        }

        @Override
        protected Cursor doInBackground(Void... params) {
            Cursor cursor = null;
            try {
                // Long running background process
                cursor = db.getAllTermListItemsByDictionaryCursor(getString(R.string.TABLE_TERMS), dictionary);
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }

            return cursor;
        }
    }



    /*
    // Note: Old method that was used with custom array adapter instead of custom cusor adapter
    // AsyncTask that queries the database and loads the list view
    // Source: http://codereview.stackexchange.com/questions/44789/using-an-asynctask-to-populate-a-listview-in-a-fragment-from-a-sqlite-table
    private class LoadListTask extends AsyncTask<Void, String, List<TermListItem>> {
        private ProgressDialog progressDialog;
        private String dictionary;


        public LoadListTask(Activity activity, String dictionary) {
            progressDialog = new ProgressDialog(activity);
            this.dictionary = dictionary;
        }

        @Override
        protected  void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<TermListItem> result) {
            CustomTermsAdapter adapter = new CustomTermsAdapter(getActivity(), R.layout.custom_term_item, result);

            ListView lv = (ListView) getActivity().findViewById(R.id.viewDictionaryList);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(getActivity(), "I clicked on row " + position + " and my tag is " + view.getTag(), Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", (int)view.getTag());

                    Fragment fragment = new ViewTermFragment();
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .add(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected List<TermListItem> doInBackground(Void... params) {
            List<TermListItem> termListItems = null;
            try {
                // do the background process
                termListItems = db.getAllTermListItemsByDictionary(getString(R.string.TABLE_TERMS), dictionary);
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }

            return termListItems;
        }
    }
    */
}
