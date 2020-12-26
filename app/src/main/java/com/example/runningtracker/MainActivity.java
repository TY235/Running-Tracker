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
import android.view.MenuItem;
import android.widget.Toast;

import com.example.runningtracker.provider.RunningTrackerContract;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements RunFragment.RunFragmentListener, ResultFragment.ResultFragmentListener, ProfileFragment.ProfileFragmentListener, ActivityFragment.ActivityFragmentListener, ActivityDetailsFragment.ActivityDetailsFragmentListener, LoaderManager.LoaderCallbacks<Cursor> {


    /* Cursor IDs */
    private static final int CURSOR_ID_USER_DETAILS = 0;
    private static final int CURSOR_ID_ACTIVITY_DETAILS = 1;
    private static final int CURSOR_ID_SORT_BY_ID_DESC = 10;
    private static final int CURSOR_ID_SORT_BY_ID_ASC = 11;
    private static final int CURSOR_ID_SORT_BY_DISTANCE_DESC = 20;
    private static final int CURSOR_ID_SORT_BY_DISTANCE_ASC = 21;
    private static final int CURSOR_ID_STATS_OVERVIEW = 3;

    /* Models ArrayList */
    private final ArrayList<StatsOverviewModel> statsOverviewModelList = new ArrayList<>();
    private final ArrayList<ActivityModel> activityModelList = new ArrayList<>();

    /* Fragments */
    final RunFragment runFragment = new RunFragment();
    final ResultFragment resultFragment = new ResultFragment();
    final ActivityFragment activityFragment = new ActivityFragment();
    final ActivityDetailsFragment activityDetailsFragment = new ActivityDetailsFragment();
    final ProfileFragment profileFragment = new ProfileFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment activeFragment = runFragment;

    private BottomNavigationView bottomNav;

    private boolean firstTimeLoad = true, inResultFragment = false, sortedByDateDesc, sortedByDistanceDesc;
    private double userWeight=0, userHeight=0;
    private int startDate=0, startTime =0;
    private String userName="";

    private ContentResolver cr;
    private RunningTrackerDBHandler runningTrackerDBHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Create DB Handler and get content resolver when activity is created */
        cr = getContentResolver();
        runningTrackerDBHandler = new RunningTrackerDBHandler(this, null);

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        /* Add all the fragments to fragment manager and hide all fragments except run fragment */
        fragmentManager.beginTransaction().add(R.id.fragment_frame, runFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, activityFragment, "2").hide(activityFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, profileFragment, "3").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, resultFragment, "Result").hide(resultFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_frame, activityDetailsFragment, "Details").hide(activityDetailsFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* Query for user details when the activity is started and update the welcome text */
        getUserDetailsNUpdateWelcomeText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    /* When run icon is selected and it is not in result fragment */
                    if (item.getItemId() == R.id.nav_run && !inResultFragment){
                        /* Query for user details and update the welcome text  */
                        getUserDetailsNUpdateWelcomeText();
                        /* Hides the active fragment and shows the run fragment */
                        fragmentManager.beginTransaction().hide(activeFragment).show(runFragment).commit();
                        activeFragment = runFragment;
                    }
                    /* When run icon is selected and it is currently in result fragment */
                    else if (item.getItemId() == R.id.nav_run && inResultFragment){
                        /* Hides the active fragment and shows the result fragment */
                        fragmentManager.beginTransaction().hide(activeFragment).show(resultFragment).commit();
                        activeFragment = resultFragment;
                    }
                    /* When activity icon is selected */
                    else if (item.getItemId() == R.id.nav_activity){
                        /* Initialise the sort to be sorted by date desc */
                        sortedByDateDesc = true;
                        sortedByDistanceDesc = false;
                        navigateToActivityList();
                    }
                    /* When profile icon is selected */
                    else if (item.getItemId() == R.id.nav_profile){
                        /* Query for user details */
                        getUserDetailsNUpdateWelcomeText();
                        /* Update the text views with the query result */
                        profileFragment.updateEditTextView(userName, userHeight, userWeight);
                        /* Hides the active fragment and shows the profile fragment */
                        fragmentManager.beginTransaction().hide(activeFragment).show(profileFragment).commit();
                        activeFragment = profileFragment;
                    }
                    else{
                        return false;
                    }
                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        /* If current selected icon is run when device back button is clicked the close the app */
        if (bottomNav.getSelectedItemId() == R.id.nav_run) {
            super.onBackPressed();
            finish();
        } else {
            /* If current selected icon is not run when device back button is set the navigation bar and fragment to run fragment */
            bottomNav.setSelectedItemId(R.id.nav_run);
        }
    }

    @Override
    public void onRunStopButtonClicked(int startDate, int startTime, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond) {
        /* Set the date and time received from run fragment */
        this.startDate = startDate;
        this.startTime = startTime;
        /* Hide run fragment and show result fragment */
        fragmentManager.beginTransaction().hide(runFragment).show(resultFragment).commit();
        /* Pass details to result fragment */
        resultFragment.retrieveStats(polyline, totalTimeTakenInSeconds, distanceInKM, paceInMinutesPerKM, speedInMetersPerSecond, userHeight, userWeight);
        activeFragment = resultFragment;
        inResultFragment = true;
    }

    @Override
    public void onResultSaveButtonClicked(boolean saveBtnClicked, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond, double caloriesBurned, String weather, String satisfaction) {
        /* Add new activity to database if save button is clicked */
        if (saveBtnClicked){
            int id;
            id = runningTrackerDBHandler.addActivities(startDate, startTime, distanceInKM, totalTimeTakenInSeconds, speedInMetersPerSecond, paceInMinutesPerKM, caloriesBurned, weather, satisfaction);
            if (id == -1){
                /* Display error toast if insert new activity has failed */
                displayErrorToast();
            }
            else {
                /* Shows successful toast if insert new activity is successful */
                Toast.makeText(getBaseContext(), "Result Saved!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            /* Shows discarded toast if discard button is clicked */
            Toast.makeText(getBaseContext(), "Result Discarded!", Toast.LENGTH_LONG).show();
        }
        inResultFragment = false;
        /* Query for user details and update the welcome text */
        getUserDetailsNUpdateWelcomeText();
        /* Hide result fragment and show run fragment */
        fragmentManager.beginTransaction().hide(resultFragment).show(runFragment).commit();
        /* Update run fragment UI */
        runFragment.updateStopTrackingUI();
        activeFragment = runFragment;
    }

    @Override
    public void onSortByDateClicked() {
        /* Clear the current activity list */
        resetActivityListData();
        if (sortedByDateDesc){
            /* Start cursor loader to query for the activity list sort by id ascending if it is currently in descending */
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_ASC, null, this);
            sortedByDateDesc = false;
        }
        else{
            /* Start cursor loader to query for the activity list sort by id descending */
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_DESC, null, this);
            sortedByDateDesc = true;
        }
        sortedByDistanceDesc = false;
    }

    @Override
    public void onSortByDistanceClicked() {
        /* Clear the current activity list */
        resetActivityListData();
        if (sortedByDistanceDesc){
            /* Start cursor loader to query for the activity list sort by distance ascending if it is currently in descending */
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_DISTANCE_ASC, null, this);
            sortedByDistanceDesc = false;
        }
        else{
            /* Start cursor loader to query for the activity list sort by distance descending */
            getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_DISTANCE_DESC, null, this);
            sortedByDistanceDesc = true;
        }
        sortedByDateDesc = false;
    }

    private void navigateToActivityList(){
        /* Clear the current activity list */
        resetActivityListData();
        /* Start the cursor loader to query for stats overview and activity list sort by ID (sort by date and time) descending */
        getLoaderManager().restartLoader(CURSOR_ID_STATS_OVERVIEW, null, MainActivity.this);
        getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_DESC, null, MainActivity.this);
        /* Hide the active fragment and shoe the activity fragment */
        fragmentManager.beginTransaction().hide(activeFragment).show(activityFragment).commit();
        activeFragment = activityFragment;
    }

    @Override
    public void onActivityClicked(int activityID) {
        /* Hide the active fragment and show activity details fragment */
        fragmentManager.beginTransaction().hide(activeFragment).show(activityDetailsFragment).commit();
        activeFragment = activityDetailsFragment;
        Bundle bundle = new Bundle();
        bundle.putInt("id", activityID);
        /* Start cursor loader to query for the details of the specific activity */
        getLoaderManager().restartLoader(CURSOR_ID_ACTIVITY_DETAILS, bundle, this);
    }

    @Override
    public void onActivityDetailsUpdateButtonClicked(int id, String weather, String satisfaction, String notes) {
        int rows;
        /* Updates the weather, satisfaction and notes */
        rows = runningTrackerDBHandler.updateWeather(id, weather);
        runningTrackerDBHandler.updateSatisfaction(id, satisfaction);
        runningTrackerDBHandler.updateNotes(id, notes);
        if (rows == 0){
            /* Display error toast if update activity has failed */
            displayErrorToast();
        }
        else {
            /* Shows successful toast if update activity is successful */
            Toast.makeText(getBaseContext(), "Activity Updated!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityDetailsBackButtonClicked() {
        navigateToActivityList();
    }

    @Override
    public void onActivityDetailsDeleteButtonClicked(int id) {
        /* Delete the activity from database */
        boolean deleted = runningTrackerDBHandler.deleteActivity(id);
        if (deleted){
            /* Shows successful toast if delete activity is successful and return to activity fragment */
            Toast.makeText(getBaseContext(), "Activity Deleted!", Toast.LENGTH_LONG).show();
            navigateToActivityList();
        }
        else{
            /* Display error toast if delete activity has failed */
            displayErrorToast();
        }
    }

    @Override
    public void onProfileUpdateButtonClicked(String userName, double userHeightInCM, double userWeightInKG) {
        /* Query for user details */
        Cursor cursor = cr.query(RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
        int rows;
        /* Update the user details if there is user exists in the database */
        if (cursor!=null && cursor.getCount() > 0){
            rows = runningTrackerDBHandler.updateName(userName);
            runningTrackerDBHandler.updateHeight(userHeightInCM);
            runningTrackerDBHandler.updateWeight(userWeightInKG);
            cursor.close();
            if (rows == 0){
                /* Display error toast if update user details has failed */
                displayErrorToast();
            }
            else {
                /* Shows successful toast if update user details is successful */
                Toast.makeText(getBaseContext(), "Profile Updated!", Toast.LENGTH_LONG).show();
            }
        }
        else{
            /* Add new user to database if query returns 0 result */
            int id;
            id = runningTrackerDBHandler.addUserDetails(userName, userHeightInCM, userWeightInKG);
            if (id == -1){
                /* Display error toast if insert new user details has failed */
                displayErrorToast();
            }
            else {
                /* Shows successful toast if insert new user details is successful */
                Toast.makeText(getBaseContext(), "Profile Added!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Query for user details */
    private void getUserDetailsNUpdateWelcomeText(){
        /* Initialise the cursor loader if it is the first time */
        if(firstTimeLoad){
            getLoaderManager().initLoader(CURSOR_ID_USER_DETAILS, null, this);
            firstTimeLoad = false;
        }
        /* Restart the cursor loader */
        else{
            getLoaderManager().restartLoader(CURSOR_ID_USER_DETAILS, null, this);
        }
    }

    /* Clear the activity list data required for recycler view in activity fragment to overwrite the new data to the ArrayList */
    private void resetActivityListData(){
        activityModelList.clear();
    }

    private void displayErrorToast(){
        Toast.makeText(getBaseContext(), "An error has occurred " + Utilities.getEmojiByUnicode(0x2639), Toast.LENGTH_LONG).show();
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
                    /* Update welcome text in run fragment with the user name obtained from database or blank string if no result returned */
                    runFragment.updateWelcomeText(userName);
                }
                break;

            case CURSOR_ID_STATS_OVERVIEW:
                /* Clear the stats overview model list to overwrite the new data to the ArrayList */
                statsOverviewModelList.clear();

                if (data!=null && data.getCount() > 0){
                    while(data.moveToNext()){
                        if (data.getInt(1)!=0){
                            /* Add the details to stats overview model list if the total runs is not 0 */
                            statsOverviewModelList.add(new StatsOverviewModel(data.getDouble(0), data.getInt(1), data.getDouble(2), data.getDouble(3)));
                        }
                        else{
                            /* Add an 0-valued stats overview model to stats overview model list if total run is 0 to avoid getting null values */
                            statsOverviewModelList.add(new StatsOverviewModel(0, 0, 0, 0));
                        }
                    }
                    data.close();
                }
                else {
                    for (int i = 0; i < 3; i++){
                        /* Add all 0-valued stats overview models to stats overview model list if there is no result obtained */
                        statsOverviewModelList.add(new StatsOverviewModel(0, 0, 0, 0));
                    }
                }
                /* Pass the ArrayList to activity fragment */
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
                /* Pass the ArrayList to activity fragment */
                activityFragment.retrieveActivityList(activityModelList);
                break;

            case CURSOR_ID_ACTIVITY_DETAILS:
                if (data!=null && data.getCount() > 0){
                    while(data.moveToNext()){
                        ActivityDetailsModel activity = new ActivityDetailsModel(data.getInt(0), data.getInt(1), data.getInt(2), data.getDouble(3), data.getInt(4), data.getDouble(5), data.getDouble(6), data.getDouble(7), data.getString(8), data.getString(9), data.getString(10));
                        /* Pass the ArrayList to activity details fragment */
                        activityDetailsFragment.retrieveDetails(activity);
                    }
                    data.close();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

}
