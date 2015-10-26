package com.oceane.jerome.applicationserveurbateau;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.*;
import java.net.*;


public class MenuActivity extends AppCompatActivity
{
    private Socket cliSock;
    private String adresse;
    private int port;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Button bQuitter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ConnexionServeur();

        bQuitter = (Button)findViewById(R.id.ButtonQuitter);
        bQuitter.setEnabled(false);
        bQuitter.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Logout();
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
                {
                    bQuitter.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "CONNECTE AU SERVEUR !", Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Connexion Socket : " + msg.toString(), Toast.LENGTH_LONG).show();
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
                    System.err.println("MenuActivity : Host non trouvé : " + e);
                    msg.obj = "KO" + e.getMessage();
                }
                catch(IOException e)
                {
                    System.err.println("MenuActivity : Pas de connexion ? : " + e);
                    msg.obj = "KO" + e.getMessage();
                }

                h.sendMessage(msg);
            }
        }).start();
    }

    private void Logout()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Message msg = null;

                SendMsg("LOGOUT#", msg);

                try
                {
                    dos.close();
                    dis.close();
                    cliSock.close();

                    Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                catch(IOException e)
                {
                    System.err.println("MenuActivity : Erreur de déconnexion : " + e);
                }
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
            System.err.println("MenuActivity : Erreur d'envoi de msg (IO) : " + e);
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
            System.err.println("MenuActivity : Erreur de reception de msg (IO) : " + e);
            msg.obj = "KO" + e.getMessage();
        }

        return message.toString();
    }
}
