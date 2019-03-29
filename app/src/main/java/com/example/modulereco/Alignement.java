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


public class Alignement
{
	static { System.loadLibrary("pocketsphinx_jni"); }

	private InputStream streamFichier = null;
	private ArrayList<String> resultat;
	private Context contexte;
	private Decoder decoder = null;

	public final static int PHONEME = 1;
	public final static int MOT = 2;

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
			c.setBoolean("-bestpath", false);


			if (config == MOT)
			{
				c.setString("-jsgf", new File(assetsDir, "mot-word.jsgf").getPath());
				c.setString("-dict", new File(assetsDir, "mots.dict").getPath());
			}
			else
			{
				c.setString("-jsgf", new File(assetsDir, "mot-align.jsgf").getPath());
				c.setString("-dict", new File(assetsDir, "phonemes.dict").getPath());
			}

			decoder = new Decoder(c);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

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

		int score = 0, scoreAvecSil = 0;
		int premiereFrameSansSil = 10000, premiereFrameAvecSil = 10000,
				derniereFrameSansSil = 0, derniereFrameAvecSil = 0;

		for (Segment seg : decoder.seg())
		{
			int start = seg.getStartFrame(),
					end = seg.getEndFrame();
			String mot = seg.getWord();

			if (start < premiereFrameAvecSil)
				premiereFrameAvecSil = start;

			if (!mot.equals("sil") && start < premiereFrameSansSil)
				premiereFrameSansSil = start;

			if (derniereFrameAvecSil < end)
				derniereFrameAvecSil = end;

			if (!mot.equals("sil") && derniereFrameSansSil < end)
				derniereFrameSansSil = end;

			resultat.add(start + " - " + end + " : " + mot + " (" + seg.getAscore() + ")");
			if (!mot.equals("sil"))
				score += seg.getAscore();

			scoreAvecSil += seg.getAscore();
		}

		resultat.add("\n");
		resultat.add("Score : " + score);
		resultat.add("Score (avec silence) : " + scoreAvecSil);
		resultat.add("\n");
		resultat.add("Score normalisé : " + (float)score / (derniereFrameSansSil - premiereFrameSansSil));
		resultat.add("Score normalisé (avec silence) : " + (float)scoreAvecSil / (derniereFrameAvecSil - premiereFrameAvecSil));
	}
}