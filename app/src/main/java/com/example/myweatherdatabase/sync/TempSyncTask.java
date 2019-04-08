package com.example.myweatherdatabase.sync;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.myweatherdatabase.R;
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
import org.jsoup.select.Elements;

import java.util.Map;

public class TempSyncTask {

    private static final String TAG = TempSyncTask.class.getSimpleName();
    public static final int SYNCH_SUB_PERIOD_LENGHT = 20;
    public static final int ERROR_INVALID_COOKIE = 7;
    public static final int ERROR_LOGIN = 2;
    public static final int ERROR_DEVICE_ID = 3;
    private static final int SYNC_SUCCESS = 0;
    private static final int ERROR_HISTORY_FORM = 4;
    private static final int ERROR_DATA_DOWNLOAD = 5;
    private static final int ERROR_UNKONWN_DATA = 6;
    private static final int LOGIN_SUCCESS = 1;

    private static Connection.Response loginResponse = null;
    private static Map<String, String> cookies;
    private static String url;
    private static String user;
    private static String password;
    private static String deviceId;

    /**
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static int syncTemperatures(Context context) {

        url = AppPreferences.getLoginUrl(context);
        user = AppPreferences.getUsername(context);
        password = AppPreferences.getPassword(context);
        cookies = AppPreferences.getSessionCookies(context);
        deviceId = AppPreferences.getDeviceId(context);

        //If there are session cookies and a device Id from a previous session use those
        //otherwise do login procedure again and fetch those
        //we save a lot of time by avoiding networking and parsing unnecessarily
        if (cookies == null || deviceId.isEmpty()) {
            int result = refreshCookieAndDeviceId(context);
            if (result != LOGIN_SUCCESS)
                AppPreferences.saveLastError(getResultString(ERROR_LOGIN, context), context);
                return ERROR_LOGIN;
        }

        long startDate = DataUtils.getLastSyncDateFromDb(context);
        long endDate = System.currentTimeMillis() > startDate ?
                System.currentTimeMillis() : startDate;

        int syncResult = syncSubPeriod(context, cookies, deviceId, startDate, endDate);
        // If the cookie or device Id are not (or no longer) valid
        // try to renew then by logging in again and renewing them
        if (syncResult == ERROR_DEVICE_ID || syncResult == ERROR_INVALID_COOKIE) {

            refreshCookieAndDeviceId(context);
            //syncResult = syncPeriod(context, cookies, deviceId, startDate, endDate);
            syncResult = syncSubPeriod(context, cookies, deviceId, startDate, endDate);
        }

        AppPreferences.saveLastError(getResultString(syncResult, context), context);
        return SYNC_SUCCESS;
    }

    private static int refreshCookieAndDeviceId(Context context) {

        //invalidate previous cookies and device id
        AppPreferences.saveSessionCookies(null, context);
        AppPreferences.saveDeviceId("", context);

        loginResponse = NetworkUtils.getLoginResponse(user, password, url, context);
        cookies = (loginResponse != null) ? loginResponse.cookies() : null;
        AppPreferences.saveSessionCookies(cookies, context);

        Document devicesPage = ParserUtils.parseResponse(loginResponse);
        if (devicesPage == null)
            return ERROR_LOGIN;

        /*Trying to capture errors from the login process (like wrong user,
        password, wrong URL, etc*/
        Elements errorsFound = devicesPage.select("[class=messages error]");
        if (errorsFound != null && errorsFound.size() > 0) {
            String errorString = errorsFound.first().text();
            AppPreferences.saveLastError(errorString, context);
            return ERROR_LOGIN;
        }

        Element deviceElem = ParserUtils.getThermometerElement(devicesPage);
        deviceId = ParserUtils.getDeviceIdFromElement(deviceElem);
        AppPreferences.saveDeviceId(deviceId, context);
        return LOGIN_SUCCESS;
    }

    private static int syncPeriod(Context context, Map<String, String> cookies, String deviceId, long startDate, long endDate) {
        String dateStart;
        String dateEnd;
        long endPeriod = 0;
        int result = 0;

        Log.d(TAG, "syncPeriod: " +
                "\nFROM: " + DateUtils.getDateStringInServerFormat(startDate) +
                "\nTO: " + DateUtils.getDateStringInServerFormat(endDate));

        while (true) {
            endPeriod = DateUtils.getDatePlusDeltaDays(startDate, SYNCH_SUB_PERIOD_LENGHT);
            if (endPeriod > endDate)
                break;

            result = syncSubPeriod(context, cookies, deviceId, startDate, endPeriod);
            //if there is an error return it immediately
            if (result != SYNC_SUCCESS)
                return result;

            startDate = endPeriod;
        }
        result = syncSubPeriod(context, cookies, deviceId, startDate, endDate);
        return result;
    }


    private static int syncSubPeriod(Context context, Map<String, String> cookies, String deviceId, long startDate, long endDate) {

        Log.d(TAG, "\nsyncSubPeriod: " +
                "\n         FROM: " + DateUtils.getDateStringInServerFormat(startDate) +
                "\n         TO: " + DateUtils.getDateStringInServerFormat(endDate));

        String tempArchiveLink = ParserUtils.getArchiveLinkFromElement(startDate, endDate, deviceId);
        Document archiveDocument = NetworkUtils.getHttpResponseFromHttpUrl(tempArchiveLink, cookies, context);
        if (archiveDocument == null)
            return ERROR_DEVICE_ID;

        FormElement tempArchiveForm = ParserUtils.getTempArchiveForm(archiveDocument);
        if (tempArchiveForm == null)
            return ERROR_HISTORY_FORM;

        String temperatures = NetworkUtils.getTempHistory(tempArchiveForm, cookies);
        if (temperatures.isEmpty())
            return ERROR_DATA_DOWNLOAD;

        ContentValues[] temperatureList = ParserUtils.getTemperatureList(temperatures);
        if (temperatureList == null)
            return ERROR_UNKONWN_DATA;

        // Bulk Insert our new weather data into App's Database
        long addedEntries = context.getContentResolver().bulkInsert(
                ThermContract.TempMeasurment.CONTENT_URI, temperatureList);

        Log.d(TAG, "\nsyncSubPeriod: " +
                "\n\n         " + addedEntries + " entries added.");

        return SYNC_SUCCESS;
    }

    public static String getResultString(int code, Context context) {

        String status = "";
        switch (code) {
            case SYNC_SUCCESS:
                status = context.getString(R.string.SYNC_SUCCESS);
                break;
            case ERROR_INVALID_COOKIE:
                status = context.getString(R.string.ERROR_INVALID_COOKIE);
                break;
            case ERROR_LOGIN:
                status = context.getString(R.string.ERROR_LOGIN);
                break;
            case ERROR_DEVICE_ID:
                status = context.getString(R.string.ERROR_DEVICE_ID);
                break;
            case ERROR_HISTORY_FORM:
                status = context.getString(R.string.ERROR_HISTORY_FORM);
                break;
            case ERROR_DATA_DOWNLOAD:
                status = context.getString(R.string.ERROR_DATA_DOWNLOAD);
                break;
            case ERROR_UNKONWN_DATA:
                status = context.getString(R.string.ERROR_UNKONWN_DATA);
                break;
            default:
                status = context.getString(R.string.ERROR_UNDEFINED);
        }
        return status;
    }
}