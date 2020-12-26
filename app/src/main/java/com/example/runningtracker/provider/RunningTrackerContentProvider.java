package com.example.runningtracker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.runningtracker.RunningTrackerDBHandler;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class RunningTrackerContentProvider extends ContentProvider {

    private static final int ACTIVITIES = 100;
    private static final int USER_DETAILS = 200;
    private static final int STATS_OVERVIEW = 300;

    private RunningTrackerDBHandler runningTrackerDBHandler;

    @Override
    public boolean onCreate() {
        runningTrackerDBHandler = new RunningTrackerDBHandler(getContext(), null);
        return true;
    }

    public static UriMatcher uriMatcher(){
        String authority = RunningTrackerContract.AUTHORITY;
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, RunningTrackerContract.TABLE_ACTIVITIES, ACTIVITIES);
        matcher.addURI(authority, RunningTrackerContract.TABLE_USER_DETAILS, USER_DETAILS);
        matcher.addURI(authority, RunningTrackerContract.STATS_OVERVIEW, STATS_OVERVIEW);
        return matcher;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = runningTrackerDBHandler.getReadableDatabase();

        int uriType = uriMatcher().match(uri);
        Cursor cursor;

        if (uriType == ACTIVITIES) {
            cursor = db.query(RunningTrackerContract.TABLE_ACTIVITIES, projection, selection, selectionArgs, null, null, sortOrder);
        }
        else if (uriType == USER_DETAILS){
            cursor = db.query(RunningTrackerContract.TABLE_USER_DETAILS, projection, selection, selectionArgs, null, null, sortOrder);
        }
        else if (uriType == STATS_OVERVIEW){
            ZonedDateTime currentZDT = ZonedDateTime.now();
            String dateString = String.format(Locale.ENGLISH, "%d%02d%02d", currentZDT.getYear(), currentZDT.get(ChronoField.MONTH_OF_YEAR), currentZDT.getDayOfMonth());
            String monthStart = dateString.substring(0, 6) + "00";
            String monthEnd = dateString.substring(0, 6) + "32";
            String generalSelectQuery = "SELECT SUM("+ RunningTrackerContract.ACTIVITIES_DISTANCE +"), COUNT(" + RunningTrackerContract.ACTIVITIES_ID + ") as runs, MAX(" + RunningTrackerContract.ACTIVITIES_SPEED + "), SUM(" + RunningTrackerContract.ACTIVITIES_CALORIES_BURNED + ") from " + RunningTrackerContract.TABLE_ACTIVITIES;
            String query =  generalSelectQuery +
                            " UNION ALL " +
                            generalSelectQuery + " WHERE " + RunningTrackerContract.ACTIVITIES_DATE + ">" + monthStart + " AND " + RunningTrackerContract.ACTIVITIES_DATE + "<" + monthEnd +
                            " UNION ALL " +
                            generalSelectQuery + " WHERE " + RunningTrackerContract.ACTIVITIES_DATE + "==" + dateString +
                            " ORDER BY runs DESC;";
            Log.d("vp query", "query: " + query);
            cursor = db.rawQuery(query, null);
//            Log.d("vp", "runs: " + cursor.getInt(1));
        }
        else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
