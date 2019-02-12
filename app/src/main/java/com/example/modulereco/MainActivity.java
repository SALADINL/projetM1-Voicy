package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class MainActivity extends Activity implements RecognitionListener
{
    private int RSTORAGE_PERMISSION_CODE = 1;
    private int AUDIO_PERMISSION_CODE = 2;

    private static final String KWS_SEARCH = "debout";
    private static final String MENU_SEARCH = "menu";
    private static final String KEYPHRASE = "réveille toi connard";
    private SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        runRecognizerSetup();
        setContentView(R.layout.activity_main);

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

    private void runRecognizerSetup()
    {
        new AsyncTask<Void, Void, Exception>()
        {
            @Override
            protected Exception doInBackground(Void... params)
            {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
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
                    switchSearch(KWS_SEARCH);
            }
        }.execute();
    }

    private void setupRecognizer(File assetDir) throws IOException
    {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetDir, "ptm"))
                .setDictionary(new File(assetDir, "fr.dict"))
                .getRecognizer();

        recognizer.addListener(this);

        //recognizer.addKeyPhraseSearch(KWS_SEARCH, KEYPHRASE);

        File menuGrammar = new File(assetDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
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
    if (!recognizer.getSearchName().equals(KWS_SEARCH))
        switchSearch(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();

        switch (text)
        {
            case KEYPHRASE:
                switchSearch(MENU_SEARCH);
                break;
            case "salut":
                System.out.println("Salut à toi aussi connard");
                break;
            case "ouech":
                System.out.println("WESH BRO");
                break;
            default:
                System.out.println(hypothesis.getHypstr());
                break;
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null)
            System.out.println(hypothesis.getHypstr());
    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}
