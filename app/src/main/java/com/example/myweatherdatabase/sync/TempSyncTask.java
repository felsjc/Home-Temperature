package com.example.myweatherdatabase.sync;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.utilities.NetworkUtils;
import com.example.myweatherdatabase.utilities.ParserUtils;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

public class TempSyncTask {

    private static final String TAG = TempSyncTask.class.getSimpleName();

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

        Connection.Response loginResponse = NetworkUtils.getLoginResponse(user, password, url);

        //TODO: Implement wrong user and pass handling
        if (loginResponse == null)
            return;

        Document devicesPage = ParserUtils.parseResponse(loginResponse);

        Elements devicesElements = devicesPage.select("[id*=block-views-devices_flags-block_2]");

        Element deviceElem = null;
        if (devicesElements != null && devicesElements.size() > 0) {
            deviceElem = devicesElements.get(0);
        }

        String tempArchiveLink = ParserUtils.getArchiveLinkFromElement(deviceElem);
        Document archiveDocument = NetworkUtils.getHttpResponseFromHttpUrl(tempArchiveLink, loginResponse.cookies());
        FormElement tempArchiveForm = ParserUtils.getTempArchiveForm(archiveDocument);
        String temperatures = NetworkUtils.getTempHistory(tempArchiveForm, loginResponse.cookies());

        ContentValues[] temperatureList = ParserUtils.getTemperatureList(temperatures);

        // Bulk Insert our new weather data into App's Database
        context.getContentResolver().bulkInsert(
                ThermContract.TempMeasurment.CONTENT_URI, temperatureList);

        Log.i(TAG, "tempHistory: " + temperatureList.toString());
        //TODO - finish syncTemperatures

    }
}
