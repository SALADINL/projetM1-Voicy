package com.example.modulereco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChoixList extends Activity
{
    private Button home;
    private Button list1, list2, list3, list4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_list);

        home = findViewById(R.id.home);

        list1 = findViewById(R.id.list1);
        list2 = findViewById(R.id.list2);
        list3 = findViewById(R.id.list3);
        list4 = findViewById(R.id.list4);

        list1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ChoixList.this, Reco.class);
                intent.putExtra("numeroDeListe", 1);
                intent.putExtra("random", 0);
                startActivity(intent);
            }
        });

        list2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ChoixList.this, Reco.class);
                intent.putExtra("numeroDeListe", 2);
                intent.putExtra("random", 0);
                startActivity(intent);
            }
        });

        list3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ChoixList.this, Reco.class);
                intent.putExtra("numeroDeListe", 3);
                intent.putExtra("random", 0);
                startActivity(intent);
            }
        });

        list4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ChoixList.this, Reco.class);
                intent.putExtra("numeroDeListe", 4);
                intent.putExtra("random", 0);
                startActivity(intent);
            }
        });

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
}