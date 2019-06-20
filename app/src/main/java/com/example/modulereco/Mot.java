package com.example.modulereco;

/**
 * @author Ken Bres
 *
 * Classe définissant ce qu'est un mot.
 */
public class Mot
{
	private String mot;
	private String prononciation;

	/**
	 * Constructeur permettant de créer un mot.
	 *
	 * @param m Le mot.
	 * @param p Sa prononciation.
	 */
	public Mot(String m, String p)
	{
		mot = m;
		prononciation = p;
	}

	/**
	 * Surcharge de la fonction toString afin de renvoyer l'écriture du mot.
	 *
	 * @return le mot.
	 */
	@Override
	public String toString()
	{
		return getMot();
	}

	/**
	 * Permet de créer à la volée le contenu du fichier JSGF pour l'alignement par phonème.
	 */
	public String getAlignFormat()
	{
		return "#JSGF V1.0;\ngrammar forcing;\npublic <" + mot + "> = sil " + prononciation + " [ sil ];";
	}

	/**
	 * Permet de créer à la volée le contenu du fichier JSGF pour l'alignement par mot.
	 */
	public String getWordFormat()
	{
		return "#JSGF V1.0;\ngrammar word;\npublic <wholeutt> = sil " + mot + " [ sil ];";
	}

	/**
	 * Fonction qui retourne le mot.
	 *
	 * @return le mot.
	 */
	public String getMot()
	{
		return mot;
	}

	/**
	 * Fonction qui retourne la prononciation du mot.
	 *
	 * @return la prononciation.
	 */
	public String getPrononciation()
	{
		return prononciation;
	}
}
