package ServeurPoolThreads;

import DBAcess.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RunnableTraitement implements Runnable, InterfaceRequestListener
{
    private Socket CSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private DBAcess.InterfaceBeansDBAccess beanDB;
    private Thread curThread = null;
    private ResultSet ResultatDB = null;
    
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
            System.err.println("RunnableTraitement : Host non trouvé : " + e);
        }
        
        beanDB = new BeanDBAccessOracle();
        beanDB.setBd("XE");  // PROPERTIES
        beanDB.setIp("localhost");
        beanDB.setPort(1521);
        beanDB.setUser("COMPTA");
        beanDB.setPassword("COMPTA");
        beanDB.setClient(this);
        beanDB.connexion();
    }

    @Override
    public void run()
    {
        System.out.println("RunnableTraitement : Execution du run");
        
        Boolean terminer = false;
        
        while (!terminer)
        {   
            String reponse = ReceiveMsg();  
            String[] parts = reponse.split("#");

            switch (parts[0])
            {
                case "LOGIN" :
                    Login(parts);
                    break;
                    
                case "LOGOUT" :
                    System.out.println("RunnableTraitement : Switch LOGOUT");
                    terminer = true;
                    break;
                    
                default :
                    break;
            }
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
            System.err.println("RunnableTraitement : Erreur d'envoi de msg (IO) : " + e);
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
            System.err.println("RunnableTraitement : Erreur de reception de msg (IO) : " + e);
        }
            
        return message.toString();
    }
    
    public void Login(String[] parts)
    {
        curThread = beanDB.selection("PASSWORD", "PERSONNEL", "LOGIN = '" + parts[1] + "'");

        try
        {
            curThread.join();
        }
        catch (InterruptedException ex)
        {
            System.err.println("RunnableTraitement : Join raté : " + ex);
        }
        
        try
        {
            while(ResultatDB.next())
            {
                if ((ResultatDB.getString(1)).equals(parts[2]))
                    SendMsg("OUI");
                else
                    SendMsg("NON");
            }
        }
        catch (SQLException ex)
        {
            System.err.println("RunnableTraitement : Erreur lecture ResultSet : " + ex);
        }
        
        System.out.println("RunnableTraitement : Fin LOGIN");
    }
    
    @Override
    public void resultRequest(ResultSet res)
    {
        ResultatDB = res;
    }

    @Override
    public void erreurRecue(String erreur)
    {
        System.err.println("RunnableTraitement : Erreur dans la réception des beans : " + erreur);
    }
}
