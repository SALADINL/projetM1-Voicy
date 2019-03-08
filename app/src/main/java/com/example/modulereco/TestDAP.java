package com.example.modulereco;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;

public class TestDAP extends Activity
{
	File file;

	Button boutonDAP, boutonMOT, boutonPHON;
	RadioGroup radioWAV, radioTXT;
	DAP dap;
	Alignement aMot, aPhon;
	String motADecoder, motSurFichier;

	Assets assets = null;
	File assetsDir = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_dap);

		boutonDAP = findViewById(R.id.boutonDAP);
		boutonMOT = findViewById(R.id.boutonMOT);
		boutonPHON = findViewById(R.id.boutonPHON);

		radioWAV = findViewById(R.id.radioWAV);
		radioTXT = findViewById(R.id.radioTXT);

		motSurFichier = "kanfrou";
		motADecoder = "kanfrou";

		try
		{
			assets = new Assets(this);
			assetsDir = assets.syncAssets();

			file = new File(assetsDir, "kanfrou.wav");
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}

		radioTXT.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				int checked = group.getCheckedRadioButtonId();

				if (checked == R.id.rKanfrouTXT)
					motADecoder = "kanfrou";
				else if (checked == R.id.rCalamarTXT)
					motADecoder = "calamar";
				else if (checked == R.id.rBjrTXT)
					motADecoder = "bonjour";
			}
		});

		radioWAV.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				int checked = group.getCheckedRadioButtonId();

				if (checked == R.id.rKanfrouWAV)
					motSurFichier = "kanfrou";
				else if (checked == R.id.rFanfrouWAV)
					motSurFichier = "fanfrou";
				else if (checked == R.id.rCalamarWAV)
					motSurFichier = "calamar";
				else if (checked == R.id.rBjrWAV)
					motSurFichier = "bonjour";


				System.out.println(motSurFichier);

				file = new File(assetsDir, motSurFichier + ".wav");
			}
		});

		boutonDAP.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dap = new DAP(TestDAP.this);
				ArrayList<String> text = dap.convertir(file);
				initRes(text, true);
			}
		});

		boutonPHON.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aPhon = new Alignement(TestDAP.this, Alignement.PHONEME, motADecoder);
				ArrayList<String> text = aPhon.convertir(file);
				initRes(text, true);
			}
		});

		boutonMOT.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aMot = new Alignement(TestDAP.this, Alignement.MOT, motADecoder);
				ArrayList<String> text = aMot.convertir(file);
				initRes(text, false);
			}
		});
	}

	private void initRes(ArrayList<String> output, Boolean phoneSearch)
	{

		TableLayout tab = (TableLayout) findViewById(R.id.tabResultat);
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
		TextView scoreTotal = findViewById(R.id.scoreTotal);
		TextView scoreTotalSil = findViewById(R.id.scoreTotalSil);
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
                        case 2 : dataCol.setText(array2[array2.length-1].substring(0, array2[array2.length-1].length() - 1));
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
