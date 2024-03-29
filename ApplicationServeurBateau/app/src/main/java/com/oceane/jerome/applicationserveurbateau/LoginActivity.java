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
import java.net.*;


public class LoginActivity extends AppCompatActivity
{
    public static Socket cliSock;
    private String adresse;
    private int port;
    private Button bConnexion;
    private String reponse;
    public static String curUser;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ConnexionServeur();

        bConnexion = (Button)findViewById(R.id.ButtonConnexion);
        bConnexion.setEnabled(false);
        bConnexion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Login();
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
                    Toast.makeText(getApplicationContext(), "PROBLEME : Connexion Socket : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                cliSock = null;
                adresse = "192.168.1.5";    // Le serveur n'étant pas fixe impossible d'avoir une IP fixe
                port = 31042;

                Message msg = h.obtainMessage();

                // CONNEXION SOCKET ET FLUX
                try
                {
                    cliSock = new Socket(adresse, port);
                    System.out.println(cliSock.getInetAddress().toString());
                    Utility.InitialisationFlux();
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

                h.sendMessage(msg);
            }
        }).start();
    }

    private void Login() {
        final Handler h = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.obj.equals("OK")) {
                    if (reponse.equals("OUI")) {
                        Toast.makeText(getApplicationContext(), "LOGIN REUSSI !", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);

                    } else
                        Toast.makeText(getApplicationContext(), "LOGIN RATE !", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplicationContext(), "PROBLEME : Identification à travers le serveur : " + msg.toString(), Toast.LENGTH_LONG).show();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = h.obtainMessage();

                curUser = ((TextView) (findViewById(R.id.TextFieldLogin))).getText().toString();
                String p = ((TextView) (findViewById(R.id.TextFieldPassword))).getText().toString();

                if (!curUser.isEmpty() && p.isEmpty())
                    Toast.makeText(getApplicationContext(), "REMPLISSEZ TOUS LES CHAMPS !", Toast.LENGTH_LONG).show();
                else {

                    Utility.SendMsg("LOGIN#" + curUser + "#" + p, msg);

                    reponse = Utility.ReceiveMsg(msg);
                    h.sendMessage(msg);
                }
            }
        }).start();
    }
}
