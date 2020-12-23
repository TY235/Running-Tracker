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
import android.util.Log;
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
    View startFilterBtwMapNBtn, pauseFilterBtwMapNBtns, statsBackground;
    MaterialButton startBtn, pauseBtn, resumeBtn, stopBtn;
    TextView resumeBtnText, stopBtnText, distance, pace, time, welcomeText;

    public interface RunFragmentListener {
        void onRunningStatsSent(int startDate, int startTime, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond);
    }

    public RunFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_run,
                container, false);
        initialiseLayoutComponents(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        _unregisterReceiver();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        if(isTrackingServiceRunning()){
            stopService();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onClick(final View v) {
        int btn = v.getId();
        if (btn == R.id.startButton){
            updateStartTrackingUI();

            ZonedDateTime currentZDT = ZonedDateTime.now();
            String dateString = String.format(Locale.ENGLISH, "%d%02d%02d", currentZDT.getYear(), currentZDT.get(ChronoField.MONTH_OF_YEAR), currentZDT.getDayOfMonth());
            String timeString = String.format(Locale.ENGLISH, "%02d%02d%02d",currentZDT.getHour(), currentZDT.getMinute(), currentZDT.getSecond());

            startDate = Integer.parseInt(dateString);
            startTime = Integer.parseInt(timeString);

            if(!isTrackingServiceRunning()){
                startService();
            }
        }
        else if (btn == R.id.pauseButton){
            updatePauseTrackingUI();
            zoomOutMapViewToIncludeWholeRoute(polyline);
        }
        else if (btn == R.id.resumeButton){
            updateResumeTrackingUI();
            trackingService.startUpdateLocation();
        }
        else if (btn == R.id.stopButton){
            if(isTrackingServiceRunning()){
                stopService();
            }
            listener.onRunningStatsSent(startDate, startTime, polyline, totalTimeTakenInSeconds, totalDistanceTravelledInKM, paceInMinutesPerKM, speedInMetersPerSecond);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if MainActivity implemented RunFragmentListener interface
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

    public void updateWelcomeText(String name){
        Log.d("checkFirstTime", "updateWelcomeText: " + name);
        String userName = "Anonymous";
        if (!name.isEmpty()){
            userName = name;
        }
        welcomeText.setText(String.format("Hi,\n%s!\nAre You Ready\nfor a Workout?", userName));
    }

    private void drawPolyline(ArrayList<ArrayList<LatLng>> polyline){
        map.clear();
        map.addMarker(
                new MarkerOptions()
                        .position(polyline.get(0).get(0))
                        .icon(Utilities.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_starting_point))
        );
        for (ArrayList<LatLng> coordinates:polyline){
            map.addPolyline(
                    new PolylineOptions()
                            .width(26f)
                            .color(Color.MAGENTA)
                            .addAll(coordinates)
            );
        }
        ArrayList<LatLng> latestPolyline = polyline.get(polyline.size()-1);
        LatLng latestCoordinate = latestPolyline.get(latestPolyline.size()-1);
        map.addMarker(
                new MarkerOptions()
                        .position(latestCoordinate)
                        .icon(Utilities.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_tracking_progress_actor))
        );
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                latestCoordinate,
                19f
        );
        map.animateCamera(cu);
    }

    private void zoomOutMapViewToIncludeWholeRoute(ArrayList<ArrayList<LatLng>> polylines){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ArrayList<LatLng> polyline : polylines){
            for (LatLng latLng : polyline) {
                builder.include(latLng);
            }
        }
        final LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 136);
        map.animateCamera(cu);
    }

    @SuppressWarnings("unchecked")
    private void registerBroadcastReceiver(){
        if (locationBroadcastReceiver == null){
            locationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                polyline = (ArrayList<ArrayList<LatLng>>) intent.getSerializableExtra("polyline");
                    if (polyline != null) {
                        drawPolyline(polyline);
                    }
                    totalDistanceTravelledInMeters = intent.getExtras().getDouble("distance");
                    totalDistanceTravelledInKM = Utilities.convertMtoKM(totalDistanceTravelledInMeters);
                    distance.setText(String.format(Locale.ENGLISH, "Distance\n%.2f\nkm", totalDistanceTravelledInKM));
                }
            };
        }
        if (timerBroadcastReceiver == null){
            timerBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                totalTimeTakenInSeconds = intent.getExtras().getInt("seconds");
                time.setText(String.format("Time\n%s\nmins", Utilities.convertStoHMS(totalTimeTakenInSeconds)));
                speedInMetersPerSecond = Utilities.calculateSpeedInMetersPerSecond(totalDistanceTravelledInMeters, totalTimeTakenInSeconds);
                paceInMinutesPerKM = Utilities.calculatePaceInMinutesPerKM(totalDistanceTravelledInMeters, totalTimeTakenInSeconds);
                pace.setText(String.format(Locale.ENGLISH, "Pace\n%.2f\nmins/km", paceInMinutesPerKM));
                }
            };
        }
        requireActivity().registerReceiver(locationBroadcastReceiver, new IntentFilter("updatedLocation"));
        requireActivity().registerReceiver(timerBroadcastReceiver, new IntentFilter("trackingTimer"));
    }

    private void _unregisterReceiver(){
        if(locationBroadcastReceiver != null){
            Log.d("Check", "_unregisterReceiver: ");
            requireActivity().unregisterReceiver(locationBroadcastReceiver);
        }
        if(timerBroadcastReceiver != null){
            Log.d("Check", "_unregisterReceiver: ");
            requireActivity().unregisterReceiver(timerBroadcastReceiver);
        }
    }

    private void startService(){
        Intent trackingStartServiceIntent = new Intent(getActivity(), TrackingService.class);
        requireActivity().startService(trackingStartServiceIntent);
        requireActivity().bindService(trackingStartServiceIntent, trackingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopService(){
        Intent trackingStopServiceIntent = new Intent(getActivity(), TrackingService.class);
        Log.d("g53mdp", "stopService: ");
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

    private void updatePauseTrackingUI(){
        pauseBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.VISIBLE);
        resumeBtnText.setVisibility(View.VISIBLE);
        stopBtnText.setVisibility(View.VISIBLE);
        pauseFilterBtwMapNBtns.setVisibility(View.VISIBLE);
        trackingService.stopUpdateLocation();
    }

    private void updateResumeTrackingUI(){
        resumeBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        resumeBtnText.setVisibility(View.GONE);
        stopBtnText.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.VISIBLE);
        pauseFilterBtwMapNBtns.setVisibility(View.GONE);
    }

    public void updateStopTrackingUI(){
        resumeBtn.setVisibility(View.GONE);
        resumeBtnText.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        stopBtnText.setVisibility(View.GONE);
        pace.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
        distance.setVisibility(View.GONE);
        statsBackground.setVisibility(View.GONE);
        pauseFilterBtwMapNBtns.setVisibility(View.GONE);
        map.clear();
        startFilterBtwMapNBtn.setVisibility(View.VISIBLE);
        welcomeText.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
    }

    private void initialiseLayoutComponents(View view){
        startBtn = (MaterialButton) view.findViewById(R.id.startButton);
        startBtn.setOnClickListener(this);
        pauseBtn = (MaterialButton) view.findViewById(R.id.pauseButton);
        pauseBtn.setOnClickListener(this);
        resumeBtn = (MaterialButton) view.findViewById(R.id.resumeButton);
        resumeBtn.setOnClickListener(this);
        stopBtn = (MaterialButton) view.findViewById(R.id.stopButton);
        stopBtn.setOnClickListener(this);

        resumeBtnText = (TextView) view.findViewById(R.id.resumeBtnText);
        stopBtnText = (TextView) view.findViewById(R.id.stopBtnText);
        time = (TextView) view.findViewById(R.id.time);
        distance = (TextView) view.findViewById(R.id.distance);
        pace = (TextView) view.findViewById(R.id.pace);

        startFilterBtwMapNBtn = (View) view.findViewById(R.id.startFilterBtwMapNBtn);
        pauseFilterBtwMapNBtns = (View) view.findViewById(R.id.pauseFilterBtwMapNBtns);
        statsBackground = (View) view.findViewById(R.id.statsBackground);
        welcomeText = (TextView) view.findViewById(R.id.welcomeText);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

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