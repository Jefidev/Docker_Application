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
    }
    
    public void LancementClient()
    {
        // CONNEXION
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
        
        
        // ACTION
        
        
        
        // DECONNEXION
        try
        {
            message = new StringBuffer("LOGOUT\n");
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
        csb.LancementClient();
    }
}
