package ServeurPoolThreads;


public class LancementServeur
{
    public static void main(String[] args)
    {             
        Serveur_Bateau sb = new Serveur_Bateau(31042, new ListeTaches(), 5);
        sb.LancerServeur();
    }    
}
