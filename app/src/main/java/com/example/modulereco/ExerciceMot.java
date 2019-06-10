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

/**
 * La classe ExerciceMot qui hérite de la classe Exercice
 */
public class ExerciceMot extends Exercice
{
	private ArrayList<Mot> mots;

	/**
	 * Constructeur
	 *
	 * @param context
	 */
	public ExerciceMot(Context context)
	{
		super(context);

		max = 50;
		mots = new ArrayList<>();
		dico = new File(assetsDir, "mots.dict");

		init(dico);
	}

	/**
	 * Constructeur
	 *
	 * @param nb      le nombre max de mot
	 * @param context
	 */
	public ExerciceMot(int nb, Context context)
	{
		super(context);

		max = nb;
		mots = new ArrayList<>();
		dico = new File(assetsDir, "mots.dict");

		init(dico);
	}

	/**
	 * Constructeur
	 *
	 * @param nb           le nombre max de mot
	 * @param numeroListes le numéro de liste choisit
	 * @param context
	 */
	public ExerciceMot(int nb, int numeroListes, Context context)
	{
		super(context);

		max = nb;
		mots = new ArrayList<>();
		String filepath = Environment.getExternalStorageDirectory().getPath();
		int num = numeroListes + 1;
		dico = new File(filepath, "/ModuleReco/Listes/Liste" + num);
		System.out.println("dico : " + dico);

		initAvecListe(dico);
	}

	/**
	 * Lecture du fichier et affecte dans la variable
	 * On ne mélange pas le fichier
	 *
	 * @param f Fichier qui contient la liste des non-mots
	 */
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

	/**
	 * Lecture du fichier et affecte dans la variable
	 * On mélange le fichier
	 *
	 * @param f Fichier qui contient les non-mots
	 */
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

	/**
	 * Pour ne pas prononcer deux fois le même mot
	 *
	 * @param f le fichier
	 * @return mot
	 */
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

	/**
	 * Fonction qui retourne le mot à prononcer
	 *
	 * @return le mot
	 */
	public String getText()
	{
		return mots.get(index).getMot();
	}

	/**
	 * Fonction qui mets à jour le JSGF
	 */
	protected void updateJsgf()
	{
		updateAlignJsgf();
		updateWordJsgf();
	}

	/**
	 * Fonction qui mets à jour AlignJSGF
	 */
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

	/**
	 * Fonction qui mets à jour le WordJSGF
	 */
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