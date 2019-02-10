package com.example.myweatherdatabase.utilities;

import android.content.ContentValues;
import android.content.Context;

import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.data.ThermContract.TempMeasurment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FakeDataUtils {

    private static int [] weatherIDs = {200,300,500,711,900,962};

    /**
     * Creates a single ContentValues object with random weather data for the provided date
     * @param date a normalized date
     * @return ContentValues object filled with random weather data
     */
    private static ContentValues createTestWeatherContentValues(long date) {
        ContentValues testWeatherValues = new ContentValues();
        testWeatherValues.put(TempMeasurment.COLUMN_DATE, date);
        int temp = (int)(Math.random()*100);
        testWeatherValues.put(TempMeasurment.COLUMN_TEMP, temp);
        return testWeatherValues;
    }

    /**
     * Creates random weather data for 7 days starting today
     * @param context
     */
    public static void insertFakeData(Context context) {
        //Get today's normalized date
        long today = DateUtils.normalizeDate(System.currentTimeMillis());
        List<ContentValues> fakeValues = new ArrayList<ContentValues>();
        //loop over 7 days starting today onwards
        for(int i=0; i<7; i++) {
            fakeValues.add(createTestWeatherContentValues(today + TimeUnit.DAYS.toMillis(i)));
        }
        // Bulk Insert our new data into App's Database
        context.getContentResolver().bulkInsert(
                ThermContract.TempMeasurment.CONTENT_URI,
                fakeValues.toArray(new ContentValues[7]));
    }
}
