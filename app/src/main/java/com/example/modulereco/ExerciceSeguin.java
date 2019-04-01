package com.example.modulereco;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExerciceSeguin extends Exercice
{
	private ArrayList<String> phrases;
	private File dico = null;

	public ExerciceSeguin(Context context)
	{
		super(context);

		max = 12;

		phrases = new ArrayList<>();

		dico = new File(assetsDir, "chevre.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(dico)))
		{
			String line;

			while ((line = br.readLine()) != null)
			{
				phrases.add(line);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getText()
	{
		return phrases.get(index);
	}

	public boolean fini()
	{
		return max == index;
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
		return index;
	}

	protected void updateJsgf()
	{
		try
		{
			FileReader fr = new FileReader(new File(assetsDir, "chevre" + (index + 1) + ".jsgf"));
			BufferedReader br = new BufferedReader(fr);
			String s, tmp = "";

			while ((s = br.readLine()) != null)
			{
				tmp += s + "\n";
			}

			br.close();

			File f = new File(assetsDir, "chevre.jsgf");
			BufferedWriter output = new BufferedWriter(new FileWriter(f));

			output.write(tmp);
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}