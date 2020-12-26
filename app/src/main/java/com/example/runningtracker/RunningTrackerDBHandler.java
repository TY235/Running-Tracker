package com.example.runningtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.runningtracker.provider.RunningTrackerContract;

public class RunningTrackerDBHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "runningTracker.db";

    public RunningTrackerDBHandler(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    /* Create the required tables when the DB Handler is created */
    @Override
    public void onCreate(SQLiteDatabase db) {
        addActivitiesTable(db);
        addUserDetailsTable(db);
    }

    /* Create activities table */
    private void addActivitiesTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " +  RunningTrackerContract.TABLE_ACTIVITIES + " (" +
                RunningTrackerContract.ACTIVITIES_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                RunningTrackerContract.ACTIVITIES_DATE + " INTEGER NOT NULL, " +
                RunningTrackerContract.ACTIVITIES_TIME + " INTEGER NOT NULL, " +
                RunningTrackerContract.ACTIVITIES_DISTANCE + " REAL NOT NULL, " +
                RunningTrackerContract.ACTIVITIES_TIME_TAKEN + " REAL NOT NULL, " +
                RunningTrackerContract.ACTIVITIES_SPEED + " REAL, " +
                RunningTrackerContract.ACTIVITIES_PACE + " REAL, " +
                RunningTrackerContract.ACTIVITIES_CALORIES_BURNED + " REAL, " +
                RunningTrackerContract.ACTIVITIES_WEATHER + " VARCHAR(69), " +
                RunningTrackerContract.ACTIVITIES_SATISFACTION + " VARCHAR(136), " +
                RunningTrackerContract.ACTIVITIES_NOTES + " VARCHAR(255));";
        db.execSQL(sql);
    }

    /* Create user details table */
    private void addUserDetailsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " +  RunningTrackerContract.TABLE_USER_DETAILS + " (" +
                RunningTrackerContract.USER_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                RunningTrackerContract.USER_NAME + " VARCHAR(136) NOT NULL, " +
                RunningTrackerContract.USER_HEIGHT + " REAL NOT NULL, " +
                RunningTrackerContract.USER_WEIGHT + " REAL NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql1 = "DROP TABLE IF EXISTS " + RunningTrackerContract.TABLE_ACTIVITIES + ";";
        db.execSQL(sql1);
        String sql2 = "DROP TABLE IF EXISTS " +RunningTrackerContract.TABLE_USER_DETAILS + ";";
        db.execSQL(sql2);
        onCreate(db);
    }

    /* Add new record of activity to activities table and return the new id for the record */
    public int addActivities (int date, int time, double distance, double timeTaken, double speed, double pace, double caloriesBurned, String weather, String satisfaction){
        int newId;
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.ACTIVITIES_DATE, date);
        values.put(RunningTrackerContract.ACTIVITIES_TIME, time);
        values.put(RunningTrackerContract.ACTIVITIES_DISTANCE, distance);
        values.put(RunningTrackerContract.ACTIVITIES_TIME_TAKEN, timeTaken);
        values.put(RunningTrackerContract.ACTIVITIES_SPEED, speed);
        values.put(RunningTrackerContract.ACTIVITIES_PACE, pace);
        values.put(RunningTrackerContract.ACTIVITIES_CALORIES_BURNED, caloriesBurned);
        values.put(RunningTrackerContract.ACTIVITIES_WEATHER, weather);
        values.put(RunningTrackerContract.ACTIVITIES_SATISFACTION, satisfaction);
        SQLiteDatabase db = this.getWritableDatabase();
        newId = (int) db.insert(RunningTrackerContract.TABLE_ACTIVITIES, null, values);
        db.close();
        return newId;
    }

    /* Add new record of user details to activities table and return the new id for the record */
    public int addUserDetails (String name, double height, double weight){
        int newId;
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.USER_NAME, name);
        values.put(RunningTrackerContract.USER_HEIGHT, height);
        values.put(RunningTrackerContract.USER_WEIGHT, weight);
        SQLiteDatabase db = this.getWritableDatabase();
        newId = (int) db.insert(RunningTrackerContract.TABLE_USER_DETAILS, null, values);
        db.close();
        return newId;
    }

    /* Update the user's name in user details table but only update the first record as only one user is required for the app currently and return the number of rows affected */
    public int updateName (String name){
        int noOfRowsAffected;
        int userID = 1;
        String[] args = new String[]{String.valueOf(userID)};
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.USER_NAME, name);
        SQLiteDatabase db = this.getWritableDatabase();
        noOfRowsAffected = db.update(RunningTrackerContract.TABLE_USER_DETAILS, values, RunningTrackerContract.USER_ID + "=?", args);
        return noOfRowsAffected;
    }

    /* Update the user's height in user details table but only update the first record as only one user is required for the app currently */
    public void updateHeight (double height){
        int userID = 1;
        String[] args = new String[]{String.valueOf(userID)};
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.USER_HEIGHT, height);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(RunningTrackerContract.TABLE_USER_DETAILS, values, RunningTrackerContract.USER_ID + "=?", args);
    }

    /* Update the user's weight in user details table but only update the first record as only one user is required for the app currently */
    public void updateWeight (double weight){
        int userID = 1;
        String[] args = new String[]{String.valueOf(userID)};
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.USER_WEIGHT, weight);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(RunningTrackerContract.TABLE_USER_DETAILS, values, RunningTrackerContract.USER_ID + "=?", args);
    }

    /* Update the weather in activities table and return the number of rows affected */
    public int updateWeather (int activityID, String weather){
        int noOfRowsAffected;
        String[] args = new String[]{String.valueOf(activityID)};
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.ACTIVITIES_WEATHER, weather);
        SQLiteDatabase db = this.getWritableDatabase();
        noOfRowsAffected = db.update(RunningTrackerContract.TABLE_ACTIVITIES, values, RunningTrackerContract.ACTIVITIES_ID + "=?", args);
        return noOfRowsAffected;
    }

    /* Update the satisfaction in activities table and return the number of rows affected */
    public void updateSatisfaction (int activityID, String satisfaction){
        String[] args = new String[]{String.valueOf(activityID)};
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.ACTIVITIES_SATISFACTION, satisfaction);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(RunningTrackerContract.TABLE_ACTIVITIES, values, RunningTrackerContract.ACTIVITIES_ID + "=?", args);
    }

    /* Update the notes in activities table and return the number of rows affected */
    public void updateNotes (int activityID, String notes){
        String[] args = new String[]{String.valueOf(activityID)};
        ContentValues values = new ContentValues();
        values.put(RunningTrackerContract.ACTIVITIES_NOTES, notes);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(RunningTrackerContract.TABLE_ACTIVITIES, values, RunningTrackerContract.ACTIVITIES_ID + "=?", args);
    }

    /* Delete the activity from activities table and return true if it is deleted successfully */
    public boolean deleteActivity (int activityID) {
        int noOfRowsAffected;
        String[] args = new String[]{String.valueOf(activityID)};
        SQLiteDatabase db = this.getWritableDatabase();
        noOfRowsAffected = db.delete(RunningTrackerContract.TABLE_ACTIVITIES, RunningTrackerContract.ACTIVITIES_ID + "=?", args);
        return noOfRowsAffected == 1;
    }
}
