package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class MainActivity extends Activity implements RecognitionListener
{
    private int RSTORAGE_PERMISSION_CODE = 1;
    private int AUDIO_PERMISSION_CODE = 2;

    private static final String KWS_SEARCH = "mot";
    private static final String MENU_SEARCH = "menu";
    private static final String PHONE_SEARCH = "phoque";

    private static final String KEYPHRASE = "bonjour";
    private SpeechRecognizer recognizer;
    TextView affichage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        affichage = findViewById((R.id.message));

        Button btnStockage = findViewById(R.id.btnPermStockage);
        btnStockage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RSTORAGE_PERMISSION_CODE);
            }
        });

        Button btnAudio = findViewById(R.id.btnPermAudio);
        btnAudio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
            }
        });

        new SetupTask(this).execute();
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception>
    {
        WeakReference<MainActivity> activityReference;

        SetupTask(MainActivity activity)
        {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params)
        {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result)
        {
            if (result != null)
                System.out.println(result.getMessage());
            else
                activityReference.get().switchSearch(PHONE_SEARCH);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (recognizer != null)
        {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    private void setupRecognizer(File assetDir) throws IOException
    {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetDir, "ptm"))
                .setDictionary(new File(assetDir, "fr.dict"))
                .getRecognizer();

        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File menuGrammar = new File(assetDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        File phoneticModel = new File(assetDir, "fr-phone.lm.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    private void switchSearch(String searchName)
    {
        recognizer.stop();

        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
    }

    @Override
    public void onBeginningOfSpeech() {


    }

    @Override
    public void onEndOfSpeech() {
    if (!recognizer.getSearchName().equals(PHONE_SEARCH))
        switchSearch(PHONE_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();

        affichage.setText(text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null)
        {
            String text = hypothesis.getHypstr();
            affichage.setText(text);
        }
    }

    @Override
    public void onError(Exception e) {
        affichage.setText(e.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}
