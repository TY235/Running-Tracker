package com.example.runningtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.runningtracker.provider.RunningTrackerContract;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RunFragment.RunFragmentListener, ResultFragment.ResultFragmentListener, ProfileFragment.ProfileFragmentListener {


    ResultFragment resultFragment = new ResultFragment();
    final RunFragment runFragment = new RunFragment();
    final ActivityFragment activityFragment = new ActivityFragment();
    final ProfileFragment profileFragment = new ProfileFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();



    Fragment activeFragment = runFragment;
    BottomNavigationView bottomNav;
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
//

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateWelcomeTextUserName();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.nav_run && !inResultFragment){
                        updateWelcomeTextUserName();
                        fragmentManager.beginTransaction().hide(activeFragment).show(runFragment).commit();
                        activeFragment = runFragment;
                    }
                    else if (item.getItemId() == R.id.nav_run && inResultFragment){
                        fragmentManager.beginTransaction().hide(activeFragment).show(resultFragment).commit();
                        activeFragment = resultFragment;
                    }
                    else if (item.getItemId() == R.id.nav_activity){
                        fragmentManager.beginTransaction().hide(activeFragment).show(activityFragment).commit();
                        activeFragment = activityFragment;
                    }
                    else if (item.getItemId() == R.id.nav_profile){
                        getUserDetailsFromDB();
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

    private void updateWelcomeTextUserName(){
        // Retrieve data from database
        getUserDetailsFromDB();
        // Call updateEditTextView
        runFragment.updateWelcomeText(userName);
    }

    private void getUserDetailsFromDB(){
        Cursor cursor = cr.query(RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
        if (cursor!=null && cursor.getCount() > 0){
            while(cursor.moveToNext()){
                userName = cursor.getString(1);
                userHeight = cursor.getDouble(2);
                userWeight = cursor.getDouble(3);
            }
            cursor.close();
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
            runningTrackerDBHandler.addActivities(startDate, startTime, distanceInKM, totalTimeTakenInSeconds, speedInMetersPerSecond, paceInMinutesPerKM, caloriesBurned, weather, satisfaction);
        }
        inResultFragment = false;
        updateWelcomeTextUserName();
        fragmentManager.beginTransaction().hide(resultFragment).show(runFragment).commit();
        runFragment.updateStopTrackingUI();
        activeFragment = runFragment;
    }

    @Override
    public void onUserDetailsSent(String userName, double userHeightInCM, double userWeightInKG) {
        Cursor cursor = cr.query(RunningTrackerContract.USER_DETAILS_URI, null, null, null, null);
        if (cursor!=null && cursor.getCount() > 0){
            runningTrackerDBHandler.updateName(userName);
            runningTrackerDBHandler.updateHeight(userHeightInCM);
            runningTrackerDBHandler.updateWeight(userWeightInKG);
            cursor.close();
            Toast.makeText(getBaseContext(), "Profile Updated!", Toast.LENGTH_LONG).show();
        }
        else{
            runningTrackerDBHandler.addUserDetails(userName, userHeightInCM, userWeightInKG);
            Toast.makeText(getBaseContext(), "Profile Added!", Toast.LENGTH_LONG).show();
        }

    }

}
