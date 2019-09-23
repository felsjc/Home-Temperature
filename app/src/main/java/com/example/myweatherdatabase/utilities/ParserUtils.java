package com.example.myweatherdatabase.utilities;

import android.content.ContentValues;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.data.ThermMeasurement;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ParserUtils {


    private static final String TAG = ParserUtils.class.getSimpleName();

    /**
     * Checks if an element with a given name exists
     *
     * @param name
     * @param elements
     */
    public static void checkElements(String name, Elements elements) {
        Log.d(TAG, "ENTER checkElements");
        if (elements.size() == 0) {
            throw new RuntimeException("Unable to find Elements with " + name + " in Elements: " + elements.toString());
        }

        Log.d(TAG, "EXIT checkElements: PASSED");
    }


    /**
     * Parses a given document to get HTML
     *
     * @param response is the response from a connection established previously
     * @return parsed HTML
     */
    public static Document parseResponse(Connection.Response response) {

        Log.d(TAG, "ENTER parse");

        try {
            if (response != null)
                return response.parse();
            Log.d(TAG, "EXIT parse: SUCCESSFUL");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "EXIT parse: UNABLE TO PARSE: " + response);
        }
        Log.d(TAG, "EXIT parse: NOTHING TO PARSE");
        return null;
    }


    @NonNull
    public static FormElement getTempArchiveForm(Document archivePage) {
        if (archivePage == null)
            return null;

        //Select download history form
        Elements foundForms = archivePage.select("[id=uzraugi-termo-operations-form]");

        //return null if no form was found
        if (foundForms.size() == 0) {
            return null;
        }

        //extract form used to download the csv
        FormElement auxFormElement = foundForms.forms().get(0);

        //Removing op="delete data" from the form, otherwise the response will be a page confirming data history exclusion instead csv file with temp data
        auxFormElement.elements().remove(2);
        return auxFormElement;
    }

    public static String getArchiveLinkFromElement(long startDate, long endDate, String deviceId) {

        Log.d(TAG, "ENTER getArchiveLink");

        final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_LIST_QUERY = "[id=content-area]";
        final String TIME_STAMP_QUERY = "[class=timestamp]";
        String linkTemplate = "https://secure.sarmalink.com/node/DEVICE_ID/archive/START_DATE_TIME/END_DATE_TIME";
        String archiveLink = "";


        try {


            linkTemplate = linkTemplate.replace("DEVICE_ID", deviceId);


            linkTemplate = linkTemplate.replace("START_DATE_TIME",
                    DateUtils.getDateTimeStringInServerTimeZone(startDate));

            linkTemplate = linkTemplate.replace("END_DATE_TIME",
                    DateUtils.getDateTimeStringInServerTimeZone(endDate));
            /**
             linkTemplate = linkTemplate.replace("END_DATE_TIME", timestamp);

             //Subtract one day from current date and use as start date
             //timestamp = timestamp.substring(0,10);
             DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             Date startDate = formatter.parse(timestamp);
             Calendar cal = Calendar.getInstance();
             cal.setTime(startDate);
             cal.add(Calendar.DATE, -1);
             timestamp = formatter.format(cal.getTime());
             linkTemplate = linkTemplate.replace("START_DATE_TIME", timestamp);**/
            archiveLink = linkTemplate;

        } catch (Exception e) {
            Log.e(TAG, "getArchiveLink: ", e);
            return "";
        }

        Log.d(TAG, "EXIT getArchiveLink");
        return archiveLink;
    }

    @NonNull
    public static String getDeviceIdFromElement(Element element) {

        final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";

        if (element == null)
            return "";

        //Select elements that contain id of the device (the number in front of "flag-device-alarms-")
        Elements deviceElements = element.select("[class*=" + DEVICE_ID_QUERY + "]");
        if (deviceElements == null)
            return "";

        //Find device id on string
        String deviceId = deviceElements.get(0).toString();
        int pos = deviceId.lastIndexOf(DEVICE_ID_QUERY);
        pos += DEVICE_ID_QUERY.length();
        deviceId = deviceId.substring(pos, deviceId.indexOf("\">"));
        return deviceId;
    }


    public static String getDeviceNameFromElement(Element element) {

        final String DEVICE_AREA_QUERY = "view-display-id-block_2 view-dom-id-2";
        final String DEVICE_NAME_QUERY = "device-title";

        if (element == null)
            return "";

        Elements deviceAreaElements = element.select("[class*=" + DEVICE_AREA_QUERY + "]");
        if (deviceAreaElements == null)
            return "";

        //Select elements that contain id of the device (the number in front of "flag-device-alarms-")
        Elements deviceElements = deviceAreaElements.select("[class*=" + DEVICE_NAME_QUERY + "]");
        if (deviceElements == null)
            return "";

        //Get device name from element found
        String deviceName = deviceElements.get(0).text();
        return deviceName;
    }

    public static String getArchiveLinkFromStoredDevice(long startDate, long endDate, Context context) {

        Log.d(TAG, "ENTER getArchiveLinkFromStoredDevice");

        //final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_LIST_QUERY = "[id=content-area]";
        final String TIME_STAMP_QUERY = "[class=timestamp]";
        String linkTemplate = "https://secure.sarmalink.com/node/DEVICE_ID/archive/START_DATE_TIME/END_DATE_TIME";
        String archiveLink = "";


        try {

            String deviceId = AppPreferences.getDeviceId(context);
            linkTemplate = linkTemplate.replace("DEVICE_ID", deviceId);


            linkTemplate = linkTemplate.replace("START_DATE_TIME",
                    DateUtils.getDateTimeStringInServerTimeZone(startDate));

            linkTemplate = linkTemplate.replace("END_DATE_TIME",
                    DateUtils.getDateTimeStringInServerTimeZone(endDate));
            /**
             linkTemplate = linkTemplate.replace("END_DATE_TIME", timestamp);

             //Subtract one day from current date and use as start date
             //timestamp = timestamp.substring(0,10);
             DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             Date startDate = formatter.parse(timestamp);
             Calendar cal = Calendar.getInstance();
             cal.setTime(startDate);
             cal.add(Calendar.DATE, -1);
             timestamp = formatter.format(cal.getTime());
             linkTemplate = linkTemplate.replace("START_DATE_TIME", timestamp);**/
            archiveLink = linkTemplate;

        } catch (Exception e) {
            Log.e(TAG, "getArchiveLink: ", e);
            return "";
        }

        Log.d(TAG, "EXIT getArchiveLink");
        return archiveLink;
    }

    public static ContentValues[] getTemperatureContentValues(String temperatures) {

        //Reformat list to remove first line with column descriptions
        final String firstLine = "t,val\r\n";
        temperatures = temperatures.substring(firstLine.length());
        String[] temperaturesArray = temperatures.split("\r\n");

        ContentValues[] tempContentValues = new ContentValues[temperaturesArray.length];

        try {
            int i = temperaturesArray.length - 1;
            for (String measurement : temperaturesArray) {

                String[] dateAndTemp = measurement.split(",");
                String dateString = dateAndTemp[0].substring(1, dateAndTemp[0].length() - 1);
                String tempString = dateAndTemp[1].substring(1, dateAndTemp[1].length() - 1);

                //Convert string date to Date, using data server time zone (Latvia)
                Date date = DateUtils.getDateFromCsvString(dateString, TimeZone.getTimeZone(DateUtils.TIMEZONE_SERVER));
                //store date in seconds
                int dateLong = (int) (date.getTime() / 1000);
                Double tempDouble = Double.valueOf(tempString);

                ContentValues weatherValues = new ContentValues();
                weatherValues.put(ThermContract.TempMeasurment.COLUMN_DATE, dateLong);
                weatherValues.put(ThermContract.TempMeasurment.COLUMN_TEMP, tempDouble);
                tempContentValues[i--] = weatherValues;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tempContentValues;
    }

    public static List<ThermMeasurement> getTemperatureList(String temperatures) {

        //Reformat list to remove first line with column descriptions
        final String firstLine = "t,val\r\n";
        temperatures = temperatures.substring(firstLine.length());
        String[] temperaturesArray = temperatures.split("\r\n");

        List<ThermMeasurement> tempMeasurements = new ArrayList<>();

        try {
            for (String measurement : temperaturesArray) {

                String[] dateAndTemp = measurement.split(",");
                String dateString = dateAndTemp[0].substring(1, dateAndTemp[0].length() - 1);
                String tempString = dateAndTemp[1].substring(1, dateAndTemp[1].length() - 1);

                //Convert string date to Date, using data server time zone (Latvia)
                Date date = DateUtils.getDateFromCsvString(dateString, TimeZone.getTimeZone(DateUtils.TIMEZONE_SERVER));
                //store date in seconds
                int dateLong = (int) (date.getTime() / 1000);
                Float tempFloat = Float.valueOf(tempString);

                ThermMeasurement therMeasurement = new ThermMeasurement(dateLong,tempFloat);
                tempMeasurements.add(0,therMeasurement);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tempMeasurements;
    }


    @Nullable
    public static Element getThermometerElement(Document devicesPage) {
        Elements devicesElements = devicesPage.select("[id*=block-views-devices_flags-block_2]");

        Element deviceElem = null;
        if (devicesElements != null && devicesElements.size() > 0) {
            deviceElem = devicesElements.get(0);
        }
        return deviceElem;
    }
}
