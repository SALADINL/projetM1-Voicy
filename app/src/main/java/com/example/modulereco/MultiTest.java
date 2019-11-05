package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * @author Ken Bres,
 * La classe Reco utilisée pour l'enregistrement
 */
public class MultiTest extends Activity
{
    Exercice exo = null;
    Recorder rec = null;
    Alignement alignement = null;
    DAP dap = null;
    TextView mot = null;
    TextView compteur = null;
    ImageButton enregistrer = null;

    Button annuler;

    Button btEnd = null;
    Button retour = null;
    int type = 0;  // 1 = exo avec mot 0 = exo avec phrase
    int nbtest = 0;
    int nbPhrase = 0;
    int numeroDeListe = 0;
    int random = 0;
    File destinationFile = null;

    ArrayList<String> tabPhrase = null;
    ArrayList<String> tabPhoneme = null;
    ArrayList<String> tabDap = null;
    ArrayList<String> tabSemi = null;

    /**
     * @author Ahmet AGBEKTAS, Ken Bres, Noaman TATA
     *
     * Nous pouvons s'enregistrer avec deux types d'exercices : Logatome ou Phrase
     * Nous avons un bouton pour s'enregistrer et finir l'enregistrement, un autre pour faire retour si jamais nous avons mal prononcé
     * Nous avons un autre bouton pour annuler l'enregistrement et revenir à l'accueil, l'annulation effacera le dossier de l'enregistrement en cours
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_test);

        verifierPermissions();

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 1);
        nbtest = intent.getIntExtra("nbtest", 3);
        nbPhrase = intent.getIntExtra("nbPhrase", 12);
        numeroDeListe = intent.getIntExtra("numeroDeListe", 1);
        random = intent.getIntExtra("random", 0);

        tabPhrase = new ArrayList<>();

        exo = new ExerciceSeguin(1, this);

        rec = new Recorder("" + exo.getIndex());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(sdCardRoot, "ModuleReco/multiTest");
        for (File wav : yourDir.listFiles())
        {
            if (wav.isFile())
            {
                final File file = creerDossierv2();

                analyser(wav);

                try
                {
                    copyFileToAnotherDir(wav, destinationFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyFileToAnotherDir(File sourceFile, File destinationFile) throws IOException
    {
        FileChannel source = null;
        FileChannel destination = null;

        try
        {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destinationFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private File creerDossierv2()
    {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),"ModuleReco/Exercices/Exo" + rec.getCurrentTimeUsingCalendar("1"));

        destinationFile = new File(file.getAbsolutePath()+"/1/1.wav");

        Log.d("abcdef", "destination : " + destinationFile);

        if (!file.exists())
            file.mkdirs();

        rec.setExo("Exo" + rec.getCurrentTimeUsingCalendar("1"));

        return file;
    }

    private void analyser(File wav)
    {
        clearTab();

        //File wav = new File(rec.getFilename());

        alignement = new Alignement(MultiTest.this, Alignement.VOISIN);
        tabPhrase = alignement.convertir(wav);

            try
            {
                sauverResultats();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            ArrayList<Pair<Integer, Integer>> timings = alignement.getTimings(wav, type);

            dap = new DAP(this);
            tabSemi = new ArrayList<>();

            for (Pair<Integer, Integer> p : timings)
                tabSemi.add(dap.convertirSemiVersion1(wav, p.first, p.second));

            try
            {
                sauverResultatsSemiContraint();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    private void sauverResultats() throws IOException
    {
        String nom = rec.getFilename();
        nom = nom.substring(0, nom.length() - 4);


        FileWriter writer = new FileWriter(nom + "-score.txt");

        for (String str : tabPhrase)
            writer.write(str + "\n");

        writer.close();

    }

    private void sauverResultatsSemiContraint() throws IOException
    {
        String nom = rec.getFilename();
        nom = nom.substring(0, nom.length() - 4);

        FileWriter writer = new FileWriter(nom + "-score-semiContraint.txt");

        for (String str : tabSemi)
            writer.write(str + "\n");

        writer.close();
    }

    private void clearTab()
    {
        if(tabPhrase != null)
        {
            if (!tabPhrase.isEmpty())
            {
                tabPhrase.clear();
            }
        }
    }

    /**
     * @author Ahmet AGBEKTAS
     *
     * Vérification des permissions d'accès au stockage et au microphone
     */
    private void verifierPermissions()
    {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
        {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
        }
    }


}
