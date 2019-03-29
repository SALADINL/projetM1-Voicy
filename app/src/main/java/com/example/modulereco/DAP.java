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

public class DAP
{
	static { System.loadLibrary("pocketsphinx_jni"); }

	private InputStream streamFichier = null;
	private ArrayList<String> resultat;
	private Context contexte;
	private Decoder decoder = null;

	public DAP(Context contexte)
	{
		this.contexte = contexte;
		resultat = new ArrayList<>();

		try
		{
			Assets assets = new Assets(this.contexte);
			File assetsDir = assets.syncAssets();

			Config c = Decoder.defaultConfig();
			c.setString("-hmm", new File(assetsDir, "ptm").getPath());
			c.setString("-allphone", new File(assetsDir, "fr-phone.lm.dmp").getPath());
			c.setBoolean("-backtrace", true);
			c.setFloat("-beam", 1e-20);
			c.setFloat("-pbeam", 1e-20);
			c.setFloat("-lw", 2.0);

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

		faireDAP(streamFichier);

		return resultat;
	}

	private void faireDAP(final InputStream stream)
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

			if (!mot.equals("SIL") && start < premiereFrameSansSil)
				premiereFrameSansSil = start;

			if (derniereFrameAvecSil < end)
				derniereFrameAvecSil = end;

			if (!mot.equals("SIL") && derniereFrameSansSil < end)
				derniereFrameSansSil = end;

			resultat.add(start + " - " + end + " : " + mot + " (" + seg.getAscore() + ")");
			System.out.println(seg.getProb());
			if (!mot.equals("SIL"))
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
