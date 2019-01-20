package com.example.myweatherdatabase.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ThermContract {

    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */
    public static final String CONTENT_AUTHORITY = "com.example.myweatherdatabase";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for the app.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that the this
     * app can handle. For instance,
     *
     *     content://com.example.android.app/weather/
     *     [           BASE_CONTENT_URI         ][ PATH_TEMP ]
     *
     * is a valid path for looking at weather data.
     *
     *      content://com.example.android.app/givemeroot/
     *
     * will fail, as the ContentProvider hasn't been given any information on what to do with
     * "givemeroot". At least, let's hope not. Don't be that dev, reader. Don't be that dev.
     */
    public static final String PATH_TEMP = "temp";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ThermContract(){};

    //TempEntry represents a temperature measured at a date and time
    public static class TempMeasurment implements BaseColumns{

        /* The base CONTENT_URI used to query the Weather table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TEMP)
                .build();

        public static final String TABLE_NAME = "temperatures";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TEMP = "temperature";
    }

}