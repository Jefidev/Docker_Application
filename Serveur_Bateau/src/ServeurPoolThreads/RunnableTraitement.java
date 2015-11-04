package ServeurPoolThreads;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import newBean.BeanBDAccess;
import newBean.connexionException;
import newBean.requeteException;


public class RunnableTraitement implements Runnable
{
    private Socket CSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private BeanBDAccess beanOracle;
    private BeanBDAccess beanCSV;
    private ArrayList<Parc> ListeParc = null;
    
    private ArrayList<Parc> ListCurrentContainer =  null;
    private ArrayList<Parc> containerToRemove = null;
    private ArrayList<Bateau> ListeBateauAmarre;
    
    boolean first = true;
    
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
        
        beanOracle = new BeanBDAccess();
        try {
            beanOracle.connexionOracle("localhost", 1521, "COMPTA", "COMPTA", "XE");
        } catch (ClassNotFoundException ex) {
            System.err.println("Class not found " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("SQL Exception (oracle)" + ex.getMessage()); 
        } catch (connexionException ex) {
            System.err.println(ex.getNumException() + " -- " + ex.getMessage());
        }
        
        beanCSV = new BeanBDAccess();
        try {
            beanCSV.connexionCSV();
        } catch (ClassNotFoundException ex) {
            System.err.println("Class not found " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("SQL Exception (CSV)" + ex.getMessage()); 
        } catch (connexionException ex) {
            System.err.println(ex.getNumException() + " -- " + ex.getMessage());
        }
        
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
        String pathFichierBateau = System.getProperty("user.dir") + System.getProperty("file.separator") + "bateaux.dat";
        
        try
        {
            FileInputStream fis = new FileInputStream(pathFichierBateau);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            ListeBateauAmarre = (ArrayList<Bateau>)ois.readObject();
            
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
                    
                case "GET_CONTAINERS" :
                    GetContainers(parts);
                    break;
                    
                case "HANDLE_CONTAINER_OUT" :
                    HandleContainerOut(parts);
                    break;
                
                case "END_CONTAINER_OUT" :
                    EndContainerOut();
                    break;
                    
                case "LOGOUT" :
                    System.out.println("RunnableTraitement : LOGOUT");
                    terminer = true;
                    break;
                    
                default :
                    terminer = true;
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
        ResultSet ResultatDB = null;
        try {
            ResultatDB = beanOracle.selection("PASSWORD", "PERSONNEL", "LOGIN = '" + parts[1] + "'");
        } catch (SQLException ex) {
            System.err.println("SQLexception line 219 " + ex.getMessage());
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
        
        if(ListCurrentContainer == null)
            ListCurrentContainer = new ArrayList<>();
        MaJListeParc();
        for(Parc p : ListeParc)
        {
            if (p.getFlag().equals("0"))
            {
                ListCurrentContainer.add(new Parc(parts[1], parts[2].toUpperCase()));
                ListCurrentContainer.get(ListCurrentContainer.size()-1).setDateAjout();
                p.setFlag("1");
                SendMsg("OUI");
                HashMap<String, String> donnees = new HashMap<>();
                donnees.put("flag", "1");
                
                String condition = "X = " + p.getX() + " AND Y = " + p.getY();
                try {
                    beanCSV.miseAJour("\"parc.csv\"", donnees, condition);
                } catch (requeteException ex) {
                    System.err.println("ligne 290 " + ex.getNumErreur() + "---" + ex.getMessage() );
                }
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
        
        if(ListCurrentContainer == null)
        {
            SendMsg("OUI");
            return;
        }
        
        MaJListeParc();
        
        boolean fichierMaJ = true;
        boolean curContAdd = false;
        
        for(Parc curCont : ListCurrentContainer)
        {
            curContAdd = false;
            for(Parc p : ListeParc)
            {
                System.err.println(p.getFlag());
                if (p.getFlag().equals("1"))
                {
                    HashMap<String, String> donnees = new HashMap<>();
                    donnees.put("IdContainer", curCont.getId());
                    donnees.put("Destination", curCont.getDestination());
                    donnees.put("DateAjout", curCont.getDateAjout());
                    donnees.put("flag", "2");

                    p.setId(curCont.getId());
                    p.setDestination(curCont.getDestination());
                    p.setDateAjout();
                    p.setFlag("2");

                    String condition = "X = " + p.getX() + " AND Y = " + p.getY();

                    try {
                        beanCSV.miseAJour("\"parc.csv\"", donnees, condition);
                    } catch (requeteException ex) {
                        System.err.println("line 331 : " + ex.getNumErreur() + "---" + ex.getMessage());
                    }

                    curContAdd = true;
                    break;                  
                }
            }
            if(curContAdd == false)
            {
                fichierMaJ = false;
                break;        
            }
        }
        
        ListCurrentContainer = null;
        if(fichierMaJ)
            SendMsg("OUI");
        else
        {
            
            SendMsg("NON");
        }
        
        System.out.println("RunnableTraitement : Fin END_CONTAINER_IN");
    }
    
    public void GetContainers(String[] parts)
    {
        String requeteCond =  "Destination = '" + parts[1].toUpperCase() + "'";
        first = false;
        if(parts[2].equals("FIRST"))
        {
           first =  true; 
           requeteCond  =  requeteCond + " ORDER BY DateAjout";
        }

        ResultSet ResultatDB = null;
        try {
            ResultatDB = beanCSV.selection("*", "\"parc.csv\"", requeteCond);
        } catch (SQLException ex) {
            System.err.println("line 368 SQLException " + ex.getMessage());
        }
        
        
        
        String Message ="";
        ListCurrentContainer =  new ArrayList<>();
        try
        {
            while(ResultatDB.next())
            {
                Message = Message + ResultatDB.getString("X") +"$"+ResultatDB.getString("Y") +"$"+ResultatDB.getString("IdContainer") + "$";
                Message = Message + ResultatDB.getString("Destination")+"$"+ResultatDB.getString("DateAjout")+"#";
                ListCurrentContainer.add(new Parc(ResultatDB.getString("X"), ResultatDB.getString("Y"), ResultatDB.getString("IdContainer")));           
            }
        }
        catch (SQLException ex)
        {
            System.err.println("RunnableTraitement : Erreur lecture ResultSet : " + ex);
        }
        
        containerToRemove = new ArrayList<>();
        SendMsg(Message);
    }
    
    
    public void HandleContainerOut(String[] parts)
    {
        
        if(ListCurrentContainer == null || ListCurrentContainer.size() == 0)
        {
            SendMsg("NON");
            return;
        }
        
        Parc toRemove = new Parc(parts[2], parts[3], parts[1]);
        
        if(first) //On vérifie qu'on retire bien le 1er
        {
            if(!ListCurrentContainer.get(0).getX().equals(toRemove.getX()) || !ListCurrentContainer.get(0).getY().equals(toRemove.getY()))
            {
                SendMsg("NON");
                return;
            }
            else
            {
                ListCurrentContainer.remove(0);
            }          
        }
        else
        {
            boolean containerRemoved = false;
            for(Parc elem : ListCurrentContainer)
            {      
               if(toRemove.getX().equals(elem.getX()) && toRemove.getY().equals(elem.getY()) && toRemove.getId().equals(elem.getId()))
               {
                   containerRemoved = true;
                   ListCurrentContainer.remove(elem);
                   break;
               }
            }
            if(!containerRemoved)
            {
                SendMsg("NON");
                return;
            }
        }
        
        containerToRemove.add(toRemove);
        SendMsg("OUI");
    }
    
    public void EndContainerOut()
    {
        if(containerToRemove == null)
        {
            SendMsg("OUI");
            return;
        }
        
        MaJListeParc();
        
        boolean fichierMaJ = true;
        boolean curContAdd = false;
        
        for(Parc curCont : containerToRemove)
        {
            curContAdd = false;
            for(Parc p : ListeParc)
            {
                if (p.getId().equals(curCont.getId()) && p.getX().equals(curCont.getX()) && p.getY().equals(curCont.getY()))
                {
                    HashMap<String, String> donnees = new HashMap<>();
                    donnees.put("IdContainer", "0");
                    donnees.put("Destination", "0");
                    donnees.put("DateAjout", "0");
                    donnees.put("flage", "0");

                    p.setId("0");
                    p.setDestination("0");

                    String condition = "X = " + p.getX() + " AND Y = " + p.getY();

                    try {
                        beanCSV.miseAJour("\"parc.csv\"", donnees, condition);
                    } catch (requeteException ex) {
                        System.err.println("line 473 " + ex.getNumErreur() + "---" + ex.getMessage());
                    }

                    curContAdd = true;
                    break;                  
                }
            }
            if(curContAdd == false)
            {
                fichierMaJ = false;
                break;        
            }
        }
        
        ListCurrentContainer = null;
        if(fichierMaJ)
            SendMsg("OUI");
        else
            SendMsg("NON");
        
        System.out.println("RunnableTraitement : Fin END_CONTAINER_OUT");
    }
    
    
    public void MaJListeParc()
    {
        ResultSet ResultatDB = null;
        try {
            ResultatDB = beanCSV.selection("*", "\"parc.csv\"", null);
        } catch (SQLException ex) {
            System.err.println("SQLException line 503 " + ex.getMessage());
        }

        try
        {
            ListeParc = new ArrayList<>(); 
            while(ResultatDB.next())
            {
                Parc p = new Parc(ResultatDB.getString("X"), ResultatDB.getString("Y"), ResultatDB.getString("IdContainer"), ResultatDB.getString("Destination"), ResultatDB.getString("DateAjout"), ResultatDB.getString("flag"));
                ListeParc.add(p);
            }
        }
        catch (SQLException ex)
        {
            System.err.println("RunnableTraitement : Erreur lecture ResultSet : " + ex);
        }
    }
}
