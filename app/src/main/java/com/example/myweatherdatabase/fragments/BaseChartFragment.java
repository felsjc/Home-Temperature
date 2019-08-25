package com.example.myweatherdatabase.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myweatherdatabase.R;
import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermContract;
import com.example.myweatherdatabase.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class BaseChartFragment extends Fragment {

    private ActivityMainBinding mMainBinding;
    private LineChart chart;
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd - HH:mm");

    public static String BUNDLE_ARG = "QUERY_TYPES";

    public final static int
            FRAGMENT_24H = 0,
            FRAGMENT_7DAYS = 1,
            FRAGMENT_30DAYS = 2,
            FRAGMENT_1YEAR = 3,
            FRAGMENT_ALLTIME = 4;

    public final static String[] TAB_TITLES = {
            "24 \n HOURS",
            "1 \n WEEK",
            "1 \n MONTH",
            "1 \n YEAR",
            "ALL \n TIME"
    };

    public final static String[] QUERY_TYPES = {
            "(date%180<60) AND (date >= (SELECT DATE FROM temperatures ORDER BY _id DESC LIMIT 1)-86400)",
            "(date%3600<60) AND (date >= (SELECT DATE FROM temperatures ORDER BY _id DESC LIMIT 1)-8*86400)",
            "(date%3600<60) AND (date >= (SELECT DATE FROM temperatures ORDER BY _id DESC LIMIT 1)-32*86400)",
            "(date%(12*3600)<60) AND (date >= (SELECT DATE FROM temperatures ORDER BY _id DESC LIMIT 1)-366*86400)",
            "(date%(3600)<60)"};

    public final static SimpleDateFormat[] DATE_FORMATS = {
            new SimpleDateFormat("E - HH:mm"),
            new SimpleDateFormat("E d - HH:mm"),
            new SimpleDateFormat("MMM d - HH:mm"),
            new SimpleDateFormat("MMM d"),
            new SimpleDateFormat("d MMM yyyy"),
    };

    private String selection;
    private String title;
    private SimpleDateFormat simpleDateFormat;
    protected Context context;

    public String getTitle() {
        return title;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chart_main_24h, container, false);
        chart = v.findViewById(R.id.chart_main_24h);

        Bundle args = getArguments();
        int chartType = args.getInt(this.BUNDLE_ARG);
        selection = QUERY_TYPES[chartType];
        title = TAB_TITLES[chartType];
        simpleDateFormat = DATE_FORMATS[chartType];


        ContentResolver resolver = getActivity().getContentResolver();

        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.animateX(1000);
        chart.resetTracking();
        chart.setDrawGridBackground(false);
        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);
        // chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(true);
        //chart.getXAxis().setDrawAxisLine(false);
        chart.setExtraBottomOffset(5f);

        //resolver = getContentResolver();
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(createDateFormatter());
        //xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(270f);

        String sortOrder = "";


        setData(resolver.query(ThermContract.TempMeasurment.CONTENT_URI,
                null,
                selection,
                null,
                sortOrder));

        // redraw
        chart.invalidate();

/*
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf");
        Legend l = chart.getLegend();
        l.setTypeface(tf);
*/


        return v;
    }


    private void setData(Cursor cursor) {

        int dateIndex;
        int tempIndex;
        long longDate = 0;
        long longInitialDate = 0, longFinalDate = 0;
        float temp;

        // Indices for the _id, description, and priority columns
        dateIndex = cursor.getColumnIndex("date");
        tempIndex = cursor.getColumnIndex("temperature");

        if (cursor.moveToFirst()) {
            longInitialDate = (long) cursor.getInt(dateIndex) * 1000;
        }
        cursor.moveToPosition(-1);

        ArrayList<Entry> values = new ArrayList<>();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd - HH:mm");
        fmt.setTimeZone(TimeZone.getDefault()); // sets time zone... I think I did this properly...

        int entries = 0;
        float lowTemp = 100;
        float highTemp = -100;

        // Iterate through all the returned rows in the cursor
        while (cursor.moveToNext()) {
            // Use that index to extract the String or Int value of the word
            // at the current row the cursor is on.
//            int id = cursor.getInt(idIndex);

            //Converting to milliseconds
            longDate = (long) cursor.getInt(dateIndex) * 1000;
            temp = cursor.getFloat(tempIndex);

            highTemp = temp > highTemp ? temp : highTemp;
            lowTemp = temp < lowTemp ? temp : lowTemp;

            values.add(new Entry(longDate, temp));
            Log.i("VALUES", fmt.format(longDate) + " - " + Float.toString(temp));
            entries++;
        }

        longFinalDate = longDate;

        Log.i("VALUES", "INITIAL DATE :" + fmt.format(longInitialDate) + " ---- FINAL DATE: " + fmt.format(longFinalDate));

        Log.i("NUMBER OF ENTRIES", String.valueOf(entries));
        Log.i("LOW TEMP", String.valueOf(lowTemp));
        Log.i("HIGH TEMP", String.valueOf(highTemp));

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");

        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setCubicIntensity(0.1f);
        set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(0.0f);
        set1.setCircleRadius(0f);
        set1.setCircleColor(Color.WHITE);
        //set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);
        set1.setFillAlpha(180);
        //set1.setFillAlpha(100);
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setDrawValues(false);


        chart.getAxisLeft().setAxisMinimum(lowTemp - 5);
        chart.getAxisLeft().setAxisMaximum(highTemp + 5);

        chart.getXAxis().setAxisMinimum(longInitialDate);
        chart.getXAxis().setAxisMaximum(longFinalDate);
        chart.getXAxis().setLabelCount(7);


        chart.getAxisLeft().removeAllLimitLines();
        LimitLine minLimit = new LimitLine(lowTemp, "Min: " + String.valueOf(lowTemp));
        LimitLine maxLimit = new LimitLine(highTemp, "Max: " + String.valueOf(highTemp));
        minLimit.setYOffset(-12);
        minLimit.setLineColor(Color.CYAN);
        maxLimit.setLineColor(Color.YELLOW);
        chart.getAxisLeft().addLimitLine(minLimit);
        chart.getAxisLeft().addLimitLine(maxLimit);

        // create a data object with the data sets
        LineData data = new LineData(set1);

        // set data
        chart.setData(data);


        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }

    ValueFormatter createDateFormatter() {
        ValueFormatter formatter = new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {
                Date date = new Date((long) value);

                simpleDateFormat.setTimeZone(
                        TimeZone.getTimeZone(
                                AppPreferences.getThermometerTimeZone(context)
                        )
                );
                String dateString = simpleDateFormat.format(date);
                String s = dateString;

                return s;
            }

            // we don't draw numbers, so no decimal digits needed
            public int getDecimalDigits() {
                return 0;
            }

        };

        return formatter;
    }

}
