package com.example.modulereco;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.Segment;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class Test extends Activity implements RecognitionListener
{
    Button findFile = null;
    Button analyser = null;
    TextView filepath = null;
    TextView decodage = null;

    String currentFile;
    File file = null;
    InputStream stream = null;

    private static final int ACTIVITY_CHOOSE_FILE = 3;

    private SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findFile = findViewById(R.id.parcourirBtn);
        analyser = findViewById(R.id.analyserBtn);
        filepath = findViewById(R.id.filePath);
        decodage = findViewById(R.id.decode);

        findFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("audio/*");
                intent = Intent.createChooser(chooseFile, "Choisissez un fichier");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });

        analyser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                convertir(stream);
            }
        });
    }

    private void convertir(final InputStream stream)
    {
        TestDecodage decodeAsync = new TestDecodage(this, stream, new TestDecodage.AsyncResponse() {

            @Override                                              // implémentation de l'interface avec récupération des infos
            public void processFinish(ArrayList<String> output)
            {
                TextView decode = findViewById(R.id.decode);
                String display = "";

                for(String it : output)
                    if(it != null)
                        display += it + "\n";

                decode.setText(display);
            }
        });
        decodeAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override public void onBeginningOfSpeech() { }
    @Override public void onEndOfSpeech() { }
    @Override public void onPartialResult(Hypothesis hypothesis) { }
    @Override public void onResult(Hypothesis hypothesis) { }
    @Override public void onError(Exception e) { }
    @Override public void onTimeout() { }


    private static class TestDecodage extends AsyncTask<Void, Void, ArrayList<String>>
    {
        WeakReference<Test> activityReference;
        InputStream stream;
        AsyncResponse delegate;

        TestDecodage(Test activity, InputStream stream, AsyncResponse delegate)
        {
            this.activityReference = new WeakReference<>(activity);
            this.stream = stream;
            this.delegate = delegate;
        }

        public interface AsyncResponse // recupère l'output de l'async
        {
            void processFinish(ArrayList<String> output);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids)
        {
            ArrayList<String> response = new ArrayList<>();
            try {
                Assets assets = new Assets(activityReference.get());
                File assetsDir = assets.syncAssets();

                activityReference.get().setupRecognizer(assetsDir);

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

                try {
                    int nbytes;

                    while ((nbytes = stream.read(b)) >= 0)
                    {
                        ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);

                        bb.order(ByteOrder.LITTLE_ENDIAN);

                        short[] s = new short[nbytes / 2];
                        bb.asShortBuffer().get(s);
                        d.processRaw(s, nbytes / 2, false, false);
                    }
                } catch (IOException e) {
                    System.out.println("Error when reading inputstream" + e.getMessage());
                }

                d.endUtt();
                System.out.println(d.hyp().getHypstr());

                for (Segment seg : d.seg())
                {
                    response.add(seg.getStartFrame() + " - " + seg.getEndFrame() + " : " + seg.getWord() + " (" + seg.getAscore() + ")");
                    System.out.println(seg.getStartFrame() + " - " + seg.getEndFrame() + " : " + seg.getWord() + " (" + seg.getAscore() + ")");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        public void onPostExecute(ArrayList<String> res) // renvoie le resultat reçu à la fin de la tache async
        {
            delegate.processFinish(res);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK)
            return;

        if(requestCode == ACTIVITY_CHOOSE_FILE)
        {
            Uri uri = data.getData();
            currentFile = getRealPathFromURI(uri);

            if (currentFile != null)
                System.out.println(currentFile.substring(currentFile.length() - 4));

            if (currentFile != null && currentFile.substring(currentFile.length() - 4).toLowerCase().equals(".wav"))
            {
                filepath.setText(currentFile);
                analyser.setEnabled(true);
                file = new File(currentFile);

                try {
                    stream = new FileInputStream(file);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            else
            {
                filepath.setText("Veuillez sélectionner un fichier valide");
                analyser.setEnabled(false);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri)
    {
        String [] proj = {MediaStore.Images.Media.DATA};
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(contentUri, proj,null,null,null);

        if (cursor == null)
            return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private void setupRecognizer(File assetDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetDir, "ptm"))
                .setDictionary(new File(assetDir, "fr.dict"))
                .getRecognizer();

        recognizer.addListener(this);
    }
}
