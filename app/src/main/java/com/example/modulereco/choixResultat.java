package com.example.modulereco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class choixResultat extends Activity {

    private ListView listExo = null;
    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_exercice);

        listExo = findViewById(R.id.listExo);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listExo.setAdapter(adapter);

        String filepath = Environment.getExternalStorageDirectory().getPath();
        final File extStorageDir = new File(filepath,"/ModuleReco/Exercices");
        String [] fileList=extStorageDir.list();

        for(String fileName:fileList)
           listItems.add(fileName);
        Collections.sort(listItems);
        adapter.notifyDataSetChanged();

        listExo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String filepath = Environment.getExternalStorageDirectory().getPath()+"/ModuleReco/Exercices/"+parent.getItemAtPosition(position);
                    File path = new File(filepath, "/ModuleReco/Exercices"+parent.getItemAtPosition(position));
                    Intent myIntent = new Intent(view.getContext(), Resultat.class);
                    myIntent.putExtra("path", filepath);
                    startActivity(myIntent);
            }
        });
    }
}
