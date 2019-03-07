package com.example.modulereco;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
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
	TextView resultats;
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
		resultats = findViewById(R.id.resText);

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
				String res = "";

				for (String s : text)
					res += s + "\n";

				resultats.setText(res);
			}
		});

		boutonPHON.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aPhon = new Alignement(TestDAP.this, Alignement.PHONEME, motADecoder);
				ArrayList<String> text = aPhon.convertir(file);
				String res = "";

				for (String s : text)
					res += s + "\n";

				resultats.setText(res);
			}
		});

		boutonMOT.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aMot = new Alignement(TestDAP.this, Alignement.MOT, motADecoder);
				ArrayList<String> text = aMot.convertir(file);
				String res = "";

				for (String s : text)
					res += s + "\n";

				resultats.setText(res);
			}
		});
	}
}
