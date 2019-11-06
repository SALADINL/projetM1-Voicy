package com.example.modulereco;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Noaman TATA
 * La classe Resultat permettant de faie tous les traitements d'affichage, récupération etc des resultats
 */
public class Resultat  extends Activity
{
	private Context ctx;
	private ConstraintLayout rLayout;
	private ListView listMot = null;
	private ArrayList<String> listItems = new ArrayList<>();
	private ArrayAdapter<String> adapter;
	private PopupWindow popUp;
	private String filepath, fileType ="";
	private Bundle bund;
	private TextView titrePopUp = null;
	private Switch swDap;
	private ImageView play;
	private MediaPlayer mediaPlayer;

	private Button homeButton;

	/**
	 * @author Noaman TATA
	 * Afficher tous les exercices déjà effectué
	 * Possibilité d'afficher une phrase ou un non-mot sous forme de tableau dans une pop-up avec les phonèmes, les durées et les scores
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resultat);

		ctx = getApplicationContext();
		rLayout = findViewById(R.id.const_layout);

		bund = getIntent().getExtras();
		filepath = bund.getString("path");

		listMot = findViewById(R.id.listMot);

		homeButton = findViewById(R.id.homeButton);

		adapter = new ArrayAdapter<>(this,
					android.R.layout.simple_list_item_1,
					listItems);
		listMot.setAdapter(adapter);

		final File extStorageDir = new File(filepath);
		String [] fileList=extStorageDir.list();

		Collections.addAll(listItems, fileList);

		adapter.notifyDataSetChanged();

		getType();				//Savoir de quel type est le fichier phoneme mots phrase etc

		// Choix d'un élément dans la liste avec récupération et affichage de ce dernier
		listMot.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				//https://android--code.blogspot.com/2016/01/android-popup-window-example.html

				LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
				View customView = inflater.inflate(R.layout.popup_layout,null);

				//pop up dans laquel est display le tableau
				popUp = new PopupWindow(customView,
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT,
							true);

				popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				popUp.setElevation(5.0f);

				play = customView.findViewById(R.id.btnPlay);
				play.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if (!mediaPlayer.isPlaying())
							mediaPlayer.start();
					}
				});

				swDap = customView.findViewById(R.id.swDap);
				ImageButton closeButton = customView.findViewById(R.id.ib_close);
				closeButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view) // Dismiss the popup window
					{
						popUp.dismiss();
						mediaPlayer.stop();
					}
				});
				// Stop l'audio si la popUp perd l'affichage
				popUp.setOnDismissListener(new PopupWindow.OnDismissListener()
				{
					@Override
					public void onDismiss()
					{
						mediaPlayer.stop();
					}
				});

				popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
				popUp.showAtLocation(rLayout, Gravity.CENTER,0,0);
				popUp.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				titrePopUp = popUp.getContentView().findViewById(R.id.titrePopUp);

				if (fileType.equals("-score.txt"))
					titrePopUp.setText("Phrase n°"+parent.getItemAtPosition(position));
				else
					titrePopUp.setText(parent.getItemAtPosition(position)+"");

				filepath = bund.getString("path");
				filepath += "/" + parent.getItemAtPosition(position);

				// Récupération des résultats
				final ArrayList<String> dataPhone = getData(parent, position, true);

				initRes(dataPhone, true);
				chargerWav(parent, position);

				/*if (!fileType.equals("-score.txt")) // si c'est le mode phrase pas de DAP
				{
					final ArrayList<String> dataSemi = getData(parent, position, false);
					swDap.setVisibility(View.VISIBLE);
					swDap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
					{
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
						{
							if (isChecked)
								initRes(dataSemi, false);
							else
								initRes(dataPhone, true);
						}
					});
				}*/
				final ArrayList<String> dataSemi = getData(parent, position, false);
				swDap.setVisibility(View.VISIBLE);
				swDap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						if (isChecked)
							initRes(dataSemi, false);
						else
							initRes(dataPhone, true);
					}
				});

			}
		});

		homeButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(Resultat.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}

	/**
	 * @author Noaman TATA, Ken Bres
	 * Remplir l'arrayList avec les données du fichier score
	 *
	 * @param parent la view sur laquel il faut récupérer
	 * @param position position de l'élément dans la liste
	 * @param type
	 */
	private ArrayList<String> getData(AdapterView<?> parent, int position, boolean type) // type true = phoneme false = DAP
	{
		String line, typePath = fileType;
		File file;
		ArrayList<String> data = new ArrayList<>();

		if (!type)
			typePath = "-score-semiContraint.txt";

		try
		{
			file = new File(filepath, "/" + parent.getItemAtPosition(position) + typePath);
			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null)
				data.add(line);

			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * @author Noaman TATA
	 * Fonction pour savoir de quel type est le fichier ; phonème, mots, phrase, etc
	 */
	private void getType()
	{
		if (!listItems.isEmpty())
		{
			File f = new File(filepath+"/"+listItems.get(0));
			String [] r = f.list();
			String res;

			for (String str : r)
			{
				if (str.contains("-"))
				{
					res = str.substring(str.indexOf("-"));

					if (res.equals("-score.txt") || res.equals("-score-phoneme.txt"))
					{
						fileType = res;
						return;
					}
				}
			}
		}
	}

	/**
	 * Fonction pour charger le fichier Wav, pour pouvoir la lire
	 *
	 * @param parent
	 * @param position
	 */
	private void chargerWav(AdapterView<?> parent, int position)
	{
		mediaPlayer = new MediaPlayer();
		String path = filepath+ "/" + parent.getItemAtPosition(position) + ".wav";

		try
		{
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		}
		catch (Exception e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * @author Noaman Tata
	 *
	 * Fonction pour remplir le tableau du résultat, avec plusieurs colonnes ; Phonème ou Semi-contraint, Durée (frames) et Score
	 *
	 * @param output Listes des résultats à afficher
	 * @param phoneSearch Type de résultat à afficher true = phonème false = Semi contraint
	 */
	private void initRes(ArrayList<String> output, Boolean phoneSearch)
	{
		TableLayout tab = popUp.getContentView().findViewById(R.id.tabResultat);
		tab.setMinimumWidth(700);
		tab.removeAllViews();
		//---- SPECIFICATION CATEGORIES
		TableRow ligneTitre = new TableRow(this);
		ligneTitre.setBackgroundResource(R.drawable.row_border);
		ligneTitre.setBackgroundColor(Color.BLACK);
		ligneTitre.setPadding(0, 0, 0, 2); //Border between rows

		for (int i = 0; i < 3; i++) // boucle pour les titres des colonnes
		{
			TextView col = new TextView(this);
			col.setGravity(Gravity.CENTER_HORIZONTAL);
			col.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
			col.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
			col.setTextColor(Color.WHITE);

			switch(i)
			{
				case 0 :
					if (phoneSearch)
						col.setText(" Phoneme ");
					else
						col.setText(" Semi-Contraint ");
					break;

				case 1 :
					col.setText(" Durée (frames) ");
					break;

				case 2 :
					col.setText(" Score ");
					break;
			}
			ligneTitre.addView(col);
		}

		tab.addView(ligneTitre);
		String [] array, array2;
		TextView scoreNorm = popUp.getContentView().findViewById(R.id.scoreNorm);
		String res;
		// boucle pour saisir les valeurs dans le tableau. Le tableau est crée dynamiquement
		for (int i = 0; i < output.size(); i++)
		{
			res = output.get(i);

			if (i == 0 && phoneSearch)
				scoreNorm.setText(res);
			else if (i > 1 || !phoneSearch)
			{
				// JUste un split car les résultats sont du genre: 00-34 : SIL (-100)  array 1 contient la première partie
				array = res.split(":");
				// array2[0] contient le phonème et array[fin] contient le score
				array2 = array[array.length-1].split("\\(");
				TableRow tabLigne = new TableRow(this);
				tabLigne.setBackgroundColor(Color.GRAY);

				for (int j = 0; j < 3; j++)
				{
					TextView dataCol = new TextView(this);
					dataCol.setBackgroundResource(R.drawable.row_border);
					dataCol.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
					dataCol.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
					dataCol.setGravity(Gravity.CENTER_HORIZONTAL);
					dataCol.setPadding(0, 5, 0, 5);
					dataCol.setTextColor(Color.WHITE);

					if (!array2[array2.length-1].substring(0, array2[array2.length-1].length() - 1).equals("0"))
					{
						switch (j)
						{
							case 0:
								dataCol.setText(array2[0]);
								break;

							case 1:
								dataCol.setText(array[0]);
								break;

							case 2:
								dataCol.setText(array2[array2.length - 1].substring(0, array2[array2.length-1].length()-1));
								break;
						}
					}
					else
					{
						TableRow.LayoutParams params = (TableRow.LayoutParams)dataCol.getLayoutParams();
						params.span = 4;
						dataCol.setLayoutParams(params); // causes layout update
						dataCol.setText("");
					}
					tabLigne.addView(dataCol);
				}
				tab.addView(tabLigne);
			}
		}
	}
}

