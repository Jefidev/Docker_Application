package ServeurPoolThreads;

import java.net.*;
import java.io.*;
import requeteThreads.Requete;


public class Serveur_Bateau extends Thread
{
    private int port;
    private SourceTaches tachesAExecuter;
    private ServerSocket SSocket = null;
    private int nbrThreads;
    
    public Serveur_Bateau(int p, SourceTaches st, int nt)
    {
        port = p;
        tachesAExecuter = st;
        nbrThreads = nt;
    }
    
    public void LancerServeur()
    {
        try
        {
            SSocket = new ServerSocket(port);
        }
        catch(IOException e)
        {
            System.err.println("Erreur de la creation de socket  : " + e);
        }
        
        for(int i = 0; i < nbrThreads; i++)
        {
            ThreadTraitement tt = new ThreadTraitement(tachesAExecuter);
            tt.start();
        }
        
        Socket Csocket = null;
        
        while(!isInterrupted())
        {
            try
            {
                System.out.println("Csocket attend un client.");
                Csocket = SSocket.accept();
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'accept (ThreadServeur) : " + e);
            }
            
            ObjectInputStream ois = null;
            Requete reqClient = null;
            
            try
            {
                ois = new ObjectInputStream(Csocket.getInputStream());
                reqClient = (Requete)ois.readObject();
                System.out.println("Requete lue par le serveur, instance de " + reqClient.getClass().getName());
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'IO (inputstream) (ThreadServeur ligne 68) : " + e);
            } 
            catch (ClassNotFoundException ex) 
            {
                System.err.println("Erreur classNotFound (ThreadServeur ligne 72) : " + ex);
            }
            
            Runnable travail = reqClient.createRunnable(Csocket);
            
            if(travail != null)
            {
                tachesAExecuter.recordTache(travail);
                System.out.println("Travail mis dans la file");
            }
            else
                System.out.println("Pas de mise en file");
        }
    }
}
