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

	InputStream streamFichier = null;
	ArrayList<String> resultat;
	Context contexte;


	public DAP(Context contexte)
	{
		this.contexte = contexte;
		resultat = new ArrayList<>();
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

		if(!resultat.isEmpty())
			System.out.println(" C PA VIDE TAMER "+resultat.get(0));

		return resultat;
	}

	private void faireDAP(final InputStream stream)
	{
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

			Decoder d = new Decoder(c);

			d.startUtt();
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
					d.processRaw(s, nbytes / 2, false, false);
				}
			}
			catch (IOException e)
			{
				System.out.println("Error when reading inputstream" + e.getMessage());
			}

			d.endUtt();

			for (Segment seg : d.seg())
				resultat.add(seg.getStartFrame() + " - " + seg.getEndFrame() + " : " + seg.getWord());

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
