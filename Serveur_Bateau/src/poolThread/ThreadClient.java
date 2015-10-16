/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolThread;

/**
 *
 * @author Jerome
 */
public class ThreadClient extends Thread{
    private SourceTaches tacheList;
    private String nom;
    
    private Runnable curTache;
    
    public ThreadClient(SourceTaches st, String n)
    {
        tacheList = st;
        nom = n;
    }
    
    public void run()
    {
        while(!isInterrupted())
        {
            try
            {
                curTache = tacheList.getTache();
            }
            catch(InterruptedException e)
            {
                System.err.println("Erreur de recuperation de la tâche " + e);
            }
            
            curTache.run();
        }
    }

}

