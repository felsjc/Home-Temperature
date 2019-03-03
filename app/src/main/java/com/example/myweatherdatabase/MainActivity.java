package com.example.myweatherdatabase;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


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
    private static final int CURSOR_LOADER_ID = 0;
    private final String TAG = MainActivity.class.getSimpleName();

    // Instance fields
    Account mAccount;

    private static ProgressBar progressBar;
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

        }
    };
    private LineChart mGraph;
    private TextView textViewTemp;
    private TextView textViewDate;
    private ContentResolver mContentResolver;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewDate = findViewById(R.id.textview_date);
        textViewTemp = findViewById(R.id.textview_temp);
        progressBar = findViewById(R.id.progressBar);
        mContentResolver = getContentResolver();

        //mGraph = (LineChart) findViewById(R.id.chart);
        testButtons();
        testPreferences();
        //setupChart();
        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);


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

    }

    @Override
    protected void onStart() {
        super.onStart();
        View view = findViewById(R.id.fab);
        actionSync(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        switch (loaderId) {

            case CURSOR_LOADER_ID:
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
            entries.add(new Entry(longDate, temp));
            textViewDate.setText(DateUtils.getDateStringInLocalTime(MainActivity.this,
                    longDate));
            textViewTemp.setText(String.valueOf(temp));
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

    private void testButtons() {
        //Inserting bulk data
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                actionSync(view);
            }
        });

        //Deleting all data
        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {

                        mContentResolver.delete(ThermContract.TempMeasurment.CONTENT_URI, null, null);

                        Snackbar.make(view, "All data deleted", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();


                        return null;
                    }
                }.execute();
            }
        });

        //Deleting one entry
        FloatingActionButton fab3 = findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                TextInputEditText idEditText = findViewById(R.id.input_edit_text);
                final String[] idString = {idEditText.getText().toString()};


                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {


                        mContentResolver.delete(ThermContract.TempMeasurment.CONTENT_URI, "_ID = ?", idString);


                        Snackbar.make(view, "Entry with _ID = " + idString.toString() + " deleted", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        return null;
                    }
                }.execute();
            }
        });
    }

    private void testPreferences() {

        final TextInputEditText userEdit = findViewById(R.id.edit_user);
        final TextInputEditText passEdit = findViewById(R.id.edit_pass);
        final TextInputEditText timeEdit = findViewById(R.id.edit_time);

        userEdit.setText(AppPreferences.getUsername(this));
        passEdit.setText(AppPreferences.getPassword(this));
        timeEdit.setText(AppPreferences.getDeviceTimeZone(this));

        FloatingActionButton fab4 = findViewById(R.id.fab4);
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {


                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {

                        AppPreferences.saveUsername(userEdit.getText().toString(), MainActivity.this);
                        AppPreferences.savePassword(passEdit.getText().toString(), MainActivity.this);
                        AppPreferences.saveDeviceTimeZone(timeEdit.getText().toString(), MainActivity.this);

                        Snackbar.make(view, "AppPreferences saved", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        return null;
                    }
                }.execute();
            }
        });

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

                String label = DateUtils.getDateStringInLocalTime(MainActivity.this, (long) value);
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

    private void actionSync(final View view) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                Message msg = new Message();
                msg.arg1 = 1;
                handler.sendMessage(msg);

                //FakeDataUtils.insertFakeData(MainActivity.this);

                //TempSyncTask.syncTemperatures(MainActivity.this);


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
                ContentResolver.requestSync(mAccount,
                        getResources().getString(R.string.content_authority),
                        settingsBundle);


                Snackbar.make(view, "Synchronization finished.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Message msg2 = new Message();
                msg2.arg1 = 0;
                handler.sendMessage(msg2);
                return null;
            }
        }.execute();
    }
}
