package ServeurPoolThreads;

import DBAcess.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;


public class RunnableTraitement implements Runnable, InterfaceRequestListener
{
    private Socket CSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private DBAcess.InterfaceBeansDBAccess beanOracle;
    private DBAcess.InterfaceBeansDBAccess beanCSV;
    private Thread curThread = null;
    private ResultSet ResultatDB = null;
    private ArrayList<Parc> ListeParc = null;
    
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
        
        beanOracle = new BeanDBAccessOracle();
        beanOracle.setBd("XE");  // PROPERTIES
        beanOracle.setIp("localhost");
        beanOracle.setPort(1521);
        beanOracle.setUser("COMPTA");
        beanOracle.setPassword("COMPTA");
        beanOracle.setClient(this);
        beanOracle.connexion();
        
        beanCSV = new BeanDBAccessCSV();        // A CHANGER
        beanCSV.setBd("XE");                    // PROPERTIES
        beanCSV.setIp("localhost");
        beanCSV.setPort(1521);
        beanCSV.setUser("COMPTA");
        beanCSV.setPassword("COMPTA");
        beanCSV.setClient(this);
        beanCSV.connexion();
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
                    
                case "BOAT_ARRIVED" :
                    BoatArrived(parts);
                    break;
                    
                case "HANDLE_CONTAINER_IN" :
                    HandleContainerIn(parts);
                    break;
                    
                case "END_CONTAINER_IN" :
                    EndContainerIn();
                    break;
                    
                case "LOGOUT" :
                    System.out.println("RunnableTraitement : LOGOUT");
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
        curThread = beanOracle.selection("PASSWORD", "PERSONNEL", "LOGIN = '" + parts[1] + "'");

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
    
    public void BoatArrived(String[] parts)
    {
        Bateau b = new Bateau(parts[1], parts[2]); // parts[1] = id, parts[2] = destination
        
        String FichierPath = System.getProperty("user.dir");
        
        try
        {
            FileOutputStream fos = new FileOutputStream(FichierPath);
            ObjectOutputStream ecriture = new ObjectOutputStream(fos);
            ecriture.writeObject(b);
        }
        catch (FileNotFoundException ex)
        {
            System.err.println("RunnableTraitement : Fichier bateau non trouvé : " + ex);
        }
        catch(IOException e)
        {
            System.err.println("RunnableTraitement : " + e);
        }
        
        SendMsg("OUI");
    }
    
    public void HandleContainerIn(String[] parts)
    {
        Parc p = new Parc(parts[1], parts[2], parts[3], parts[4], parts[5]);
        // parts[1] = x, parts[2] = y, parts[3] = id, parts[4] = destination, parts[5] = date d'ajout
        
        // Recherche dans le csv après un emplacement

        // envoie oui si place + ListeParc.add(p);
        // envoie non si pas de place
    }
    
    public void EndContainerIn()
    {
        // On boucle sur la liste pour ajouter réellement dans le fichier.
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
