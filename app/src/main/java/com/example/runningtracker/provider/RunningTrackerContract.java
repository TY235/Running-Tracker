package com.example.runningtracker.provider;

import android.net.Uri;

public class RunningTrackerContract {

    public static final String AUTHORITY = "com.example.runningtracker.provider.RunningTrackerContentProvider";

    /* Table names */
    public static final String TABLE_ACTIVITIES = "activities";
    public static final String TABLE_USER_DETAILS = "user_details";

    public static final String STATS_OVERVIEW = "stats_overview";

    /* The uris to access the content provider */
    public static final Uri ACTIVITIES_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ACTIVITIES);
    public static final Uri USER_DETAILS_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_USER_DETAILS);
    public static final Uri STATS_OVERVIEW_URI = Uri.parse("content://" + AUTHORITY + "/" + STATS_OVERVIEW);

    /* Field names */
    public static final String ACTIVITIES_ID = "_id";
    public static final String ACTIVITIES_DATE = "_date";
    public static final String ACTIVITIES_TIME = "_time";
    public static final String ACTIVITIES_DISTANCE = "distance";
    public static final String ACTIVITIES_TIME_TAKEN = "time_taken";
    public static final String ACTIVITIES_SPEED = "speed";
    public static final String ACTIVITIES_PACE = "pace";
    public static final String ACTIVITIES_CALORIES_BURNED = "calories_burned";
    public static final String ACTIVITIES_WEATHER = "weather";
    public static final String ACTIVITIES_SATISFACTION = "satisfaction";
    public static final String ACTIVITIES_NOTES = "notes";

    public static final String USER_ID = "_id";
    public static final String USER_NAME = "name";
    public static final String USER_HEIGHT = "height";
    public static final String USER_WEIGHT = "weight";

}
