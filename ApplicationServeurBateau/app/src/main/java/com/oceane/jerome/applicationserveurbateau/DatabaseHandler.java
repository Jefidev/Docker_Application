package com.oceane.jerome.applicationserveurbateau;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHandler extends SQLiteOpenHelper
{
    public static final String CREATE_TABLE_STATISTIQUES = "CREATE TABLE STATISTIQUES (Mouvement TEXT, Date TEXT, Duree INTERGER, Docker TEXT, Destination TEXT);";
    public  static final String DROP_TABLE_STATISTIQUES = "DROP TABLE IF EXISTS STATISTIQUES;";

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE_STATISTIQUES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_STATISTIQUES);
        onCreate(db);
    }
}