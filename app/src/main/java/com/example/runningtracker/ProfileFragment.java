package com.example.runningtracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment implements View.OnClickListener{


    private String userName;
    private double userHeightInCM, userWeightInKG;
    private EditText nameView, heightView, weightView;
    private MaterialButton updateBtn;
    private ProfileFragmentListener listener;

    public interface ProfileFragmentListener {
        void onUserDetailsSent(String userName, double userHeightInCM, double userWeightInKG);
    }

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initialiseComponents(view);
        return view;
    }

    private void initialiseComponents(View view){
        nameView = (EditText) view.findViewById(R.id.editUserName);
        heightView = (EditText) view.findViewById(R.id.editUserHeight);
        weightView = (EditText) view.findViewById(R.id.editUserWeight);
        updateBtn = (MaterialButton) view.findViewById(R.id.updateProfileButton);
        updateBtn.setOnClickListener(this);
    }

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
        userName = nameView.getText().toString();

        if (heightView.getText().toString().isEmpty()){
            heightView.setError("Height is required " + Utilities.getEmojiByUnicode(0x1F625));
        }
        else{
            userHeightInCM = Utilities.round(Double.parseDouble(heightView.getText().toString()), 2);
        }
        if (weightView.getText().toString().isEmpty()){
            weightView.setError("Weight is required " + Utilities.getEmojiByUnicode(0x1F625));
        }
        else{
            userWeightInKG = Utilities.round(Double.parseDouble(weightView.getText().toString()), 2);
        }
        if (!heightView.getText().toString().isEmpty() && !weightView.getText().toString().isEmpty()){
            listener.onUserDetailsSent(userName, userHeightInCM, userWeightInKG);

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Check if MainActivity implemented ProfileFragmentListener interface
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