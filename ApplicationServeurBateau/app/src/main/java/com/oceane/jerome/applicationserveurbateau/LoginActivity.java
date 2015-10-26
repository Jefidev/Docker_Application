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

import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.net.*;


public class LoginActivity extends AppCompatActivity
{
    private Socket cliSock;
    private String adresse;
    private int port;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Button bConnexion;
    private String reponse;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        ConnexionServeur();

        bConnexion = (Button)findViewById(R.id.ButtonConnexion);
        bConnexion.setEnabled(false);
        bConnexion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Identification();
            }
        });
    }

    private void ConnexionServeur()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                    bConnexion.setEnabled(true);
                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Connexion Socket : " + msg.toString(),
                            Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                cliSock = null;
                adresse = "10.59.14.51";    // "Properties" sur le gsm serait mieux (le serveur n'étant pas fixe impossible d'avoir une IP fixe)
                port = 31042;

                Message msg = h.obtainMessage();

                // CONNEXION SOCKET ET FLUX
                try
                {
                    cliSock = new Socket(adresse, port);
                    System.out.println(cliSock.getInetAddress().toString());
                    dis = new DataInputStream(new BufferedInputStream(cliSock.getInputStream()));
                    dos = new DataOutputStream(new BufferedOutputStream(cliSock.getOutputStream()));
                    msg.obj = "OK";
                }
                catch(UnknownHostException e)
                {
                    System.err.println("LoginActivity : Host non trouvé : " + e);
                    msg.obj = "KO" + e.getMessage();
                }
                catch(IOException e)
                {
                    System.err.println("LoginActivity : Pas de connexion ? : " + e);
                    msg.obj = "KO" + e.getMessage();
            }

                System.out.println("LoginActivity : Connexion client finie");
                h.sendMessage(msg);
            }
        }).start();
    }

    private void Identification()
    {
        final Handler h = new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.obj.equals("OK"))
                {
                    if (reponse.equals("OUI"))
                        Toast.makeText(getApplicationContext(), "LOGIN REUSSI !", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), "LOGIN RATE !", Toast.LENGTH_LONG).show();
                }

                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Identification à travers le serveur : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String l = ((TextView) (findViewById(R.id.TextFieldLogin))).getText().toString();
                String p = ((TextView) (findViewById(R.id.TextFieldPassword))).getText().toString();
                SendMsg("LOGIN#" + l + "#" + p);

                reponse = ReceiveMsg();
            }
        }).start();
    }

    public void SendMsg(String chargeUtile)
    {
        int taille = chargeUtile.length();
        String message = String.valueOf(taille) + "#" + chargeUtile;

        try
        {
            dos.write(message.getBytes());
            dos.flush();
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateu : Erreur d'envoi de msg (IO) : " + e);
        }
    }

    public String ReceiveMsg()
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
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Erreur de reception de msg (IO) : " + e);
        }

        return message.toString();
    }
}
