package ServeurPoolThreads;


public class ThreadTraitement extends Thread
{
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
                System.err.println("ThreadTraitement : Erreur de recuperation de la tache : " + e);
            }
            
            System.out.println("ThreadTraitement : Lancement du run de la tacheEnCours");
            tacheEnCours.run();
        }
    }
}
