package ServeurPoolThreads;

import java.net.*;
import java.io.*;


public class ServeurBateau extends Thread
{
    private int port;
    private SourceTaches tachesAExecuter;
    private ServerSocket SSocket = null;
    private int nbrThreads;
    
    public ServeurBateau(int p, SourceTaches st, int nt)
    {
        port = p;
        tachesAExecuter = st;
        nbrThreads = nt;
    }
    
    public void run()
    {
        try
        {
            SSocket = new ServerSocket(port);
        }
        catch(IOException e)
        {
            System.err.println("ServeurBateau : Erreur de la creation de socket  : " + e);
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
                System.out.println("ServeurBateau : Csocket attend un client.");
                CSocket = SSocket.accept();
                System.out.println("ServeurBateau : Client dispo");
            }
            catch(IOException e)
            {
                System.err.println("ServeurBateau : Erreur d'accept : " + e);
            }

            tachesAExecuter.recordTache(new RunnableTraitement(CSocket));
            System.out.println("ServeurBateau : Travail mis dans la file");
        }
    }
}
