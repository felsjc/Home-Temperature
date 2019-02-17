package com.example.myweatherdatabase.sync;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.utilities.DataUtils;
import com.example.myweatherdatabase.utilities.DateUtils;
import com.example.myweatherdatabase.utilities.NetworkUtils;
import com.example.myweatherdatabase.utilities.ParserUtils;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

public class TempSyncTask {

    private static final String TAG = TempSyncTask.class.getSimpleName();
    public static final int SYNCH_SUB_PERIOD_LENGHT = 20;
    private static Connection.Response loginResponse = null;

    /**
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncTemperatures(Context context) {

        String url = AppPreferences.getLoginUrl(context);
        String user = AppPreferences.getUsername(context);
        String password = AppPreferences.getPassword(context);

        if (loginResponse == null)
            loginResponse = NetworkUtils.getLoginResponse(user, password, url);

        //TODO: Implement wrong user and pass handling
        if (loginResponse == null)
            return;

        Document devicesPage = ParserUtils.parseResponse(loginResponse);
        Element deviceElem = ParserUtils.getThermometerElement(devicesPage);

        long startDate = DataUtils.getLatestMeasDate(context);
        long endDate = System.currentTimeMillis() > startDate ?
                System.currentTimeMillis() : startDate;

        syncPeriod(context, loginResponse, deviceElem, startDate, endDate);

    }

    private static void syncPeriod(Context context, Connection.Response loginResponse, Element deviceElem, long startDate, long endDate) {
        String dateStart;
        String dateEnd;
        long endPeriod = 0;

        Log.d(TAG, "syncPeriod: " +
                "\nFROM: " + DateUtils.getDateStringInServerFormat(startDate) +
                "\nTO: " + DateUtils.getDateStringInServerFormat(endDate));

        while (true) {
            endPeriod = DateUtils.getDatePlusDeltaDays(startDate, SYNCH_SUB_PERIOD_LENGHT);
            if (endPeriod > endDate)
                break;
            syncSubPeriod(context, loginResponse, deviceElem, startDate, endPeriod);
            startDate = endPeriod;
        }
        syncSubPeriod(context, loginResponse, deviceElem, startDate, endDate);
    }


    private static void syncSubPeriod(Context context, Connection.Response loginResponse, Element deviceElem, long startDate, long endDate) {

        Log.d(TAG, "\nsyncSubPeriod: " +
                "\n         FROM: " + DateUtils.getDateStringInServerFormat(startDate) +
                "\n         TO: " + DateUtils.getDateStringInServerFormat(endDate));

        String tempArchiveLink = ParserUtils.getArchiveLinkFromElement(deviceElem, startDate, endDate);
        Document archiveDocument = NetworkUtils.getHttpResponseFromHttpUrl(tempArchiveLink, loginResponse.cookies());
        FormElement tempArchiveForm = ParserUtils.getTempArchiveForm(archiveDocument);
        if (tempArchiveForm == null)
            return;
        String temperatures = NetworkUtils.getTempHistory(tempArchiveForm, loginResponse.cookies());
        if (temperatures.isEmpty())
            return;
        ContentValues[] temperatureList = ParserUtils.getTemperatureList(temperatures);
        // Bulk Insert our new weather data into App's Database
        long addedEntries = context.getContentResolver().bulkInsert(
                ThermContract.TempMeasurment.CONTENT_URI, temperatureList);

        Log.d(TAG, "\nsyncSubPeriod: " +
                "\n\n         " + addedEntries + " entries added.");

    }
}