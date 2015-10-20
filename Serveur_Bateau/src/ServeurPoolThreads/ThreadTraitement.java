package ServeurPoolThreads;


public class ThreadTraitement extends Thread
{
    // while(termine)
    // traitement des requêtes du client
    
    
    private SourceTaches tachesAExecuter;
    
    private Runnable tacheEnCours;
    
    public ThreadTraitement(SourceTaches st)
    {
        tachesAExecuter = st;
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
