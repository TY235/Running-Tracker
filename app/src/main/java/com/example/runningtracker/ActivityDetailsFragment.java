package com.example.runningtracker;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;


public class ActivityDetailsFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{


    private final String[] weatherItems = new String[]{"N/S", "Sunny", "Cloudy", "Windy", "Rainy", "Stormy"};
    private final String[] satisfactionItems = new String[]{"N/S", "Excellent", "Good", "Acceptable", "So-so", "Poor"};

    private ActivityDetailsFragment.ActivityDetailsFragmentListener listener;

    private int activityID, date, time, totalTimeTakenInSeconds;
    private double totalDistanceTravelledInKM, paceInMinutesPerKM, speedInMetersPerSecond, caloriesBurned;
    private String weather, satisfaction, notes;

    private TextView dateView, timeView, distanceView, timeTakenView, speedView, caloriesBurnedView, paceView;
    private Spinner weatherSpinner, satisfactionSpinner;
    private EditText notesView;
    private MaterialButton uploadBtn;
    private ImageButton backBtn, deleteBtn;

    public interface ActivityDetailsFragmentListener {
        void onDetailsUpdate (int id, String weather, String satisfaction, String notes);
        void onBackButtonClicked();
        void onDeleteDetails(int id);
    }

    public ActivityDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_details, container, false);
        initialiseComponents(view);
        return view;
    }

    public void receiveDetails(Bundle details){


        activityID = details.getInt("id");
        date = details.getInt("date");
        time = details.getInt("time");
        totalTimeTakenInSeconds = details.getInt("timeTaken");
        totalDistanceTravelledInKM = details.getDouble("distance");
        paceInMinutesPerKM = details.getDouble("pace");
        speedInMetersPerSecond = details.getDouble("speed");
        caloriesBurned = details.getDouble("caloriesBurned");
        weather = details.getString("weather");
        satisfaction = details.getString("satisfaction");
        notes = details.getString("notes");
        Log.d("CHECK", "date: " + date);
        Log.d("CHECK", "time: " + time);
        Log.d("CHECK", "timetaken: " + totalTimeTakenInSeconds);
        Log.d("CHECK", "pace: " + paceInMinutesPerKM);
        Log.d("CHECK", "speed: " + speedInMetersPerSecond);
        Log.d("CHECK", "calories: " + caloriesBurned);
        Log.d("CHECK", "weather: " + weather);
        Log.d("CHECK", "satisfaction: " + satisfaction);
        setView();
    }

    private void setView(){
        dateView.setText(Utilities.formatDateToDDMMYYY(date));
        timeView.setText(Utilities.formatTimeToHHMM(time));
        distanceView.setText(String.format(Locale.ENGLISH, "%.2f", totalDistanceTravelledInKM));
        timeTakenView.setText(Utilities.convertStoHMS(totalTimeTakenInSeconds));
        paceView.setText(String.format(Locale.ENGLISH, "%s", paceInMinutesPerKM));
        speedView.setText(String.format(Locale.ENGLISH, "%s", speedInMetersPerSecond));
        caloriesBurnedView.setText(String.format(Locale.ENGLISH, "%.1f", caloriesBurned));
        weatherSpinner.setSelection(findSpinnerItemPosition(weather, 0));
        satisfactionSpinner.setSelection(findSpinnerItemPosition(satisfaction, 1));
        notesView.setText(notes);
    }

    private void initialiseComponents(View view){
        dateView = (TextView) view.findViewById(R.id.activityDetails_date);
        timeView = (TextView) view.findViewById(R.id.activityDetails_time);
        distanceView = (TextView) view.findViewById(R.id.activityDetails_distance);
        timeTakenView = (TextView) view.findViewById(R.id.activityDetails_timeTaken);
        speedView = (TextView) view.findViewById(R.id.activityDetails_speed);
        paceView = (TextView) view.findViewById(R.id.activityDetails_pace);
        caloriesBurnedView = (TextView) view.findViewById(R.id.activityDetails_caloriesBurned);
        weatherSpinner = (Spinner) view.findViewById(R.id.activityDetails_weather);
        satisfactionSpinner = (Spinner) view.findViewById(R.id.activityDetails_satisfaction);
        notesView = (EditText) view.findViewById(R.id.activityDetails_notes);
        uploadBtn = (MaterialButton) view.findViewById(R.id.activityDetails_updateButton);
        backBtn = (ImageButton) view.findViewById(R.id.activityDetails_backButton);
        deleteBtn = (ImageButton) view.findViewById(R.id.activityDetails_deleteButton);

        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<String>(getActivity(), R.layout.style_dropdown_item, weatherItems);
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weatherSpinner.setAdapter(weatherAdapter);
        weatherSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> satisfactionAdapter = new ArrayAdapter<String>(getActivity(), R.layout.style_dropdown_item, satisfactionItems);
        satisfactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        satisfactionSpinner.setAdapter(satisfactionAdapter);
        satisfactionSpinner.setOnItemSelectedListener(this);

        uploadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);

    }



    private int findSpinnerItemPosition(String item, int option){
        String[] spinnerItems = {};
        if (option == 0){
            spinnerItems = weatherItems;
        }
        else if (option == 1){
            spinnerItems = satisfactionItems;
        }

        for (int i = 0; i < spinnerItems.length; i++){
            if (item.equals(spinnerItems[i])){
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if MainActivity implemented ActivityDetailsFragmentListener interface
        if (context instanceof ActivityDetailsFragment.ActivityDetailsFragmentListener){
            listener = (ActivityDetailsFragment.ActivityDetailsFragmentListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + " must implement ActivityDetailsFragmentListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinner = parent.getId();
        if (spinner == R.id.activityDetails_weather){
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
        if (spinner == R.id.activityDetails_satisfaction){
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
        Log.d("Update", "fragment spinner weather: " + weather);
        Log.d("Update", "fragment spinner satisfaction: " + satisfaction);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.activityDetails_updateButton){
            Log.d("Update", "fragment id: " + activityID);
            Log.d("Update", "fragment weather: " + weather);
            Log.d("Update", "fragment satisfaction: " + satisfaction);
            listener.onDetailsUpdate(activityID, weather, satisfaction, notesView.getText().toString());
        }
        else if (id == R.id.activityDetails_backButton){
            listener.onBackButtonClicked();
        }
        else if (id == R.id.activityDetails_deleteButton){
            listener.onDeleteDetails(activityID);
        }
    }
}