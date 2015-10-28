package ServeurPoolThreads;

import DBAcess.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RunnableTraitement implements Runnable, InterfaceRequestListener
{
    private Socket CSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private DBAcess.InterfaceBeansDBAccess beanOracle;
    private DBAcess.BeanDBAccessCSV beanCSV;
    private Thread curThread = null;
    private ResultSet ResultatDB = null;
    private ArrayList<Parc> ListeParc = null;
    
    private Parc currentContainer =  null;
    
    private ArrayList<Bateau> ListeBateauAmarre;
    
    
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
        beanOracle.setBd("XE");                 // PROPERTIES
        beanOracle.setIp("localhost");
        beanOracle.setPort(1521);
        beanOracle.setUser("COMPTA");
        beanOracle.setPassword("COMPTA");
        beanOracle.setClient(this);
        beanOracle.connexion();
        
        beanCSV = new BeanDBAccessCSV();
        beanCSV.setClient(this);
        beanCSV.connexion();
        
        /* FICHIER CSV */
        File f = new File(System.getProperty("user.dir")+ System.getProperty("file.separator") + "parc.csv");
        if(!f.exists())
        {
            try
            {
                f.createNewFile();
            }
            catch (IOException ex)
            {
                System.err.println("Creation du fichier CSV ratee : " + ex);
            }
        }
        else
        {
            MaJListeParc();
        }
        
        /*Recuperation des bateaux amarrés*/
        System.err.println("Recuperation fichier bateau");
        String pathFichierBateau = System.getProperty("user.dir") + System.getProperty("file.separator") + "bateaux.dat";
        
        try
        {
            FileInputStream fis = new FileInputStream(pathFichierBateau);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            ListeBateauAmarre = (ArrayList<Bateau>)ois.readObject();
            System.err.println("recuperation de la liste des bateaux");
            
        } 
        catch (FileNotFoundException ex) 
        {
            ListeBateauAmarre =  new ArrayList<>();
        } 
        catch (IOException ex) 
        {
            System.err.println("Erreur fichier bateau : " + ex);
        } 
        catch (ClassNotFoundException ex) 
        {
            System.err.println("Erreur fichier bateau (class not found) : " + ex);
        }
        
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
    
    /* Envoi d'un message au client */
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
    
    /* Réception d'un message du client */
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
    
    /* Login */
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
    
    /* On met dans un fichier les bateaux entrant */
    public void BoatArrived(String[] parts)
    {
        Bateau b = new Bateau(parts[1], parts[2]);
        
        ListeBateauAmarre.add(b);
        
        String FichierPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "bateaux.dat";
        
        try
        {
            FileOutputStream fos = new FileOutputStream(FichierPath);
            ObjectOutputStream ecriture = new ObjectOutputStream(fos);
            ecriture.writeObject(ListeBateauAmarre);
        }
        catch(IOException e)
        {
            System.err.println("RunnableTraitement BoatArrived : " + e);
        }
        
        SendMsg("OUI");
        
        System.out.println("RunnableTraitement : Fin BOAT_ARRIVED");
        
        System.out.println("Nbr bateaux amarrés : " + ListeBateauAmarre.size());
    }
    
    /* On stocke dans une liste les emplacements du container à insérer dans le parc */
    public void HandleContainerIn(String[] parts)
    {
        Boolean trouve = false;
        System.err.println(ListeParc.size());
        for(Parc p : ListeParc)
        {
            if (p.getId().equals("0"))
            {
                currentContainer =  new Parc(parts[1], parts[2]);
                currentContainer.setDateAjout();
                SendMsg("OUI");
                trouve = true;
                break;
            }  
        }
        
        if (trouve == false)
            SendMsg("NON");
        
        System.out.println("RunnableTraitement : Fin HANDLE_CONTAINER_IN");
    }
    
    /* On insère les containers de la liste dans le fichier .csv du parc */
    public void EndContainerIn()
    {   
        
        if(currentContainer == null)
        {
            SendMsg("NON");
            return;
        }
        
        MaJListeParc();
        
        boolean fichierMaJ = false;
        
        for(Parc p : ListeParc)
        {
            if (p.getId().equals("0"))
            {
                HashMap<String, String> donnees = new HashMap<>();
                donnees.put("IdContainer", currentContainer.getId());
                donnees.put("Destination", currentContainer.getDestination());
                donnees.put("DateAjout", currentContainer.getDateAjout());
                
                p.setId(currentContainer.getId());
                p.setDestination(currentContainer.getDestination());
                p.setDateAjout();
                
                String condition = "X = " + p.getX() + " AND Y = " + p.getY();
        
                curThread = beanCSV.miseAJour("\"parc.csv\"", donnees, condition);

                try
                {
                    curThread.join();
                }
                catch (InterruptedException ex)
                {
                    System.err.println("RunnableTraitement : Join raté : " + ex);
                }
                
                currentContainer = null;
                fichierMaJ = true;
                break;                  
            }
        }
        
        if(fichierMaJ)
            SendMsg("OUI");
        else
            SendMsg("NON");
        
        System.out.println("RunnableTraitement : Fin END_CONTAINER_IN");
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
    
    public void MaJListeParc()
    {
        curThread = beanCSV.selection("*", "\"parc.csv\"", null);
        try
        {
            curThread.join();
        }
        catch (InterruptedException ex)
        {
            System.err.println("RunnableTraitement : Join rate : " + ex);
        }

        try
        {
            ListeParc = new ArrayList<>(); 
            while(ResultatDB.next())
            {
                Parc p = new Parc(ResultatDB.getString("X"), ResultatDB.getString("Y"), ResultatDB.getString("IdContainer"), ResultatDB.getString("Destination"), ResultatDB.getString("DateAjout"));
                ListeParc.add(p);
            }
        }
        catch (SQLException ex)
        {
            System.err.println("RunnableTraitement : Erreur lecture ResultSet : " + ex);
        }
    }
}
