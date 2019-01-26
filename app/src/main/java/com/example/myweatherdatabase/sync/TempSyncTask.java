package com.example.myweatherdatabase.sync;

import android.content.Context;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.utilities.NetworkUtils;
import com.example.myweatherdatabase.utilities.ParserUtils;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

public class TempSyncTask {


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
        if (loginResponse == null)
            return;

        Document devicesPage = ParserUtils.parse(loginResponse);

        //TODO - finish syncTemperatures

    }
}
