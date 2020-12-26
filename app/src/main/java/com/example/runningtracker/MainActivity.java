package com.example.runningtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.runningtracker.provider.RunningTrackerContract;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements RunFragment.RunFragmentListener, ResultFragment.ResultFragmentListener, ProfileFragment.ProfileFragmentListener, ActivityFragment.ActivityFragmentListener, ActivityDetailsFragment.ActivityDetailsFragmentListener, LoaderManager.LoaderCallbacks<Cursor> {


    private static final int CURSOR_ID_USER_DETAILS = 0;
    private static final int CURSOR_ID_ACTIVITY_DETAILS = 1;
    private static final int CURSOR_ID_SORT_BY_ID_DESC = 10;
    private static final int CURSOR_ID_SORT_BY_ID_ASC = 11;
    private static final int CURSOR_ID_SORT_BY_DISTANCE_DESC = 20;
    private static final int CURSOR_ID_SORT_BY_DISTANCE_ASC = 21;
    private static final int CURSOR_ID_STATS_OVERVIEW = 3;

    private final ArrayList<StatsOverviewModel> statsOverviewModelList = new ArrayList<>();
    private final ArrayList<ActivityModel> activityModelList = new ArrayList<>();

    final ActivityDetailsFragment activityDetailsFragment = new ActivityDetailsFragment();
    final ResultFragment resultFragment = new ResultFragment();
    final RunFragment runFragment = new RunFragment();
    final ActivityFragment activityFragment = new ActivityFragment();
    final ProfileFragment profileFragment = new ProfileFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();



    Fragment activeFragment = runFragment;
    BottomNavigationView bottomNav;
    private boolean firstTimeLoad = true;
    private boolean sortedByDateDesc;
    private boolean sortedByDistanceDesc;
    private boolean inResultFragment = false;
    private RunningTrackerDBHandler runningTrackerDBHandler;
    private ContentResolver cr;
    private int startDate=0, startTime =0;
    private double userWeight=0, userHeight=0;
    private String userName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cr = getContentResolver();
        runningTrackerDBHandler = new RunningTrackerDBHandler(this, null);

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        fragmentManager.beginTransaction().add(R.id.fragment_frame, runFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, activityFragment, "2").hide(activityFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, profileFragment, "3").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, resultFragment, "Result").hide(resultFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, activityDetailsFragment, "Details").hide(activityDetailsFragment).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserDetailsNUpdateWelcomeText();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.nav_run && !inResultFragment){
                        getUserDetailsNUpdateWelcomeText();
                        fragmentManager.beginTransaction().hide(activeFragment).show(runFragment).commit();
                        activeFragment = runFragment;
                    }
                    else if (item.getItemId() == R.id.nav_run && inResultFragment){
                        fragmentManager.beginTransaction().hide(activeFragment).show(resultFragment).commit();
                        activeFragment = resultFragment;
                    }
                    else if (item.getItemId() == R.id.nav_activity){
                        Log.d("vp", "clicked nav bar");
                        sortedByDateDesc = true;
                        sortedByDistanceDesc = false;
                        navigateToActivityList();

                    }
                    else if (item.getItemId() == R.id.nav_profile){
                        getUserDetailsNUpdateWelcomeText();
                        profileFragment.updateEditTextView(userName, userHeight, userWeight);
                        fragmentManager.beginTransaction().hide(activeFragment).show(profileFragment).commit();
                        activeFragment = profileFragment;
                    }
                    else{
                        return false;
                    }
                    return true;
                }
            };


    private void getUserDetailsNUpdateWelcomeText(){
        if(firstTimeLoad){
            Log.d("checkFirstTime", "getUserDetailsNUpdateWelcomeText: first time load");
            getLoaderManager().initLoader(CURSOR_ID_USER_DETAILS, null, this);
            firstTimeLoad = false;
        }
        else{
            Log.d("checkFirstTime", "getUserDetailsNUpdateWelcomeText: reload");
            getLoaderManager().restartLoader(CURSOR_ID_USER_DETAILS, null, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (bottomNav.getSelectedItemId() == R.id.nav_run) {
            super.onBackPressed();
            finish();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_run);
        }
    }

    @Override
    public void onRunningStatsSent(int startDate, int startTime, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond) {
        this.startDate = startDate;
        this.startTime = startTime;
        fragmentManager.beginTransaction().hide(runFragment).show(resultFragment).commit();
        resultFragment.retrieveStats(polyline, totalTimeTakenInSeconds, distanceInKM, paceInMinutesPerKM, speedInMetersPerSecond, userHeight, userWeight);
        activeFragment = resultFragment;
        inResultFragment = true;
    }

    @Override
    public void onResultStatsSent(boolean saveBtnClicked, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond, double caloriesBurned, String weather, String satisfaction) {
        if (saveBtnClicked){
            int id;
            id = runningTrackerDBHandler.addActivities(startDate, startTime, distanceInKM, totalTimeTakenInSeconds, speedInMetersPerSecond, paceInMinutesPerKM, caloriesBurned, weather, satisfaction);
            if (id == -1){
                displayErrorToast();
            }
            else {
                Toast.makeText(getBaseContext(), "Result Saved!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getBaseContext(), "Result Discarded!", Toast.LENGTH_LONG).show();
        }
        inResultFragment = false;
        getUserDetailsNUpdateWelcomeText();
        fragmentManager.beginTransaction().hide(resultFragment).show(runFragment).commit();
        runFragment.updateStopTrackingUI();
        activeFragment = runFragment;
    }

    @Override
    public void onUserDetailsSent(String userName, double userHeightInCM, double userWeightInKG) {
        Cursor cursor = cr.query(RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
        int rows;
        if (cursor!=null && cursor.getCount() > 0){
            rows = runningTrackerDBHandler.updateName(userName);
            runningTrackerDBHandler.updateHeight(userHeightInCM);
            runningTrackerDBHandler.updateWeight(userWeightInKG);
            cursor.close();
            if (rows == 0){
                displayErrorToast();
            }
            else {
                Toast.makeText(getBaseContext(), "Profile Updated!", Toast.LENGTH_LONG).show();
            }
        }
        else{
            int id;
            id = runningTrackerDBHandler.addUserDetails(userName, userHeightInCM, userWeightInKG);
            if (id == -1){
              displayErrorToast();
            }
            else {
                Toast.makeText(getBaseContext(), "Profile Added!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void resetActivityListData(){
        activityModelList.clear();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CURSOR_ID_USER_DETAILS){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
        }
        else if (id == CURSOR_ID_SORT_BY_ID_DESC){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, null, null, RunningTrackerContract.ACTIVITIES_ID + " DESC");
        }
        else if (id == CURSOR_ID_SORT_BY_ID_ASC){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, null, null, RunningTrackerContract.ACTIVITIES_ID + " ASC");
        }
        else if (id == CURSOR_ID_SORT_BY_DISTANCE_DESC){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, null, null, RunningTrackerContract.ACTIVITIES_DISTANCE + " DESC");
        }
        else if (id == CURSOR_ID_SORT_BY_DISTANCE_ASC){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, null, null, RunningTrackerContract.ACTIVITIES_DISTANCE + " ASC");
        }
        else if (id == CURSOR_ID_ACTIVITY_DETAILS){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, RunningTrackerContract.ACTIVITIES_ID + "=?", new String[]{String.valueOf(Objects.requireNonNull(args).getInt("id"))}, null);
        }
        else if (id == CURSOR_ID_STATS_OVERVIEW){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.STATS_OVERVIEW_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case CURSOR_ID_USER_DETAILS:
                if (data!=null && data.getCount() > 0){
                    while(data.moveToNext()){
                        userName = data.getString(1);
                        userHeight = data.getDouble(2);
                        userWeight = data.getDouble(3);
                    }
                    data.close();
                    runFragment.updateWelcomeText(userName);
                    Log.d("checkFirstTime", "onLoadFinished: " + userName);
                    Log.d("checkFirstTime", "onLoadFinished: " + userHeight);
                    Log.d("checkFirstTime", "onLoadFinished: " + userWeight);
                }
                break;

            case CURSOR_ID_STATS_OVERVIEW:
                statsOverviewModelList.clear();
                Log.d("vp", "load finished");
                if (data!=null && data.getCount() > 0){
                    Log.d("vp", "data count : " + data.getCount());

                    while(data.moveToNext()){
                        if (data.getInt(1)!=0){
                            statsOverviewModelList.add(new StatsOverviewModel(data.getDouble(0), data.getInt(1), data.getDouble(2), data.getDouble(3)));
                            Log.d("vp", "total runs : " + data.getInt(1));
                        }
                        else{
                            Log.d("vp", "total runs else : " + data.getInt(1));
                            statsOverviewModelList.add(new StatsOverviewModel(0, 0, 0, 0));
                        }
                    }
                    data.close();
                }
                else {
                    for (int i = 0; i < 3; i++){
                        statsOverviewModelList.add(new StatsOverviewModel(0, 0, 0, 0));
                    }
                }


                activityFragment.retrieveStatsOverviewList(statsOverviewModelList);
                break;

            case  CURSOR_ID_SORT_BY_ID_DESC:
            case  CURSOR_ID_SORT_BY_ID_ASC:
            case  CURSOR_ID_SORT_BY_DISTANCE_DESC:
            case  CURSOR_ID_SORT_BY_DISTANCE_ASC:
                if (data!=null && data.getCount() > 0){
                    while(data.moveToNext()){
                        activityModelList.add(new ActivityModel(data.getInt(0), data.getInt(1), data.getInt(2), data.getDouble(3), data.getInt(4), data.getDouble(5), data.getDouble(7), data.getString(8), data.getString(9)));
                    }
                    data.close();
                }
                activityFragment.retrieveActivityList(activityModelList);
                break;

            case CURSOR_ID_ACTIVITY_DETAILS:
                if (data!=null && data.getCount() > 0){
                    while(data.moveToNext()){
                        ActivityDetailsModel activity = new ActivityDetailsModel(data.getInt(0), data.getInt(1), data.getInt(2), data.getDouble(3), data.getInt(4), data.getDouble(5), data.getDouble(6), data.getDouble(7), data.getString(8), data.getString(9), data.getString(10));
                        Log.d("Update", "onLoadFinished: id : " + data.getInt(0));
                        activityDetailsFragment.receiveDetails(activity);
                    }
                    data.close();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}



    @Override
    public void onDetailsUpdate(int id, String weather, String satisfaction, String notes) {
        Log.d("update", "id: " + id);
        Log.d("update", "weather: " + weather);
        Log.d("update", "satisfaction: " + satisfaction);
        Log.d("update", "notes: " + notes);
        int rows;
        rows = runningTrackerDBHandler.updateWeather(id, weather);
        runningTrackerDBHandler.updateSatisfaction(id, satisfaction);
        runningTrackerDBHandler.updateNotes(id, notes);
        if (rows == 0){
            displayErrorToast();
        }
        else {
            Toast.makeText(getBaseContext(), "Activity Updated!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityDetailsBackButtonClicked() {
        navigateToActivityList();
    }

    @Override
    public void onActivityDetailsDeleteButtonClicked(int id) {
        boolean deleted = runningTrackerDBHandler.deleteActivity(id);
        if (deleted){
            Toast.makeText(getBaseContext(), "Activity Deleted!", Toast.LENGTH_LONG).show();
            navigateToActivityList();
        }
        else{
            displayErrorToast();
        }
    }

    private void navigateToActivityList(){
        Log.d("vp", "in function");
        resetActivityListData();
        Log.d("vp", "before start thread");
        getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_DESC, null, MainActivity.this);
        getLoaderManager().restartLoader(CURSOR_ID_STATS_OVERVIEW, null, MainActivity.this);
        fragmentManager.beginTransaction().hide(activeFragment).show(activityFragment).commit();
        activeFragment = activityFragment;
    }

    private void displayErrorToast(){
        Toast.makeText(getBaseContext(), "An error has occurred " + Utilities.getEmojiByUnicode(0x2639), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onActivityClicked(int activityID) {
        fragmentManager.beginTransaction().hide(activeFragment).show(activityDetailsFragment).commit();
        activeFragment = activityDetailsFragment;
        Bundle bundle = new Bundle();
        bundle.putInt("id", activityID);
        getLoaderManager().restartLoader(CURSOR_ID_ACTIVITY_DETAILS, bundle, this);
    }

    @Override
    public void onSortByDateClicked() {
        resetActivityListData();
        if (sortedByDateDesc){
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_ASC, null, this);
            sortedByDateDesc = false;
        }
        else{
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_DESC, null, this);
            sortedByDateDesc = true;
        }
        sortedByDistanceDesc = false;

    }

    @Override
    public void onSortByDistanceClicked() {
        resetActivityListData();
        if (sortedByDistanceDesc){
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_DISTANCE_ASC, null, this);
            sortedByDistanceDesc = false;
        }
        else{
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_DISTANCE_DESC, null, this);
            sortedByDistanceDesc = true;
        }
        sortedByDateDesc = false;
    }
}
