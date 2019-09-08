package com.example.myweatherdatabase.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.InputStream;

@Database(entities = {ThermMeasurement.class},version = 1,exportSchema = false)
public abstract class ThermRoomDatabase extends RoomDatabase {

    public abstract ThermMeasDao thermMeasDao();
    private static ThermRoomDatabase INSTANCE;
    public static ThermRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ThermRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ThermRoomDatabase.class, "ThermRoomDatabase")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
