package com.example.modulereco;

/**
 * La classe Mot
 */
public class Mot
{
	private String mot;
	private String prononciation;

	/**
	 * Constructeur
	 *
	 * @param m Mot
	 * @param p Prononciation
	 */
	public Mot(String m, String p)
	{
		mot = m;
		prononciation = p;
	}

	/**
	 * Fonction qui retourne le mot
	 *
	 * @return le mot
	 */
	@Override
	public String toString()
	{
		return getMot();
	}

	/**
	 * Fonction pour remplir le fichier JSGF utilisé par l’alignement par phonème
	 *
	 */
	public String getAlignFormat()
	{
		return "#JSGF V1.0;\ngrammar forcing;\npublic <" + mot + "> = sil " + prononciation + " [ sil ];";
	}

	/**
	 * Fonction pour remplir le fichier JSGF utilisé par l’alignement par mot
	 *
	 */
	public String getWordFormat()
	{
		return "#JSGF V1.0;\ngrammar word;\npublic <wholeutt> = sil " + mot + " [ sil ];";
	}

	/**
	 * Fonction qui retourne le mot
	 *
	 * @return le mot
	 */
	public String getMot()
	{
		return mot;
	}

	/**
	 * Fonction qui retourne la prononciation
	 *
	 * @return la prononciation
	 */
	public String getPrononciation()
	{
		return prononciation;
	}
}
