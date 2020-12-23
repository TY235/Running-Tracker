package com.example.runningtracker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.runningtracker.RunningTrackerDBHandler;

public class RunningTrackerContentProvider extends ContentProvider {

    private static final int ACTIVITIES = 100;
    private static final int USER_DETAILS = 200;

    private RunningTrackerDBHandler runningTrackerDBHandler;

    @Override
    public boolean onCreate() {
        runningTrackerDBHandler = new RunningTrackerDBHandler(getContext(), null, null, 1);
        return true;
    }

    public static UriMatcher uriMatcher(){
        String authority = RunningTrackerContract.AUTHORITY;
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, RunningTrackerContract.TABLE_ACTIVITIES, ACTIVITIES);
        matcher.addURI(authority, RunningTrackerContract.TABLE_USER_DETAILS, USER_DETAILS);
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
