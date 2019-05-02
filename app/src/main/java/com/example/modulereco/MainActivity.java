package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
    private Button btResultat;
    private Button btLogatome;
    private Button btPhrase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        verifierPermissions();

        btResultat = findViewById(R.id.btResultat);
        btLogatome = findViewById(R.id.btLogatome);
        btPhrase = findViewById(R.id.btPhrase);

        btPhrase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, Reco.class);
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });
        btLogatome.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, Reco.class);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });
        btResultat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, choixResultat.class);
                startActivity(intent);
            }
        });
    }

    private void verifierPermissions()
    {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
        {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
        }
    }
}
