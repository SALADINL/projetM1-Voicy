package com.example.modulereco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button btResultat;
    private Button btLogatome;
    private Button btPhrase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        btResultat = findViewById(R.id.btResultat);
        btLogatome = findViewById(R.id.btLogatome);
        btPhrase = findViewById(R.id.btPhrase);

        btPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Reco.class);
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });
        btLogatome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Reco.class);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });
        btResultat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, choixResultat.class);
                startActivity(intent);
            }
        });
    }
}
