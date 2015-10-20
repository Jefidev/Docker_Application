package ServeurPoolThreads;

import java.util.*;


public class ListeTaches implements SourceTaches
{   
    private LinkedList listeTaches;
    
    public ListeTaches()
    {
        listeTaches = new LinkedList();
    }

    @Override
    public synchronized Runnable getTache() throws InterruptedException
    {
        //System.out.println("getTache avant wait");
        
        while(!existTaches())
            wait();
        
        return (Runnable)listeTaches.remove();
    }

    @Override
    public boolean existTaches()
    {
        return listeTaches.isEmpty();
    }

    @Override
    public synchronized void recordTache(Runnable r)
    {
        listeTaches.addLast(r);
        //System.out.println("ListeTaches : tache dans la file");
        notify();
    }
}
