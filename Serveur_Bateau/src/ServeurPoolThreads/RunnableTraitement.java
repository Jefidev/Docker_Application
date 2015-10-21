package ServeurPoolThreads;

import java.net.*;


public class RunnableTraitement implements Runnable
{
    private Socket CSocket = null;
    
    public RunnableTraitement(Socket s)
    {
        CSocket = s;
    }

    @Override
    public void run()
    {
        Boolean terminer = false;
        
        while (!terminer)
        {
            // traitement des requêtes du client
            System.out.println("RunnableTraitement : Execution du run");
            terminer = true;
        }
    }
}
