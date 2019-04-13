package com.example.myweatherdatabase.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.myweatherdatabase.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class AppPreferences {

    public static String getUsername(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(
                context.getResources().getString(R.string.key_user),
                context.getResources().getString(R.string.default_user));
        return user;
    }

    public static void saveUsername(String user, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_user), user);
        editor.apply();
    }

    public static String getPassword(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pass = preferences.getString(
                context.getResources().getString(R.string.key_password),
                context.getResources().getString(R.string.default_password));
        return pass;
    }

    public static void savePassword(String pass, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_password), pass);
        editor.apply();
    }

    public static String getThermometerTimeZone(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String timezone = preferences.getString(
                context.getResources().getString(R.string.key_device_time_zone),
                context.getResources().getString(R.string.default_device_time_zone));
        return timezone;
    }

    public static void saveDeviceTimeZone(String timeZone, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_device_time_zone), timeZone);
        editor.apply();
    }

    public static String getLoginUrl(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(
                context.getResources().getString(R.string.key_login_url),
                context.getResources().getString(R.string.default_login_url));
        return user;
    }

    public static void saveLoginUrl(String loginUrl, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_login_url), loginUrl);
        editor.apply();
    }


    public static Map<String, String> getSessionCookies(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String gsonCookiesString = preferences.getString(
                context.getResources().getString(R.string.key_session_cookies),
                context.getResources().getString(R.string.default_session_cookies));

        //convert to string using gson
        Gson gson = new Gson();

        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        Map<String, String> sessionCookies = gson.fromJson(gsonCookiesString, type);

        return sessionCookies;
    }

    public static void saveSessionCookies(Map<String, String> sessionCookies, Context context) {

        //convert to string using gson
        Gson gson = new Gson();
        String gsonCookies = gson.toJson(sessionCookies);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_session_cookies),
                gsonCookies);
        editor.apply();

    }

    public static void saveDeviceId(String deviceId, Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_device_id),
                deviceId);
        editor.apply();

    }

    public static String getDeviceId(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(
                context.getResources().getString(R.string.key_device_id),
                context.getResources().getString(R.string.default_device_id));
        return user;
    }


    public static void saveDeviceName(String deviceName, Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_device_name),
                deviceName);
        editor.apply();

    }

    public static String getDeviceName(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(
                context.getResources().getString(R.string.key_device_name),
                context.getResources().getString(R.string.default_device_name));
        return user;
    }

    public static void saveLastError(String error, Context context) {

        long timeEpoch = System.currentTimeMillis() / 1000;
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss z")
                .format(new java.util.Date(timeEpoch * 1000));

        error += ("\n When: " + date);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getResources().getString(R.string.key_last_error),
                error);
        editor.apply();

    }

    public static String getLastError(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(
                context.getResources().getString(R.string.key_last_error),
                context.getResources().getString(R.string.default_last_error));
        return user;
    }
}
