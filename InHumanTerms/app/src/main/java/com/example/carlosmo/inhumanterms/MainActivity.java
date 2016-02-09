package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    DatabaseHelper db;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate the database helper object
        db = new DatabaseHelper(this);

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up the custom action bar
        restoreActionBar();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        // Source: http://stackoverflow.com/questions/20638967/how-to-change-fragments-using-android-navigation-drawer

        Fragment fragment;
        switch(position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new FavouritesFragment();
                break;
            case 2:
                fragment = new RecentFragment();
                break;
            case 3:
                fragment = new DictionariesFragment();
                break;
            case 4:
                fragment = new AboutFragment();
                break;
            default:
                fragment = new HomeFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.app_name);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }

    // Sets up the app's action bar
    public void restoreActionBar() {
        // Source: http://stackoverflow.com/questions/15804805/android-action-bar-searchview-as-autocomplete
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.grass)));
        //actionBar.setTitle(mTitle);

        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_searchbox, null);

        // Prepare cursor adapter for autocomplete search
        // Source: http://www.outofwhatbox.com/blog/2010/11/android-simpler-autocompletetextview-with-simplecursoradapter/
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.custom_search_dropdown_item,
                db.getAllTermListItemsCursor(getString(R.string.TABLE_TERMS)),
                new String[] { getString(R.string.KEY_TERM) },
                new int[] { R.id.search_item_term });

        // Attach the adapter to the autocompletetextview
        AutoCompleteTextView textView = (AutoCompleteTextView) v.findViewById(R.id.search_field);
        textView.setAdapter(adapter);

        // Set an OnItemClickListener, when a choice is made in the AutoCompleteTextView
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the term id from this row in the database.
                int termId = cursor.getInt(cursor.getColumnIndexOrThrow(getString(R.string.KEY_ID)));

                // Add term to recent searches
                db.addTermWithId(db.getTerm(termId, getString(R.string.TABLE_TERMS)), getString(R.string.TABLE_RECENTS));

                // Display the view term fragment
                Bundle bundle = new Bundle();
                bundle.putInt("id", termId);

                Fragment fragment = new ViewTermFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Set the CursorToStringConverter, to provide the labels for the choices to be displayed in the AutoCompleteTextView.
        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public String convertToString(android.database.Cursor cursor) {
                // Get the label for this row out of the "state" column
                final int columnIndex = cursor.getColumnIndexOrThrow(getString(R.string.KEY_TERM));
                final String str = cursor.getString(columnIndex);
                return str;
            }
        });

        // Set the FilterQueryProvider, to run queries for choices that match the specified input.
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                // Search for states whose names begin with the specified letters.
                Cursor cursor = db.getAllTermListItemsCursorWithFilter(getString(R.string.TABLE_TERMS), constraint.toString());
                return cursor;
            }
        });

        // Set the custom view for the action bar
        actionBar.setCustomView(v);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        // If the search button was clicked
        if (id == R.id.action_search) {
            // Get what was inputted in the search field
            AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search_field);
            String searchTerm = actv.getText().toString();

            // Check if anything was inputted in the search field.
            if (searchTerm.isEmpty() || searchTerm.equals(getString(R.string.search_hint))) return true;
            else {
                // Check to see if there is an exact match in the database
                Cursor cursor = db.findTermCursor(getString(R.string.TABLE_TERMS), searchTerm);
                if (cursor.moveToFirst()) {
                    // Add term to recent searches
                    db.addTermWithId(db.getTerm(Integer.parseInt(cursor.getString(0)), getString(R.string.TABLE_TERMS)), getString(R.string.TABLE_RECENTS));

                    // Display the term details in another fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", Integer.parseInt(cursor.getString(0)));

                    Fragment fragment = new ViewTermFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    // Display search suggestions in another fragment
                    Bundle bundle = new Bundle();
                    bundle.putString("search", searchTerm);

                    Fragment fragment = new SuggestionsFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        }


        return super.onOptionsItemSelected(item);

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
