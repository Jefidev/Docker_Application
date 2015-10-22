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

    
    public void Connexion(String log, String pwd)
    {
        String chargeUtile = new String("LOGIN#" + log + "#" + pwd);
        int taille = chargeUtile.length();
        message = new StringBuffer(taille.toString() + "#" + chargeUtile);
            
        try
        {    
            dos.write(message.toString().getBytes());
        }
        catch(IOException e)
        {
            System.err.println("ClientServeurBateau : Erreur de connexion : " + e);
        }
    }
    
    public void Deconnexion()
    {
        message = new StringBuffer("LOGOUT\n");
        
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
        ClientServeurBateau csb = new ClientServeurBateau("192.168.1.3", 31042);    // A AMELIORER => fichier properties
                
        csb.Connexion("oce", "oce");    // Dans le projet android à récupérer dans le GUI
        
        //GetContainers();
        
        //InputContainer();
        //GetContainers();
        
        //OutputContainers();
        //GetContainers();
        
        csb.Deconnexion();
    }
}
