package com.example.myweatherdatabase.data;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.myweatherdatabase.AppExecutors;
import com.example.myweatherdatabase.sync.TempSyncTask;

import java.util.List;

public class ThermMeasRepository {

    private static final String TAG = ThermMeasurement.class.getSimpleName();
    private ThermMeasDao mDao;
    private LiveData<List<ThermMeasurement>> mAllMeasurements;
    private LiveData<ThermMeasurement> mLastMeasurement;
    private boolean mInitialized = false;
    private final AppExecutors mExecutors;
    private Context mApplicationContext;

    public ThermMeasRepository(@NonNull Application application) {
        mApplicationContext = application;
        ThermRoomDatabase db = ThermRoomDatabase.getDatabase(application);
        this.mDao = db.thermMeasDao();
        mAllMeasurements = mDao.getAllMeasurements();
        mLastMeasurement = mDao.getLastMeasurement();
        mExecutors = AppExecutors.getInstance();


    }

    public LiveData<ThermMeasurement> getLastMeasurement() {
        initializeData();
        return mLastMeasurement;
    }

    public LiveData<List<ThermMeasurement>> getAllMeasurements() {
        initializeData();
        return mAllMeasurements;
    }

    public void insert(ThermMeasurement measurement) {
        new insertAsyncTask(mDao).execute(measurement);
    }

    public void bulkInsert(List<ThermMeasurement> measurements) {
        new bulkInsertAsyncTask(mDao).execute(measurements);
    }

    ;

    public void deleteAll() {
        new deleteAllAsyncTask(mDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<ThermMeasurement, Void, Void> {

        private ThermMeasDao mAsyncTaskDao;

        insertAsyncTask(ThermMeasDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ThermMeasurement... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class bulkInsertAsyncTask extends AsyncTask<List<ThermMeasurement>, Void, Void> {

        private ThermMeasDao mAsyncTaskDao;

        bulkInsertAsyncTask(ThermMeasDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final List<ThermMeasurement>... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private ThermMeasDao mAsyncTaskDao;

        deleteAllAsyncTask(ThermMeasDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    private synchronized void initializeData() {


        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;

        mInitialized = true;
        Log.d(TAG, "initializeData from repository: " + hashCode());

        mExecutors.networkIO().execute(() -> {
            startFetchDataService(mApplicationContext);
            return;
        });
    }

    /**
     * Network related operation
     */

    private void startFetchDataService(Context context) {
        ThermMeasWrapper thermMeasWrapper = TempSyncTask.syncTemperatures(context);
        if (!thermMeasWrapper.hasError())
            bulkInsert(thermMeasWrapper.getMeasurements());
    }
}
