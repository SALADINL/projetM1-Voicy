package com.example.modulereco;

import android.os.Bundle;

import android.app.Activity;
import android.os.AsyncTask;
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
    private SpeechRecognizer jarvis;

    private int RSTORAGE_PERMISSION_CODE = 1;
    private int AUDIO_PERMISSION_CODE = 2;

    private static final String KWS_SEARCH = "hey";
    private static final String MENU_SEARCH = "menu";
    private static final String PHONE_SEARCH = "phone";

    private static final String KEYPHRASE = "jarvis";

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById((R.id.message));


        new SetupTask(this).execute();
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception>
    {
        WeakReference<MainActivity> activityReference;

        SetupTask(MainActivity mainActivity)
        {
            this.activityReference = new WeakReference<>(mainActivity);
        }
        @Override
        protected Exception doInBackground(Void... params)
        {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            }
            catch (IOException e)
            {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result)
        {
            if (result != null)
            {
                System.out.println(result.getMessage());
            }
            else
            {
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (jarvis != null) {
            jarvis.cancel();
            jarvis.shutdown();
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis)
    {
        if (hypothesis == null)
        {
            return;
        }

        String text = hypothesis.getHypstr();

        textView.setText(text);
    }

    @Override
    public void onResult(Hypothesis hypothesis)
    {
        if (hypothesis != null)
        {
            String text = hypothesis.getHypstr();

            textView.setText(text);
        }
    }

    @Override
    public void onBeginningOfSpeech()
    {

    }

    @Override
    public void onEndOfSpeech()
    {
        if (!jarvis.getSearchName().equals(KWS_SEARCH))
        {
            switchSearch(KWS_SEARCH);
        }
    }

    private void switchSearch(String searchName)
    {
        jarvis.stop();

        if (searchName.equals(KWS_SEARCH))
        {
            jarvis.startListening(searchName);
        }
        else
        {
            jarvis.startListening(searchName, 10000);
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException
    {
        jarvis = defaultSetup()
                                .setAcousticModel(new File(assetsDir, "fr-fr-ptm"))
                                .setDictionary(new File(assetsDir, "fr.dict"))
                                .getRecognizer();

        jarvis.addListener(this);

        jarvis.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File menuGrammar = new File(assetsDir, "fr.gram");
        jarvis.addGrammarSearch(MENU_SEARCH, menuGrammar);

        File phoneticModel = new File(assetsDir, "fr-phone.lm.dmp");
        jarvis.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error)
    {
        textView.setText(error.getMessage());
    }

    @Override
    public void onTimeout()
    {
        switchSearch(KWS_SEARCH);
    }
}
