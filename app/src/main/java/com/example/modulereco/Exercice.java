package com.example.modulereco;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import edu.cmu.pocketsphinx.Assets;

public class Exercice
{
	private ArrayList<Mot> mots;
	private int nbMots;
	private int motActuel;

	private Assets assets = null;
	private File assetsDir = null;
	private File dico = null;

	public Exercice(int nb, Context context)
	{
		nbMots = nb;
		motActuel = 0;
		mots = new ArrayList<>();

		try
		{
			assets = new Assets(context);
			assetsDir = assets.syncAssets();

			dico = new File(assetsDir, "mots.dict");
			init(dico);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void init(File f)
	{
		mots.clear();

		try
		{
			for (int i = 0; i < nbMots; i++)
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

	public Mot getMot()
	{
		return getMot(motActuel);
	}

	public Mot getMot(int index)
	{
		if (!fini())
			return mots.get(index);

		return null;
	}

	private void updateJsgf()
	{
		updateAlignJsgf();
		updateWordJsgf();
	}

	private void updateAlignJsgf()
	{
		File f = null;
		String tmp = mots.get(motActuel).getAlignFormat();
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
		String tmp = mots.get(motActuel).getWordFormat();
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

	public void resetCetExo()
	{
		motActuel = 0;
	}

	public boolean fini()
	{
		return nbMots == motActuel;
	}

	public void next()
	{
		if (motActuel < nbMots - 1)
		{
			motActuel++;
			updateJsgf();
		}
	}

	public void prev()
	{
		if (motActuel > 0)
		{
			motActuel--;
			updateJsgf();
		}
	}

	public int getMaxMots()
	{
		return nbMots;
	}

	public int getIndex()
	{
		return motActuel;
	}
}
