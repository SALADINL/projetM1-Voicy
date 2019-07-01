package com.example.modulereco;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Noaman TATA, Ken Bres
 * La classe Recorder permet d'effectuer les enregistrement des fichiers .wav
 *
 */
public class Recorder
{
	/**
	 * Les fichier .wav utilisé par sphinx ont les caractéristiques suivante
	 */
	private static final int RECORDER_BPP = 16;
	private static String AUDIO_RECORDER_FOLDER = "";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final int RECORDER_SAMPLERATE = 16000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	short[] audioData;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;

	private String output;

	/**
	 * constructeur
	 * @param path
	 */
	public Recorder(String path)
	{
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
		RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

		audioData = new short[bufferSize]; // short array that pcm data is put into.
		output = path;
	}

	/**
	 * Setter pour le path de l'exo
	 * @param exoPath
	 */
	public void setExo(String exoPath)
	{
		AUDIO_RECORDER_FOLDER = "ModuleReco/Exercices/"+exoPath;
	}

	/**
	 * Getter sur l'état d'enregistrement
	 * @return
	 */
	public boolean getRecording()
	{
		return isRecording;
	}

	/**
	 * @author Noaman TATA
	 * Renvoie la date et l'heure à laquel un exo est fait
	 * @param type = 0 renvoie la date normal = 1 renvoie la date sous forme de code pour nommer les exercices
	 * @return Date et heure
	 */
	public String getCurrentTimeUsingCalendar(String type)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String res = sdf.format(new Date());

		if(type == "0") // Date normale
		{
			res = res.replace(" ", "_");
			res = res.replace("/", "-");
		}

		if(type == "1") // si on veut la date sous forme de code pour les exercices ex : Exercice260319161656
		{
			sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			res = sdf.format(new Date());
			res = res.replace(" ", "");
			res = res.replace("/", "");
			res = res.replace(":", "");
		}
		else
			res = "0000";

		return res;
	}

	/**
	 * @author Noaman TATA
	 * Renvoie le nom du fichier wav si le dossier de l'exercice n'existe pas il est alors crée.
	 * @return nom du fichier wav
	 */
	public String getFilename()
	{
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER + "/" + output);

		if (!file.exists())
			file.mkdirs();

		return (file.getAbsolutePath() + "/" + output + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	/**
	 * @author Noaman TATA
	 * Crée le dossier et le fichier wav temporaire nécessaire au wav final
	 * @return retourne le path du fichier temp
	 */
	private String getTempFilename()
	{
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists())
			file.mkdirs();

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	/**
	 * @author Noaman TATA, Ken BRES
	 * Commence l'enregistrement du fichier
	 */
	public void startRecording()
	{
		recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
		RECORDER_SAMPLERATE, RECORDER_CHANNELS,
		RECORDER_AUDIO_ENCODING, bufferSize);

		if (recorder.getState() == 1)
			recorder.startRecording();

		isRecording = true;

		recordingThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();
	}

	/**
	 * author Noaman TATA & https://stackoverflow.com/questions/22279414/how-to-record-audio-using-audiorecorder-in-android
	 * Convertie l'audio en byte
	 */
	private void writeAudioDataToFile()
	{
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try
		{
			os = new FileOutputStream(filename);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		int read = 0;

		if (null != os)
		{
			while (isRecording)
			{
				read = recorder.read(data, 0, bufferSize);

				if (AudioRecord.ERROR_INVALID_OPERATION != read)
				{
					try
					{
						os.write(data);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			try
			{
				os.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stop l'enregistrement et delete le fichier temporaire
	 */
	public void stopRecording()
	{
		if (null != recorder)
		{
			isRecording = false;

			if (recorder.getState() == 1)
				recorder.stop();

			recorder.release();

			recorder = null;
			recordingThread = null;
		}

		copyWaveFile(getTempFilename(), getFilename());
		deleteTempFile();
	}

	/**
	 * Supprime le wav temporaire
	 */
	private void deleteTempFile()
	{
		File file = new File(getTempFilename());
		file.delete();
	}

	/**
	 * Copie un fichier wav dans un autres
	 * @param inFilename fichier source
	 * @param outFilename fichier en sortie
	 */
	private void copyWaveFile(String inFilename, String outFilename)
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		int channels = 1;
		long totalAudioLen = 0,
			 totalDataLen = totalAudioLen + 36,
			 byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
		byte[] data = new byte[bufferSize];

		try
		{
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen, RECORDER_SAMPLERATE, channels, byteRate);

			while (in.read(data) != -1)
				out.write(data);

			in.close();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @author Noaman TATA & https://github.com/krvarma/krvarma-android-samples/blob/master/AudioRecorder.2/src/com/varma/samples/audiorecorder/RecorderActivity.java
	 *
	 * @param out fichier de sortie
	 * @param totalAudioLen
	 * @param totalDataLen
	 * @param longSampleRate
	 * @param channels
	 * @param byteRate
	 * @throws IOException
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException
	{
		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (16 /8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}
}