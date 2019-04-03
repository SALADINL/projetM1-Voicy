package com.example.modulereco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Reco extends Activity
{
	Exercice exo = null;

	Recorder rec = null;
	Alignement alignement = null;
	DAP dap = null;
	TextView mot = null;
	TextView compteur = null;
	Button enregistrer = null;
	Button btEnd = null;
	Button retour = null;
	int type = 0;

	ArrayList<String> tabPhrase = null;
	ArrayList<String> tabPhoneme = null;
	ArrayList<String> tabDap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reco);

		mot = findViewById(R.id.mot);
		compteur = findViewById(R.id.compteur);
		enregistrer = findViewById(R.id.record);
		btEnd = findViewById(R.id.btnEnd);
		retour = findViewById(R.id.back);

		type = 2;

		if (type == 1)
		{
			tabPhoneme = new ArrayList<>();
			tabDap = new ArrayList<>();
			dap = new DAP(this);
			exo = new ExerciceMot(5, this);
		}
		else if (type == 2)
		{
			tabPhrase = new ArrayList<>();
			exo = new ExerciceSeguin(this);
		}

		initialiser();
		creerDossier();

		enregistrer.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!rec.getRecording())
				{
					rec.startRecording();
					enregistrer.setText("STOP");
				}
				else
				{
					rec.stopRecording();

					analyser();

					try
					{
						sauverResultats();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					actualiser();
					enregistrer.setText("Enregistrer");

					if (exo.getIndex() > 0)
						retour.setEnabled(true);
				}
			}
		});

		retour.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				exo.prev();
				initialiser();
				btEnd.setVisibility(View.GONE);
				if (exo.getIndex() == 0)
					retour.setEnabled(false);
			}
		});


	}

	private void actualiser()
	{
	    if(!exo.fini())
        {
            exo.next();
            initialiser();
        }
		else
        {
            final Intent intent = new Intent(this, choixResultat.class);

            mot.setText("Exercice terminé !");
            compteur.setText("");

            btEnd.setVisibility(View.VISIBLE);
            btEnd.setOnClickListener(new Button.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivity(intent);
                }
            });
        }
	}

	private void initialiser()
	{
		mot.setText("" + exo.getText());
		compteur.setText(exo.getIndex() + "/" + exo.getMax());

		rec = new Recorder("" + exo.getIndex());
	}

	private void analyser()
	{
		clearTab();

		File wav = new File(rec.getFilename());

		if (type == 1)
		{
			alignement = new Alignement(Reco.this, Alignement.PHONEME);
			tabPhoneme = alignement.convertir(wav);
			dap = new DAP(Reco.this);
			tabDap = dap.convertir(wav);
		}
		else if (type == 2)
		{
			alignement = new Alignement(Reco.this, Alignement.VOISIN);
			tabPhrase = alignement.convertir(wav);
		}
	}

	private void sauverResultats() throws IOException
	{
		String nom = rec.getFilename();
		nom = nom.substring(0, nom.length() - 4);

		if (type == 1)
		{
			FileWriter writer = new FileWriter(nom + "-score-phoneme.txt");

			for (String str : tabPhoneme)
				writer.write(str + "\n");

			writer.close();

			writer = new FileWriter(nom + "-score-dap.txt");

			for (String str : tabDap)
				writer.write(str + "\n");

			writer.close();
		}
		else if (type == 2)
		{
			FileWriter writer = new FileWriter(nom + "-score.txt");

			for (String str : tabPhrase)
				writer.write(str + "\n");

			writer.close();
		}
	}

	private void clearTab()
	{
		if (type == 1)
		{
			if (!tabPhoneme.isEmpty())
				tabPhoneme.clear();

			if (!tabDap.isEmpty())
				tabDap.clear();
		}
		else if (type == 2)
		{
			if (! tabPhrase.isEmpty())
				tabPhrase.clear();
		}
	}

	private void creerDossier()
	{
		File file = new File(Environment.getExternalStorageDirectory().getPath(),"ModuleReco/Exercices/Exo" + rec.getCurrentTimeUsingCalendar("1"));

		if (!file.exists())
			file.mkdirs();

		rec.setExo("Exo" + rec.getCurrentTimeUsingCalendar("1"));
	}
}
