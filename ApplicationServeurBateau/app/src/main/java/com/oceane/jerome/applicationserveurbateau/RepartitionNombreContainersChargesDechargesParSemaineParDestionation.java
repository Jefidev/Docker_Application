package com.oceane.jerome.applicationserveurbateau;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import static org.achartengine.ChartFactory.getBarChartIntent;


public class RepartitionNombreContainersChargesDechargesParSemaineParDestionation
{
    private DatabaseHandler sqlLiteConnection;
    private SQLiteDatabase DB;

    public Intent getIntent(Context context)
    {
        sqlLiteConnection = new DatabaseHandler(context, "DonneesDocker.sqlite", null, 3);
        DB = sqlLiteConnection.getReadableDatabase();

        String selectQueryIn = "SELECT Docker, AVG(Duree) FROM STATISTIQUES WHERE Mouvement ='IN' GROUP BY Docker";
        String selectQueryOut = "SELECT Docker, AVG(Duree) FROM STATISTIQUES WHERE Mouvement ='OUT' GROUP BY Docker";

        Cursor cursorIn = DB.rawQuery(selectQueryIn, null);
        Cursor cursorOut = DB.rawQuery(selectQueryOut, null);

        CategorySeries serie = new CategorySeries("Déchargé");
        CategorySeries serie2 = new CategorySeries("Chargé");
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();


        // Parcours des curseurs et ajout dans les listes.
        int i = 0;
        int maxY = 0;
        if (cursorIn.moveToFirst()) {
            do {
                System.out.println(cursorIn.getString(0) + " - " + cursorIn.getString(1));
                serie.add(cursorIn.getInt(1));

                if (cursorIn.getInt(1) > maxY)   // Calcul du maxY
                {
                    maxY = cursorIn.getInt(1);
                }

                mRenderer.addXTextLabel(i + 1, "_" + cursorIn.getString(0));
                i++;
            } while (cursorIn.moveToNext());
        }
        int j = 0;
        if (cursorOut.moveToFirst()) {
            do {
                System.out.println(cursorOut.getString(0) + " - " + cursorOut.getString(1));
                serie2.add(cursorOut.getInt(1));

                if (cursorOut.getInt(1) > maxY)   // Calcul du maxY
                {
                    maxY = cursorOut.getInt(1);
                }

                mRenderer.addXTextLabel(j + 1, "____" + cursorOut.getString(0));
                j++;
            } while (cursorOut.moveToNext());
        }

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(serie.toXYSeries());
        dataset.addSeries(serie2.toXYSeries());


        /* MISE EN PAGE DES BATONNETS */

        // Histogramme 1
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setDisplayChartValues(true);
        renderer.setChartValuesSpacing((float) 1);
        renderer.setColor(Color.CYAN);
        renderer.setChartValuesTextSize(50);
        // Histogramme 2
        XYSeriesRenderer renderer2 = new XYSeriesRenderer();
        renderer2.setDisplayChartValues(true);
        renderer2.setChartValuesSpacing((float) 1);
        renderer2.setColor(Color.BLUE);
        renderer2.setChartValuesTextSize(50);


        /* MISE EN PAGE DE CE QU'IL Y A AUTOUR (axes etc) */

        mRenderer.addSeriesRenderer(renderer);
        mRenderer.addSeriesRenderer(renderer2);
        mRenderer.setChartTitle("Temps moyens de \nchargement/déchargement par docker");
        mRenderer.setYTitle("Temps moyens chargement/déchargement");
        mRenderer.setAxisTitleTextSize(30);
        mRenderer.setShowAxes(true);

        mRenderer.setChartTitleTextSize(35);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setLabelsColor(Color.GRAY);
        mRenderer.setShowLegend(true);
        mRenderer.setLegendTextSize(50);

        mRenderer.setMargins(new int[]{10, 50, 250, 50});
        mRenderer.setXLabelsAlign(Paint.Align.CENTER);
        mRenderer.setXLabelsAngle(90);
        mRenderer.setLabelsTextSize(30);
        mRenderer.setXLabelsColor(Color.CYAN);
        mRenderer.setXLabels(0);
        mRenderer.setYLabels(0);

        if (i > j)
            mRenderer.setXAxisMax(i + 1);
        else
            mRenderer.setXAxisMax(j + 1);
        mRenderer.setYAxisMax(maxY + 5);
        mRenderer.setXAxisMin(0);
        mRenderer.setYAxisMin(0);

        mRenderer.setBarSpacing(0.5);
        mRenderer.setLegendHeight(50);

        mRenderer.setZoomEnabled(false);
        mRenderer.setClickEnabled(false);

        mRenderer.setFitLegend(true);
        mRenderer.setPanEnabled(true, false);
        mRenderer.setZoomEnabled(false, false);

        mRenderer.setShowLabels(true);

        mRenderer.setYLabelsAlign(Paint.Align.LEFT);


        Intent intent = getBarChartIntent(context, dataset, mRenderer, BarChart.Type.DEFAULT);
        return intent;
    }
}
