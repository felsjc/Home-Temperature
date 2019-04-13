/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.myweatherdatabase.utilities;

import android.content.Context;

import com.example.myweatherdatabase.data.AppPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
/**
 * Class for handling date conversions that are useful for Sunshine.
 */
public final class DateUtils {

    /* Milliseconds in a day */
    public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    public static final String SERVER_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String FRIENDLY_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMEZONE_SERVER = "Europe/Riga";
    public static final SimpleDateFormat serverDateFormat = new SimpleDateFormat(SERVER_DATE_PATTERN);
    public static final DateFormat friendlyDateFormat = DateFormat.getDateInstance(DateFormat.FULL);
    public static final DateFormat friendlyTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

    /**
     * This method returns the number of milliseconds (UTC time) for today's date at midnight in
     * the local time zone. For example, if you live in California and the day is September 20th,
     * 2016 and it is 6:30 PM, it will return 1474329600000. Now, if you plug this number into an
     * Epoch time converter, you may be confused that it tells you this time stamp represents 8:00
     * PM on September 19th local time, rather than September 20th. We're concerned with the GMT
     * date here though, which is correct, stating September 20th, 2016 at midnight.
     * <p>
     * As another example, if you are in Hong Kong and the day is September 20th, 2016 and it is
     * 6:30 PM, this method will return 1474329600000. Again, if you plug this number into an Epoch
     * time converter, you won't get midnight for your local time zone. Just keep in mind that we
     * are just looking at the GMT date here.
     * <p>
     * This method will ALWAYS return the date at midnight (in GMT time) for the time zone you
     * are currently in. In other words, the GMT date will always represent your date.
     * <p>
     * Since UTC / GMT time are the standard for all time zones in the world, we use it to
     * normalize our dates that are stored in the database. When we extract values from the
     * database, we adjust for the current time zone using time zone offsets.
     *
     * @return The number of milliseconds (UTC / GMT) for today's date at midnight in the local
     * time zone
     */
    public static long getNormalizedUtcDateForToday() {

        /*
         * This number represents the number of milliseconds that have elapsed since January
         * 1st, 1970 at midnight in the GMT time zone.
         */
        long utcNowMillis = System.currentTimeMillis();

        /*
         * This TimeZone represents the device's current time zone. It provides us with a means
         * of acquiring the offset for local time from a UTC time stamp.
         */
        TimeZone currentTimeZone = TimeZone.getDefault();

        /*
         * The getOffset method returns the number of milliseconds to add to UTC time to get the
         * elapsed time since the epoch for our current time zone. We pass the current UTC time
         * into this method so it can determine changes to account for daylight savings time.
         */
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);

        /*
         * UTC time is measured in milliseconds from January 1, 1970 at midnight from the GMT
         * time zone. Depending on your time zone, the time since January 1, 1970 at midnight (GMT)
         * will be greater or smaller. This variable represents the number of milliseconds since
         * January 1, 1970 (GMT) time.
         */
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;

        /* This method simply converts milliseconds to days, disregarding any fractional days */
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);

        /*
         * Finally, we convert back to milliseconds. This time stamp represents today's date at
         * midnight in GMT time. We will need to account for local time zone offsets when
         * extracting this information from the database.
         */
        long normalizedUtcMidnightMillis = TimeUnit.DAYS.toMillis(daysSinceEpochLocal);

        return normalizedUtcMidnightMillis;
    }

    /**
     * This method returns the number of days since the epoch (January 01, 1970, 12:00 Midnight UTC)
     * in UTC time from the current date.
     *
     * @param utcDate A date in milliseconds in UTC time.
     * @return The number of days from the epoch to the date argument.
     */
    private static long elapsedDaysSinceEpoch(long utcDate) {
        return TimeUnit.MILLISECONDS.toDays(utcDate);
    }

    /**
     * Normalizes a date (in milliseconds).
     * <p>
     * Normalize, in our usage within Sunshine means to convert a given date in milliseconds to
     * the very beginning of the date in UTC time.
     * <p>
     * For example, given the time representing
     * <p>
     * Friday, 9/16/2016, 17:45:15 GMT-4:00 DST (1474062315000)
     * <p>
     * this method would return the number of milliseconds (since the epoch) that represents
     * <p>
     * Friday, 9/16/2016, 00:00:00 GMT (1473984000000)
     * <p>
     * To make it easy to query for the exact date, we normalize all dates that go into
     * the database to the start of the day in UTC time. In order to normalize the date, we take
     * advantage of simple integer division, noting that any remainder is discarded when dividing
     * two integers.
     * <p>
     * For example, dividing 7 / 3 (when using integer division) equals 2, not 2.333 repeating
     * as you may expect.
     *
     * @param date The date (in milliseconds) to normalize
     * @return The UTC date at 12 midnight of the date
     */
    public static long normalizeDate(long date) {
        long daysSinceEpoch = elapsedDaysSinceEpoch(date);
        long millisFromEpochToTodayAtMidnightUtc = daysSinceEpoch * DAY_IN_MILLIS;
        return millisFromEpochToTodayAtMidnightUtc;
    }

    /**
     * In order to ensure consistent inserts into WeatherProvider, we check that dates have been
     * normalized before they are inserted. If they are not normalized, we don't want to accept
     * them, and leave it up to the caller to throw an IllegalArgumentException.
     *
     * @param millisSinceEpoch Milliseconds since January 1, 1970 at midnight
     * @return true if the date represents the beginning of a day in Unix time, false otherwise
     */
    public static boolean isDateNormalized(long millisSinceEpoch) {
        boolean isDateNormalized = false;
        if (millisSinceEpoch % DAY_IN_MILLIS == 0) {
            isDateNormalized = true;
        }

        return isDateNormalized;
    }

    /**
     * This method will return the local time midnight for the provided normalized UTC date.
     *
     * @param normalizedUtcDate UTC time at midnight for a given date. This number comes from the
     *                          database
     * @return The local date corresponding to the given normalized UTC date
     */
    private static long getLocalMidnightFromNormalizedUtcDate(long normalizedUtcDate) {
        /* The timeZone object will provide us the current user's time zone offset */
        TimeZone timeZone = TimeZone.getDefault();
        /*
         * This offset, in milliseconds, when added to a UTC date time, will produce the local
         * time.
         */
        long gmtOffset = timeZone.getOffset(normalizedUtcDate);
        long localMidnightMillis = normalizedUtcDate - gmtOffset;
        return localMidnightMillis;
    }



    /**
     * Returns a date string in the format specified, which shows an abbreviated date without a
     * year.
     *
     * @param context      Used by DateUtils to format the date in the current locale
     * @param timeInMillis Time in milliseconds since the epoch (local time)
     * @return The formatted date string
     */
    public static String getReadableDateString(Context context, long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_SHOW_YEAR
                | android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
                | android.text.format.DateUtils.FORMAT_SHOW_TIME;

        return android.text.format.DateUtils.formatDateTime(context, timeInMillis, flags);
    }


    public static Date getDateFromCsvString(final String str, final TimeZone tz) {
        serverDateFormat.setTimeZone(tz);
        try {
            return serverDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(0);
    }

    public static String getDateTimeStringInServerTimeZone(long longDate) {
        Date date = new Date(longDate);
        serverDateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE_SERVER));

        String stringDate = serverDateFormat.format(date);

        return stringDate;
    }

    public static String getDateStringInDeviceTimeZone(Context context, long longDate) {
        Date date = new Date(longDate);
        friendlyDateFormat.setTimeZone(TimeZone.getTimeZone(AppPreferences.getThermometerTimeZone(context)));
        String stringDate = friendlyDateFormat.format(date);

        return stringDate;
    }

    public static String getTimeStringInDeviceTimeZone(Context context, long longDate) {
        Date time = new Date(longDate);
        friendlyTimeFormat.setTimeZone(TimeZone.getTimeZone(AppPreferences.getThermometerTimeZone(context)));
        String stringTime = friendlyTimeFormat.format(time);

        return stringTime;
    }

    public static long getDatePlusDeltaDays(long startDate, int deltaDays) {
        long endPeriod;
        Calendar sumCalendar = Calendar.getInstance();
        sumCalendar.setTimeInMillis(startDate);
        sumCalendar.add(Calendar.DATE, deltaDays);
        endPeriod = sumCalendar.getTimeInMillis();
        return endPeriod;
    }

    public static long getDateMinusDeltaDays(long startDate, int deltaDays) {
        long endPeriod;
        Calendar sumCalendar = Calendar.getInstance();
        sumCalendar.setTimeInMillis(startDate);
        sumCalendar.add(Calendar.DATE, -deltaDays);
        endPeriod = sumCalendar.getTimeInMillis();
        return endPeriod;
    }

    public static long getHoursToRows(int numberOfHours) {

        long numberOfRows = numberOfHours * 60;

        return numberOfRows;
    }

    public static long getBeginningOfDay(long dayTimestamp, String timeZoneId) {

        long timestamp = dayTimestamp;

        java.util.Date date = new java.util.Date(timestamp);
        String itemDateStr = new SimpleDateFormat("dd-MMM HH:mm").format(date);

        // use UTC time zone
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        Calendar cal = Calendar.getInstance(timeZone);

        //get beginning of the day
        cal.setTimeInMillis(timestamp); // compute start of the day for the timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long today = cal.getTimeInMillis();

        date = new java.util.Date(today);
        itemDateStr = new SimpleDateFormat("dd-MMM HH:mm").format(date);

        return today;
    }


    public static long toTimeZone(long timestamp, String timeZone) {
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        Calendar cal = Calendar.getInstance(tz);

        int offset = cal.getTimeZone().getOffset(timestamp);
        return timestamp - offset;
    }
}