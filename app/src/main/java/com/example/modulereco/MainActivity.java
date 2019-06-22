package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Ken Bres,
 * La classe MainActivity, la page d'accueil de l'application
 */
public class MainActivity extends Activity
{
    private Button btResultat;
    private Button btLogatome;
    private Button btPhrase;
    private Button button;

    private EditText nbExo;
    private EditText nbPhrase;

    /**
     * @author Ahmet AGBEKTAS
     *
     * Nous avons plusieurs boutons "Phrase", "Logatome Listes", "Logatome Aléatoire" et "Résultat"
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        verifierPermissions();

        btResultat = findViewById(R.id.btResultat);
        btLogatome = findViewById(R.id.btLogatome);
        btPhrase = findViewById(R.id.btPhrase);
        button = findViewById(R.id.button);

        nbExo = findViewById(R.id.nbExo);
        nbPhrase = findViewById(R.id.nbPhrase);

        btPhrase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, Reco.class);
                intent.putExtra("type", 2);
                try
                {
                    if (Integer.parseInt(nbPhrase.getText().toString()) >= 13)
                    {
                        Toast.makeText(MainActivity.this, "Le nombre maximum de phrases autorisés est de 12 !", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        intent.putExtra("nbPhrase", Integer.parseInt(nbPhrase.getText().toString()));
                        startActivity(intent);
                    }
                }
                catch (Exception e)
                {
                    e.getStackTrace();
                }
            }
        });
        btLogatome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, ChoixList.class);
                startActivity(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, Reco.class);
                intent.putExtra("type", 1);
                intent.putExtra("random", 1);
                try
                {
                    intent.putExtra("nbtest", Integer.parseInt(nbExo.getText().toString()));
                }
                catch (Exception e)
                {
                    e.getStackTrace();
                }
                startActivity(intent);
            }
        });
        btResultat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifierPermissions();
                Intent intent = new Intent(MainActivity.this, choixResultat.class);
                startActivity(intent);
            }
        });
    }

    /**
     * @author Ahmet AGBEKTAS
     *
     * Vérification des permissions d'accès au stockage et au microphone
     */
    private void verifierPermissions()
    {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }
}
