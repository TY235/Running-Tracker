package com.example.runningtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.card.MaterialCardView;

public class ActivityFragment extends Fragment {

    MaterialCardView statsOverview;
    RecyclerView activityList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        initialiseComponents(view);
        return view;
    }

    private void initialiseComponents(View view){
        statsOverview = (MaterialCardView) view.findViewById(R.id.statsOverview);
        activityList = (RecyclerView) view.findViewById(R.id.activityList);
    }
}