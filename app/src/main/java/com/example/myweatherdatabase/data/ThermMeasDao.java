package com.example.myweatherdatabase.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ThermMeasDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ThermMeasurement measurement);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<ThermMeasurement> measurements);

    @Query("DELETE FROM ThermMeasurement")
    void deleteAll();

    @Query("SELECT * FROM ThermMeasurement")
    LiveData<List<ThermMeasurement>> getAllMeasurements();

    @Query("SELECT * FROM ThermMeasurement ORDER BY DATE DESC LIMIT 1")
    LiveData<ThermMeasurement> getLastMeasurement();
}
