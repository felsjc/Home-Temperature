package com.example.myweatherdatabase.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.myweatherdatabase.data.ThermContract;

public class DataUtils {

    //Default value: 1 January 2015 00:00:00
    public static final long LATEST_DATE_DEFAULT = 1420070400000L;

    public static long getLastSyncDateFromDb(Context context) {

        long latestMeasDate = LATEST_DATE_DEFAULT;

        //URI for all rows of temperature data in our weather table
        Uri tempHistoryQueryUri = ThermContract.TempMeasurment.CONTENT_URI;

        // Sort order: Ascending by date
        String sortOrder = ThermContract.TempMeasurment._ID + " DESC LIMIT 1";


        int numberOfHours = 3;
        long numberOfRows = DateUtils.getHoursToRows(numberOfHours);
        sortOrder = ThermContract.TempMeasurment._ID + " DESC LIMIT "
                + numberOfRows;


        String selection = "";

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
