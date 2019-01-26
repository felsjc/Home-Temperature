package com.example.myweatherdatabase.utilities;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

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
    public static Document parse(Connection.Response response) {

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
}
