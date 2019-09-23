package com.example.myweatherdatabase.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.myweatherdatabase.data.AppPreferences;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getClass().getSimpleName();

    private static URL buildUrl(String stringUrl) {
        Uri uri = Uri.parse(stringUrl).buildUpon().build();

        try {
            URL url = new URL(uri.toString());
            Log.v(TAG, "URL: " + url);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Provides authentication to the portal
     *
     * @param user     Portal user
     * @param password Password for account
     */
    static public Connection.Response getLoginResponse(String user, String password, String url, Context context) {

        Log.d(TAG, "ENTER doLogin");

        Connection.Response loginResponse = null;

        try {

            //Retrieving the login from main page
            Document loginFormDoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .method(Connection.Method.GET)
                    .timeout(12000)
                    .get();

            //Extract login form
            Elements foundForms = loginFormDoc.select("[id*=user-login]");
            ParserUtils.checkElements("[id=user-login-form]", foundForms);

            //fill in with user and password and then connect
            Connection conn = foundForms.forms().get(0).submit();
            conn.data("name").value(user);
            conn.data("pass").value(password);
            loginResponse = conn.execute();

        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HttpStatusException e) {

            AppPreferences.saveLastSyncResult(e.toString(), context);
            e.printStackTrace();
            return null;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

            return loginResponse;

        }
        // Log.d(TAG, "EXIT doLogin: SUCCESS");

    }

    public static Document getHttpResponseFromHttpUrl
            (String link, Map<String, String> cookies, Context context) {

        Document archivePage = null;

        try {
            archivePage = Jsoup.connect(link)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .maxBodySize(0)
                    .timeout(12000)
                    // .ignoreHttpErrors(true)
                    .get();

        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (HttpStatusException e) {

            AppPreferences.saveLastSyncResult(e.toString(), context);
            e.printStackTrace();
            return null;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            return archivePage;
        }
    }

    public static String getTempHistory(FormElement auxFormElement, Map<String, String> cookies) {
        if (auxFormElement == null)
            return "";
        String temperatures = "";//add cookie from existing session and submit form to download csv with past temperatures
        Connection conn =
                auxFormElement.submit()
                        .maxBodySize(0)
                        .timeout(20000)
                        .cookies(cookies);
        try {
            temperatures = conn.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temperatures;
    }
}
