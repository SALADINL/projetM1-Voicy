package com.example.modulereco;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;

public abstract class Exercice
{
	protected Assets assets = null;
	protected File assetsDir = null;
	protected File dico = null;

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

	public boolean fini()
	{
		return index == max - 1;
	}

	public void next()
	{
		if (index < max - 1)
		{
			index++;
			updateJsgf();
		}
	}

	public void prev()
	{
		if (index > 0)
		{
			index--;
			updateJsgf();
		}
	}

	public int getMax()
	{
		return max;
	}

	public int getIndex()
	{
		return index + 1;
	}

	protected abstract String getText();
	protected abstract void updateJsgf();
}