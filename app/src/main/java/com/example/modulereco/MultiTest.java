package com.example.modulereco;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
    Button btnPhrase = null;
    Button btnPhoneme = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_test);

        verifierPermissions();

        btnPhrase = findViewById(R.id.btnMultiPhrase);
        btnPhoneme = findViewById(R.id.btnMultiPhoneme);

        btnPhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifierPermissions();
                new AsyncPhrase().execute();
            }
        });

        btnPhoneme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                verifierPermissions();
                new AsyncPhoneme().execute();
            }
        });

    }

    private void verifierPermissions()
    {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
        {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
        }
    }

    //-------------------------------------- CODE POUR LE LANCEMENT DES MULTI TEST SUR PHRASE ----------------------------------
    private class AsyncPhrase extends AsyncTask<Void, Integer, Void>
    {
        ProgressDialog mProgressDialog = new ProgressDialog(MultiTest.this);
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
        String nomDossierExercice = "";
        String nomDuFichier = "";
        int index = 0;
        int nbFichiers;

        ArrayList<String> tabPhrase = null;
        ArrayList<String> tabPhoneme = null;
        ArrayList<String> tabDap = null;
        ArrayList<String> tabSemi = null;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(MultiTest.this);
            mProgressDialog.setTitle("Patientez");
            mProgressDialog.setMessage("Traitement de la voix en cours ...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params)
        {

            Intent intent = getIntent();
            type = intent.getIntExtra("type", 1);
            nbtest = intent.getIntExtra("nbtest", 3);
            nbPhrase = intent.getIntExtra("nbPhrase", 12);
            numeroDeListe = intent.getIntExtra("numeroDeListe", 1);
            random = intent.getIntExtra("random", 0);

            tabPhrase = new ArrayList<>();

            // On sélectionne que la première phrase de l'exercice Seguin
            exo = new ExerciceSeguin(1, MultiTest.this);

            // On va créer qu'un seul dossier appelé n°1
            rec = new Recorder("" + exo.getIndex());

            // Chemin vers la SD CARD
            File sdCardRoot = Environment.getExternalStorageDirectory();

            // Chemin vers notre dossier de wav
            File yourDir = new File(sdCardRoot, "ModuleReco/multiTest");

            nomDossierExercice = "ModuleReco/Exercices/MultPhrase" + rec.getCurrentTimeUsingCalendar("1");

            nbFichiers = yourDir.listFiles().length;

            // Pour chaque fichier wav présent dans le fichier
            for (File wav : yourDir.listFiles())
            {
                if (wav.isFile())
                {
                    index++;

                    int progress = (100 / nbFichiers) * index;

                    publishProgress(progress);

                    nomDuFichier = wav.getName().substring(0, wav.getName().length() - 4);

                    // On créer son dossier et créer un fichier wav vide qui va être utiliser dans la fonction copyFileToAnotherDir(..., ...)
                    final File file = creerDossierv2();

                    // Permet de lancer les algorithmes de pocket sphinx et d'enregistrer le resultat dans deux fichiers .txt
                    analyser(wav);

                    // Copie le wav dans le dossier de l'analyse
                    try
                    {
                        copyFileToAnotherDir(wav, destinationFile);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            mProgressDialog.dismiss();

            Intent intent = new Intent(MultiTest.this, choixResultat.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }

        public void copyFileToAnotherDir(File sourceFile, File destinationFile) throws IOException
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
            File file = new File(Environment.getExternalStorageDirectory().getPath(), nomDossierExercice);

            destinationFile = new File(file.getAbsolutePath()+ "/" + nomDuFichier + "/" + nomDuFichier + ".wav");

            Log.d("abcdef", "destination : " + destinationFile);

            if (!file.exists())
                file.mkdirs();

            File file2 = new File(Environment.getExternalStorageDirectory().getPath(), nomDossierExercice + "/" + nomDuFichier);

            if (!file2.exists())
                file2.mkdirs();

            rec.setExo("Exo" + rec.getCurrentTimeUsingCalendar("1"));

            return file;
        }

        private void analyser(File wav)
        {
            clearTab();

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

            ArrayList<Pair<Integer, Integer>> timings = alignement.getTimings(wav, Environment.getExternalStorageDirectory().getPath() + "/" +nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score.txt");

            dap = new DAP(MultiTest.this);
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

            FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/" +nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score.txt");

            for (String str : tabPhrase)
                writer.write(str + "\n");

            writer.close();

        }

        private void sauverResultatsSemiContraint() throws IOException
        {
            FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/" +nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score-semiContraint.txt");

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
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------

    private class AsyncPhoneme extends AsyncTask<Void, Integer, Void>
    {
        ProgressDialog mProgressDialog = new ProgressDialog(MultiTest.this);
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
        int index = 0;
        int nbFichiers;
        File destinationFile = null;
        String nomDossierExercice = "";
        String nomDuFichier = "";

        ArrayList<String> tabPhoneme = null;
        ArrayList<String> tabDap = null;
        ArrayList<String> tabSemi = null;


        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MultiTest.this);
            mProgressDialog.setTitle("Patientez");
            mProgressDialog.setMessage("Traitement de la voix en cours ...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();

            Intent intent = new Intent(MultiTest.this, choixResultat.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            Intent intent = getIntent();
            type = intent.getIntExtra("type", 1);
            nbtest = intent.getIntExtra("nbtest", 3);
            nbPhrase = intent.getIntExtra("nbPhrase", 12);
            numeroDeListe = intent.getIntExtra("numeroDeListe", 1);
            random = intent.getIntExtra("random", 0);


            tabPhoneme = new ArrayList<>();
            tabDap = new ArrayList<>();
            dap = new DAP(MultiTest.this);

            exo = new ExerciceMot(52, numeroDeListe,MultiTest.this);

            // Chemin vers la SD CARD
            File sdCardRoot = Environment.getExternalStorageDirectory();

            // Chemin vers notre dossier de wav
            File yourDir = new File(sdCardRoot, "ModuleReco/multiPhoneme");

            rec = new Recorder("1");

            nomDossierExercice = "ModuleReco/Exercices/MultPhoneme" + rec.getCurrentTimeUsingCalendar("1");

            nbFichiers = yourDir.listFiles().length;

            // Pour chaque fichier wav présent dans le fichier
            for (File wav : yourDir.listFiles())
            {
                if (wav.isFile())
                {
                    nomDuFichier = wav.getName().substring(0, wav.getName().length() - 4);

                    index++;

                    int progress = (100 / nbFichiers) * index;

                    publishProgress(progress);

                    //Log.d("abcdef", "File : " + nomDossierExercice);
                    //Log.d("abcdef", "Fichier : " + nomDuFichier);

                    // On créer son dossier et créer un fichier wav vide qui va être utiliser dans la fonction copyFileToAnotherDir(..., ...)
                    final File file = creerDossierv2();

                    // Permet de lancer les algorithmes de pocket sphinx et d'enregistrer le resultat dans deux fichiers .txt
                    analyser(wav);

                    // Copie le wav dans le dossier de l'analyse
                    try
                    {
                        copyFileToAnotherDir(wav, destinationFile);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    // Patiente xxx milisecondes avant de passer aux fichiers wav suivant
                    try { Thread.sleep(800); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            }

            return null;
        }

        public void copyFileToAnotherDir(File sourceFile, File destinationFile) throws IOException
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
            File file = new File(Environment.getExternalStorageDirectory().getPath(), nomDossierExercice);

            destinationFile = new File(file.getAbsolutePath()+ "/" + nomDuFichier + "/" + nomDuFichier + ".wav");

            Log.d("abcdef", "destination : " + destinationFile);

            if (!file.exists())
                file.mkdirs();

            File file2 = new File(Environment.getExternalStorageDirectory().getPath(), nomDossierExercice + "/" + nomDuFichier);

            if (!file2.exists())
                file2.mkdirs();

            rec.setExo("Exo" + rec.getCurrentTimeUsingCalendar("1"));

            return file;
        }


        private void analyser(File wav)
        {
            clearTab();

            alignement = new Alignement(MultiTest.this, Alignement.PHONEME);
            tabPhoneme = alignement.convertir(wav);
            tabDap = dap.convertir(wav);

            try
            {
                sauverResultats();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            ArrayList<Pair<Integer, Integer>> timings = alignement.getTimings(wav, Environment.getExternalStorageDirectory().getPath() + "/" +nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score-phoneme.txt");

            dap = new DAP(MultiTest.this);
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

        private void clearTab()
        {
            if(tabPhoneme != null)
                if (!tabPhoneme.isEmpty())
                    tabPhoneme.clear();
            if(tabDap != null)
                if (!tabDap.isEmpty())
                    tabDap.clear();
        }

        private void sauverResultats() throws IOException
        {
            FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/" +nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score-phoneme.txt");

            for (String str : tabPhoneme)
                writer.write(str + "\n");

            writer.close();

            writer = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/" + nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score-dap.txt");

            for (String str : tabDap)
                writer.write(str + "\n");

            writer.close();
        }


        private void sauverResultatsSemiContraint() throws IOException
        {
            FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/" + nomDossierExercice + "/" + nomDuFichier + "/" + nomDuFichier + "-score-semiContraint.txt");

            for (String str : tabSemi)
                writer.write(str + "\n");

            writer.close();
        }
    }

}
