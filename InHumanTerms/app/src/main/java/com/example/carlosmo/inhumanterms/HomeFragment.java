package com.example.carlosmo.inhumanterms;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Carlos Mo on 20/11/2015.
 */
public class HomeFragment extends Fragment implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    DatabaseHelper db;
    ImageButton homeFavouriteBtn, homeTextToSpeechBtn;
    Term randomTerm;
    TextToSpeech tts;

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Get all dictionaries that the user has installed from the database
        db = new DatabaseHelper(getActivity());
        List<String> homeDictionariesList = db.getAllUserDictionaries();

        // If no dictionaries are installed, show a message to prompt the user to install a dictionary
        if (homeDictionariesList.isEmpty()) { homeDictionariesList.add(getString(R.string.addNewDictionaryMessage)); }

        // Configure the adapter and listview for the list of dictionaries
        CustomHomeDictionariesAdapter adapter = new CustomHomeDictionariesAdapter(getActivity(), R.layout.custom_home_dictionaries_item, homeDictionariesList);

        ListView lv = (ListView) rootView.findViewById(R.id.homeDictionariesList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDictionary = (String) parent.getItemAtPosition(position);

                // If the user clicks the add new dictionary message, direct them to the manage dictionaries page
                if (selectedDictionary.equals(getString(R.string.addNewDictionaryMessage))) {
                    Fragment fragment = new DictionariesFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    // Display all the terms from the selected dictionary
                    // Source: http://stackoverflow.com/questions/15392261/android-pass-dataextras-to-a-fragment
                    Bundle bundle = new Bundle();
                    bundle.putString("dictionary", selectedDictionary);
                    bundle.putInt("index", Arrays.asList(getResources().getStringArray(R.array.dictionary_names)).indexOf(selectedDictionary));

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


        // Set text for the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        TextView currentDate = (TextView) rootView.findViewById(R.id.currentDate);
        currentDate.setText(dateFormat.format(new Date()));

        // Find term elements
        TextView homeTerm = (TextView) rootView.findViewById(R.id.homeTerm);
        TextView homeField = (TextView) rootView.findViewById(R.id.homeField);
        TextView homeSubject = (TextView) rootView.findViewById(R.id.homeSubject);
        TextView homeDefinition = (TextView) rootView.findViewById(R.id.homeDefinition);
        TextView homeSynonyms = (TextView) rootView.findViewById(R.id.homeSynonyms);
        homeFavouriteBtn = (ImageButton) rootView.findViewById(R.id.homeFavouriteBtn);
        homeTextToSpeechBtn = (ImageButton) rootView.findViewById(R.id.homeTextToSpeechBtn);

        // Check if there are records on the terms table
        if (db.checkEmptyTable(getString(R.string.TABLE_TERMS))) {
            homeFavouriteBtn.setVisibility(View.INVISIBLE);
            homeTextToSpeechBtn.setVisibility(View.INVISIBLE);
            homeTerm.setText(getString(R.string.welcomeMessage));
            homeDefinition.setText(getString(R.string.getStartedMessage));
            homeField.setText("");
            homeSubject.setText("");
            homeSynonyms.setText("");
        }
        else {
            // Get a suitable random term from the database
            randomTerm = db.getRandomTerm(getString(R.string.TABLE_TERMS));

            // Prepare strings for views
            String fieldText = "Field: " + randomTerm.getDictionary();
            String subjectText = "Subject: " + randomTerm.getSubject();

            // Format the definitions string
            String[] definitionsArray = randomTerm.getDefinition().split(";");
            String definitionsText = "";
            int numDefinitions = 0;
            for (String definition : definitionsArray) {
                if (!definition.isEmpty()) {
                    numDefinitions++;
                    definitionsText += numDefinitions + ".   " + definition + "\n\n";
                    break;
                }
            }
            if (numDefinitions == 0) definitionsText = "Sorry, no available definition.";
            else definitionsText = definitionsText.substring(0, definitionsText.length() - 2);

            // Format the synonyms string
            String synonymsText = "";
            if (!randomTerm.getSynonyms().isEmpty())
                synonymsText = "Synonyms(s): " + randomTerm.getSynonyms().replaceAll(";", ", ");

            // Configure term details
            homeTerm.setText(randomTerm.getTerm());
            homeField.setText(fieldText);
            homeSubject.setText(subjectText);
            homeDefinition.setText(definitionsText);
            homeSynonyms.setText(synonymsText);

            // Set up the favourite button
            if (randomTerm.getFavourited() == 1)
                homeFavouriteBtn.setImageResource(R.drawable.fa_star);
            else homeFavouriteBtn.setImageResource(R.drawable.fa_star_o_light);
            homeFavouriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (randomTerm.getFavourited() == 0) {
                        // Change the image resource of the button
                        homeFavouriteBtn.setImageResource(R.drawable.fa_star);
                        // Add term from favourites
                        db.addTermToFavourites(randomTerm);
                        randomTerm.setFavourited(1);
                        // Display message
                        Toast.makeText(getActivity(), "Added to favourites.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Change the image resource of the button
                        homeFavouriteBtn.setImageResource(R.drawable.fa_star_o_light);
                        // Delete term from favourites
                        db.deleteTermFromFavourites(randomTerm.getId());
                        randomTerm.setFavourited(0);
                        // Display message
                        Toast.makeText(getActivity(), "Removed from favourites.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set up text to speech
            // Source: http://www.survivingwithandroid.com/2015/02/android-text-to-speech-tts.html
            // Source: http://www.tutorialspoint.com/android/android_text_to_speech.htm
            // Source: http://stackoverflow.com/questions/6645893/onutterancecompleted-does-not-get-called
            tts = new TextToSpeech(getActivity(), this);

            homeTextToSpeechBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    homeTextToSpeechBtn.setImageResource(R.drawable.fa_volume_up_pressed);
                    String toSpeak = randomTerm.getTerm();

                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FINISHED PLAYING");
                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
                }
            });
        }

        return rootView;
    }

    // Initialization of the text to speech engine
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.UK);
            tts.setOnUtteranceCompletedListener(this);
        }
    }

    // Upon completion of text to speech utterance
    @Override
    public void onUtteranceCompleted(final String utteranceId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //UI changes
                if (utteranceId.equals("FINISHED PLAYING")) {
                    homeTextToSpeechBtn.setImageResource(R.drawable.fa_volume_up);
                }
            }
        });
    }

    // Shutting down the text to speech listener
    @Override
    public void onDestroyView() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
    }
}
