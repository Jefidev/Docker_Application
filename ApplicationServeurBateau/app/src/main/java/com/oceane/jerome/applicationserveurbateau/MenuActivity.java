package com.oceane.jerome.applicationserveurbateau;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.io.*;


public class MenuActivity extends AppCompatActivity
{
    private DataInputStream dis;
    private DataOutputStream dos;
    private String user;

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

        Button bOut = (Button)findViewById(R.id.ButtonOut);
        bOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ContainerOutActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        Button bIn = (Button)findViewById(R.id.ButtonIn);
        bIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, BoatArrivedActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        Button bStat1 = (Button)findViewById(R.id.ButtonStat3);
        bStat1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NombreContainersChargesDechargesParJour stat = new NombreContainersChargesDechargesParJour();
                Intent intent = stat.getIntent(MenuActivity.this);
                startActivity(intent);
            }
        });

        Button bStat2 = (Button)findViewById(R.id.ButtonStat3);
        bStat2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TempsMoyenChargementDechargementParDocker stat = new TempsMoyenChargementDechargementParDocker();
                Intent intent = stat.getIntent(MenuActivity.this);
                startActivity(intent);
            }
        });

        Button bStat3 = (Button)findViewById(R.id.ButtonStat3);
        bStat3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TempsMoyenChargementDechargementParDocker stat = new TempsMoyenChargementDechargementParDocker();
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

        user = getIntent().getStringExtra("user");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        user = getIntent().getStringExtra("user");
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

                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(intent);
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
