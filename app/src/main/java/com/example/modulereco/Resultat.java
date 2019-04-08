package com.example.modulereco;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Resultat  extends Activity
{

	private Context ctx;
	private ConstraintLayout rLayout;
	private ListView listMot = null;
	private ArrayList<String> listItems = new ArrayList<>();
	private ArrayAdapter<String> adapter;
	private PopupWindow popUp;
	private String filepath;
	private Bundle bund;
	private String fileType ="";
	private TextView titrePopUp = null;
	private float dpWidth;

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
		adapter = new ArrayAdapter<>(this,
		android.R.layout.simple_list_item_1,
		listItems);
		listMot.setAdapter(adapter);


		final File extStorageDir = new File(filepath);
		String [] fileList=extStorageDir.list();

		Collections.addAll(listItems, fileList);

		Collections.sort(listItems);
		adapter.notifyDataSetChanged();

		//Savoir de quel type est le fichier phoneme mots phrase etc
        if(!listItems.isEmpty())
        {
            File f = new File(filepath+"/"+listItems.get(0));
            String [] r = f.list();
            fileType = r[0];
            fileType = fileType.substring(fileType.indexOf("-"));
        }
		System.out.println(" TYPE -> "+fileType);

		listMot.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			private MediaPlayer mediaPlayer;
			private String chemin;
			private String fichierAudio;

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				//https://android--code.blogspot.com/2016/01/android-popup-window-example.html

				LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
				View customView = inflater.inflate(R.layout.popup_layout,null);


				popUp = new PopupWindow(
				customView,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,true);
				//popUp.setWidth((int) (LinearLayout.LayoutParams.MATCH_PARENT*0.8));
				popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				// Set an elevation value for popup window
				// Call requires API level 21
				if(Build.VERSION.SDK_INT >= 21)
					popUp.setElevation(5.0f);

				// Get a reference for the custom view close button
				ImageButton closeButton = customView.findViewById(R.id.ib_close);

				// Set a click listener for the popup window close button
				closeButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						// Dismiss the popup window
						popUp.dismiss();
					}
				});

				popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
				popUp.showAtLocation(rLayout, Gravity.CENTER,0,0);
				popUp.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

				File f = null;
				ArrayList<String> res = new ArrayList<>();

				try
				{
					titrePopUp = popUp.getContentView().findViewById(R.id.titrePopUp);
					if(fileType.equals("-score.txt"))
						titrePopUp.setText("Phrase n°"+parent.getItemAtPosition(position));
					else
						titrePopUp.setText(parent.getItemAtPosition(position)+"");

					String line;
					filepath = bund.getString("path");
					filepath += "/" + parent.getItemAtPosition(position);
          
					f = new File(filepath, "/" + parent.getItemAtPosition(position) + fileType);

					if (filepath.length() == 60)
					{
						chemin = filepath.substring(19, filepath.length());
						fichierAudio = filepath.substring(filepath.length() - 1, filepath.length());
					}
					else if (filepath.length() == 61)
					{
						chemin = filepath.substring(19, filepath.length());
						fichierAudio = filepath.substring(filepath.length() - 2, filepath.length());
					}

					this.mediaPlayer = MediaPlayer.create(getApplicationContext(),
							Uri.parse(Environment.getExternalStorageDirectory().getPath()+ "/" + chemin + "/" + fichierAudio + ".wav"));
					mediaPlayer.start();
          
					BufferedReader br = new BufferedReader(new FileReader(f));

					while ((line = br.readLine()) != null)
						res.add(line);

					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
				dpWidth = displayMetrics.widthPixels / displayMetrics.density;
				initRes(res, true);
			}
		});
	}

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
						col.setText(" Mots ");
					break;

				case 1 :
					col.setText(" Temps (frame) ");
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

		for (int i = 0; i < output.size(); i++)
		{
			res = output.get(i);

			if (i == 0)
			{
				scoreNorm.setText(res);
			}
			else if (i > 1)
			{
				array = res.split(":");
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
					if(!array2[array2.length-1].substring(0,array2[array2.length-1].length()-1).equals("0")) {
						switch (j) {
							case 0:
								dataCol.setText(array2[0]);
								break;

							case 1:
								dataCol.setText(array[0]);
								break;

							case 2:
								dataCol.setText(array2[array2.length - 1].substring(0, array2[array2.length - 1].length() - 1));
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

