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

        Button bIn = (Button)findViewById(R.id.ButtonIn);
        bIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, BoatArrivedActivity.class);
                startActivity(intent);
            }
        });

        Button bOut = (Button)findViewById(R.id.ButtonIn);
        bOut.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(MenuActivity.this, ContainerOutActivity.class);
                startActivity(intent);
            }
        });

        Button bQuitter = (Button)findViewById(R.id.ButtonQuitter);
        bQuitter.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Logout();
            }
        });
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
                    System.exit(0);
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
