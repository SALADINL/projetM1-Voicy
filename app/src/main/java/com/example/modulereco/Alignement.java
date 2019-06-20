package com.example.modulereco;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
		byte[] b = new byte[4096];

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
}