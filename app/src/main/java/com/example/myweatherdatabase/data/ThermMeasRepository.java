package com.example.myweatherdatabase.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ThermMeasRepository {
    private ThermMeasDao mDao;

    private LiveData<List<ThermMeasurement>> mAllMeasurements;

    private LiveData<ThermMeasurement> mLastMeasurement;

    public ThermMeasRepository(Application application) {
        ThermRoomDatabase db = ThermRoomDatabase.getDatabase(application);
        this.mDao = db.thermMeasDao();
        mAllMeasurements = mDao.getAllMeasurements();
        mLastMeasurement = mDao.getLastMeasurement();
    }

    public LiveData<ThermMeasurement> getLastMeasurement() {
        return mLastMeasurement;
    }

    public LiveData<List<ThermMeasurement>> getAllMeasurements() {
        return mAllMeasurements;
    }

    public void insert (ThermMeasurement measurement) {
        new insertAsyncTask(mDao).execute(measurement);
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
}
