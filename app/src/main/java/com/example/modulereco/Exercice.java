package com.example.modulereco;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;

public abstract class Exercice
{
	protected Assets assets = null;
	protected File assetsDir = null;

	protected int max, index;

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

	protected abstract String getText();
	protected abstract boolean fini();
	protected abstract void next();
	protected abstract void prev();
	protected abstract int getMax();
	protected abstract int getIndex();
	protected abstract void updateJsgf();
}