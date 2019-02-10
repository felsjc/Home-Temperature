package com.example.myweatherdatabase;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.sync.TempSyncTask;
import com.example.myweatherdatabase.utilities.DateUtils;
import com.example.myweatherdatabase.utilities.FakeDataUtils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = MainActivity.class.getSimpleName();

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * weather data.
     */
    public static final String[] TEMP_DATA_PROJECTION = {
            ThermContract.TempMeasurment._ID,
            ThermContract.TempMeasurment.COLUMN_DATE,
            ThermContract.TempMeasurment.COLUMN_TEMP,
    };

    private static final int CURSOR_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        testButtons();
        testPreferences();

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        new AsyncTask() {
            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @param objects The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected Object doInBackground(Object[] objects) {
                TempSyncTask.syncTemperatures(MainActivity.this);
                return null;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                /* Sort order: Ascending by date */
                String sortOrder = ThermContract.TempMeasurment._ID + " ASC";

                /*
                 * A SELECTION in SQL declares which rows you'd like to return.
                 */
                //TODO: Implement function to return the desired period
                String selection = "";
                //String selection = ThermContract.TempMeasurment.getSqlSelectForLast24h();

                return new CursorLoader(this,
                        tempHistoryQueryUri,
                        TEMP_DATA_PROJECTION,
                        selection,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        TextView displayView = findViewById(R.id.content_text_view);

        displayView.setText("The table contains " + cursor.getCount() + " measurements.\n\n");
        displayView.setMovementMethod(new ScrollingMovementMethod());
        displayView.append(ThermContract.TempMeasurment._ID + " - " +
                ThermContract.TempMeasurment.COLUMN_DATE + " - " +
                ThermContract.TempMeasurment.COLUMN_TEMP + "\n");

        // Indices for the _id, description, and priority columns
        int idIndex = cursor.getColumnIndex(ThermContract.TempMeasurment._ID);
        int dateIndex = cursor.getColumnIndex(ThermContract.TempMeasurment.COLUMN_DATE);
        int tempIndex = cursor.getColumnIndex(ThermContract.TempMeasurment.COLUMN_TEMP);

        if (cursor == null)
            return;

        cursor.moveToPosition(-1);

        // Iterate through all the returned rows in the cursor
        while (cursor.moveToNext()) {
            // Use that index to extract the String or Int value of the word
            // at the current row the cursor is on.
            int id = cursor.getInt(idIndex);
            int intDate = cursor.getInt(dateIndex);
            double temp = cursor.getDouble(tempIndex);
            // Display the values from each column of the current row in the cursor in the TextView
            displayView.append(("\n" + id + " - " +
                    DateUtils.getFriendlyDateString(this, intDate, true) + " - " +
                    Double.toString(temp)));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }


    private void testButtons() {
        //Inserting bulk data
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {

                        FakeDataUtils.insertFakeData(MainActivity.this);

                        Snackbar.make(view, "Fake data added", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        return null;
                    }
                }.execute();
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

                        getContentResolver().delete(ThermContract.TempMeasurment.CONTENT_URI, null, null);

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

                        getContentResolver().delete(ThermContract.TempMeasurment.CONTENT_URI, "_ID = ?", idString);


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
                        AppPreferences.saveDeviceTimeZone(timeEdit.getText().toString(),MainActivity.this);

                        Snackbar.make(view, "AppPreferences saved", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        return null;
                    }
                }.execute();
            }
        });

    }
}
