package com.oceane.jerome.applicationserveurbateau;


public class Container
{
    private String X;
    private String Y;
    private String id;
    private String destination;
    private String dateAjout;

    public Container(String x, String y, String i, String d, String da)
    {
        X = x;
        Y = y;
        id = i;
        destination = d;
        dateAjout = da;
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

    @Override
    public String toString() {
        return "(" + this.X + "," + this.Y + ") : " + this.id + " - " + this.destination + " - " + this.dateAjout;
    }
}
