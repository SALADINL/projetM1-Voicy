package com.example.modulereco;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btAnalyse = findViewById(R.id.btAnalyse);

        btAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alignement();
            }
        });
    }

    private void alignement() {
    }
}
