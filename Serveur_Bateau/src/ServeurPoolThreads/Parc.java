package ServeurPoolThreads;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Parc
{
    private String X;
    private String Y;
    private String id;
    private String destination;
    private String dateAjout;
    private String flag;
    
    public Parc(String i, String d)
    {
        id = i;
        destination = d;
    }
    
    public Parc(String x, String y, String i, String d, String da, String f)
    {
        X = x;
        Y = y;
        id = i;
        destination = d;
        dateAjout = da;
        flag = f;
    }
    
    public Parc(String x, String y, String i)
    {
        X = x;
        Y = y;
        id = i;
    }
    
    public void setId (String i)
    {
        id = i;
    }
    
    public void setDestination (String d)
    {
        destination = d;
    }
    
    public void setDateAjout ()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        dateAjout = format.format(c.getTime());
    }
    
    public void setFlage(String f)
    {
        flag = f;
    }
    
    public String getX()
    {
        return X;
    }
        
    public String getY()
    {
        return Y;
    }
    
    public String getId()
    {
        return id;
    }
    
    public String getDestination()
    {
        return destination;
    }
    
    public String getDateAjout()
    {
        return dateAjout;
    }
    
    public String getFlag()
    {
        return flag;
    }
}
