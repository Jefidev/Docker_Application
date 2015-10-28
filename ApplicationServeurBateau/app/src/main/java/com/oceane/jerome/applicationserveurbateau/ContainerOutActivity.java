package com.oceane.jerome.applicationserveurbateau;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;


public class ContainerOutActivity extends AppCompatActivity
{

    private DataInputStream dis;
    private DataOutputStream dos;
    private String reponse;
    //private ArrayList<> ListeContainersRecherche;
    private ListView ListeContainersGraphique;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_out);

        try
        {
            dis = new DataInputStream(new BufferedInputStream(LoginActivity.cliSock.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(LoginActivity.cliSock.getOutputStream()));
        }
        catch (IOException e)
        {
            System.err.println("ContainerOutActivity : Erreur de création de dis et dos : " + e);
        }

        Button bRechercher = (Button)findViewById(R.id.ButtonRechercher);
        bRechercher.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Rechercher();
            }
        });

        Button bTerminer = (Button)findViewById(R.id.ButtonTerminer);
        bTerminer.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Terminer();
            }
        });

        ListeContainersGraphique = (ListView)(findViewById(R.id.listViewContainers));
    }

    private void Rechercher()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    // REMPLISSAGE DE LA LISTVIEW
                    //Récupération des tuples dans la liste de containers
                    //ArrayAdapter<String> adapter = new ArrayAdapter<Container>(ContainerOutActivity.this, android.R.layout., prenoms);
                    //ListeContainersGraphique.setAdapter(adapter);
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

                String c;
                if ((findViewById(R.id.checkBoxTri)).isSelected())
                    c = "FIRST";
                else
                    c = "RANDOM";

                SendMsg("GET_CONTAINERS#" + d + "#" + c, msg);

                reponse = ReceiveMsg(msg);
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
                SendMsg("END_CONTAINER_OUT#", msg);

                reponse = ReceiveMsg(msg);
                h.sendMessage(msg);
            }
        }).start();
    }

    public void SendMsg(String chargeUtile, Message msg)
    {
        int taille = chargeUtile.length();
        String message = String.valueOf(taille) + "#" + chargeUtile;

        try
        {
            dos.write(message.getBytes());
            dos.flush();
            if (msg != null)
                msg.obj = "OK";
        }
        catch(IOException e)
        {
            System.err.println("ContainerOutActivity : Erreur d'envoi de msg (IO) : " + e);
            if (msg != null)
                msg.obj = "KO" + e.getMessage();
        }
    }

    public String ReceiveMsg(Message msg)
    {
        byte b;
        StringBuffer taille = new StringBuffer();
        StringBuffer message = new StringBuffer();

        try
        {
            while ((b = dis.readByte()) != (byte)'#')
            {
                if (b != (byte)'#')
                    taille.append((char)b);
            }

            for (int i = 0; i < Integer.parseInt(taille.toString()); i++)
            {
                b = dis.readByte();
                message.append((char)b);
            }
            msg.obj = "OK";
        }
        catch(IOException e)
        {
            System.err.println("ContainerOutActivity : Erreur de reception de msg (IO) : " + e);
            msg.obj = "KO" + e.getMessage();
        }

        return message.toString();
    }
}
