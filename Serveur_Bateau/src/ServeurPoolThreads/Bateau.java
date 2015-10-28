package ServeurPoolThreads;

import java.io.Serializable;


public class Bateau implements Serializable
{
    private String id;
    private String destination;
    
    public Bateau(String i, String d)
    {
        id = i;
        destination = d;
    }
}
