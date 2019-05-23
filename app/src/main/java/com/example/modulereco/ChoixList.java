package com.example.modulereco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ChoixList extends Activity {
    private Button home;
    private ListView listes = null;
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    //private Button list1, list2, list3, list4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_list);

        creerListe();

        home = findViewById(R.id.home);
        listes = findViewById(R.id.listes);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listes.setAdapter(adapter);

        String filepath = Environment.getExternalStorageDirectory().getPath();

        try
        {
            final File extStorageDir = new File(filepath, "/ModuleReco/Listes");
            final String[] fileList = extStorageDir.list();


            listItems.addAll(Arrays.asList(fileList));

            Collections.sort(listItems);
            adapter.notifyDataSetChanged();

            listes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    String filepath = Environment.getExternalStorageDirectory().getPath() + "/ModuleReco/Listes/" + parent.getItemAtPosition(position);
                    File path = new File(filepath, "/ModuleReco/Listes" + parent.getItemAtPosition(position));
                    Intent myIntent = new Intent(view.getContext(), Reco.class);
                    myIntent.putExtra("path", filepath);
                    myIntent.putExtra("numeroDeListe", position);
                    myIntent.putExtra("random", 0);
                    myIntent.putExtra("type", 1);
                    startActivity(myIntent);
                }
            });
        }catch (Exception e)
        {
            Toast.makeText(ChoixList.this, "Le dossier 'Listes' est vide !", Toast.LENGTH_LONG).show();

            e.getStackTrace();
        }

        home.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChoixList.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private File creerListe()
    {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),"ModuleReco/Listes/");

        if (!file.exists())
            file.mkdirs();

        return file;
    }
}