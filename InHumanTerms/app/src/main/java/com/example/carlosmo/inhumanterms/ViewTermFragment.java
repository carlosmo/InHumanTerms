package com.example.carlosmo.inhumanterms;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Carlos Mo on 23/11/2015.
 */
public class ViewTermFragment extends Fragment implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    DatabaseHelper db;
    Term t;
    ImageButton favouriteBtn, textToSpeechBtn;
    TextToSpeech tts;

    public ViewTermFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_view_term, container, false);

        // Get bundle extras
        Bundle bundle = this.getArguments();
        int id = bundle.getInt("id");

        // Query selected term from database
        db = new DatabaseHelper(getActivity());
        t = db.getTerm(id, getString(R.string.TABLE_TERMS));

        // Determine dictionary icon resource id
        int index = Arrays.asList(getResources().getStringArray(R.array.dictionary_names)).indexOf(t.getDictionary());

        // Prepare strings for views
        String fieldText = "Field: " + t.getDictionary();
        String subjectText = "Subject: " + t.getSubject();

        // Format the definitions string
        String[] definitionsArray = t.getDefinition().split(";");
        String definitionsText = "";
        int numDefinitions = 0;
        for (String definition : definitionsArray) {
            if (!definition.isEmpty()) {
                numDefinitions++;
                definitionsText += numDefinitions + ".   " + definition + "\n\n";
            }
        }
        if (numDefinitions == 0) definitionsText = "Sorry, no available definition.";
        else definitionsText = definitionsText.substring(0, definitionsText.length()-2);

        // Format the synonyms string
        String synonymsText = "";
        if (!t.getSynonyms().isEmpty()) synonymsText = "Synonyms(s): " + t.getSynonyms().replaceAll(";", ", ");

        // Configure view term top bar title and icon
        TextView viewTermTitle = (TextView) rootView.findViewById(R.id.viewTermTitle);
        ImageView viewTermIcon = (ImageView) rootView.findViewById(R.id.viewTermIcon);

        viewTermTitle.setText(t.getDictionary());
        viewTermIcon.setImageResource(getResources().obtainTypedArray(R.array.home_dictionary_icons).getResourceId(index, 0));

        // Configure term details
        TextView term = (TextView) rootView.findViewById(R.id.term);
        TextView field = (TextView) rootView.findViewById(R.id.field);
        TextView subject = (TextView) rootView.findViewById(R.id.subject);
        TextView definition = (TextView) rootView.findViewById(R.id.definition);
        TextView synonyms = (TextView) rootView.findViewById(R.id.synonyms);

        term.setText(t.getTerm());
        field.setText(fieldText);
        subject.setText(subjectText);
        definition.setText(definitionsText);
        synonyms.setText(synonymsText);

        // Set up the favourite button
        favouriteBtn = (ImageButton) rootView.findViewById(R.id.favouriteBtn);
        if (t.getFavourited() == 1) favouriteBtn.setImageResource(R.drawable.fa_star);
        else favouriteBtn.setImageResource(R.drawable.fa_star_o_light);
        favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t.getFavourited() == 0) {
                    // Change the image resource of the button
                    favouriteBtn.setImageResource(R.drawable.fa_star);
                    // Add term from favourites
                    db.addTermToFavourites(t);
                    t.setFavourited(1);
                    // Display message
                    Toast.makeText(getActivity(), "Added to favourites.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Change the image resource of the button
                    favouriteBtn.setImageResource(R.drawable.fa_star_o_light);
                    // Delete term from favourites
                    db.deleteTermFromFavourites(t.getId());
                    t.setFavourited(0);
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

        textToSpeechBtn = (ImageButton) rootView.findViewById(R.id.textToSpeechBtn);
        textToSpeechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeechBtn.setImageResource(R.drawable.fa_volume_up_pressed);
                String toSpeak = t.getTerm();

                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FINISHED PLAYING");
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
            }
        });


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
        /*
        if (utteranceId.equals("FINISHED PLAYING")) {
            textToSpeechBtn.setImageResource(R.drawable.fa_volume_up);
        }
        */
        // Note: much faster response time when implementing the thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //UI changes
                if (utteranceId.equals("FINISHED PLAYING")) {
                    textToSpeechBtn.setImageResource(R.drawable.fa_volume_up);
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
