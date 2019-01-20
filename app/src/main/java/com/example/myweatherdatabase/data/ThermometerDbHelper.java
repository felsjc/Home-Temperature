package com.example.example.myweatherdatabase.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myweatherdatabase.data.ThermContract.TempMeasurment;

import static com.example.myweatherdatabase.data.ThermContract.*;

public class ThermometerDbHelper extends SQLiteOpenHelper {


    /*
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "thermometer.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;


    public ThermometerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TempMeasurment.TABLE_NAME + " (" +
                        /*
                         * WeatherEntry did not explicitly declare a column called "_ID". However,
                         * WeatherEntry implements the interface, "BaseColumns", which does have a field
                         * named "_ID". We use that here to designate our table's primary key.
                         */
                        TempMeasurment._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        TempMeasurment.COLUMN_DATE + " INTEGER," +
                        TempMeasurment.COLUMN_TEMP + " REAL)";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TempMeasurment.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
