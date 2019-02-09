package com.example.modulereco;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private Button btAnalyse;
    private HashMap<String, Integer> captions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        captions = new HashMap<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //runRecognizerSetup();

        btAnalyse = findViewById(R.id.btAnalyse);

        btAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alignement();
            }
        });
    }

    private void alignement() {
        //recognizer = defaultSetup();
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }
}
