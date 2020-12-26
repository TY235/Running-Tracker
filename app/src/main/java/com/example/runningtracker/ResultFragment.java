package com.example.runningtracker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Locale;

public class ResultFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, OnMapReadyCallback {

    private final String[] weatherItems = new String[]{"N/S", "Sunny", "Cloudy", "Windy", "Rainy", "Stormy"};
    private final String[] satisfactionItems = new String[]{"N/S", "Excellent", "Good", "Acceptable", "So-so", "Poor"};

    private ResultFragment.ResultFragmentListener listener;

    private GoogleMap map;
    private MapView mapView;
    private Spinner weatherSpinner, satisfactionSpinner;
    private TextView timeTakenView, distanceView, paceView, speedView, caloriesBurnedView;

    private ArrayList<ArrayList<LatLng>> polyline;
    private double totalDistanceTravelledInKM;
    private double paceInMinutesPerKM;
    private double speedInMetersPerSecond;
    private double caloriesBurned;
    private int totalTimeTakenInSeconds;
    private String weather;
    private String satisfaction;

    /* Interface for passing the values or triggers an event when the buttons are clicked */
    public interface ResultFragmentListener {
        void onResultSaveButtonClicked(boolean saveBtnClicked, ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond, double caloriesBurned, String weather, String satisfaction);
    }

    public ResultFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        initialiseLayoutComponents(view);
        return view;
    }

    /* Initialise the components of the layout */
    private void initialiseLayoutComponents(View view){
        timeTakenView = view.findViewById(R.id.resultTime);
        distanceView = view.findViewById(R.id.resultDistance);
        paceView = view.findViewById(R.id.resultPace);
        speedView = view.findViewById(R.id.resultSpeed);
        caloriesBurnedView = view.findViewById(R.id.resultKcal);
        weatherSpinner = view.findViewById(R.id.weather);
        satisfactionSpinner = view.findViewById(R.id.satisfaction);
        mapView = view.findViewById(R.id.resultMapView);
        MaterialButton saveBtn = view.findViewById(R.id.saveButton);
        MaterialButton discardBtn = view.findViewById(R.id.discardButton);

        mapView.onCreate(null);
        mapView.getMapAsync(this);
        saveBtn.setOnClickListener(this);
        discardBtn.setOnClickListener(this);

        /* Set an adapted to populate the weather drop down list */
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(getActivity(), R.layout.style_dropdown_item, weatherItems);
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weatherSpinner.setAdapter(weatherAdapter);
        weatherSpinner.setOnItemSelectedListener(this);

        /* Set an adapted to populate the satisfaction drop down list */
        ArrayAdapter<String> satisfactionAdapter = new ArrayAdapter<>(getActivity(), R.layout.style_dropdown_item, satisfactionItems);
        satisfactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        satisfactionSpinner.setAdapter(satisfactionAdapter);
        satisfactionSpinner.setOnItemSelectedListener(this);
    }

    /* Function to retrieve the details when MainActivity has completed the query */
    @SuppressWarnings("unchecked")
    public void retrieveStats(ArrayList<ArrayList<LatLng>> polyline, int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond, double userHeight, double userWeight){
        this.polyline = (ArrayList<ArrayList<LatLng>>) polyline.clone();
        this.totalTimeTakenInSeconds = totalTimeTakenInSeconds;
        this.totalDistanceTravelledInKM = distanceInKM;
        this.paceInMinutesPerKM = paceInMinutesPerKM;
        this.speedInMetersPerSecond = speedInMetersPerSecond;
        /* Calculate the total calories burned if the user height and weight is not 0 */
        if (userHeight != 0 && userWeight != 0){
            this.caloriesBurned = Utilities.calculateCaloriesBurned(userHeight, userWeight, speedInMetersPerSecond, totalTimeTakenInSeconds);
        }
        else{
            /* Set calories burned to 0.001 to avoid confusion between real 0 calories burned as total calories burned calculated are set to 1 decimal place only */
            this.caloriesBurned = 0.001;
        }
        updateResultView(totalTimeTakenInSeconds, distanceInKM, paceInMinutesPerKM, speedInMetersPerSecond, caloriesBurned);
    }

    /* Update the views of the layout once the details are retrieved */
    public void updateResultView(int totalTimeTakenInSeconds, double distanceInKM, double paceInMinutesPerKM, double speedInMetersPerSecond, double caloriesBurned){
        /* Draw the route on map view */
        drawRouteOnMap();
        timeTakenView.setText(Utilities.convertStoHMS(totalTimeTakenInSeconds));
        distanceView.setText(String.format(Locale.ENGLISH, "%.2f", distanceInKM));
        paceView.setText(String.format(Locale.ENGLISH, "%.2f", paceInMinutesPerKM));
        speedView.setText(String.format(Locale.ENGLISH, "%.2f", speedInMetersPerSecond));
        /* Set calories burned view to N/A when calories burned is equal to 0.001 (User weight and height is 0 hence calories burned not calculated) */
        if (caloriesBurned == 0.001){
            caloriesBurnedView.setText("N/A");
        }
        else {
            caloriesBurnedView.setText(String.format(Locale.ENGLISH, "%.1f", caloriesBurned));
        }
    }

    /* Draw route on map view */
    private void drawRouteOnMap(){
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

        /* Find the last coordinate from the last polyline stored */
        ArrayList<LatLng> lastPolyline = polyline.get(polyline.size()-1);
        LatLng lastCoordinate = lastPolyline.get(lastPolyline.size()-1);
        /* Add finishing point marker */
        map.addMarker(
                new MarkerOptions()
                        .position(lastCoordinate)
                        .icon(Utilities.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_finishing_point))
        );

        /* Create a builder to include all the points */
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ArrayList<LatLng> line : polyline){
            for (LatLng latLng : line) {
                builder.include(latLng);
            }
        }

        /* Define the map view size */
        int height = getResources().getDisplayMetrics().heightPixels/3;
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.16);

        /* Zoom out the map to include the whole route */
        final LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        map.animateCamera(cu);
    }

    /* Set the respective values for weather and satisfaction when the items of the drop down list is clicked */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinner = parent.getId();
        if (spinner == R.id.weather){
            switch (position){
                case 0:
                    weather = "";
                    break;
                case 1:
                    weather = weatherItems[1];
                    break;
                case 2:
                    weather = weatherItems[2];
                    break;
                case 3:
                    weather = weatherItems[3];
                    break;
                case 4:
                    weather = weatherItems[4];
                    break;
                case 5:
                    weather = weatherItems[5];
                    break;
            }
        }
        if (spinner == R.id.satisfaction){
            switch (position){
                case 0:
                    satisfaction = "";
                    break;
                case 1:
                    satisfaction = satisfactionItems[1];
                    break;
                case 2:
                    satisfaction = satisfactionItems[2];
                    break;
                case 3:
                    satisfaction = satisfactionItems[3];
                    break;
                case 4:
                    satisfaction = satisfactionItems[4];
                    break;
                case 5:
                    satisfaction = satisfactionItems[5];
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onClick(View v) {
        int btn = v.getId();
        if (btn == R.id.saveButton || btn == R.id.discardButton){
            /* Reset the spinner to initial position */
            resetSpinnersOptions();
            /* Send the details to the class that implemented the interface (MainActivity) */
            listener.onResultSaveButtonClicked(true, polyline, totalTimeTakenInSeconds, totalDistanceTravelledInKM, paceInMinutesPerKM, speedInMetersPerSecond, caloriesBurned, weather, satisfaction);
        }
    }

    /* Reset the spinner to initial position (0) */
    private void resetSpinnersOptions() {
        weatherSpinner.setSelection(0);
        satisfactionSpinner.setSelection(0);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /* Check if MainActivity implemented RunFragmentListener interface */
        if (context instanceof ResultFragment.ResultFragmentListener){
            listener = (ResultFragment.ResultFragmentListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + " must implement ResultFragmentListener.");
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
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

}
