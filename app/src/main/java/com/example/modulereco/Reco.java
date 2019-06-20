package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Ken Bres,
 * La classe Reco utilisée pour l'enregistrement
 */
public class Reco extends Activity
{
	Exercice exo = null;

	Recorder rec = null;
	Alignement alignement = null;
	DAP dap = null;
	TextView mot = null;
	TextView compteur = null;
	ImageButton enregistrer = null;

	Button annuler;

	Button btEnd = null;
	Button retour = null;
	int type = 0;  // 1 = exo avec mot 0 = exo avec phrase
	int nbtest = 0;
	int nbPhrase = 0;
	int numeroDeListe = 0;
	int random = 0;

	ArrayList<String> tabPhrase = null;
	ArrayList<String> tabPhoneme = null;
	ArrayList<String> tabDap = null;

	/**
	 * Nous pouvons s'enregistrer avec deux types d'exercices : Logatome ou Phrase
	 * Nous avons un bouton pour s'enregistrer et finir l'enregistrement, un autre pour faire retour si jamais nous avons mal prononcé
	 * Nous avons un autre bouton pour annuler l'enregistrement et revenir à l'accueil, l'annulation effacera le dossier de l'enregistrement en cours
	 *
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reco);
		findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
		verifierPermissions();

		mot = findViewById(R.id.mot);
		compteur = findViewById(R.id.compteur);
		enregistrer = findViewById(R.id.record);
		btEnd = findViewById(R.id.btnEnd);
		retour = findViewById(R.id.back);

		annuler = findViewById(R.id.annuler);

		Intent intent = getIntent();
		type = intent.getIntExtra("type", 1);
		nbtest = intent.getIntExtra("nbtest", 3);
		nbPhrase = intent.getIntExtra("nbPhrase", 12);
		numeroDeListe = intent.getIntExtra("numeroDeListe", 1);
		random = intent.getIntExtra("random", 0);

		if (type == 1 && random == 0)
		{
			tabPhoneme = new ArrayList<>();
			tabDap = new ArrayList<>();
			dap = new DAP(this);
			System.out.println("numeroDeListe : " + numeroDeListe);
			exo = new ExerciceMot(52, numeroDeListe,this);
		}
		else if (type == 1 && random == 1)
		{
			tabPhoneme = new ArrayList<>();
			tabDap = new ArrayList<>();
			dap = new DAP(this);
			exo = new ExerciceMot(nbtest, this);
		}
		else if (type == 2)
		{
			tabPhrase = new ArrayList<>();
			exo = new ExerciceSeguin(nbPhrase, this);
		}

		initialiser();
		//creerDossier();
        final File file = creerDossierv2();

		enregistrer.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!rec.getRecording())
				{
					verifierPermissions();
					rec.startRecording();
					retour.setEnabled(false);
					enregistrer.setImageResource(R.drawable.ic_mic_black_85dp2);
				}
				else
				{
					rec.stopRecording();
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
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
					enregistrer.setImageResource(R.drawable.ic_mic_black_85dp);

					if (exo.getIndex() > 0)
						retour.setEnabled(true);
                    findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
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
				btEnd.setEnabled(false);

				if (exo.getIndex() == 0)
					retour.setEnabled(false);
			}
		});

		annuler.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
               	//System.out.println("file : " + file);

				supprimerDossier(file);

				Intent intent = new Intent(Reco.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

	}

	/***
	 * Tant que nous sommes pas arrivé à la fin de l'exercice on continue à prononcer les mots ou phrases
	 * Si nous avons finit, un bouton terminer apparait qui nous permet de voir les résultats
	 */
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

	/**
	 * Vérification des permissions d'accès au stockage et au microphone
	 */
	private void verifierPermissions()
	{
		if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
				(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
				(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
		{
			requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
		}
	}

	/**
	 * Pour préparer les informations utiles à l'enregistrement
	 * Afficher le mot ou phrase à prononcer, mettre et gérer le compteur
	 */
	private void initialiser()
	{
		mot.setText(exo.getText());
		compteur.setText(exo.getIndex() + "/" + exo.getMax());
		if (type == 1)
			rec = new Recorder("" + exo.getText());
		else
			rec = new Recorder("" + exo.getIndex());
	}

	/**
	 * Fonction pour analyser l'enregistrement, en fonction du type d'exercice (Phrase ou Phoneme) on applique différent alignement (phonème ou voisin)
	 */
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

	/**
	 * Fonction pour sauvegarder les résultats
	 */
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

	/**
	 * Fonction pour effacer le tableau qui va contenir les résultats
	 */
	private void clearTab()
	{
		if (type == 1)
		{
			if(tabPhoneme != null)
				if (!tabPhoneme.isEmpty())
					tabPhoneme.clear();
			if(tabDap != null)
				if (!tabDap.isEmpty())
					tabDap.clear();
		}
		else if (type == 2)
		{
			if(tabPhrase != null)
				if (!tabPhrase.isEmpty())
					tabPhrase.clear();
		}
	}

	/**
	 * Fonction pour créer le dossier avec l'exercice en question dans le stockage du téléphone
	 *
	 * @return le dossier crée
	 */
    private File creerDossierv2()
    {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),"ModuleReco/Exercices/Exo" + rec.getCurrentTimeUsingCalendar("1"));

        if (!file.exists())
            file.mkdirs();

        rec.setExo("Exo" + rec.getCurrentTimeUsingCalendar("1"));

        return file;
    }

	/**
	 * Fonction utilisée pour supprimer un dossier et sous-dossier
	 * @param file Dossier à supprimer
	 */
    private void supprimerDossier(File file)
    {
        if (file.isDirectory())
        {
            File[] listFiles = file.listFiles();

            for (int i = 0; i < listFiles.length; i++)
            {
                if (listFiles[i].isDirectory())
                {
                    supprimerDossier(listFiles[i]);
                }
                else
                {
                    listFiles[i].delete();
                }
            }
        }
        file.delete();
    }
}
