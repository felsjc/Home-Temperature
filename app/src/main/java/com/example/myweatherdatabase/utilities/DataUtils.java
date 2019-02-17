package com.example.myweatherdatabase.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.myweatherdatabase.data.ThermContract;

public class DataUtils {

    //Default value: 1 January 2015 00:00:00
    public static final long LATEST_DATE_DEFAULT = 1420070400000L;

    public static long getLatestMeasDate(Context context) {

        long latestMeasDate = LATEST_DATE_DEFAULT;

        Uri tempHistoryQueryUri = ThermContract.TempMeasurment.CONTENT_URI;
        tempHistoryQueryUri = tempHistoryQueryUri.buildUpon()
                .appendPath(ThermContract.PATH_LATEST_DAYS)
                .appendPath("1").build();

        /* Sort order: Ascending by date */
        String sortOrder = ThermContract.TempMeasurment._ID + " DESC";

        /*
         * A SELECTION in SQL declares which rows you'd like to return.
         */
        //TODO: Implement function to return the desired period
        String selection = "";
        //String selection = ThermContract.TempMeasurment.getSqlSelectForLast24h();

        Cursor cursor = context.getContentResolver().query(
                tempHistoryQueryUri,
                ThermContract.TempMeasurment.TEMP_DATA_PROJECTION,
                selection,
                null,
                sortOrder);

        if (cursor == null)
            return latestMeasDate;

        int dateIndex = cursor.getColumnIndex(ThermContract.TempMeasurment.COLUMN_DATE);

        cursor.moveToPosition(-1);
        // Iterate through all the returned rows in the cursor
        while (cursor.moveToNext()) {
            //Converting to milliseconds
            latestMeasDate = (long) cursor.getInt(dateIndex) * 1000;
        }
        return latestMeasDate;

    }
}
