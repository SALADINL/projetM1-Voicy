package com.example.modulereco;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * La classe ExerciceSeguin qui hérite de la classe Exercice
 */
public class ExerciceSeguin extends Exercice
{
	private ArrayList<String> phrases;

	/**
	 * Constructeur
	 *
	 * @param context
	 */
	public ExerciceSeguin(Context context)
	{
		super(context);

		max = 12;
		phrases = new ArrayList<>();
		dico = new File(assetsDir, "chevre.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(dico)))
		{
			String line;

			while ((line = br.readLine()) != null)
			{
				phrases.add(line);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		updateJsgf();
	}

	/**
	 * Constructeur
	 *
	 * @param nb      le nombre max de phrase
	 * @param context
	 */
	public ExerciceSeguin(int nb, Context context)
	{
		this(context);
		max = nb;
	}

	/**
	 * Fonction qui retourne la phrase à prononcer
	 *
	 * @return la phrase
	 */
	public String getText()
	{
		return phrases.get(index);
	}

	/**
	 * Fonction qui mets à jour le JSGF
	 */
	protected void updateJsgf()
	{
		try
		{
			FileReader fr = new FileReader(new File(assetsDir, "chevre" + (index + 1) + ".jsgf"));
			BufferedReader br = new BufferedReader(fr);
			String s, tmp = "";

			while ((s = br.readLine()) != null)
			{
				tmp += s + "\n";
			}

			br.close();

			File f = new File(assetsDir, "chevre.jsgf");
			BufferedWriter output = new BufferedWriter(new FileWriter(f));

			output.write(tmp);
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}