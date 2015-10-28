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


public class ContainerInActivity extends AppCompatActivity
{
    private DataInputStream dis;
    private DataOutputStream dos;
    private String reponse;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        try
        {
            dis = new DataInputStream(new BufferedInputStream(LoginActivity.cliSock.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(LoginActivity.cliSock.getOutputStream()));
        }
        catch (IOException e)
        {
            System.err.println("MenuActivity : Erreur de création de dis et dos : " + e);
        }

        Button bAjouter = (Button)findViewById(R.id.ButtonAjouter);
        bAjouter.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Ajouter();
            }
        });

        Button bTerminer = (Button)findViewById(R.id.ButtonTerminer);
        bTerminer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Terminer();
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
                    if (reponse.equals("OUI"))
                        Toast.makeText(getApplicationContext(), "CONTAINER AJOUTE !", Toast.LENGTH_LONG).show();
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
                SendMsg("HANDLE_CONTAINER_IN#" + i + "#" + d, msg);

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
                SendMsg("END_CONTAINER_IN#", msg);

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
            System.err.println("ContainerInActivity : Erreur d'envoi de msg (IO) : " + e);
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
            System.err.println("ContainerInActivity : Erreur de reception de msg (IO) : " + e);
            msg.obj = "KO" + e.getMessage();
        }

        return message.toString();
    }
}
