package com.example.myweatherdatabase.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myweatherdatabase.R;
import com.example.myweatherdatabase.ThermMeasWordViewModel;
import com.example.myweatherdatabase.data.AppPreferences;
import com.example.myweatherdatabase.data.ThermMeasurement;
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
import java.util.List;
import java.util.TimeZone;

public class BaseChartFragment extends Fragment {

    private static final String TAG = BaseChartFragment.class.getSimpleName();
    private ActivityMainBinding mMainBinding;
    private int hash;
    private boolean mIsInitialized = false;


    public LineChart getChart() {
        return mChart;
    }

    private ThermMeasWordViewModel mThermViewModel;
    private LineChart mChart;
    private LineDataSet mLineDataSet;
    private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm");

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
    private AsyncSetDataTask asyncSetData;

    public String getTitle() {
        return title;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v;
        hash = hashCode();
        v = inflater.inflate(R.layout.fragment_chart_main_24h, container, false);
        mChart = v.findViewById(R.id.chart_main_24h);

        initializeView();
        mIsInitialized = true;

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeView() {
        Bundle args = getArguments();
        int chartType = args.getInt(this.BUNDLE_ARG);
        selection = QUERY_TYPES[chartType];
        title = TAB_TITLES[chartType];
        simpleDateFormat = DATE_FORMATS[chartType];

        ContentResolver resolver = getActivity().getContentResolver();

        prepareChart();
        mLineDataSet = getPreparedLineDataSet();
        asyncSetData = new AsyncSetDataTask();

        // redraw
        //mChart.invalidate();
        mThermViewModel = ViewModelProviders.of(requireActivity()).get(ThermMeasWordViewModel.class);
        mThermViewModel.getAllMeasurements().observe(getViewLifecycleOwner(), new Observer<List<ThermMeasurement>>() {
            @Override
            public void onChanged(List<ThermMeasurement> measurements) {

                //finalize any current task
                asyncSetData.cancel(true);
                //it can take a while until it gets cancelled, so start a new one
                asyncSetData = new AsyncSetDataTask();
                asyncSetData.execute(measurements);

            }
        });
    }

    private void prepareChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.animateX(1000);
        mChart.resetTracking();
        mChart.setDrawGridBackground(false);
        // no description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        // mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getXAxis().setDrawGridLines(true);
        //mChart.getXAxis().setDrawAxisLine(false);
        mChart.setExtraBottomOffset(5f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(createDateFormatter());
        //xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(270f);
    }

    private LineDataSet getPreparedLineDataSet() {

        LineDataSet lineDataSet = new LineDataSet(null, "Temperatures");

        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.1f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(0.2f);
        lineDataSet.setCircleRadius(1);
        lineDataSet.setCircleColor(Color.WHITE);
        //set1.setHighLightColor(Color.rgb(244, 117, 117));
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setFillColor(Color.WHITE);
        lineDataSet.setFillAlpha(180);
        //set1.setFillAlpha(100);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawValues(false);

        return lineDataSet;
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

    class AsyncSetDataTask extends AsyncTask<List<ThermMeasurement>, Integer, ArrayList<Entry>> {


        private SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd - HH:mm");
        private int entries = 0;
        private float lowTemp = 100;
        private float highTemp = -100;
        private long xAxisInitialDate;
        private long xAxisFinalDate;

        public AsyncSetDataTask() {
            super();
            fmt.setTimeZone(TimeZone.getDefault()); // sets time zone... I think I did this properly...
        }

        @Override
        protected ArrayList<Entry> doInBackground(List<ThermMeasurement>... measurements) {

            if (measurements[0] == null || measurements[0].size() == 0) {
                Log.i("VALUES", "NO DATA!!");
                mChart.setData(null);
                return new ArrayList<>();
            }

            long longDate = 0;
            float temp;
            ArrayList<Entry> values = new ArrayList<>();

            // Iterate through all the returned rows in the cursor
            for (ThermMeasurement measurement : measurements[0]) {
                if (isCancelled())
                    return null;

                //Converting to milliseconds
                longDate = measurement.getDate() * 1000;
                temp = measurement.getTemperature();

                highTemp = temp > highTemp ? temp : highTemp;
                lowTemp = temp < lowTemp ? temp : lowTemp;

                values.add(new Entry(longDate, temp));
                Log.i("VALUES", fmt.format(longDate) + " - " + Float.toString(temp));

                entries++;
            }

            xAxisInitialDate = measurements[0].get(0).getDate() * 1000;
            xAxisFinalDate = measurements[0].get(measurements[0].size() - 1).getDate() * 1000;

            Log.i("VALUES", "INITIAL DATE :" + fmt.format(xAxisInitialDate) + " ---- FINAL DATE: " + fmt.format(xAxisFinalDate));
            Log.i("NUMBER OF ENTRIES", String.valueOf(entries));
            Log.i("LOW TEMP", String.valueOf(lowTemp));
            Log.i("HIGH TEMP", String.valueOf(highTemp));

            return values;
        }

        @Override
        protected void onPostExecute(ArrayList<Entry> values) {
            super.onPostExecute(values);
            if (isCancelled())
                return;

            if (values.size() == 0) {
                mChart.clear();
                return;
            }

            mChart.getAxisLeft().setAxisMinimum(lowTemp - 5);
            mChart.getAxisLeft().setAxisMaximum(highTemp + 5);

            mChart.getXAxis().setAxisMinimum(xAxisInitialDate);
            mChart.getXAxis().setAxisMaximum(xAxisFinalDate);
            mChart.getXAxis().setLabelCount(7);

            mChart.getAxisLeft().removeAllLimitLines();
            LimitLine minLimit = new LimitLine(lowTemp, "Min: " + String.valueOf(lowTemp));
            LimitLine maxLimit = new LimitLine(highTemp, "Max: " + String.valueOf(highTemp));
            minLimit.setYOffset(-12);
            minLimit.setLineColor(Color.CYAN);
            maxLimit.setLineColor(Color.YELLOW);
            mChart.getAxisLeft().addLimitLine(minLimit);
            mChart.getAxisLeft().addLimitLine(maxLimit);

            mLineDataSet.setValues(values);

            // create a data object with the data sets
            LineData data = new LineData(mLineDataSet);
            // set data
            mChart.setData(data);

            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();
            l.setEnabled(false);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            Log.d(TAG, "onPostExecute: from class: " + hash);
        }
    }

}
