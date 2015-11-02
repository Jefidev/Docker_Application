package com.oceane.jerome.applicationserveurbateau;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import org.achartengine.ChartFactory;
import org.achartengine.chart.PieChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;


/* REPARTITION DU NOMBRE DE CONTAINERS CHARGES OU DECHARGES PAR SEMAINE PAR DESTINATION */
public class GraphRepartition
{
    private DatabaseHandler sqlLiteConnection;
    private SQLiteDatabase DB;

    public Intent getIntent(Context context, String semaine, String mouvement)
    {
        int[] colors = {Color.BLUE, Color.MAGENTA, Color.GREEN, Color.CYAN, Color.RED, Color.YELLOW};

        // Connexion BD
        sqlLiteConnection = new DatabaseHandler(context, "DonneesDocker.sqlite", null, 3);
        DB = sqlLiteConnection.getReadableDatabase();

        // Récupération des données
        String selectQuery = "SELECT COUNT(Date), Date, Destination FROM STATISTIQUES WHERE strftime('%W', Date) = '" + semaine + "' AND Mouvement = '" + mouvement + "' GROUP BY Destination ORDER BY 1, 4";
        Cursor cursor = DB.rawQuery(selectQuery, null);


        /* TRAITEMENT */
        DefaultRenderer rendererGlobal = new DefaultRenderer();
        rendererGlobal.setLegendTextSize(50);
        rendererGlobal.setLabelsColor(Color.GRAY);
        rendererGlobal.setChartTitle("Répartition du nombre de containers \nchargés ou déchargés par semaine \npar destination");
        rendererGlobal.setChartTitleTextSize(50);
        CategorySeries distributionSeries = new CategorySeries("Répartition du nombre de containers \nchargés ou déchargés par semaine \npar destination");

        if (cursor.moveToFirst())
        {
            int i = 0;
            do
            {
                System.out.println(cursor.getString(0) + " - " + cursor.getString(2) + " - " + cursor.getString(3) + " - " + cursor.getString(1));
                distributionSeries.add(cursor.getString(2) + " - " + cursor.getString(3) + " - " + cursor.getString(0), cursor.getDouble(0));
                SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                renderer.setColor(colors[i]);
                rendererGlobal.addSeriesRenderer(renderer);
                rendererGlobal.setLabelsTextSize(30);
                i++;
            } while (cursor.moveToNext());
        }


        Intent intent = ChartFactory.getPieChartIntent(context, distributionSeries, rendererGlobal, "AChartEnginePieChartDemo");
        return intent;
    }
}
