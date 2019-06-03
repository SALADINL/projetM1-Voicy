package com.example.modulereco;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class ExerciceMot extends Exercice
{
	private ArrayList<Mot> mots;

	public ExerciceMot(Context context)
	{
		super(context);

		max = 50;
		mots = new ArrayList<>();
		dico = new File(assetsDir, "mots.dict");

		init(dico);
	}

	public ExerciceMot(int nb, Context context)
	{
		super(context);

		max = nb;
		mots = new ArrayList<>();
		dico = new File(assetsDir, "mots.dict");

		init(dico);
	}

	public ExerciceMot(int nb, int numeroListes, Context context)
	{
		super(context);

		max = nb;
		mots = new ArrayList<>();
		//dico = new File(assetsDir, "Listes/Liste " + numeroListes);
		String filepath = Environment.getExternalStorageDirectory().getPath();
		int num = numeroListes + 1;
		dico = new File(filepath, "/ModuleReco/Listes/Liste" + num);
		System.out.println("dico : " + dico);

		initAvecListe(dico);
	}

	public void initAvecListe(File f)
	{
		mots.clear();

		String[] res = null;

		try (BufferedReader br = new BufferedReader(new FileReader(f)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				res = line.split("\t");
				mots.add(new Mot(res[0], res[1]));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		updateJsgf();
	}


	public void init(File f)
	{
		mots.clear();

		try
		{
			for (int i = 0; i < max; i++)
			{
				String[] res = null;
				boolean pasok = false;

				do
				{
					res = getMotRandom(f).split("\t");

					for (int j = 0; j < mots.size(); j++)
						if (mots.get(j).getMot().equals(res[0]))
						{
							pasok = true;
							break;
						}

				} while (pasok);

				mots.add(new Mot(res[0], res[1]));
			}

			updateJsgf();
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e.getStackTrace());
		}
	}

	private static String getMotRandom(File f) throws FileNotFoundException
	{
		String result = null;
		Random rand = new Random();
		int n = 0;

		for(Scanner sc = new Scanner(f); sc.hasNext();)
		{
			++n;
			String line = sc.nextLine();
			if(rand.nextInt(n) == 0)
				result = line;
		}

		return result;
	}

	public String getText()
	{
		return mots.get(index).getMot();
	}

	protected void updateJsgf()
	{
		updateAlignJsgf();
		updateWordJsgf();
	}

	private void updateAlignJsgf()
	{
		File f = null;
		String tmp = mots.get(index).getAlignFormat();
		BufferedWriter output = null;

		try
		{
			f = new File(assetsDir, "mot-align.jsgf");
			output = new BufferedWriter(new FileWriter(f));
			output.write(tmp);
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void updateWordJsgf()
	{
		File f = null;
		String tmp = mots.get(index).getWordFormat();
		BufferedWriter output = null;

		try
		{
			f = new File(assetsDir, "mot-word.jsgf");
			output = new BufferedWriter(new FileWriter(f));
			output.write(tmp);
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}