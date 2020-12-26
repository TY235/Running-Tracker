package com.example.runningtracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

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

    /* Interface for passing the values or triggers an event when the buttons are clicked */
    public interface ActivityDetailsFragmentListener {
        void onActivityDetailsUpdateButtonClicked(int id, String weather, String satisfaction, String notes);
        void onActivityDetailsBackButtonClicked();
        void onActivityDetailsDeleteButtonClicked(int id);
    }

    public ActivityDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_details, container, false);
        initialiseComponents(view);
        return view;
    }

    /* Initialise the components of the layout */
    private void initialiseComponents(View view){
        dateView = view.findViewById(R.id.activityDetails_date);
        timeView = view.findViewById(R.id.activityDetails_time);
        distanceView = view.findViewById(R.id.activityDetails_distance);
        timeTakenView = view.findViewById(R.id.activityDetails_timeTaken);
        speedView = view.findViewById(R.id.activityDetails_speed);
        paceView = view.findViewById(R.id.activityDetails_pace);
        caloriesBurnedView = view.findViewById(R.id.activityDetails_caloriesBurned);
        weatherSpinner = view.findViewById(R.id.activityDetails_weather);
        satisfactionSpinner = view.findViewById(R.id.activityDetails_satisfaction);
        notesView = view.findViewById(R.id.activityDetails_notes);
        MaterialButton uploadBtn = view.findViewById(R.id.activityDetails_updateButton);
        ImageButton backBtn = view.findViewById(R.id.activityDetails_backButton);
        ImageButton deleteBtn = view.findViewById(R.id.activityDetails_deleteButton);

        uploadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);

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
    public void retrieveDetails(ActivityDetailsModel activity){
        activityID = activity.getActivityID();
        date = activity.getDate();
        time = activity.getTime();
        totalTimeTakenInSeconds = activity.getTotalTimeTaken();
        totalDistanceTravelledInKM = activity.getTotalDistance();
        paceInMinutesPerKM = activity.getPace();
        speedInMetersPerSecond = activity.getAvgSpeed();
        caloriesBurned = activity.getTotalCaloriesBurned();
        weather = activity.getWeather();
        satisfaction = activity.getSatisfaction();
        notes = activity.getNotes();
        setView();
    }

    /* Set the views of the layout with the details retrieved */
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

    /* Find the position of weather/satisfaction in the weather/satisfaction String array that were defined in this fragment */
    private int findSpinnerItemPosition(String item, int option){
        String[] spinnerItems = {};
        /* Option 0 for finding position of weather and option 1 for finding position of satisfaction */
        if (option == 0){
            spinnerItems = weatherItems;
        }
        else if (option == 1){
            spinnerItems = satisfactionItems;
        }

        /* loop through the array and return the position that matched the weather/satisfaction */
        for (int i = 0; i < spinnerItems.length; i++){
            if (item.equals(spinnerItems[i])){
                return i;
            }
        }
        /* Return 0 if not found */
        return 0;
    }

    /* Set the respective values for weather and satisfaction when the items of the drop down list is clicked */
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
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.activityDetails_updateButton){
            /* Send activity ID, weather, satisfaction and trimmed notes to the class that implemented the interface (MainActivity) */
            listener.onActivityDetailsUpdateButtonClicked(activityID, weather, satisfaction, notesView.getText().toString().trim());
        }
        else if (id == R.id.activityDetails_backButton){
            listener.onActivityDetailsBackButtonClicked();
        }
        else if (id == R.id.activityDetails_deleteButton){
            /* Send activity ID to the class that implemented the interface (MainActivity) */
            listener.onActivityDetailsDeleteButtonClicked(activityID);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /* Check if MainActivity implemented ActivityDetailsFragmentListener interface when the fragment is attached to the class that implemented the interface (MainActivity) */
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
        /* Set listener to null when the fragment is detached from the class that implemented the interface (MainActivity) */
        listener = null;
    }
}