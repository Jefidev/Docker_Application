package clientserveurbateau;

import java.io.*;
import java.net.*;


public class ClientServeurBateau
{
    private Socket cliSock;
    private String adresse;
    private int port;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private StringBuffer message = null;
    
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
        String chargeUtile = "LOGIN#" + login + "#" + pwd;
        int taille = chargeUtile.length();
        message = new StringBuffer(String.valueOf(taille) + "#" + chargeUtile);
            
        try
        {    
            dos.write(message.toString().getBytes());
            dos.flush();
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Erreur de connexion : " + e);
        }
        
        /*System.out.println("Attente de la réponse du serveur");
        String reponse;
        // réception
        
        reponse = "OUI";
        
        if (reponse == "OUI")
            System.out.println("OK CONNECTE ! :)");
        else
        {
            System.out.println("CONNEXION RATEE : " + reponse);
            System.exit(1);
        }*/
    }
    
    public void GetContainers (String destination, String tri)
    {
        String chargeUtile = "GET_CONTAINERS#" + destination + "#" + tri;
        int taille = chargeUtile.length();
        message = new StringBuffer(String.valueOf(taille) + "#" + chargeUtile);
            
        try
        {    
            dos.write(message.toString().getBytes());
            dos.flush();
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Erreur de connexion : " + e);
        }
    }
    
    public void Deconnexion()
    {
        String chargeUtile = "LOGOUT#";
        int taille = chargeUtile.length();
        message = new StringBuffer(String.valueOf(taille) + "#" + chargeUtile);
            
        try
        {    
            dos.write(message.toString().getBytes());
            dos.flush();
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
    
    public static void main(String[] args)
    {
        ClientServeurBateau csb = new ClientServeurBateau("localhost", 31042);    // A AMELIORER => fichier properties
                
        csb.Connexion("oce", "oce");    // Dans le projet android à récupérer dans le GUI
        
        /*csb.GetContainers("Verviers", "FIRST");
        csb.GetContainers("Verviers", "ORDER"); // Bouton radio dans app android !
        
        //InputContainer();
        csb.GetContainers("Verviers", "FIRST");
        
        //OutputContainers();
        csb.GetContainers("Verviers", "FIRST");
        
        csb.Deconnexion();*/
    }
}
