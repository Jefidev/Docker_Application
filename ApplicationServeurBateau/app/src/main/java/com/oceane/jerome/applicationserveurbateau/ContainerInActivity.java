package com.oceane.jerome.applicationserveurbateau;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ContainerInActivity extends AppCompatActivity
{
    private String reponse;
    private DatabaseHandler sqlLiteConnection;
    private SQLiteDatabase DB;
    private long TempsDebut;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_in);

        Button bAjouter = (Button)findViewById(R.id.ButtonAjouter);

        bAjouter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Ajouter();
            }
        });

        Button bTerminer = (Button)findViewById(R.id.ButtonTerminer);
        bTerminer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Terminer();
            }
        });

        TempsDebut = System.currentTimeMillis();
    }

    private void Ajouter()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    if (reponse.equals("OUI")) {
                        Toast.makeText(getApplicationContext(), "CONTAINER AJOUTE !", Toast.LENGTH_LONG).show();

                        sqlLiteConnection = new DatabaseHandler(getApplicationContext(), "DonneesDocker.sqlite", null, 3);
                        DB = sqlLiteConnection.getWritableDatabase();

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        String date = format1.format(c.getTime());
                        long TempsFin = System.currentTimeMillis();
                        long temps = (TempsFin - TempsDebut)/1000;
                        String d = ((TextView) (findViewById(R.id.TextFieldDestination))).getText().toString();

                        ContentValues listeValeur = new ContentValues();
                        listeValeur.put("Mouvement", "IN");
                        listeValeur.put("Date", date);
                        listeValeur.put("Duree", temps);
                        listeValeur.put("Docker", LoginActivity.curUser);
                        listeValeur.put("Destination", d);
                        DB.insert("STATISTIQUES", null, listeValeur);

                        TempsDebut = System.currentTimeMillis();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "PLUS DE PLACE DANS LE PARC !", Toast.LENGTH_LONG).show();
                }

                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Ajout du container : " + msg.toString(), Toast.LENGTH_LONG).show();
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
                    Utility.SendMsg("HANDLE_CONTAINER_IN#" + i + "#" + d, msg);

                    reponse = Utility.ReceiveMsg(msg);
                    h.sendMessage(msg);
                }
                else
                    System.err.println("REMPLIR TOUS LES CHAMPS !");

            }
        }).start();
    }

    private void Terminer()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    if (reponse.equals("OUI"))
                    {
                        Toast.makeText(getApplicationContext(), "FICHIER A JOUR !", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ContainerInActivity.this, MenuActivity.class);
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "FICHIER PAS MIS A JOUR !", Toast.LENGTH_LONG).show();
                }

                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Ecriture fichier parc : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Message msg = h.obtainMessage();
                Utility.SendMsg("END_CONTAINER_IN#", msg);

                reponse = Utility.ReceiveMsg(msg);
                h.sendMessage(msg);
            }
        }).start();
    }
}
