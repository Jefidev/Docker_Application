package poolThread;


public class ThreadClient extends Thread
{
    private SourceTaches tachesAExecuter;
    private String nom;
    
    private Runnable tacheEnCours;
    
    public ThreadClient(SourceTaches st, String n)
    {
        tachesAExecuter = st;
        nom = n;
    }
    
    public void run()
    {
        while(!isInterrupted())
        {
            try
            {
                tacheEnCours = tachesAExecuter.getTache();
            }
            catch(InterruptedException e)
            {
                System.err.println("Erreur de recuperation de la tache : " + e);
            }
            
            //System.out.println("Lancement du run de la tacheEnCours a partir du ThreadClient");
            tacheEnCours.run(); // Un thread par client, à modifier
        }
    }
}
