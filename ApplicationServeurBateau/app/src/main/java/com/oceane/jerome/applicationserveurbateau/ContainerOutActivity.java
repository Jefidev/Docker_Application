package com.oceane.jerome.applicationserveurbateau;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ContainerOutActivity extends AppCompatActivity
{
    private String reponse;
    private ArrayList<Container> ListeContainersRecherche = null;
    private ListView ListeContainersGraphique;
    private ArrayAdapter<Container> adapter;
    private ProgressBar progressbar;
    private int cptProgress = 0;
    private Button bRechercher;
    private DatabaseHandler sqlLiteConnection;
    private SQLiteDatabase DB;
    private long TempsDebut;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_out);

        bRechercher = (Button)findViewById(R.id.ButtonRechercher);
        bRechercher.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Rechercher();
            }
        });

        ListeContainersRecherche = new ArrayList<>();
        ListeContainersGraphique = (ListView)(findViewById(R.id.listViewContainers));
        progressbar = (ProgressBar)(findViewById(R.id.progressBar));

        ListeContainersGraphique.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SortieContainer((Container) (ListeContainersGraphique.getItemAtPosition(position)));
            }
        });

        Button bTerminer = (Button)findViewById(R.id.ButtonTerminer);
        bTerminer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Terminer();
            }
        });
    }

    private void Rechercher()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    String[] tuples = reponse.split("#");

                    if(!tuples[0].isEmpty())
                    {
                        for (String token : tuples) {
                            String[] champs = token.split("\\$");

                            Container c = new Container(champs[0], champs[1], champs[2], champs[3], champs[4]);
                            ListeContainersRecherche.add(c);
                        }

                        adapter = new ArrayAdapter<Container>(ContainerOutActivity.this, android.R.layout.simple_list_item_1, ListeContainersRecherche);
                        ListeContainersGraphique.setAdapter(adapter);
                        progressbar.setMax(ListeContainersRecherche.size());

                        bRechercher.setEnabled(false);

                        TempsDebut = System.currentTimeMillis();
                    }
                }

                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Recherche des containers : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Message msg = h.obtainMessage();

                String d = ((TextView) (findViewById(R.id.TextFieldDestination))).getText().toString();
                CheckBox cb = (CheckBox)(findViewById(R.id.checkBoxTri));

                String c;
                if (cb.isChecked())
                    c = "FIRST";
                else
                    c = "RANDOM";

                if (!d.isEmpty())
                {
                    Utility.SendMsg("GET_CONTAINERS#" + d + "#" + c, msg);

                    reponse = Utility.ReceiveMsg(msg);
                    h.sendMessage(msg);
                }
                else
                    System.err.println("REMPLIR TOUS LES CHAMPS !");
            }
        }).start();
    }

    private void SortieContainer(Container c)
    {
        final Container curCont = c;

        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    if (reponse.equals("OUI"))
                    {
                        ListeContainersRecherche.remove(curCont);
                        adapter.notifyDataSetChanged();
                        cptProgress++;
                        progressbar.setProgress(cptProgress);
                        Toast.makeText(getApplicationContext(), "Container en voie d'être supprimé", Toast.LENGTH_LONG).show();


                        sqlLiteConnection = new DatabaseHandler(getApplicationContext(), "DonneesDocker.sqlite", null, 3);
                        DB = sqlLiteConnection.getWritableDatabase();

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                        String date = format1.format(c.getTime());
                        long TempsFin = System.currentTimeMillis();
                        long temps = (TempsFin - TempsDebut)/1000;
                        String d = ((TextView) (findViewById(R.id.TextFieldDestination))).getText().toString();

                        ContentValues listeValeur = new ContentValues();
                        listeValeur.put("Mouvement", "OUT");
                        listeValeur.put("Date", date);
                        listeValeur.put("Duree", temps);
                        listeValeur.put("Docker", LoginActivity.curUser);
                        listeValeur.put("Destination", d);
                        DB.insert("STATISTIQUES", null, listeValeur);

                        TempsDebut = System.currentTimeMillis();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Le container choisi n'est pas le premier de la liste !", Toast.LENGTH_LONG).show();
                }

                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Container chargé : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Message msg = h.obtainMessage();

                Utility.SendMsg("HANDLE_CONTAINER_OUT#" + curCont.getId() + "#" + curCont.getX() + "#" + curCont.getY(), msg);

                reponse = Utility.ReceiveMsg(msg);
                h.sendMessage(msg);
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

                        Intent intent = new Intent(ContainerOutActivity.this, MenuActivity.class);
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
                Utility.SendMsg("END_CONTAINER_OUT#", msg);

                reponse = Utility.ReceiveMsg(msg);
                h.sendMessage(msg);
            }
        }).start();
    }
}
