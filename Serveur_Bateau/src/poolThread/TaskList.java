/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolThread;

import java.util.LinkedList;

/**
 *
 * @author Jerome
 */
public class TaskList implements SourceTaches{
    
    private LinkedList taskList;
    
    public TaskList()
    {
        taskList = new LinkedList();
    }

    @Override
    public synchronized Runnable getTache() throws InterruptedException {
        
        while(!existTaches())
            wait();
        
        return (Runnable)taskList.remove();
    }

    @Override
    public boolean existTaches() {
        
        return taskList.isEmpty();
    }

    @Override
    public synchronized void recordTache(Runnable r) {
        taskList.addLast(r);
        notify();
    }
    
}
