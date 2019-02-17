package com.example.myweatherdatabase.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.example.myweatherdatabase.data.ThermometerDbHelper;
import com.example.myweatherdatabase.data.ThermContract.TempMeasurment;
import com.example.myweatherdatabase.utilities.DateUtils;

import java.util.ArrayList;

public class ThermometerProvider extends ContentProvider {

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_MEASUREMENT = 100;
    public static final int CODE_MEASUREMENT_WITH_DATE = 101;
    public static final int CODE_LASTEST_DAYS = 102;
    public static final int CODE_LATEST_MEASUREMENT = 103;
    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of WeatherProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = ThermometerProvider.class.getSimpleName();

    /**
     * Creates the UriMatcher that will match each URI to the CODE constants defined above.
     *
     * @return A UriMatcher that correctly matches the constants for each CODE
     */
    private static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ThermContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and
         * they aren't going to change.
         */

        /* This URI is content://com.example.android.app/temperatures/ */
        matcher.addURI(authority, ThermContract.PATH_TEMPERATURES, CODE_MEASUREMENT);

        /*
         * This URI would look something like content://com.example.android.app/temperatures/1472214172
         * The "/#" signifies to the UriMatcher that if PATH_TEMP is followed by ANY number,
         * that it should return the CODE_MEASUREMENT_WITH_DATE code
         */
        matcher.addURI(authority, ThermContract.PATH_TEMPERATURES + "/#", CODE_MEASUREMENT_WITH_DATE);

        matcher.addURI(authority, ThermContract.PATH_TEMPERATURES + "/"
                + ThermContract.PATH_LATEST_DAYS + "/#", CODE_LASTEST_DAYS);

        matcher.addURI(authority, ThermContract.PATH_TEMPERATURES + "/"
                + ThermContract.PATH_LATEST_TEMPERATURE, CODE_LATEST_MEASUREMENT);

        return matcher;

    }

    private com.example.example.myweatherdatabase.data.ThermometerDbHelper mDbHelper;

    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     * <p>
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     * <p>
     * Deferred initialization keeps application startup fast, avoids unnecessary work if the
     * provider turns out not to be needed, and stops database errors (such as a full disk) from
     * halting application launch.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {

        /*
         * As noted in the comment above, onCreate is run on the main thread, so performing any
         * lengthy operations will cause lag in your app. Since DbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mDbHelper = new ThermometerDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {


        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            case CODE_LATEST_MEASUREMENT: {

                sortOrder = ThermContract.TempMeasurment._ID + " DESC LIMIT 1";

                cursor = mDbHelper.getReadableDatabase().query(
                        TempMeasurment.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_LASTEST_DAYS: {

                int numberOfDays = Integer.parseInt(uri.getLastPathSegment());
                long numberOfRows = DateUtils.getDaysToRows(numberOfDays);
                sortOrder = ThermContract.TempMeasurment._ID + " DESC LIMIT "
                        + numberOfRows;

                cursor = mDbHelper.getReadableDatabase().query(
                        TempMeasurment.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }
            /*
             * When sUriMatcher's match method is called with a URI that looks something like this
             *
             *      content://com.example.android.app/temp/1472214172
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return the measurement for a particular date. The date in this code is encoded in
             * milliseconds and is at the very end of the URI (1472214172) and can be accessed
             * programmatically using Uri's getLastPathSegment method.
             *
             * In this case, we want to return a cursor that contains one row of temperature data for
             * a particular date.
             */
            case CODE_MEASUREMENT_WITH_DATE: {

                /*
                 * In order to determine the date associated with this URI, we look at the last
                 * path segment. In the comment above, the last path segment is 1472214172 and
                 * represents the number of seconds since the epoch, or UTC time.
                 */
                String normalizedUtcDateString = uri.getLastPathSegment();

                /*
                 * The query method accepts a string array of arguments, as there may be more
                 * than one "?" in the selection statement. Even though in our case, we only have
                 * one "?", we have to create a string array that only contains one element
                 * because this method signature accepts a string array.
                 */
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                cursor = mDbHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        TempMeasurment.TABLE_NAME,
                        /*
                         * A projection designates the columns we want returned in our Cursor.
                         * Passing null will return all columns of data within the Cursor.
                         * However, if you don't need all the data from the table, it's best
                         * practice to limit the columns returned in the Cursor with a projection.
                         */
                        projection,
                        /*
                         * The URI that matches CODE_MEASUREMENT_WITH_DATE contains a date at the end
                         * of it. We extract that date and use it with these next two lines to
                         * specify the row of weather we want returned in the cursor. We use a
                         * question mark here and then designate selectionArguments as the next
                         * argument for performance reasons. Whatever Strings are contained
                         * within the selectionArguments array will be inserted into the
                         * selection statement by SQLite under the hood.
                         */
                        TempMeasurment.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }

            /*
             * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
             *
             *      content://com.example.android.sunshine/weather/
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return all of the weather in our weather table.
             *
             * In this case, we want to return a cursor that contains every row of weather data
             * in our weather table.
             */
            case CODE_MEASUREMENT: {
                cursor = mDbHelper.getReadableDatabase().query(
                        TempMeasurment.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_MEASUREMENT:
                // Get writable database
                SQLiteDatabase database = mDbHelper.getWritableDatabase();

                // Insert the new measurement with the given values
                long id = database.insert(TempMeasurment.TABLE_NAME, null, contentValues);
                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }

                getContext().getContentResolver().notifyChange(uri, null);

                // Return the new URI with the ID (of the newly inserted row) appended at the end
                return ContentUris.withAppendedId(uri, id);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    /**
     * Handles requests to insert a set of new rows. In Sunshine, we are only going to be
     * inserting multiple rows of data at a time from a weather forecast. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert. In a normal ContentProvider's implementation, you will probably want
     * to provide proper functionality for the insert method as well.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_MEASUREMENT:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        /*
                        long weatherDate =
                                value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }
                        */
                        long _id = db.insertWithOnConflict(TempMeasurment.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_IGNORE);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {

                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }


    public int bulkInsert(@NonNull Uri uri, @NonNull ArrayList<ContentValues> values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_MEASUREMENT:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        /*
                        long weatherDate =
                                value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }
                        */
                        long _id = db.insert(TempMeasurment.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

            case CODE_MEASUREMENT:
                numRowsDeleted = mDbHelper.getWritableDatabase().delete(
                        TempMeasurment.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case CODE_MEASUREMENT_WITH_DATE:
                // Delete a single row given by the ID in the URI
                selection = TempMeasurment._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = mDbHelper.getWritableDatabase().delete(
                        TempMeasurment.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }


}
