package ServeurPoolThreads;

import java.net.*;
import java.io.*;


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
        
        Socket CSocket = null;
        
        while(!isInterrupted())
        {
            try
            {
                System.out.println("Csocket attend un client.");
                CSocket = SSocket.accept();
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'accept (ThreadServeur) : " + e);
            }

            tachesAExecuter.recordTache(new RunnableTraitement(CSocket));
            System.out.println("Travail mis dans la file");
        }
    }
}
