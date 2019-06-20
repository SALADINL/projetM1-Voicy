package com.example.modulereco;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;

/**
 * @author Ken Bres
 *
 * Modèle abstrait d'exercice permettant de faire les actions de bases d'un exercice.
 * Un exercice est une liste d'éléments à prononcer.
 */
public abstract class Exercice
{
	protected Assets assets = null;
	protected File assetsDir = null;
	protected File dico = null;

	protected int max, index;

	/**
	 * Initialise l'exercice (dossier asset).
	 *
	 * @param context Contexte dans lequel sera utilisé l'exercice.
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
	 * Permet de savoir si l'exercice est fini.
	 *
	 * @return True si c'est fini et False sinon
	 */
	public boolean fini()
	{
		return index == max - 1;
	}

	/**
	 * Permet de passer à l'élément suivant.
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
	 * Permet de revenir à l'élément précédent.
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
	 * Permet de savoir le nombre d'éléments que possède l'exercice.
	 *
	 * @return le nombre d'éléments de l'exercice.
	 */
	public int getMax()
	{
		return max;
	}

	/**
	 * Permet de savoir à quel élément est l'exercice.
	 *
	 * @return l'index de l'élément courant.
	 */
	public int getIndex()
	{
		return index + 1;
	}

	/**
	 * Retourne le texte à prononcer de l'élément courant.
	 *
	 * @return le texte à prononcer.
	 */
	protected abstract String getText();

	/**
	 * Met à jour le fichier JSGF.
	 */
	protected abstract void updateJsgf();
}