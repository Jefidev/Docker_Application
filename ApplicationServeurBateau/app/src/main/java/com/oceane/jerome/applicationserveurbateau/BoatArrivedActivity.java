package com.oceane.jerome.applicationserveurbateau;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;


public class BoatArrivedActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boat_arrived);

        Button bAjouter = (Button)findViewById(R.id.ButtonAjouter);
        bAjouter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ajouter();
            }
        });
    }

    private void Ajouter()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    Toast.makeText(getApplicationContext(), "BATEAU AJOUTE !", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(BoatArrivedActivity.this, ContainerInActivity.class);
                    startActivity(intent);
                }

                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Ajout du bateau : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Message msg = h.obtainMessage();

                String i = ((TextView) (findViewById(R.id.TextFieldId))).getText().toString();
                String d = ((TextView) (findViewById(R.id.TextFieldDestination))).getText().toString();

                if (!i.isEmpty() && !d.isEmpty())
                {
                    Utility.SendMsg("BOAT_ARRIVED#" + i + "#" + d, msg);

                    Utility.ReceiveMsg(msg);
                    h.sendMessage(msg);
                }
                else
                    System.err.println("REMPLIR TOUS LES CHAMPS !");
            }
        }).start();
    }
}
