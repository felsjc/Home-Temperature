package com.example.myweatherdatabase.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ThermMeasurement {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @ColumnInfo(name = "date")
    private long mDate;
    @ColumnInfo (name = "temperature")
    private Float mTemperature;
    public ThermMeasurement(@NonNull long date,
                            @NonNull Float temperature) {
        this.mDate = date;
        this.mTemperature = temperature;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getDate() {
        return mDate;
    }

    public Float getTemperature() {
        return mTemperature;
    }


}
