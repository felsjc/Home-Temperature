package com.example.myweatherdatabase.utilities;

import android.net.Uri;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;

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
    static public Connection.Response getLoginResponse(String user, String password, String url) {

        Log.d(TAG, "ENTER doLogin");

        Connection.Response loginResponse = null;

        try {

            //Retrieving the login from main page
            Document loginFormDoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .method(Connection.Method.GET)
                    .ignoreHttpErrors(true)
                    //.timeout(5000)
                    .get();

            //Extract login form
            Elements foundForms = loginFormDoc.select("[id*=user-login]");
            ParserUtils.checkElements("[id=user-login-form]", foundForms);

            //fill in with user and password and then connect
            Connection conn = foundForms.forms().get(0).submit();
            conn.data("name").value(user);
            conn.data("pass").value(password);
            loginResponse = conn.execute();

        } catch (Exception e) {
            Log.e(TAG, "doLogin: ", e);
        }
        Log.d(TAG, "EXIT doLogin: SUCCESS");
        return loginResponse;
    }

}
