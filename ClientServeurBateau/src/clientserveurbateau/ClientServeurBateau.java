package clientserveurbateau;

import java.io.*;
import java.net.*;


public class ClientServeurBateau
{
    private Socket cliSock;
    private String adresse;
    private int port;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    public ClientServeurBateau(String a, int p)
    {
        cliSock = null;
        adresse = a;
        port = p;
        
        // CONNEXION SOCKET ET FLUX
        try
        {
            cliSock = new Socket(adresse, port);
            System.out.println(cliSock.getInetAddress().toString());
            dis = new DataInputStream(new BufferedInputStream(cliSock.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(cliSock.getOutputStream()));
        }
        catch(UnknownHostException e)
        {
            System.err.println("ClientServeurBateau : Host non trouvé : " + e);
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Pas de connexion ? : " + e);
        }
        
        System.out.println("ClientServeurBateau : Connexion client acceptée");
    }

    public void Connexion(String login, String pwd)
    {
        SendMsg("LOGIN#" + login + "#" + pwd);
        
        String reponse = ReceiveMsg();
        
        if (reponse.equals("OUI"))
            System.out.println("CLIENT CONNECTE !");
        else
            System.out.println("CONNEXION RATEE !");
    }
    
    public void Deconnexion()
    {
        SendMsg("LOGOUT#");       
        
        try
        {
            dos.close();
            dis.close();
            cliSock.close();
            System.out.println("ClientServeurBateau : Client déconnecté");
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Erreur de déconnexion : " + e);
        }
    }
    
    public void testBoatArrived()
    {
        SendMsg("BOAT_ARRIVED#BATEAU1#ANVERS");
        
        String reponse  = ReceiveMsg();
        System.out.println("Ajout d'un bateau : " + reponse);
    }
    
    public void testHANDLE_CONTAINER_IN()
    {
        SendMsg("HANDLE_CONTAINER_IN#256#BRUXELLES");
        
        String reponse  = ReceiveMsg();
        System.out.println("Handle cont  repoonse : " + reponse);
    }
    
    public void testEND_CONTAINER_IN()
    {
        SendMsg("END_CONTAINER_IN");
        
        String reponse  = ReceiveMsg();
        System.out.println("end container  repoonse : " + reponse);
    }
    
    public void testGetContainers()
    {
        SendMsg("GET_CONTAINERS#LIEGE#FIRST");
        
        String reponse  = ReceiveMsg();
        System.out.println(reponse);
    }


    public void SendMsg(String chargeUtile)
    {
        int taille = chargeUtile.length();
        String message = String.valueOf(taille) + "#" + chargeUtile;
            
        try
        {           
            dos.write(message.getBytes());
            dos.flush();
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateu : Erreur d'envoi de msg (IO) : " + e);
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
            System.err.println("ClientServeurBateau : Erreur de reception de msg (IO) : " + e);
        }
            
        return message.toString();
    }
    
    public static void main(String[] args)
    {
        ClientServeurBateau csb = new ClientServeurBateau("localhost", 31042);    // A AMELIORER => fichier properties
                
        csb.Connexion("oce", "oce");    // Dans le projet android à récupérer dans le GUI
        
        //csb.testBoatArrived();
        
        //csb.testHANDLE_CONTAINER_IN();
        //csb.testHANDLE_CONTAINER_IN();
        
        //csb.testEND_CONTAINER_IN();
        //csb.testEND_CONTAINER_IN();
        csb.testGetContainers();
        
        csb.Deconnexion();
    }
}
