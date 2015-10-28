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
    private DataInputStream dis;
    private DataOutputStream dos;

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

        try
        {
            dis = new DataInputStream(new BufferedInputStream(LoginActivity.cliSock.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(LoginActivity.cliSock.getOutputStream()));
        }
        catch (IOException e)
        {
            System.err.println("BoatArrivedActivity : Erreur de création de dis et dos : " + e);
        }
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
                SendMsg("BOAT_ARRIVED#" + i + "#" + d, msg);

                ReceiveMsg(msg);
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
            System.err.println("BoatArrivedActivity : Erreur d'envoi de msg (IO) : " + e);
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
            System.err.println("BoatArrivedActivity : Erreur de reception de msg (IO) : " + e);
            msg.obj = "KO" + e.getMessage();
        }

        return message.toString();
    }
}
