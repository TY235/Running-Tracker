package com.example.runningtracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment implements View.OnClickListener{

    private double userHeightInCM, userWeightInKG;
    private EditText nameView, heightView, weightView;
    private ProfileFragmentListener listener;

    /* Interface for passing the values or triggers an event when the buttons are clicked */
    public interface ProfileFragmentListener {
        void onProfileUpdateButtonClicked(String userName, double userHeightInCM, double userWeightInKG);
    }

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initialiseComponents(view);
        return view;
    }

    /* Initialise the components of the layout */
    private void initialiseComponents(View view){
        nameView = view.findViewById(R.id.editUserName);
        heightView = view.findViewById(R.id.editUserHeight);
        weightView = view.findViewById(R.id.editUserWeight);
        MaterialButton updateBtn = view.findViewById(R.id.updateProfileButton);
        updateBtn.setOnClickListener(this);
    }

    /* Update the edit text views when the details are received */
    public void updateEditTextView(String name, double height, double weight){
        if (!name.equals("")){
            nameView.setText(name);
        }
        if (height!=0){
            heightView.setText(String.valueOf(height));
        }
        if (weight!=0){
            weightView.setText(String.valueOf(weight));
        }
    }

    @Override
    public void onClick(View v) {
        String userName = nameView.getText().toString().trim();

        if (heightView.getText().toString().trim().isEmpty()){
            /* Shows error if height column is left empty when update button is clicked */
            heightView.setError("Height is required " + Utilities.getEmojiByUnicode(0x1F625));
        }
        else{
            userHeightInCM = Utilities.round(Double.parseDouble(heightView.getText().toString()), 2);
        }
        if (weightView.getText().toString().trim().isEmpty()){
            /* Shows error if weight column is left empty when update button is clicked */
            weightView.setError("Weight is required " + Utilities.getEmojiByUnicode(0x1F625));
        }
        else{
            userWeightInKG = Utilities.round(Double.parseDouble(weightView.getText().toString()), 2);
        }
        if (!heightView.getText().toString().trim().isEmpty() && !weightView.getText().toString().trim().isEmpty()){
            /* Send user's name, height and weight to the class that implemented the interface (MainActivity) if they are both not empty */
            listener.onProfileUpdateButtonClicked(userName, userHeightInCM, userWeightInKG);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /* Check if MainActivity implemented ProfileFragmentListener interface when the fragment is attached to the class that implemented the interface (MainActivity) */
        if (context instanceof ProfileFragment.ProfileFragmentListener){
            listener = (ProfileFragment.ProfileFragmentListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + " must implement ProfileFragmentListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}