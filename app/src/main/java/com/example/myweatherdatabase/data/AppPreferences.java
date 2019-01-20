package com.example.myweatherdatabase.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {

    /* Keys and default values used in shared preferences */
    private static final String KEY_USER = "u";
    private static final String DEFAULT_USER = "dummytestuser";

    private static final String KEY_PASSWORD = "p";
    private static final String DEFAULT_PASSWORD = "111222333";

    private static final String KEY_DEVICE_TIME_ZONE = "device_time_zone";
    private static final String DEFAULT_DEVICE_TIME_ZONE = "Europe/Stockholm";

    private static final String KEY_USER_ID = "user_id";
    private static final String DEFAULT_USER_ID = "0";
    


    public static String getUsername(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(KEY_USER,DEFAULT_USER);
        return user;
    }

    public static void saveUsername(String user, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER,user);
        editor.apply();
    }

    public static String getPassword(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pass = preferences.getString(KEY_PASSWORD,DEFAULT_PASSWORD);
        return pass;
    }

    public static void savePassword(String pass, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PASSWORD,pass);
        editor.apply();
    }

    public static String getDeviceTimeZone(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String timezone = preferences.getString(KEY_DEVICE_TIME_ZONE,DEFAULT_DEVICE_TIME_ZONE);
        return timezone;
    }

    public static void saveDeviceTimeZone(String timeZone, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DEVICE_TIME_ZONE,timeZone);
        editor.apply();
    }
}
