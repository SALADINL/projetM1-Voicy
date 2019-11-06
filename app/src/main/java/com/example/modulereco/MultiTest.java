package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * @author Ken Bres,
 * La classe Reco utilisée pour l'enregistrement
 */
public class MultiTest extends Activity
{
    Button btnPhrase = null;
    Button btnPhoneme = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_test);

        verifierPermissions();

        btnPhrase = findViewById(R.id.btnMultiPhrase);
        btnPhoneme = findViewById(R.id.btnMultiPhoneme);

        btnPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifierPermissions();
                Intent intent = new Intent(MultiTest.this, MultiPhrase.class);
                intent.putExtra("type", 2);
                try
                {
                    intent.putExtra("nbPhrase", Integer.parseInt("1"));
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    e.getStackTrace();
                }
            }
        });

        btnPhoneme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                Intent intent = new Intent(MultiTest.this, MultiPhoneme.class);
                intent.putExtra("type", 1);
                intent.putExtra("random", 1);
                try
                {
                    intent.putExtra("nbtest", Integer.parseInt("1"));
                }
                catch (Exception e)
                {
                    e.getStackTrace();
                }
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
