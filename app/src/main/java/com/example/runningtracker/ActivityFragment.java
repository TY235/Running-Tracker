package com.example.runningtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ActivityFragment extends Fragment {

    StatsOverviewSliderAdapter statsOverviewSliderAdapter;
    ViewPager2 statsOverviewViewPager;
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
        statsOverviewViewPager = (ViewPager2) view.findViewById(R.id.statsOverview);
        activityList = (RecyclerView) view.findViewById(R.id.activityList);
    }
//
//    public void retrieveActivityListData(){
//    }

    public void retrieveStatsOverviewList(ArrayList<StatsOverviewModel> statsOverviewModelList){
        statsOverviewSliderAdapter = new StatsOverviewSliderAdapter(statsOverviewModelList);
        statsOverviewViewPager.setAdapter(statsOverviewSliderAdapter);
    }

}