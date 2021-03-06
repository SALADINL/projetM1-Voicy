package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/***
 * @author Ahmet AGBEKTAS, Noaman TATA
 *
 * Cette classe permet l'affichage des résultats d'exercices déjà effectué
 */
public class choixResultat extends Activity
{
	private ListView listExo = null;
	private ArrayList<String> listItems = new ArrayList<>();
	private ArrayAdapter<String> adapter;

	private Button homeButton;

	/**
	 * @author Noaman TATA, Ahmet AGBEKTAS
	 *
	 * Affichage des résultats d'exercices déjà effectué sous forme de listes ces derniers sont stocké dans le stockage interne de l'appareil ModuleReco/Exercices
	 * Un simple click pour sélectionner l'exercice
	 * Un long click pour supprimer l'exercice
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choix_exercice);

		verifierPermissions();

		homeButton = findViewById(R.id.homeButton);

		listExo = findViewById(R.id.listExo);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
		listExo.setAdapter(adapter);

		String filepath = Environment.getExternalStorageDirectory().getPath();

		try
		{
			final File extStorageDir = new File(filepath, "/ModuleReco/Exercices");
			final String[] fileList = extStorageDir.list();


			listItems.addAll(Arrays.asList(fileList));

			Collections.sort(listItems, Collections.reverseOrder());
			adapter.notifyDataSetChanged();
			// AFFICHAGE
			listExo.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					String filepath = Environment.getExternalStorageDirectory().getPath() + "/ModuleReco/Exercices/" + parent.getItemAtPosition(position);
					File path = new File(filepath, "/ModuleReco/Exercices" + parent.getItemAtPosition(position));
					Intent myIntent = new Intent(view.getContext(), Resultat.class);
					myIntent.putExtra("path", filepath);
					startActivity(myIntent);
				}
			});

			// SUPPRESSION
			listExo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id)
				{
					String filepath = Environment.getExternalStorageDirectory().getPath() + "/ModuleReco/Exercices/" + parent.getItemAtPosition(position);
					final File path = new File(filepath);//, "/ModuleReco/Exercices" + parent.getItemAtPosition(position));

					AlertDialog.Builder alert = new AlertDialog.Builder(choixResultat.this);

					alert.setTitle("Confirmation");
					alert.setMessage("Êtes-vous sûr de vouloir supprimer cet exercice ?");

					alert.setPositiveButton("Oui", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int i)
						{
							if (path.exists())
							{
								supprimerDossier(path);
								adapter.remove(adapter.getItem(position));
								Toast.makeText(choixResultat.this, "L'exercice a été supprimé !", Toast.LENGTH_LONG).show();
							}
							dialog.dismiss();
						}
					});

					alert.setNegativeButton("Non", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int i)
						{
							dialog.dismiss();
						}
					});

					alert.show();
					return true;
				}
			});
		}
		catch (Exception e)
		{

			Toast.makeText(choixResultat.this, "Le dossier 'Exercices' est vide !", Toast.LENGTH_LONG).show();

			e.getStackTrace();
		}

		homeButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(choixResultat.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
				(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
		{
			requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
		}
	}

	/**
	 * @author Ahmet AGBEKTAS
	 *
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
