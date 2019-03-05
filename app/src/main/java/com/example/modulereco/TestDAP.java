package com.example.modulereco;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;

public class TestDAP extends Activity
{
	File file;

	Button bouton;
	TextView resultatDAP;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_dap);

		bouton = findViewById(R.id.boutonDAP);
		resultatDAP = findViewById(R.id.resText);

		try
		{
			Assets assets = new Assets(this);
			File assetsDir = assets.syncAssets();

			file = new File(assetsDir, "calamar.wav");
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}

		ArrayList<String> text;

		bouton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				DAP dap = new DAP(TestDAP.this);
				ArrayList<String> text = dap.convertir(file);
				String res = "";

				for (String s : text)
					res += s + "\n";

				resultatDAP.setText(res);
			}
		});
	}
}
