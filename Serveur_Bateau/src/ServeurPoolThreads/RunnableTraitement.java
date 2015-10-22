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
            
            byte b;
            StringBuffer taille = new StringBuffer();
            StringBuffer message = new StringBuffer();
            String[] parts = null;
            
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
                
                parts = (message.toString()).split("#");
            }
            catch(IOException e)
            {
                System.err.println("ClientServeurBateau : Host non trouvé : " + e);
            }
            
            switch (parts[0])
            {
                case "LOGIN" :
                    System.out.println(parts[1]);
                    System.out.println(parts[2]);
                    SendMsg("LOGIN#OUI");
                    System.out.println("RunnableTraitement : Switch LOGIN");
                    break;
                    
                case "LOGOUT" :
                    System.out.println("RunnableTraitement : Switch LOGOUT");
                    terminer = true;
                    break;
                    
                default :
                    break;
            }
            
            //terminer = true;
        }
        
        System.out.println("RunnableTraitement : Fin du while et du client");
    }
    
    public void SendMsg(String msg)
    {
        String chargeUtile = msg;
        int taille = chargeUtile.length();
        StringBuffer message = new StringBuffer(String.valueOf(taille) + "#" + chargeUtile);
            
        try
        {    
            dos.write(message.toString().getBytes());
            dos.flush();
        }
        catch(IOException e)
        {
            System.err.println("RunnableTraitement : Erreur d'envoi de msg : " + e);
        }
    }
}
