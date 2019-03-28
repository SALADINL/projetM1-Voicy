package com.example.modulereco;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Resultat  extends Activity {

    private TextView t = null;
    private Context ctx;
    private ConstraintLayout rLayout;
    private ListView listMot = null;
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private PopupWindow popUp;
    private String filepath;
    private Bundle bund;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultat);

        ctx = getApplicationContext();
        rLayout = findViewById(R.id.const_layout);
        t = findViewById(R.id.pathExo);
        bund = getIntent().getExtras();
        filepath = bund.getString("path");

        listMot = findViewById(R.id.listMot);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listMot.setAdapter(adapter);


        final File extStorageDir = new File(filepath);
        String [] fileList=extStorageDir.list();
        for(String fileName:fileList)
            listItems.add(fileName);
        Collections.sort(listItems);
        adapter.notifyDataSetChanged();

        listMot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //https://android--code.blogspot.com/2016/01/android-popup-window-example.html

                LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.popup_layout,null);


                popUp = new PopupWindow(
                        customView,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                );
                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    popUp.setElevation(5.0f);
                }
                // Get a reference for the custom view close button
                ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);

                // Set a click listener for the popup window close button
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the popup window
                        popUp.dismiss();
                    }
                });


                popUp.showAtLocation(rLayout, Gravity.CENTER,0,0);
                popUp.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                popUp.setTouchable(false);
                popUp.setFocusable(false);

                File f = null;
                ArrayList<String> res = new ArrayList<>();

                try
                {

                    String line;
                    filepath = bund.getString("path");
                    filepath += "/"+parent.getItemAtPosition(position);
                    f = new File(filepath, "/"+parent.getItemAtPosition(position)+"-score-dap.txt");
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    while ((line = br.readLine()) != null) {
                        res.add(line);
                    }
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                initRes(res, true);


            }
        });


    }
    	private void initRes(ArrayList<String> output, Boolean phoneSearch)
	{

		TableLayout tab = popUp.getContentView().findViewById(R.id.tabResultat);
		tab.removeAllViews();
		//---- SPECIFICATION CATEGORIES
		TableRow ligneTitre = new TableRow(this);
		ligneTitre.setBackgroundResource(R.drawable.row_border);
		ligneTitre.setBackgroundColor(Color.BLACK);
		ligneTitre.setPadding(0, 0, 0, 2); //Border between rows

		for(int i = 0; i < 3 ; i++) // boucle pour les titres des colonnes
		{
			TextView col = new TextView(this);
			col.setGravity(Gravity.CENTER_HORIZONTAL);
			col.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
			col.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
			col.setTextColor(Color.WHITE);
			switch(i){
				case 0 : if (phoneSearch) col.setText(" Phoneme ");
						 else col.setText(" Mots ");
						break;
				case 1 : col.setText(" Temps (frame) ");
						break;
				case 2 : col.setText(" Score ");
						break;
			}
			ligneTitre.addView(col);
		}


		tab.addView(ligneTitre);
		String [] array, array2;
		TextView scoreTotal = popUp.getContentView().findViewById(R.id.scoreTotal);
		TextView scoreTotalSil = popUp.getContentView().findViewById(R.id.scoreTotalSil);
		String res;
		for(int i = 0 ; i < output.size(); i++)
		{
			res = output.get(i);
			if(i < output.size()-3)
			{
				array = res.split(":");
				array2 = array[array.length-1].split("\\(");
				TableRow tabLigne = new TableRow(this);
				tabLigne.setBackgroundColor(Color.GRAY);

                for(int j = 0 ; j < 3 ; j++)
                {
					TextView dataCol= new TextView(this);
					dataCol.setBackgroundResource(R.drawable.row_border);
					dataCol.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
					dataCol.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
					dataCol.setGravity(Gravity.CENTER_HORIZONTAL);
					dataCol.setPadding(0, 5, 0, 5);
					dataCol.setTextColor(Color.WHITE);
                    switch(j){
                        case 0 : dataCol.setText(array2[0]);
                            break;
                        case 1 : dataCol.setText(array[0]);
                            break;
                       case 2 : dataCol.setText(array2[array2.length-1]);
                            break;

                    }

                    tabLigne.addView(dataCol);
                }
				tab.addView(tabLigne);
			}
			else
			{
				if(i == output.size()-2) scoreTotal.setText(res);
				else scoreTotalSil.setText(res);

			}
		}


	}
}

