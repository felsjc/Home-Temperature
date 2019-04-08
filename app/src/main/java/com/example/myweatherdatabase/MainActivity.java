package com.example.myweatherdatabase;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.sync.TempSyncTask;
import com.example.myweatherdatabase.utilities.DateUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {


    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.myweatherdatabase";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "myweatherdatabase.example.com";
    // The account name
    public static final String USERNAME = "dummyaccount";
    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 120L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;
    private static final int CURSOR_LOADER_TEMP_ID = 0;
    private static final int CURSOR_LOADER_MAX_TEMP_ID = 1;
    private static final int CURSOR_LOADER_MIN_TEMP_ID = 2;
    private final String TAG = MainActivity.class.getSimpleName();

    // Instance fields
    Account mAccount;

    private LineChart mGraph;
    private TextView textViewTemp;
    private TextView textViewDate;
    private TextView textViewMaxTemp;
    private TextView textViewMinTemp;
    private ContentResolver mContentResolver;
    private SwipeRefreshLayout pullToRefresh;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                USERNAME, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        ContentResolver.setIsSyncable(newAccount,
                context.getResources().getString(R.string.content_authority),
                1);
        ContentResolver.setSyncAutomatically(newAccount,
                context.getResources().getString(R.string.content_authority),
                true);
        return newAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewDate = findViewById(R.id.textview_date);
        textViewTemp = findViewById(R.id.textview_temperature);
        textViewMaxTemp = findViewById(R.id.max_temp);
        textViewMinTemp = findViewById(R.id.min_temp);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContentResolver = getContentResolver();

        //mGraph = (LineChart) findViewById(R.id.chart);

        //setupChart();
        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(CURSOR_LOADER_TEMP_ID, null, this);
//        getSupportLoaderManager().initLoader(CURSOR_LOADER_MIN_TEMP_ID, null, this);
//        getSupportLoaderManager().initLoader(CURSOR_LOADER_MAX_TEMP_ID, null, this);

        // Create the dummy account
        mAccount = CreateSyncAccount(this);

        /*
         * Turn on periodic syncing
         */
        ContentResolver.addPeriodicSync(
                mAccount,
                getResources().getString(R.string.content_authority),
                Bundle.EMPTY,
                SYNC_INTERVAL);

        setupSharedPreferences();
        actionSync();


        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pullToRefresh.setRefreshing(true);
                actionSync(); // your code

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        switch (loaderId) {

            case CURSOR_LOADER_TEMP_ID:
                /* URI for all rows of temperature data in our weather table */
                Uri tempHistoryQueryUri = ThermContract.TempMeasurment.CONTENT_URI;
                tempHistoryQueryUri = tempHistoryQueryUri.buildUpon()
                        .appendPath(ThermContract.PATH_LATEST_TEMPERATURE).build();

                /* Sort order: Ascending by date */
                String sortOrder; //ThermContract.TempMeasurment._ID + " DESC";

                /*
                 * A SELECTION in SQL declares which rows you'd like to return.
                 */
                //TODO: Implement function to return the desired period
                String selection = "";
                //String selection = ThermContract.TempMeasurment.getSqlSelectForLast24h();

                return new CursorLoader(this,
                        tempHistoryQueryUri,
                        ThermContract.TempMeasurment.TEMP_DATA_PROJECTION,
                        selection,
                        null,
                        null);

            case CURSOR_LOADER_MIN_TEMP_ID:

            case CURSOR_LOADER_MAX_TEMP_ID:

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
/*
        TextView displayView = findViewById(R.id.content_text_view);

        displayView.setText("The table contains " + cursor.getCount() + " measurements.\n\n");
        displayView.setMovementMethod(new ScrollingMovementMethod());
        displayView.append(ThermContract.TempMeasurment._ID + " - " +
                ThermContract.TempMeasurment.COLUMN_DATE + " - " +
                ThermContract.TempMeasurment.COLUMN_TEMP + "\n");
*/

        pullToRefresh.setRefreshing(false);
        // Indices for the _id, description, and priority columns
        int idIndex = cursor.getColumnIndex(ThermContract.TempMeasurment._ID);
        int dateIndex = cursor.getColumnIndex(ThermContract.TempMeasurment.COLUMN_DATE);
        int tempIndex = cursor.getColumnIndex(ThermContract.TempMeasurment.COLUMN_TEMP);

        if (cursor == null)
            return;

        cursor.moveToFirst();
        cursor.moveToPosition(-1);

        ArrayList<Entry> entries = new ArrayList<>();


        // Iterate through all the returned rows in the cursor
        while (cursor.moveToNext()) {
            // Use that index to extract the String or Int value of the word
            // at the current row the cursor is on.
//            int id = cursor.getInt(idIndex);

            //Converting to milliseconds
            long longDate = (long) cursor.getInt(dateIndex) * 1000;
            float temp = cursor.getFloat(tempIndex);
            // Display the values from each column of the current row in the cursor in the TextView
            /*displayView.append(("\n" + id + " - " +
                    DateUtils.getDateStringInLocalTime(this, longDate) + " - " +
                    Double.toString(temp)));
        */
            //entries.add(new Entry(longDate, temp));

            textViewDate.setText(DateUtils.getDateStringInDeviceTimeZone(MainActivity.this,
                    longDate));
            textViewTemp.setText(String.valueOf(temp) + "\u00b0");

        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void setupChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4));
        entries.add(new Entry(1, 1));
        entries.add(new Entry(2, 2));
        entries.add(new Entry(3, 4));

        LineDataSet dataSet = new LineDataSet(entries, "Customized values");
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        //****
        // Controlling X axis
        XAxis xAxis = mGraph.getXAxis();
        // Set the xAxis position to bottom. Default is top
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //Customizing x axis value
        final String[] months = new String[]{"Jan", "Feb", "Mar", "Apr"};

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                String label = DateUtils.getDateStringInDeviceTimeZone(MainActivity.this, (long) value);
                return label;
            }
        };
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        //***
        // Controlling right side of y axis
        YAxis yAxisRight = mGraph.getAxisRight();
        yAxisRight.setEnabled(false);

        //***
        // Controlling left side of y axis
        YAxis yAxisLeft = mGraph.getAxisLeft();
        yAxisLeft.setGranularity(1f);

        // Setting Data
        LineData data = new LineData(dataSet);
        mGraph.setData(data);
        //mGraph.animateX(2500);
        //refresh
        mGraph.setVisibleXRangeMaximum(2000);
        mGraph.invalidate();
    }

    private void actionSync() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {


                //FakeDataUtils.insertFakeData(MainActivity.this);
                TempSyncTask.syncTemperatures(MainActivity.this);


                // Pass the settings flags by inserting them in a bundle
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                /*
                 * Request the sync for the default account, authority, and
                 * manual sync settings
                 */
                // ContentResolver.requestSync(mAccount,getResources().getString(R.string.content_authority),settingsBundle);

                //Toast.makeText(getApplicationContext(),"Synchronization finished.", Toast.LENGTH_LONG).show();


                return null;
            }
        }.execute();
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {

        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Register the listener
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     *
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.key_user))) {
            mContentResolver.delete(ThermContract.TempMeasurment.CONTENT_URI, null, null);
            AppPreferences.saveDeviceId("", this);
            AppPreferences.saveSessionCookies(null, this);
            Toast.makeText(this, "All data deleted", Toast.LENGTH_LONG).show();
            TempSyncTask.syncTemperatures(MainActivity.this);
        }
    }
}
