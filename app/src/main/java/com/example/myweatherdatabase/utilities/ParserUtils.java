package com.example.myweatherdatabase.utilities;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.myweatherdatabase.data.ThermContract;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        //Select download history form
        Elements foundForms = archivePage.select("[id=uzraugi-termo-operations-form]");
        checkElements("[uzraugi-termo-operations-form]", foundForms);
        FormElement auxFormElement = foundForms.forms().get(0);

        //Removing op="delete data" from the form, otherwise the response will be a page confirming data history exclusion instead csv file with temp data
        auxFormElement.elements().remove(2);
        return auxFormElement;
    }

    public static String getArchiveLinkFromElement(Element element) {

        Log.d(TAG, "ENTER getArchiveLink");

        //final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_ID_QUERY = "flag-wrapper flag-my-favourite-devices flag-my-favourite-devices-";
        final String DEVICE_LIST_QUERY = "[id=content-area]";
        final String TIME_STAMP_QUERY = "[class=timestamp]";
        String linkTemplate = "https://secure.sarmalink.com/node/DEVICE_ID/archive/START_DATE_TIME/END_DATE_TIME";
        String archiveLink = "";

        if (element == null)
            return "";

        try {

            //Select elements that contain id of the device (the number in front of "flag-device-alarms-")
            Elements deviceElements = element.select("[class*=" + DEVICE_ID_QUERY + "]");
            checkElements("[class*=" + DEVICE_ID_QUERY + "]", deviceElements);

            //Find device id on string
            String deviceId = deviceElements.get(0).toString();
            int pos = deviceId.lastIndexOf(DEVICE_ID_QUERY);
            pos += DEVICE_ID_QUERY.length();
            deviceId = deviceId.substring(pos, deviceId.indexOf("\">"));

            linkTemplate = linkTemplate.replace("DEVICE_ID", deviceId);

            //Find current timestamp from device
            deviceElements = element.select(TIME_STAMP_QUERY);
            checkElements(TIME_STAMP_QUERY, deviceElements);
            String timestamp = deviceElements.get(0).text();

            linkTemplate = linkTemplate.replace("END_DATE_TIME", timestamp);

            //Subtract one day from current date and use as start date
            //timestamp = timestamp.substring(0,10);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = formatter.parse(timestamp);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DATE, -1);
            timestamp = formatter.format(cal.getTime());
            linkTemplate = linkTemplate.replace("START_DATE_TIME", timestamp);
            archiveLink = linkTemplate;

        } catch (Exception e) {
            Log.e(TAG, "getArchiveLink: ", e);
        }

        Log.d(TAG, "EXIT getArchiveLink");
        return archiveLink;
    }

    public static ContentValues[] getTemperatureList(String temperatures) {

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

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long dateLong = dateFormat.parse(dateString).getTime();
                Double tempDouble = Double.valueOf(tempString);

                ContentValues weatherValues = new ContentValues();
                weatherValues.put(ThermContract.TempMeasurment.COLUMN_DATE, dateLong);
                weatherValues.put(ThermContract.TempMeasurment.COLUMN_TEMP, tempDouble);
                tempContentValues[i--] = weatherValues;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempContentValues;
    }
}
