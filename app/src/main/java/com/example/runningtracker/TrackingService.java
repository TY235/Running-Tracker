package com.example.runningtracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static com.example.runningtracker.TrackingNotification.CHANNEL_ID;

/* Service class that provides tracking of users movement */
public class TrackingService extends Service {

    final String NOTIFICATION_CONTEXT_TITLE = "Running Tracker";
    final String NOTIFICATION_CONTEXT = "Running Tracker is tracking your run.";

    private final IBinder myBinder = new MyLocalBinder();
    private boolean firstLocation;
    private double distance;
    private int seconds = 0;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private ArrayList<LatLng> coordinates;
    private ArrayList<ArrayList<LatLng>> polyline;
    private Handler handler;

    public TrackingService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        TrackingService getService(){
            return TrackingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        firstLocation = true;
        distance = 0;
        seconds = 0;
        handler = new Handler();
        polyline = new ArrayList<>();
        setNotification();
        startTracking();
        return START_REDELIVER_INTENT;
    }

    /* Starts the tracking of users movement */
    private void startTracking(){
        Location previousLocation = new Location("");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                /*  If the current location is the first location */
                if (firstLocation){
                    firstLocation = false;
                    /* Create new ArrayList to store the coordinates so that they can form a polyline */
                    coordinates = new ArrayList<>();
                    /* Set the previous location as current location */
                    previousLocation.setLatitude(location.getLatitude());
                    previousLocation.setLongitude(location.getLongitude());
                    /* Store the polyline recorded before the user clicked paused into a polyline ArrayList */
                    polyline.add(coordinates);
                }

                /* Add the distance from the previous location to the current location to distance variable */
                distance += location.distanceTo(previousLocation);
                /* Store the current coordinate as LatLng to coordinates ArrayList */
                coordinates.add(new LatLng(location.getLatitude(), location.getLongitude()));

                /* Broadcast the polyline, speed and distance */
                Intent i = new Intent("updatedLocation");
                i.putExtra("polyline", polyline);
                i.putExtra("currentSpeed", "Current Speed: " +location.getSpeed());
                i.putExtra("distance", distance);
                sendBroadcast(i);

                /* Set the current location as the previous location for the next round */
                previousLocation.setLatitude(location.getLatitude());
                previousLocation.setLongitude(location.getLongitude());

            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // information about the signal, i.e. number of satellites
                Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
            }
            @Override
            public void onProviderEnabled(String provider) {
                // the user enabled (for example) the GPS
                Log.d("g53mdp", "onProviderEnabled: " + provider);
            }
            @Override
            public void onProviderDisabled(String provider) {
                // the user disabled (for example) the GPS
                Log.d("g53mdp", "onProviderDisabled: " + provider);
                /* Brings user to location setting page to turn on location service if it is disabled when the tracking has started */
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
        /* Call the function to start the location listener and timer thread */
        startUpdateLocation();
    }

    /* Timer thread that counts the time passed every seconds and broadcast it */
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent i = new Intent("trackingTimer");
            i.putExtra("seconds", seconds);
            sendBroadcast(i);
            seconds++;
            handler.postDelayed(this, 1000);
        }
    };

    /* Set notification when service is running */
    private void setNotification(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER ) ;
        notificationIntent.setAction(Intent.ACTION_MAIN) ;
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(NOTIFICATION_CONTEXT_TITLE)
                .setContentText(NOTIFICATION_CONTEXT)
                .setSmallIcon(R.drawable.ic_run)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    /* Function that starts the location listener and timer thread */
    public void startUpdateLocation(){
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    locationListener);
        }
        catch (SecurityException e) {
            Log.d("g53mdp", e.toString());
        }
        runnable.run();
    }

    /* Function that stops the timer thread and location listener from running */
    public void stopUpdateLocation(){
        firstLocation = true;
        handler.removeCallbacks(runnable);
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    /* Stop the service when user kills the app by swiping it from recent apps */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent i = new Intent("appKilledFlag");
        i.putExtra("appKilledFlag", true);
        sendBroadcast(i);
        stopSelf();
    }

    /* Stop location listener and timer thread from running and show a toast when the service is stopped */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdateLocation();
        Toast.makeText(this, "Tracking Service Stopped", Toast.LENGTH_SHORT).show();
    }
}