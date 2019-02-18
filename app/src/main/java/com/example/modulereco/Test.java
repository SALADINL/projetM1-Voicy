package com.example.modulereco;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Segment;

public class Test extends Activity
{
    Button findFile = null;
    Button analyser = null;
    TextView filepath = null;
    TextView decodage = null;

    File file = null;
    InputStream stream = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findFile = findViewById(R.id.parcourirBtn);
        analyser = findViewById(R.id.analyserBtn);
        filepath = findViewById(R.id.filePath);
        decodage = findViewById(R.id.decode);

        try
        {
            Assets assets = new Assets(Test.this);
            File assetsDir = assets.syncAssets();

            file = new File(assetsDir, "salut.wav");
            filepath.setText("Salut");
            stream = new FileInputStream(file);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        findFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (filepath.getText() == "Salut")
                {
                    try
                    {
                        Assets assets = new Assets(Test.this);
                        File assetsDir = assets.syncAssets();

                        file = new File(assetsDir, "kanfrou.wav");
                        stream = new FileInputStream(file);
                        filepath.setText("Kanfrou");
                    }
                    catch (IOException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                else
                {
                    try
                    {
                        Assets assets = new Assets(Test.this);
                        File assetsDir = assets.syncAssets();

                        file = new File(assetsDir, "salut.wav");
                        stream = new FileInputStream(file);
                        filepath.setText("Salut");
                    }
                    catch (IOException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }

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
        TestDecodage decodeAsync = new TestDecodage(this, stream, new TestDecodage.AsyncResponse(){
            @Override                                              // implémentation de l'interface avec récupération des infos
            public void processFinish(ArrayList<String> output){
                TextView decode = findViewById(R.id.decode);
                String display = "";
                for(String it : output)
                {
                    if(it != null)
                        display += it+" \n";
                }
                decode.setText(display);
            }
        });
        decodeAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class TestDecodage extends AsyncTask<Void, Void, ArrayList<String>>
    {
        WeakReference<Test> activityReference;
        InputStream stream;
        public AsyncResponse delegate = null; //

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
            //String response = null;
            ArrayList<String> response = new ArrayList();
            try
            {
                Assets assets = new Assets(activityReference.get());
                File assetsDir = assets.syncAssets();

                Config c = Decoder.defaultConfig();
                c.setString("-hmm", new File(assetsDir, "ptm").getPath());
                c.setString("-dict", new File(assetsDir, "fr.dict").getPath());
                c.setBoolean("-allphone_ci", true);
                c.setString("-lm", new File(assetsDir, "fr-phone.lm.dmp").getPath());
                c.setFloat("-lw", 2.0);
                c.setFloat("-beam", 1e-20);
                c.setFloat("-pbeam", 1e-20);
                c.setFloat("-vad_threshold", 3.0);
                c.setBoolean("-remove_noise", true);

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
                System.out.println(d.hyp().getHypstr());
                //response = "AYO";

                for (Segment seg : d.seg())
                {
                    response.add(seg.getStartFrame() + " - " + seg.getEndFrame() + " : " + seg.getWord());
                    System.out.println(seg.getStartFrame() + " - " + seg.getEndFrame() + " : " + seg.getWord());
                }
            }
            catch (IOException e)
            {
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

}
