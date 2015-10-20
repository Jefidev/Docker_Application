package ServeurPoolThreads;

import java.net.*;


public class RunnableTraitement implements Runnable
{
    // while(termine)
    // traitement des requêtes du client
    
    private Socket CSocket = null;
    
    public RunnableTraitement(Socket s)
    {
        CSocket = s;
    }

    @Override
    public void run()
    {

    }
}
