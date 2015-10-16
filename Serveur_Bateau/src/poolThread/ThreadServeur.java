/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import requeteThreads.Requete;

/**
 *
 * @author Jerome
 */
public class ThreadServeur extends Thread{
    
    private int port;
    private SourceTaches tachesList;
    private ConsoleServeur trace;
    private ServerSocket SSocket = null;
    private int nbrThrads = 5; //à recupérer dans un properties
    
    public ThreadServeur(int p, SourceTaches st, ConsoleServeur cs)
    {
        port = p;
        tachesList = st;
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
            ThreadClient tc = new ThreadClient(tachesList, "Thread n° " + i);
            tc.start();
        }
        
        Socket Csocket = null;
        
        while(!isInterrupted())
        {
            try
            {
                System.out.println("j'attend un client");
                
                Csocket = SSocket.accept();
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'accepte (threadServeur lgn 60) : " + e);
            }
            
            ObjectInputStream ois = null;
            Requete reqClient = null;
            
            try
            {
                ois = new ObjectInputStream(Csocket.getInputStream());
                reqClient = (Requete)ois.readObject();
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'inputstream thread serveur 75 " + e);
            } 
            catch (ClassNotFoundException ex) 
            {
                System.err.println("Erreur classNotFound threadServeur ligne  81 " + ex);
            }
            
            Runnable travail = reqClient.createRunnable(Csocket, trace);
            
            if(travail != null)
            {
                tachesList.recordTache(travail);
            }
        }
    }
}
