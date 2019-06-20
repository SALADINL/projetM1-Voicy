package com.example.modulereco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import edu.cmu.pocketsphinx.Assets;

/**
 * Cette classe permet l'affichage des listes et la configuration des listes
 */
public class ChoixList extends Activity
{
    private Button home;
    private ListView listes = null;
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    protected Assets assets = null;
    protected File assetsDir = null;

    /**
     * Copier les listes qui se trouvent dans le dossier “Assets/Listes” du projet, vers le dossier “ModuleReco/Listes” du téléphone.
     * Afficher les listes qui se trouvent sur le téléphone
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_list);

        creerListe();

        try
        {
            assets = new Assets(this);
            assetsDir = assets.syncAssets();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        File source = new File(assetsDir, "/Listes");

        File destination = new File(Environment.getExternalStorageDirectory().getPath(), "/ModuleReco/Listes/");

        if (source.isDirectory())
        {
            File[] listFiles = source.listFiles();

            for (int i = 0; i < listFiles.length; i++)
                copyFileOrDirectory(String.valueOf(listFiles[i]), String.valueOf(destination));
        }

        home = findViewById(R.id.home);
        listes = findViewById(R.id.listes);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listes.setAdapter(adapter);

        String filepath = Environment.getExternalStorageDirectory().getPath();

        try
        {
            final File extStorageDir = new File(filepath, "/ModuleReco/Listes");
            final String[] fileList = extStorageDir.list();


            listItems.addAll(Arrays.asList(fileList));

            Collections.sort(listItems);
            adapter.notifyDataSetChanged();

            listes.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    String filepath = Environment.getExternalStorageDirectory().getPath() + "/ModuleReco/Listes/" + parent.getItemAtPosition(position);
                    File path = new File(filepath, "/ModuleReco/Listes" + parent.getItemAtPosition(position));
                    Intent myIntent = new Intent(view.getContext(), Reco.class);
                    myIntent.putExtra("path", filepath);
                    myIntent.putExtra("numeroDeListe", position);
                    myIntent.putExtra("random", 0);
                    myIntent.putExtra("type", 1);
                    startActivity(myIntent);
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(ChoixList.this, "Le dossier 'Listes' est vide !", Toast.LENGTH_LONG).show();

            e.getStackTrace();
        }

        home.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ChoixList.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    /**
     * Créer le dossier Listes à l'intérieur du dossier ModuleReco qui se trouve sur le téléphone
     *
     * @return le dossier Listes
     */
    private File creerListe()
    {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),"ModuleReco/Listes/");

        if (!file.exists())
            file.mkdirs();

        return file;
    }


    // https://stackoverflow.com/questions/29867121/how-to-copy-programmatically-a-file-to-another-directory

    /**
     * Fonction pour copier un dossier dans un autre dossier
     * On s'en sert pour copier les listes (4 listes prédéfini) qui se trouvent dans le dossier Listes (assets/sync/Listes) vers le dossier ModuleReco/Listes du téléphone
     *
     * @param srcDir dossier source
     * @param dstDir dossier destination
     */
    public static void copyFileOrDirectory(String srcDir, String dstDir)
    {
        try
        {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory())
            {
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else
                copyFile(src, dst);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Fonction pour copier un fichier, on s'en sert dans la fonction "copyFileOrDirectory"
     *
     * @param sourceFile fichier source
     * @param destFile   fichier destination
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException
    {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists())
            destFile.createNewFile();

        FileChannel source = null;
        FileChannel destination = null;

        try
        {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally
        {
            if (source != null)
                source.close();

            if (destination != null)
                destination.close();
        }
    }
}