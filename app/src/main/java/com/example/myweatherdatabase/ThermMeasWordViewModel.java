package com.example.myweatherdatabase;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myweatherdatabase.data.ThermMeasRepository;
import com.example.myweatherdatabase.data.ThermMeasurement;

import java.util.List;

public class ThermMeasWordViewModel extends AndroidViewModel {

    private ThermMeasRepository mMeasRepository;
    private LiveData<List<ThermMeasurement>> mAllMeasurements;
    private LiveData<ThermMeasurement> mLastMeasurement;

    public ThermMeasWordViewModel(@NonNull Application application) {
        super(application);
        mMeasRepository = new ThermMeasRepository(application);
        mAllMeasurements = mMeasRepository.getAllMeasurements();
        mLastMeasurement = mMeasRepository.getLastMeasurement();
    }

    public LiveData<List<ThermMeasurement>> getAllMeasurements() {
        return mAllMeasurements;
    }

    public LiveData<ThermMeasurement> getLastMeasurement() {
        return mLastMeasurement;
    }

    public void insert(ThermMeasurement measurement){
        mMeasRepository.insert(measurement);
    }

    public void deleteAll(){mMeasRepository.deleteAll();}

}
