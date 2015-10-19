package requeteThreads;

import java.net.Socket;
import poolThread.ConsoleServeur;


public interface Requete
{
    public Runnable createRunnable(Socket s, ConsoleServeur cs);    
}
