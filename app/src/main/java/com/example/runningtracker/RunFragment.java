package com.example.runningtracker;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Locale;
import java.time.ZonedDateTime;



public class RunFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private RunFragmentListener listener;
    private BroadcastReceiver locationBroadcastReceiver;
    private BroadcastReceiver timerBroadcastReceiver;
    private TrackingService trackingService;
    private boolean isBound = false;
    private double totalDistanceTravelledInMeters;
    private double totalDistanceTravelledInKM;
    private double paceInMinutesPerKM;
    private double speedInMetersPerSecond;
    private int totalTimeTakenInSeconds;
    private int startDate;
    private int startTime;

    private ArrayList<ArrayList<LatLng>> polyline;
    private GoogleMap map;

    MapView mapView;
    View startFilterBtwMapNBtn, pauseFilterBtwMapNBtn, statsBackground;
    MaterialButton startBtn, pauseBtn, resumeBtn, stopBtn;
    TextView resumeBtnText, stopBtnText, distance, pace, time, welcomeText;

    public interface RunFragmentListener {
        void onRunStopButtonClicked(int startDate, int startTime, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond);
    }

    public RunFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);
        initialiseLayoutComponents(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /* Check if MainActivity implemented RunFragmentListener interface */
        if (context instanceof RunFragmentListener){
            listener = (RunFragmentListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + " must implement RunFragmentListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        /* Register the broadcast receivers when the fragment is currently running */
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        /* Unregister the broadcast receivers when the fragment is not running */
        _unregisterReceiver();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        /* Stop the tracking service if the tracking service is running */
        if(isTrackingServiceRunning()){
            stopService();
        }
        super.onDestroy();
    }

    /* Update the welcome text with the user's name retrieved from MainActivity */
    public void updateWelcomeText(String name){
        /* Set name as Anonymous if user's name is empty */
        String userName = "Anonymous";
        if (!name.isEmpty()){
            userName = name;
        }
        welcomeText.setText(String.format("Hi,\n%s!\nAre You Ready\nfor a Workout?", userName));
    }

    @Override
    public void onClick(final View v) {
        int btn = v.getId();
        if (btn == R.id.startButton){

            /* Update the UI when start button is clicked */
            updateStartTrackingUI();

            /* Get the current date(YYYYMMDD) and time(HHMMSS) when start button is clicked */
            ZonedDateTime currentZDT = ZonedDateTime.now();
            String dateString = String.format(Locale.ENGLISH, "%d%02d%02d", currentZDT.getYear(), currentZDT.get(ChronoField.MONTH_OF_YEAR), currentZDT.getDayOfMonth());
            String timeString = String.format(Locale.ENGLISH, "%02d%02d%02d",currentZDT.getHour(), currentZDT.getMinute(), currentZDT.getSecond());

            startDate = Integer.parseInt(dateString);
            startTime = Integer.parseInt(timeString);

            /* Start tracking service if tracking service is not currently running */
            if(!isTrackingServiceRunning()){
                startService();
            }
        }
        else if (btn == R.id.pauseButton){
            /* Update the UI and zoom out the map to include the whole route */
            updatePauseTrackingUI();
            zoomOutMapViewToIncludeWholeRoute(polyline);
            /* Stop the location update */
            trackingService.stopUpdateLocation();
        }
        else if (btn == R.id.resumeButton){
            /* Update the UI and starts the location update again */
            updateResumeTrackingUI();
            trackingService.startUpdateLocation();
        }
        else if (btn == R.id.stopButton){
            /* Stop tracking service if tracking service is currently running */
            if(isTrackingServiceRunning()){
                stopService();
            }
            /* Send the details to the class that implemented the interface (MainActivity) */
            listener.onRunStopButtonClicked(startDate, startTime, polyline, totalTimeTakenInSeconds, totalDistanceTravelledInKM, paceInMinutesPerKM, speedInMetersPerSecond);
        }
    }

    private void drawPolyline(ArrayList<ArrayList<LatLng>> polyline){
        /* Reset the map */
        map.clear();
        /* Add starting point marker */
        map.addMarker(
                new MarkerOptions()
                        .position(polyline.get(0).get(0))
                        .icon(Utilities.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_starting_point))
        );

        /* Draw polyline for each polyline that is stored in polyline ArrayList */
        for (ArrayList<LatLng> coordinates:polyline){
            map.addPolyline(
                    new PolylineOptions()
                            .width(26f)
                            .color(Color.MAGENTA)
                            .addAll(coordinates)
            );
        }

        /* Find the latest coordinate (current user location) from the last polyline stored */
        ArrayList<LatLng> latestPolyline = polyline.get(polyline.size()-1);
        LatLng latestCoordinate = latestPolyline.get(latestPolyline.size()-1);

        /* Add current user location marker */
        map.addMarker(
                new MarkerOptions()
                        .position(latestCoordinate)
                        .icon(Utilities.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_tracking_progress_actor))
        );

        /* Zoom in to the current user location */
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                latestCoordinate,
                19f
        );
        map.animateCamera(cu);
    }

    /* Zoom out the map to include the whole route */
    private void zoomOutMapViewToIncludeWholeRoute(ArrayList<ArrayList<LatLng>> polyline){
        /* Create a builder to include all the points */
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ArrayList<LatLng> line : polyline){
            for (LatLng latLng : line) {
                builder.include(latLng);
            }
        }
        final LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 136);
        map.animateCamera(cu);
    }

    @SuppressWarnings("unchecked")
    private void registerBroadcastReceiver(){
        /* Create new location broadcast receiver if it is null */
        if (locationBroadcastReceiver == null){
            locationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    polyline = (ArrayList<ArrayList<LatLng>>) intent.getSerializableExtra("polyline");
                    /* Draw the polyline on the map if it is not null */
                    if (polyline != null) {
                        drawPolyline(polyline);
                    }
                    totalDistanceTravelledInMeters = intent.getExtras().getDouble("distance");
                    totalDistanceTravelledInKM = Utilities.convertMtoKM(totalDistanceTravelledInMeters);
                    distance.setText(String.format(Locale.ENGLISH, "Distance\n%.2f\nkm", totalDistanceTravelledInKM));
                }
            };
        }
        /* Create new timer broadcast receiver if it is null */
        if (timerBroadcastReceiver == null){
            timerBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                totalTimeTakenInSeconds = intent.getExtras().getInt("seconds");
                time.setText(String.format("Time\n%s\nmin", Utilities.convertStoHMS(totalTimeTakenInSeconds)));
                speedInMetersPerSecond = Utilities.calculateSpeedInMetersPerSecond(totalDistanceTravelledInMeters, totalTimeTakenInSeconds);
                paceInMinutesPerKM = Utilities.calculatePaceInMinutesPerKM(totalDistanceTravelledInMeters, totalTimeTakenInSeconds);
                pace.setText(String.format(Locale.ENGLISH, "Pace\n%.2f\nmin/km", paceInMinutesPerKM));
                }
            };
        }
        /* Register the receivers */
        requireActivity().registerReceiver(locationBroadcastReceiver, new IntentFilter("updatedLocation"));
        requireActivity().registerReceiver(timerBroadcastReceiver, new IntentFilter("trackingTimer"));
    }

    /* Unregister the receivers if they are not null */
    private void _unregisterReceiver(){
        if(locationBroadcastReceiver != null){
            requireActivity().unregisterReceiver(locationBroadcastReceiver);
        }
        if(timerBroadcastReceiver != null){
            requireActivity().unregisterReceiver(timerBroadcastReceiver);
        }
    }

    /* Starts the service and bind the service */
    private void startService(){
        Intent trackingStartServiceIntent = new Intent(getActivity(), TrackingService.class);
        requireActivity().startService(trackingStartServiceIntent);
        requireActivity().bindService(trackingStartServiceIntent, trackingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* Unbind the service if it is bounded and stops the service */
    private void stopService(){
        Intent trackingStopServiceIntent = new Intent(getActivity(), TrackingService.class);
        if(isBound){
            requireActivity().unbindService(trackingServiceConnection);
            isBound = false;
        }
        requireActivity().stopService(trackingStopServiceIntent);
    }

    /* Bound service function */
    private final ServiceConnection trackingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackingService.MyLocalBinder binder = (TrackingService.MyLocalBinder) service;
            trackingService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    /*  Update the UI when tracking started */
    private void updateStartTrackingUI(){
        startBtn.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
        startFilterBtwMapNBtn.setVisibility(View.GONE);
        welcomeText.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        pace.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);
        distance.setVisibility(View.VISIBLE);
        statsBackground.setVisibility(View.VISIBLE);
    }

    /*  Update the UI when tracking paused */
    private void updatePauseTrackingUI(){
        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.VISIBLE);
        resumeBtnText.setVisibility(View.VISIBLE);
        stopBtnText.setVisibility(View.VISIBLE);
        pauseFilterBtwMapNBtn.setVisibility(View.VISIBLE);
    }

    /*  Update the UI when tracking resumed */
    private void updateResumeTrackingUI(){
        resumeBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        resumeBtnText.setVisibility(View.GONE);
        stopBtnText.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        pauseFilterBtwMapNBtn.setVisibility(View.GONE);
    }

    /*  Update the UI when tracking stopped */
    public void updateStopTrackingUI(){
        resumeBtn.setVisibility(View.GONE);
        resumeBtnText.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        stopBtnText.setVisibility(View.GONE);
        pace.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
        distance.setVisibility(View.GONE);
        statsBackground.setVisibility(View.GONE);
        pauseFilterBtwMapNBtn.setVisibility(View.GONE);
        map.clear();
        startFilterBtwMapNBtn.setVisibility(View.VISIBLE);
        welcomeText.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
    }

    /* Initialise the components of the layout */
    private void initialiseLayoutComponents(View view){
        resumeBtnText = view.findViewById(R.id.resumeBtnText);
        stopBtnText = view.findViewById(R.id.stopBtnText);
        time = view.findViewById(R.id.time);
        distance = view.findViewById(R.id.distance);
        pace = view.findViewById(R.id.pace);

        startFilterBtwMapNBtn = view.findViewById(R.id.startFilterBtwMapNBtn);
        pauseFilterBtwMapNBtn = view.findViewById(R.id.pauseFilterBtwMapNBtn);
        statsBackground = view.findViewById(R.id.statsBackground);
        welcomeText = view.findViewById(R.id.welcomeText);

        startBtn = view.findViewById(R.id.startButton);
        pauseBtn = view.findViewById(R.id.pauseButton);
        resumeBtn = view.findViewById(R.id.resumeButton);
        stopBtn = view.findViewById(R.id.stopButton);

        mapView = view.findViewById(R.id.mapView);

        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        resumeBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    /* Function to check if tracking service is currently running */
    @SuppressWarnings("deprecation")
    private boolean isTrackingServiceRunning() {
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}