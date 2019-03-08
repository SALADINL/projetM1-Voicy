package com.example.modulereco;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
				initRes(text);
			}
		});

		boutonPHON.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aPhon = new Alignement(TestDAP.this, Alignement.PHONEME, motADecoder);
				ArrayList<String> text = aPhon.convertir(file);
				initRes(text);
			}
		});

		boutonMOT.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aMot = new Alignement(TestDAP.this, Alignement.MOT, motADecoder);
				ArrayList<String> text = aMot.convertir(file);
				initRes(text);
			}
		});
	}

	private void initRes(ArrayList<String> output)
	{

		TableLayout tab = (TableLayout) findViewById(R.id.tabResultat);
		tab.removeAllViews();
		//---- SPECIFICATION CATEGORIES
		TableRow ligneTitre = new TableRow(this);

		//---- COLONNE PHONEME
		// a faire test si c'est align mot donc changer nom categorie
		TextView colPhone= new TextView(this);
		colPhone.setText(" Phoneme ");
		colPhone.setGravity(Gravity.CENTER_HORIZONTAL);
		colPhone.setTextColor(Color.BLACK);
		ligneTitre.addView(colPhone);

		//---- COLONNE TEMPS
		TextView colTemps = new TextView(this);
		colTemps.setText(" Temps (frame) ");
		colTemps.setGravity(Gravity.CENTER_HORIZONTAL);
		colTemps.setTextColor(Color.BLACK);
		ligneTitre.addView(colTemps);

		//---- COLONNE SCORE
		TextView colScore = new TextView(this);
		colScore.setText(" Score ");
		colScore.setGravity(Gravity.CENTER_HORIZONTAL);
		colScore.setTextColor(Color.BLACK);
		ligneTitre.addView(colScore);


		tab.addView(ligneTitre);
		String [] array, array2;
		TextView scoreTotal = findViewById(R.id.scoreTotal);
		TextView scoreTotalSil = findViewById(R.id.scoreTotalSil);
		String res;
		for(int i = 0 ; i < output.size(); i++)
		{
			res = output.get(i);
			if(i < output.size()-2)
			{
				array = res.split(":");
				array2 = array[array.length-1].split("\\(");

				TableRow tabLigne = new TableRow(this);

				TextView dataPhone= new TextView(this);
				dataPhone.setText(array2[0]);
				dataPhone.setGravity(Gravity.CENTER_HORIZONTAL);
				dataPhone.setTextColor(Color.BLACK);
				tabLigne.addView(dataPhone);

				TextView dataTemps= new TextView(this);
				dataTemps.setText(array[0]);
				dataTemps.setGravity(Gravity.CENTER_HORIZONTAL);
				dataTemps.setTextColor(Color.BLACK);
				tabLigne.addView(dataTemps);

				TextView dataScore= new TextView(this);
				dataScore.setText(array2[array2.length-1].substring(0, array2[array2.length-1].length() - 1));
				dataScore.setGravity(Gravity.CENTER_HORIZONTAL);
				dataScore.setTextColor(Color.BLACK);
				tabLigne.addView(dataScore);

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
