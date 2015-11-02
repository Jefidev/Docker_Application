package com.oceane.jerome.applicationserveurbateau;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MenuActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button bOut = (Button)findViewById(R.id.ButtonOut);
        bOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ContainerOutActivity.class);
                startActivity(intent);
            }
        });

        Button bIn = (Button)findViewById(R.id.ButtonIn);
        bIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, BoatArrivedActivity.class);
                startActivity(intent);
            }
        });

        Button bStat1 = (Button)findViewById(R.id.ButtonStat1);
        bStat1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GraphMouvements stat = new GraphMouvements();
                Intent intent = stat.getIntent(MenuActivity.this);
                startActivity(intent);
            }
        });

        Button bStat2 = (Button)findViewById(R.id.ButtonStat2);
        bStat2.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String semaine = ((TextView) (findViewById(R.id.editTextSemaine))).getText().toString();
                String mouvement;
                if (findViewById(R.id.radioButtonIn).isSelected())
                    mouvement = "IN";
                else
                    mouvement = "OUT";

                if (!semaine.isEmpty() && !mouvement.isEmpty())
                {
                    GraphRepartition stat = new GraphRepartition();
                    Intent intent = stat.getIntent(MenuActivity.this, semaine, mouvement);
                    startActivity(intent);
                }
            }
        });

        Button bStat3 = (Button)findViewById(R.id.ButtonStat3);
        bStat3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GraphTempsOperation stat = new GraphTempsOperation();
                Intent intent = stat.getIntent(MenuActivity.this);
                startActivity(intent);
            }
        });

        Button bQuitter = (Button)findViewById(R.id.ButtonQuitter);
        bQuitter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Logout();
            }
        });
    }

    private void Logout()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Message msg = null;

                Utility.SendMsg("LOGOUT#", msg);

                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }).start();
    }
}
