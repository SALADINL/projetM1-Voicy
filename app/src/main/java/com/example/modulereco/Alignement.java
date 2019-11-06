package com.example.modulereco;

import android.content.Context;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Segment;

/**
 * @author Ken Bres
 *
 * Classe permettant d'initialiser un décodeur de façon a effectuer un Alignement.
 */
public class Alignement
{
	static { System.loadLibrary("pocketsphinx_jni"); }

	private InputStream streamFichier = null;
	private ArrayList<String> resultat;
	private Context contexte;
	private Decoder decoder = null;

	public final static int PHONEME = 1,
							MOT = 2,
							VOISIN = 3;

	/**
	 * Permet d'initialiser le décodeur d'une certaine façon.
	 *
	 * @param contexte 	le contexte dans lequel sera appliqué l'alignement.
	 * @param config 	le type d'alignement qui va être effectué
	 */
	public Alignement(Context contexte, int config)
	{
		this.contexte = contexte;
		resultat = new ArrayList<>();

		try
		{
			Assets assets = new Assets(this.contexte);
			File assetsDir = assets.syncAssets();

			Config c = Decoder.defaultConfig();
			c.setString("-hmm", new File(assetsDir, "ptm").getPath());
			c.setBoolean("-backtrace", true);
			c.setBoolean("-fsgusefiller", false);


			if (config == MOT)
			{
				c.setBoolean("-bestpath", false);
				c.setString("-jsgf", new File(assetsDir, "mot-word.jsgf").getPath());
				c.setString("-dict", new File(assetsDir, "mots.dict").getPath());
			}
			else if (config == PHONEME)
			{
				c.setBoolean("-bestpath", false);
				c.setString("-jsgf", new File(assetsDir, "mot-align.jsgf").getPath());
				c.setString("-dict", new File(assetsDir, "phonemes.dict").getPath());
			}
			else if (config == VOISIN)
			{
				c.setBoolean("-bestpath", true);
				c.setString("-jsgf", new File(assetsDir, "chevre.jsgf").getPath());
				c.setString("-dict", new File(assetsDir, "phonemes.dict").getPath());
			}

			decoder = new Decoder(c);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Fonction permettant d'effectuer un alignement sur un fichier audio.
	 *
	 * @param fichier le fichier WAV converti en type File.
	 * @return un tableau contenant les résultats.
	 */
	public ArrayList<String> convertir(final File fichier)
	{
		try
		{
			streamFichier = new FileInputStream(fichier);
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			return null;
		}

		aligner(streamFichier);

		return resultat;
	}

	/**
	 * Fonction appellée par la fonction convertir(). C'est ici qu'est fait le traîtement.
	 *
	 * @param stream le fichier WAV converti en type InputStream.
	 */
	private void aligner(final InputStream stream)
	{
		decoder.startUtt();
		byte[] b = new byte[1024];
		int i = 0;

		try
		{
			int nbytes;

			while ((nbytes = stream.read(b)) >= 0)
			{
				ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);

				bb.order(ByteOrder.LITTLE_ENDIAN);

				short[] s = new short[nbytes / 2];
				bb.asShortBuffer().get(s);
				decoder.processRaw(s, nbytes / 2, false, false);
				i += nbytes;
			}
		}
		catch (IOException e)
		{
			System.out.println("Error when reading inputstream" + e.getMessage());
		}

		decoder.endUtt();

		int score = 0,
				trames = 0,
				tramesBonus = 0;

		for (Segment seg : decoder.seg())
		{
			if (!seg.getWord().equals("sil"))
			{
				trames += seg.getEndFrame() - seg.getStartFrame();
				score += seg.getAscore();

				if (seg.getEndFrame() != seg.getStartFrame())
					tramesBonus++;
			}
			else if (tramesBonus != 0)
			{
				trames += tramesBonus - 1;
				tramesBonus = 0;
			}
		}

		if (tramesBonus != 0)
		{
			trames += tramesBonus - 1;
		}

		resultat.add("Score normalisé : " + ((float)score / trames) + "\n");

		for (Segment seg : decoder.seg())
		{
			int start = seg.getStartFrame(),
					end   = seg.getEndFrame();
			String mot = seg.getWord();

			resultat.add(start + " - " + end + " : " + mot + " (" + seg.getAscore() + ")");
		}
	}

	/**
	 * Permet de récupérer les temps de début et de fin convertis en byte de chaque phonèmes trouvés par l'alignement.
	 * Utile à l'alignement semi-contraint.
	 * @param fichier 	Le fichier audio
	 * @param type		Le type d'alignement effectué au préalable sur ce fichier
	 * @return			Un tableau de paires pour chaque phonème
	 */
	public ArrayList<Pair<Integer, Integer>> getTimings(final File fichier, int type)
	{
		String path = fichier.getAbsolutePath();
		path = path.substring(0, path.length() - 4);

		if (type == 1)
			path += "-score-phoneme.txt";
		else if (type == 2)
			path += "-score.txt";

		File fichiertxt = new File(path);
		ArrayList<String> fichierString = new ArrayList<>();
		ArrayList<Pair<Integer, Integer>> res = new ArrayList<>();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fichiertxt));

			String st;
			int ligne = 0;

			while ((st = br.readLine()) != null)
			{
				if (ligne >= 2 && !st.contains("sil") && !st.contains("SIL") && !st.contains("NULL"))
					fichierString.add(st.substring(0, st.indexOf(":") - 1));

				ligne++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		for (String s : fichierString)
		{
			int debut, fin;

			debut = Integer.parseInt(s.substring(0, s.indexOf("-") - 1)) * 320;
			fin = Integer.parseInt(s.substring(s.indexOf("-") + 2)) * 320;

			res.add(new Pair<>(debut, fin));
		}

		return res;
	}

	public ArrayList<Pair<Integer, Integer>> getTimings(final File fichier, String pathFile)
	{
		String path = pathFile;

		File fichiertxt = new File(path);
		ArrayList<String> fichierString = new ArrayList<>();
		ArrayList<Pair<Integer, Integer>> res = new ArrayList<>();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fichiertxt));

			String st;
			int ligne = 0;

			while ((st = br.readLine()) != null)
			{
				if (ligne >= 2 && !st.contains("sil") && !st.contains("SIL") && !st.contains("NULL"))
					fichierString.add(st.substring(0, st.indexOf(":") - 1));

				ligne++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		for (String s : fichierString)
		{
			int debut, fin;

			debut = Integer.parseInt(s.substring(0, s.indexOf("-") - 1)) * 320;
			fin = Integer.parseInt(s.substring(s.indexOf("-") + 2)) * 320;

			res.add(new Pair<>(debut, fin));
		}

		return res;
	}
}