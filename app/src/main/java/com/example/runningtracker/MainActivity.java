package com.example.runningtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity implements RunFragment.RunFragmentListener, ResultFragment.ResultFragmentListener, ProfileFragment.ProfileFragmentListener, ActivityDetailsFragment.ActivityDetailsFragmentListener, LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewClickInterface {


    private static final int CURSOR_ID_USER_DETAILS = 0;
    private static final int CURSOR_ID_ACTIVITY_DETAILS = 1;
    private static final int CURSOR_ID_SORT_BY_ID_DESC = 10;
    private static final int CURSOR_ID_SORT_BY_ID_ASC = 11;
    private static final int CURSOR_ID_SORT_BY_DISTANCE_DESC = 20;
    private static final int CURSOR_ID_SORT_BY_DISTANCE_ASC = 21;
    private static final int CURSOR_ID_SORT_BY_SPEED_DESC = 30;
    private static final int CURSOR_ID_SORT_BY_SPEED_ASC = 31;
    private static final int CURSOR_ID_SORT_BY_TIME_TAKEN_DESC = 40;
    private static final int CURSOR_ID_SORT_BY_TIME_TAKEN_ASC = 41;
    private static final int CURSOR_ID_SORT_BY_CALORIES_BURNED_DESC = 50;
    private static final int CURSOR_ID_SORT_BY_CALORIES_BURNED_ASC = 51;

    private ArrayList<Integer> activityIDList = new ArrayList<Integer>();
    private ArrayList<Integer> dateList = new ArrayList<Integer>();
    private ArrayList<Integer> timeList = new ArrayList<Integer>();
    private ArrayList<Integer> timeTakenList = new ArrayList<Integer>();
    private ArrayList<Double> distanceList = new ArrayList<Double>();
    private ArrayList<Double> paceList = new ArrayList<Double>();
    private ArrayList<Double> speedList = new ArrayList<Double>();
    private ArrayList<Double> caloriesBurnedList = new ArrayList<Double>();
    private ArrayList<String> weatherList = new ArrayList<String>();
    private ArrayList<String> satisfactionList = new ArrayList<String>();


    final ActivityDetailsFragment activityDetailsFragment = new ActivityDetailsFragment();
    final ResultFragment resultFragment = new ResultFragment();
    final RunFragment runFragment = new RunFragment();
    final ActivityFragment activityFragment = new ActivityFragment();
    final ProfileFragment profileFragment = new ProfileFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();



    Fragment activeFragment = runFragment;
    BottomNavigationView bottomNav;
    private boolean firstTimeLoad = true;
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
        runningTrackerDBHandler = new RunningTrackerDBHandler(this, null, null, 1);

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
//
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame,
//                    new RunFragment()).commit();
//        }

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

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
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
                        backToActivityList();
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
//        Cursor cursor = cr.query(RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
//        if (cursor!=null && cursor.getCount() > 0){
//            while(cursor.moveToNext()){
//                userName = cursor.getString(1);
//                userHeight = cursor.getDouble(2);
//                userWeight = cursor.getDouble(3);
//            }
//            cursor.close();
//        }
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

    private void initialiseRecyclerView(){
        RecyclerView activityList = findViewById(R.id.activityList);
        ActivityListAdapter recipesListAdapter = new ActivityListAdapter(activityIDList, dateList, timeList, distanceList, timeTakenList, speedList, caloriesBurnedList, weatherList, satisfactionList, this);
        activityList.setAdapter(recipesListAdapter);
        activityList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void resetActivityListData(){
        activityIDList.clear();
        dateList.clear();
        timeList.clear();
        distanceList.clear();
        timeTakenList.clear();
        speedList.clear();
        paceList.clear();
        caloriesBurnedList.clear();
        weatherList.clear();
        satisfactionList.clear();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CURSOR_ID_USER_DETAILS){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
        }
        else if (id == CURSOR_ID_SORT_BY_ID_DESC){
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, null, null, RunningTrackerContract.ACTIVITIES_ID + " DESC");
        }
        else if (id == CURSOR_ID_ACTIVITY_DETAILS){
            Log.d("check", "onCreateLoader: " + Objects.requireNonNull(args).getInt("id"));
            return new CursorLoader(MainActivity.this, RunningTrackerContract.ACTIVITIES_URI, null, RunningTrackerContract.ACTIVITIES_ID + "=?", new String[]{String.valueOf(Objects.requireNonNull(args).getInt("id"))}, null);
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
            case  CURSOR_ID_SORT_BY_ID_DESC:
                if (data!=null && data.getCount() > 0){
                    while(data.moveToNext()){
                        activityIDList.add(data.getInt(0));
                        dateList.add(data.getInt(1));
                        timeList.add(data.getInt(2));
                        distanceList.add(data.getDouble(3));
                        timeTakenList.add(data.getInt(4));
                        speedList.add(data.getDouble(5));
                        caloriesBurnedList.add(data.getDouble(7));
                        weatherList.add(data.getString(8));
                        satisfactionList.add(data.getString(9));
                    }
                    data.close();
                }
                initialiseRecyclerView();

                break;

            case CURSOR_ID_ACTIVITY_DETAILS:
                if (data!=null && data.getCount() > 0){
                    Bundle bundle = new Bundle();
                    while(data.moveToNext()){
                        Log.d("Update", "onLoadFinished: id : " + data.getInt(0));
                        bundle.putInt("id", data.getInt(0));
                        bundle.putInt("date", data.getInt(1));
                        bundle.putInt("time", data.getInt(2));
                        bundle.putDouble("distance", data.getDouble(3));
                        bundle.putInt("timeTaken", data.getInt(4));
                        bundle.putDouble("speed", data.getDouble(5));
                        bundle.putDouble("pace", data.getDouble(6));
                        bundle.putDouble("caloriesBurned", data.getDouble(7));
                        bundle.putString("weather", data.getString(8));
                        bundle.putString("satisfaction", data.getString(9));
                        bundle.putString("notes", data.getString(10));
                    }
                    data.close();
                    activityDetailsFragment.receiveDetails(bundle);
                }
                break;

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    @Override
    public void onIDSent(int activityID) {
        fragmentManager.beginTransaction().hide(activeFragment).show(activityDetailsFragment).commit();
        activeFragment = activityDetailsFragment;
        Bundle bundle = new Bundle();
        bundle.putInt("id", activityID);
        getLoaderManager().restartLoader(CURSOR_ID_ACTIVITY_DETAILS, bundle, this);
    }


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
    public void onBackButtonClicked() {
        backToActivityList();
    }

    @Override
    public void onDeleteDetails(int id) {
        boolean deleted = runningTrackerDBHandler.deleteActivity(id);
        if (deleted){
            Toast.makeText(getBaseContext(), "Activity Deleted!", Toast.LENGTH_LONG).show();
            backToActivityList();
        }
        else{
            displayErrorToast();
        }
    }

    private void backToActivityList(){
        resetActivityListData();
        getLoaderManager().restartLoader(CURSOR_ID_SORT_BY_ID_DESC, null, MainActivity.this);
        fragmentManager.beginTransaction().hide(activeFragment).show(activityFragment).commit();
        activeFragment = activityFragment;
    }

    private void displayErrorToast(){
        Toast.makeText(getBaseContext(), "An error has occurred " + Utilities.getEmojiByUnicode(0x2639), Toast.LENGTH_LONG).show();
    }
}
