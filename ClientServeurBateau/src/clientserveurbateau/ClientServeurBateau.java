package clientserveurbateau;

import java.io.IOException;
import java.net.*;


public class ClientServeurBateau
{
    private Socket cliSock;
    private String adresse;
    private int port;
    
    public ClientServeurBateau(String a, int p)
    {
        adresse = a;
        port = p;
    }
    
    public void LancementClient()
    {   
        try
        {
            cliSock = new Socket(adresse, port);
            System.out.println(cliSock.getInetAddress().toString());
        }
        catch(UnknownHostException e)
        {
            System.err.println("Lancement client : Host non trouvé : " + e);
        }
        catch(IOException e)
        {
            System.err.println("Lancement client : Pas de connexion ? : " + e);
        }
        
        // COMMU PAR RESEAU
    }

    public static void main(String[] args)
    {
        ClientServeurBateau csb = new ClientServeurBateau("192.168.1.3", 31042);    // A AMELIORER => fichier properties
        csb.LancementClient();
    }
}
