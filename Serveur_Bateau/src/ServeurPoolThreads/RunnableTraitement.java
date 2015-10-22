package ServeurPoolThreads;

import java.io.*;
import java.net.*;


public class RunnableTraitement implements Runnable
{
    private Socket CSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    
    public RunnableTraitement(Socket s)
    {
        CSocket = s;
        
        try
        {
            dis = new DataInputStream(new BufferedInputStream(CSocket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(CSocket.getOutputStream()));
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Host non trouvé : " + e);
        }
    }

    @Override
    public void run()
    {
        System.out.println("RunnableTraitement : Execution du run");
        
        Boolean terminer = false;
        
        while (!terminer)
        {
            System.out.println("RunnableTraitement : Début while serveur");
            
            String typeRequete = "LOGIN";
            // LECTURE
            
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
                System.out.println(taille);
                
                for (int i = 0; i < Integer.parseInt(taille.toString()); i++)
                {
                    b = dis.readByte();
                    message.append((char)b);
                }
                System.out.println(message);
                
                String[] parts = (message.toString()).split("#");
                System.out.println(parts[0]);
                System.out.println(parts[1]);
                System.out.println(parts[2]);
            }
            catch(IOException e)
            {
                System.err.println("ClientServeurBateau : Host non trouvé : " + e);
            }
            
            switch (typeRequete)
            {
                case "LOGIN" :
                    System.out.println("RunnableTraitement : Switch LOGIN");
                    break;
                    
                default :
                    break;
            }
            
            terminer = true;
        }
    }
}
