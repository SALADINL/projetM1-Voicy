package com.example.modulereco;

public class Mot
{
	private String mot;
	private String prononciation;

	public Mot(String m, String p)
	{
		mot = m;
		prononciation = p;
	}

	@Override
	public String toString()
	{
		return getMot();
	}

	public String getAlignFormat()
	{
		return "#JSGF V1.0;\ngrammar forcing;\npublic <" + mot + "> = sil " + prononciation + " [ sil ];";
	}

	public String getWordFormat()
	{
		return "#JSGF V1.0;\ngrammar word;\npublic <wholeutt> = sil " + mot + " [ sil ];";
	}

	public String getMot()
	{
		return mot;
	}

	public String getPrononciation()
	{
		return prononciation;
	}
}
