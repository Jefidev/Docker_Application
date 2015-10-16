/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package requeteThreads;

import java.net.Socket;
import poolThread.ConsoleServeur;

/**
 *
 * @author Jerome
 */
public interface Requete {
    public Runnable createRunnable(Socket s, ConsoleServeur cs);
    
}
