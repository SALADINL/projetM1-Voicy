package com.example.modulereco;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;

/**
 * Classe abstraite Exercice
 */
public abstract class Exercice
{
	protected Assets assets = null;
	protected File assetsDir = null;
	protected File dico = null;

	protected int max, index;

	/**
	 * Constructeur
	 *
	 * @param context
	 */
	public Exercice(Context context)
	{
		index = 0;

		try
		{
			assets = new Assets(context);
			assetsDir = assets.syncAssets();
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Fonction pour déterminer la fin d'un exercice
	 *
	 * @return True si c'est fini et False sinon
	 */
	public boolean fini()
	{
		return index == max - 1;
	}

	/**
	 * On avance dans l'exercice si on n'est pas arrivé à la fin
	 */
	public void next()
	{
		if (index < max - 1)
		{
			index++;
			updateJsgf();
		}
	}

	/**
	 * On recule dans l'exercice si on n'est pas déjà au début
	 */
	public void prev()
	{
		if (index > 0)
		{
			index--;
			updateJsgf();
		}
	}

	/**
	 * Fonction pour savoir le max de mot ou de phrase dans un exercice
	 *
	 * @return max
	 */
	public int getMax()
	{
		return max;
	}

	/**
	 * Fonction pour s'avoir l'indice courant
	 *
	 * @return index
	 */
	public int getIndex()
	{
		return index + 1;
	}

	/**
	 * Fonction qui retourne le texte à prononcer
	 *
	 * @return le texte
	 */
	protected abstract String getText();

	/**
	 * Fonction qui mets à jour le JSGF
	 */
	protected abstract void updateJsgf();
}