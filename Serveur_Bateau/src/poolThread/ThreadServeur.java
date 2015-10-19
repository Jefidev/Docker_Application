package poolThread;

/*import java.util.logging.Level;
import java.util.logging.Logger;*/
import java.net.*;
import java.io.*;
import requeteThreads.Requete;


public class ThreadServeur extends Thread
{
    private int port;
    private SourceTaches tachesAExecuter;
    private ConsoleServeur trace;
    private ServerSocket SSocket = null;
    private int nbrThrads = 5; //à recupérer dans un properties
    
    public ThreadServeur(int p, SourceTaches st, ConsoleServeur cs)
    {
        port = p;
        tachesAExecuter = st;
        trace = cs;
    }
    
    public void run()
    {
        try
        {
            SSocket = new ServerSocket(port);
        }
        catch(IOException e)
        {
            System.err.println("Erreur de la creation de socket  : " + e);
        }
        
        for(int i = 0; i < nbrThrads; i++)
        {
            ThreadClient tc = new ThreadClient(tachesAExecuter, "Thread n° " + String.valueOf(i));
            tc.start();
        }
        
        Socket Csocket = null;
        
        while(!isInterrupted())
        {
            try
            {
                System.out.println("Csocket attend un client.");
                Csocket = SSocket.accept();
                // trace ?
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'accept (ThreadServeur ligne 54) : " + e);
            }
            
            ObjectInputStream ois = null;
            Requete reqClient = null;
            
            try
            {
                ois = new ObjectInputStream(Csocket.getInputStream());
                reqClient = (Requete)ois.readObject();
                //System.out.println("Requete lue par le serveur, instance de " + reqClient.getClass().getName());
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'IO (inputstream) (ThreadServeur ligne 68) : " + e);
            } 
            catch (ClassNotFoundException ex) 
            {
                System.err.println("Erreur classNotFound (ThreadServeur ligne 72) : " + ex);
            }
            
            Runnable travail = reqClient.createRunnable(Csocket, trace);
            
            if(travail != null)
            {
                tachesAExecuter.recordTache(travail);
                //System.out.println("Travail mis dans la file");
            }
            //else
                //System.out.println("Pas de mise en file");
        }
    }
}
