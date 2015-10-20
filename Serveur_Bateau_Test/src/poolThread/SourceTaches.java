package poolThread;


public interface SourceTaches
{
    public Runnable getTache() throws InterruptedException;
    public boolean existTaches();
    public void recordTache(Runnable r);
}
